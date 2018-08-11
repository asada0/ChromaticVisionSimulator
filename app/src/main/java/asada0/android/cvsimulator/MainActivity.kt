//
//  MainActivity.kt
//  Chromatic Vision Simulator
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2018/08/10.
//  Copyright 2010-2018 Kazunori Asada. All rights reserved.
//

package asada0.android.cvsimulator

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.media.MediaActionSound
import android.net.Uri
import android.opengl.*
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Size
import android.view.*
import android.view.animation.*
import android.view.WindowManager
import android.widget.*
import android.widget.PopupWindow
import java.nio.ByteBuffer
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.io.InputStream
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.popup_image_source.view.*
import kotlinx.android.synthetic.main.popup_simulation_ratio.view.*

class MainActivity : Activity() {
    private val tag: String = "CVS-MainActivity"

    // Constant Values
    companion object {
        const val SHOW_ALERT = false
        const val CV_PERMISSIONS_CAMERA_STORAGE = 42171
        const val INTENT_RESULT = 80713
        const val PANEL_HIDDEN_INTERVAL = 10000L // 10 sec
        const val PANEL_HIDDEN_DURATION = 200L // 0.2 sec
        const val STATUS_INTERVAL = 200L // 0.2 sec
        const val MAX_ZOOM = 5.0f
        const val CAPTURE_ANIMATION_TIME = 1000L // 1 sec
        const val BUTTON_COLOR_DURATION = 500L // 0.5 sec
        const val JPEG_QUALITY = 60 // 60%
    }

    // Private Variables
    private var mViewCamera: GLSurfaceView? = null
    private var mViewFile: GLSurfaceView? = null
    private var mRendererCamera: CVGLRendererCamera? = null
    private var mRendererFile: CVGLRendererFile? = null

    private var mUITimerHandler: Handler? = null
    private var mUITimerRunnable: Runnable? = null
    private var mStatusTimerHandler: Handler? = null
    private var mStatusTimerRunnable: Runnable? = null
    private var mBroadCastReceiver: BroadcastReceiver? = null

    private var mAnimeShowToolPanel: TranslateAnimation? = null
    private var mAnimeHideToolPanel: TranslateAnimation? = null

    private var mSound: MediaActionSound? = null
    private var mError: CVError? = null

    private var mIsShowingUI: Boolean = false
    private var mLabelTypes: Array<TextView?> = arrayOfNulls(4)
    private var mLabelZoom: TextView? = null
    private var mColorVisionTypeLabels: Array<String?> = arrayOfNulls(4)
    private var mColorVisionTypeLabelsShort: Array<String?> = arrayOfNulls(4)
    private var mColorVisionTypeChars: Array<String?> = arrayOfNulls(4)
    private var mCPDT: BooleanArray = booleanArrayOf(true, false, false, false)
    private var mPopupSimulationRatio: PopupWindow? = null
    private var mPopupImageSource: PopupWindow? = null
    private var mPopupImageSourceLayout: View? = null
    private var mRadioRearCamera: RadioButton? = null
    private var mRadioFrontCamera: RadioButton? = null
    private var mRadioFile: RadioButton? = null
    private var mSliderSimulationRatio: SeekBar? = null
    private var mRadioTypesViews: Array<RadioButton?> = arrayOfNulls(4)
    private var mPrePointercount = 0
    private var mPreX: Float = 0.0f
    private var mPreY: Float = 0.0f
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mIsScaling: Boolean = false
    private var mDoubleTapDetector: GestureDetector? = null
    private var mFileTextureBitmap: Bitmap? = null
    private var mIntentData: Intent? = null
    private var mIsCallFromRestoreInstanceState: Boolean = false

    // Exist Cameras
    private var mHasRearCamera: Boolean = true
    private var mHasFrontCamera: Boolean = true

    // Permissions
    private var mHasCameraPermission: Boolean = false
    private var mHasStoragePermission: Boolean = false
    private var mDoAfterPermission: () -> Unit = {}

    // Simulation Parameters
    private var mColorVisionTypeList: List<Int> = List(1) { 0 } // Color Vision Types list of multi screen
        set(cvList) {
            field = cvList
            mRendererCamera!!.mColorVisionTypeList = cvList
            mRendererFile!!.mColorVisionTypeList = cvList
        }
    private var mSimulationRatio: Float = 1.0f
        set(ratio) {
            field = kotlin.math.max(kotlin.math.min(1.0f, ratio), 0.0f)
            mRendererCamera!!.mSimulationRatio = field
            mRendererFile!!.mSimulationRatio = field
        }
    private var mZoom: Float = 1.0f
        set(zoom) {
            field = zoom
            mRendererCamera!!.mZoom = zoom
            mRendererFile!!.mZoom = zoom
        }
    private var mPanX: Float = 0.0f
        set(x) {
            field = x
            mRendererCamera!!.mPanX = x
            mRendererFile!!.mPanX = x
        }
    private var mPanY: Float = 0.0f
        set(y) {
            field = y
            mRendererCamera!!.mPanY = y
            mRendererFile!!.mPanY = y
        }
    private var mIsImageFromFile: Boolean = false
        set(yn) {
            field = yn
            mRendererCamera!!.mIsImageFromFile = yn
            mRendererFile!!.mIsImageFromFile = yn
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Start
        init()
    }

    // App Initialization after permission check
    private fun init() {
        setContentView(R.layout.activity_main)

        // Setup mError
        mError = CVError(this)

        // Check Camera existence and permission
        mHasRearCamera = hasRearCamera()
        mHasFrontCamera = hasFrontCamera()
        mHasCameraPermission = hasCameraPermission()
        mHasStoragePermission = hasStoragePermission()

        requestPermission((mHasRearCamera || mHasFrontCamera) && !mHasCameraPermission, !mHasStoragePermission) {}

        // Initialize View and Renderer
        initViewAndRendererCamera()
        initViewAndRendererFile()

        // Initialize Color Vision Type Strings
        mColorVisionTypeChars = arrayOf(getString(R.string.sCChar), getString(R.string.sPChar), getString(R.string.sDChar), getString(R.string.sTChar))
        mColorVisionTypeLabels = arrayOf(getString(R.string.sCLong), getString(R.string.sPLong), getString(R.string.sDLong), getString(R.string.sTLong))
        mColorVisionTypeLabelsShort = arrayOf(getString(R.string.sCShort), getString(R.string.sPShort), getString(R.string.sDShort), getString(R.string.sTShort))

        // Setup User Interface Tools
        // Setup Color Vision Type radio buttons
        setupColorVisionTypeRadios()

        // Setup Simulation Ratio button
        setupSimulationRatioButton()

        // Setup Simulation Ratio slider
        setupSimulationRatioSlider()

        // Setup Image Source button
        setupImageSourceButton()

        // Setup Image Source radio buttons
        setupImageSourceRadios()

        // Setup Save buttons
        setupSaveButton()

        // Setup Animation
        setupAnimation()

        // Setup Gestures
        setupGestures()

        // Setup Color Vision Type and Zoom text label
        setupTextLabels()

        // Show U/I
        showUI()
    }

    private fun hasRearCamera(): Boolean {
        return this.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    private fun hasFrontCamera(): Boolean {
        return this.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(fCamera: Boolean, fStorage: Boolean, doAfterPermission: () -> Unit) {
        var mWaitingRationaleCamera = false
        var mWaitingRationaleStorage = false
        val permissions = mutableListOf<String>()
        if (fCamera) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                mWaitingRationaleCamera = true
                val afterAlert1: () -> Unit = {
                    mWaitingRationaleCamera = false
                    if (!mWaitingRationaleStorage && !permissions.isEmpty()) {
                        // Request CAMERA and/or WRITE_EXTERNAL_STORAGE permission
                        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), CV_PERMISSIONS_CAMERA_STORAGE)
                    }
                    showUI()
                }
                AlertDialog.Builder(this, R.style.AlertDialogStyle)
                        .setMessage(R.string.sCameraReason)
                        .setPositiveButton(R.string.sDismiss) { _, _ -> afterAlert1() }
                        .setOnCancelListener { afterAlert1() }
                        .show()
            }
            permissions.add(Manifest.permission.CAMERA)
        }
        if (fStorage) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                mWaitingRationaleStorage = true
                val afterAlert2: () -> Unit = {
                    mWaitingRationaleStorage = false
                    if (!mWaitingRationaleCamera && !permissions.isEmpty()) {
                        // Request CAMERA and/or WRITE_EXTERNAL_STORAGE permission
                        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), CV_PERMISSIONS_CAMERA_STORAGE)
                    }
                    showUI()
                }
                AlertDialog.Builder(this, R.style.AlertDialogStyle)
                        .setMessage(R.string.sStorageReason)
                        .setPositiveButton(R.string.sDismiss) { _, _ -> afterAlert2() }
                        .setOnCancelListener { afterAlert2() }
                        .show()
            }
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!mWaitingRationaleCamera && !mWaitingRationaleStorage && !permissions.isEmpty()) {
            // Request CAMERA and/or WRITE_EXTERNAL_STORAGE permission
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), CV_PERMISSIONS_CAMERA_STORAGE)
        }
        mDoAfterPermission = doAfterPermission
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != CV_PERMISSIONS_CAMERA_STORAGE) return
        permissions.forEachIndexed { index, kind ->
            when (kind) {
                Manifest.permission.CAMERA -> {
                    mHasCameraPermission = (grantResults[index] == PackageManager.PERMISSION_GRANTED)
                    if (!mHasCameraPermission) {
                        mError!!.log(tag, "Camera permission request is refused.")
                    }
                }
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    mHasStoragePermission = (grantResults[index] == PackageManager.PERMISSION_GRANTED)
                    if (!mHasStoragePermission) {
                        mError!!.log(tag, "Storage Read/Write permission request is refused.")
                    }
                }
            }
        }
        // Execute user function if permissions are granted successfully
        mDoAfterPermission()
        mDoAfterPermission = {}
        showUI()
    }

    private fun initViewAndRendererCamera() {
        // Image from Camera
        mViewCamera = gl_surface_view_camera
        mViewCamera!!.setEGLContextClientVersion(2)
        mRendererCamera = CVGLRendererCamera(this.applicationContext, this)
        mViewCamera!!.setRenderer(mRendererCamera)
        when {
            mHasRearCamera -> {
                mRendererCamera!!.mCameraPosition = CVCamera.CAMERA_BACK
            }
            mHasFrontCamera -> {
                mRendererCamera!!.mCameraPosition = CVCamera.CAMERA_FRONT
            }
            else -> {
                mRendererCamera!!.mCameraPosition = CVCamera.CAMERA_NONE
                // Stop continues rendering
                mViewCamera!!.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                mError!!.log(tag, "initViewAndRendererCamera error - Neither rear nor front camera.")
            }
        }
        mViewCamera!!.visibility = View.VISIBLE
    }


    private fun initViewAndRendererFile() {
        // Image from File
        mViewFile = gl_surface_view_file
        mViewFile!!.setEGLContextClientVersion(2)
        mRendererFile = CVGLRendererFile(this.applicationContext, this)
        mViewFile!!.setRenderer(mRendererFile)
        mViewFile!!.visibility = View.INVISIBLE
    }

    // Setup Color Vision Type radio buttons
    private fun setupColorVisionTypeRadios() {
        mRadioTypesViews = arrayOf(radio_c, radio_p, radio_d, radio_t)

        // Push Color Vision Type button ("C", "P", "D", "T")
        mRadioTypesViews.forEach { it ->
            it!!.isChecked = false
            it.setOnClickListener { radioColorVisionTypeHandler(it as RadioButton) }
            it.setOnLongClickListener { radioColorVisionTypeLongHandler(it as RadioButton) }
        }
        radio_c.isChecked = true
    }

    private fun radioColorVisionTypeHandler(pushedButton: RadioButton) {
        val buttonNo: Int = mColorVisionTypeChars.indexOfFirst { it == pushedButton.text }
        if ((buttonNo < 0) || (3 < buttonNo)) {
            mError!!.log(tag, "Neither C, P, D, T button was pushed. Ignore.")
            return
        }
        if (mCPDT[buttonNo]) { // ON -> OFF
            if (mCPDT.count { it } == 1) { // only one button is ON and also pushed it, ignore
                restartUITimer()
                return
            }
            pushedButton.isChecked = false // uncheck a radio button manually
        }
        mCPDT[buttonNo] = !mCPDT[buttonNo]
        makeColorVisionTypeList(mCPDT)

        resetPan()
        // Disable Appear Simulation Ratio Slider button and badge when only "C" is pushed
        button_simulation_ratio.isEnabled = !(mCPDT[0] && !mCPDT[1] && !mCPDT[2] && !mCPDT[3])
        badge_number.visibility = if (button_simulation_ratio.isEnabled && (mSimulationRatio < 1.0f)) View.VISIBLE else View.INVISIBLE
        mZoom = adjustZoom(mZoom)
        restartUITimer()
    }

    // LongPress radio button -> single color vision type simulation
    private fun radioColorVisionTypeLongHandler(pushedButton: RadioButton): Boolean {
        val buttonNo: Int = mColorVisionTypeChars.indexOfFirst { it == pushedButton.text }
        if ((buttonNo < 0) || (3 < buttonNo)) {
            mError!!.log(tag, "Neither C, P, D, T button was pushed. Ignore.")
            return true
        }
        mCPDT = BooleanArray(4) { false }
        mCPDT[buttonNo] = true
        mCPDT.forEachIndexed { index, value ->
            mRadioTypesViews[index]!!.isChecked = value
        }
        makeColorVisionTypeList(mCPDT)

        resetPan()
        // Disable Appear Simulation Ratio Slider button and badge when only "C" is pushed
        button_simulation_ratio.isEnabled = !(mCPDT[0] && !mCPDT[1] && !mCPDT[2] && !mCPDT[3])
        badge_number.visibility = if (button_simulation_ratio.isEnabled && (mSimulationRatio < 1.0f)) View.VISIBLE else View.INVISIBLE
        mZoom = adjustZoom(mZoom)
        restartUITimer()
        return true
    }

    private fun makeColorVisionTypeList(cpdt : BooleanArray) {
        val cvlist = mutableListOf<Int>()
        cpdt.forEachIndexed { index, value -> if (value) cvlist.add(index) }
        if (cvlist.isEmpty()) cvlist.add(0)
        mColorVisionTypeList = cvlist
    }

    private fun adjustZoom(zoom: Float): Float {
        val (hvAdjustingRatioX: Float, hvAdjustingRatioY: Float) = if (!mIsImageFromFile) mRendererCamera!!.getHvAdjustingRatio() else mRendererFile!!.getHvAdjustingRatio()
        val minZoom: Float = kotlin.math.min(hvAdjustingRatioX / hvAdjustingRatioY, hvAdjustingRatioY / hvAdjustingRatioX)
        return kotlin.math.min(kotlin.math.max(minZoom, zoom), MAX_ZOOM)
    }

    // Setup Simulation Ratio Popup and Simulation Ratio Slider
    private fun setupSimulationRatioButton() {
        button_simulation_ratio.isEnabled = false
        mPopupSimulationRatio = PopupWindow()
        val view: View = layoutInflater.inflate(R.layout.popup_simulation_ratio, FrameLayout(this))
        mPopupSimulationRatio!!.contentView = view
        mPopupSimulationRatio!!.width = RelativeLayout.LayoutParams.WRAP_CONTENT
        mPopupSimulationRatio!!.height = RelativeLayout.LayoutParams.WRAP_CONTENT
        mPopupSimulationRatio!!.setBackgroundDrawable(resources.getDrawable(R.drawable.clear, null))
        mPopupSimulationRatio!!.isOutsideTouchable = true // Closes the popup when touch outside
        mPopupSimulationRatio!!.isFocusable = true

        mSliderSimulationRatio = view.slider_ratio

        // Push Appear Simulation Ratio Slider button
        button_simulation_ratio.setOnClickListener {
            // set slider progress
            mSliderSimulationRatio!!.progress = (mSimulationRatio * 100).toInt()

            // Show anchored to button
            val popupView: View = mPopupSimulationRatio!!.contentView
            popupView.measure(0, 0)
            mPopupSimulationRatio!!.showAsDropDown(it, (it.width - popupView.measuredWidth) / 2, -(tool_panel.measuredHeight + popupView.measuredHeight))

            restartUITimer()
        }
    }

    // Setup Simulation Ratio Slider in popup
    private fun setupSimulationRatioSlider() {
        mSliderSimulationRatio!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar, ratio100: Int, fromUser: Boolean) {
                mSimulationRatio = ratio100.toFloat() / 100
            }
            override fun onStartTrackingTouch(bar: SeekBar) {
                badge_number.visibility = View.VISIBLE
                restartUITimer()
            }
            override fun onStopTrackingTouch(bar: SeekBar) {
                badge_number.text = (mSimulationRatio * 100).toInt().toString()
                if (mSimulationRatio >= 1.0f) {
                    badge_number.visibility = View.INVISIBLE
                }
                updateTextLabels()
                restartUITimer()
            }
        })
    }

    // Setup Appear Image Source Popup button
    private fun setupImageSourceButton() {
        mPopupImageSource = PopupWindow(this)
        mPopupImageSourceLayout = layoutInflater.inflate(R.layout.popup_image_source, FrameLayout(this))
        mPopupImageSource = PopupWindow(mPopupImageSourceLayout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        mPopupImageSource!!.contentView = mPopupImageSourceLayout
        mPopupSimulationRatio!!.width = LinearLayout.LayoutParams.WRAP_CONTENT
        mPopupSimulationRatio!!.height = LinearLayout.LayoutParams.WRAP_CONTENT
        mPopupImageSource!!.setBackgroundDrawable(resources.getDrawable(R.drawable.clear, null))
        mPopupImageSource!!.isOutsideTouchable = true // Closes the popup when touch outside
        mPopupImageSource!!.isFocusable = true

        // Push Appear Image Source Popup button
        button_image_source.setOnClickListener {
            val popupView: View = mPopupImageSource!!.contentView
            popupView.measure(0, 0)
            // mPopupImageSource!!.showAsDropDown(it, (it.width - popupView.measuredWidth) / 2, -(tool_panel.measuredHeight + popupView.measuredHeight))
            mPopupImageSource!!.showAsDropDown(it, (it.width - popupView.measuredWidth) / 2, -(tool_panel.measuredHeight + popupView.measuredHeight))
            restartUITimer()
        }
    }

    // Setup Image Source radio buttons in popup
    private fun setupImageSourceRadios() {
        var isDelayRunning = false
        val mDelayHandler = Handler()
        val delayRunnable = Runnable {
            checkRadioSource(false, false, false)
            enableRadioSource(true, true, true)
            if (mPopupImageSource!!.isShowing) {
                mPopupImageSource!!.dismiss()
            }
            isDelayRunning = false
        }

        mRadioRearCamera = mPopupImageSourceLayout!!.radio_rear
        mRadioRearCamera!!.setOnClickListener {
            if (!isDelayRunning) {
                checkRadioSource(true, false, false)
                enableRadioSource(true, false, false)
                if (mHasCameraPermission) {
                    radioRearCameraPushed()
                } else {
                    requestPermission(true, false) { radioRearCameraPushed() }
                }
                // keep showing popup for 0.5sec
                isDelayRunning = true
                mDelayHandler.postDelayed(delayRunnable, BUTTON_COLOR_DURATION)
            }
            restartUITimer()
        }

        mRadioFrontCamera = mPopupImageSourceLayout!!.radio_front
        mRadioFrontCamera!!.setOnClickListener {
            if (!isDelayRunning) {
                checkRadioSource(false, true, false)
                enableRadioSource(false, true, false)
                if (mHasCameraPermission) {
                    radioFrontCameraPushed()
                } else {
                    requestPermission(true, false) { radioFrontCameraPushed() }
                }
                // keep showing popup for 0.5sec
                isDelayRunning = true
                mDelayHandler.postDelayed(delayRunnable, BUTTON_COLOR_DURATION)
            }
            restartUITimer()
        }

        mRadioFile = mPopupImageSourceLayout!!.radio_file
        mRadioFile!!.setOnClickListener {
            if (!isDelayRunning) {
                checkRadioSource(false, false, true)
                enableRadioSource(false, false, true)
                if (mHasStoragePermission) {
                    radioFilePushed()
                } else {
                    requestPermission(false, true) { radioFilePushed() }
                }
                // keep showing popup for 0.5sec
                isDelayRunning = true
                mDelayHandler.postDelayed(delayRunnable, BUTTON_COLOR_DURATION)
            }
            restartUITimer()
        }
        // Set enable/disable of the image source buttons depending on the camera support status of the device,
        enableRadioSource(true, true, true)
    }

    private fun checkRadioSource(rear: Boolean, front: Boolean, file: Boolean) {
        mRadioRearCamera!!.isChecked = rear
        mRadioFrontCamera!!.isChecked = front
        mRadioFile!!.isChecked = file
    }

    private fun enableRadioSource(rear: Boolean, front: Boolean, file: Boolean) {
        mRadioRearCamera!!.isEnabled = rear && mHasRearCamera
        mRadioFrontCamera!!.isEnabled = front && mHasFrontCamera
        mRadioFile!!.isEnabled = file
    }

    private fun radioRearCameraPushed() {
        if (!mRendererCamera!!.changeCameraToRear()) {
            mError!!.log(tag, "Camera error - can not change to rear")
            mError!!.show(R.string.sCameraError)
            restartUITimer()
            return
        }
        if (mIsImageFromFile) { // Image was from camera
            setImageFromCamera()
        }
        resetZoomAndPan()
        restartUITimer()
    }

    private fun radioFrontCameraPushed() {
        if (!mRendererCamera!!.changeCameraToFront()) {
            mError!!.log(tag, "Camera error - can not change to front")
            mError!!.show(R.string.sCameraError)
            restartUITimer()
            return
        }
        if (mIsImageFromFile) { // Image was from camera
            setImageFromCamera()
        }
        resetZoomAndPan()
        restartUITimer()
    }

    private fun radioFilePushed() {
        if (mPopupImageSource!!.isShowing) {
            mPopupImageSource!!.dismiss()
        }
        val mimeTypes: Array<String> = arrayOf("image/jpeg", "image/png", "image/gif")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, INTENT_RESULT)
        restartUITimer()
    }

    // An image has read from Gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == INTENT_RESULT) && (resultCode == Activity.RESULT_OK)) {
            if (data != null) {
                mIntentData = data
                val uri = data.data
                if (uri != null) {
                    // Check file size. If space of image > space of display, load reduced image to bitmap
                    try {
                        var stream: InputStream = contentResolver.openInputStream(uri)
                        val options = BitmapFactory.Options()
                        options.inJustDecodeBounds = true
                        BitmapFactory.decodeStream(stream, null, options)
                        stream.close()
                        val displaySize: Size = getDefaultDisplaySize()
                        val timesSpace = kotlin.math.max((options.outWidth * options.outHeight.toLong()).toFloat() / (displaySize.width * displaySize.height.toLong()), 1.0f)
                        options.inSampleSize = kotlin.math.sqrt(timesSpace).toInt()
                        // Log.d(tag, "image (${options.outWidth}, ${options.outHeight}), display(${displaySize.width}, ${displaySize.height}), timesSpace: $timesSpace, inSampleSize: ${options.inSampleSize}")
                        options.inJustDecodeBounds = false
                        stream = contentResolver.openInputStream(uri)
                        mFileTextureBitmap = BitmapFactory.decodeStream(stream, null, options)
                        stream.close()
                    } catch (e: IOException) {
                        mError!!.log(tag, "File read error - IOException.")
                        mError!!.show(R.string.sFileLoadError)
                        return
                    } catch (e: SecurityException) {
                        mError!!.log(tag, "File read error - SecurityException.")
                        mError!!.show(R.string.sFileLoadError)
                    }
                    if (mFileTextureBitmap == null) {
                        mError!!.log(tag, "File read error - Can not read this file.")
                        mError!!.show(R.string.sFileLoadError)
                        return
                    }
                    val buffer: ByteBuffer = ByteBuffer.allocate(mFileTextureBitmap!!.byteCount)
                    mFileTextureBitmap!!.copyPixelsToBuffer(buffer)
                    buffer.rewind()
                    setImageFromFile(mFileTextureBitmap!!)
                    if (!mIsCallFromRestoreInstanceState) {
                        resetZoomAndPan()
                    }
                    return
                } else {
                    mError!!.log(tag, "File read error - Uri == null.")
                }
            } else {
                mError!!.log(tag, "File read error - Intent == null.")
            }
            mError!!.show(R.string.sFileLoadError)
        }
    }

    // Setup Save button
    private fun setupSaveButton() {
        // Push Save button
        button_save.setOnClickListener {
            if (!mIsImageFromFile) { // camera
                mRendererCamera!!.captureRequest()
            } else { // file
                mRendererFile!!.captureRequest()
            }
            enableAllButton(false)
            mSound!!.play(MediaActionSound.SHUTTER_CLICK)
        }
        restartUITimer()
    }

    private fun enableAllButton(enable: Boolean) {
        mRadioTypesViews.forEach {
            it!!.isEnabled = enable
        }
        button_simulation_ratio.isEnabled = enable && !(mCPDT[0] && !mCPDT[1] && !mCPDT[2] && !mCPDT[3])
        button_image_source.isEnabled = enable
        button_save.isEnabled = enable
    }

    private fun setupGestures() {
        // Zoom (Pinch in/out event)
        mScaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                mIsScaling = true
                return true
            }
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                super.onScale(detector)
                val scaleFactor: Float = detector.scaleFactor
                mZoom = adjustZoom(mZoom * scaleFactor)
                val (panX, panY) = adjustTextureOffset(mPanX, mPanY, mZoom)
                mPanX = panX
                mPanY = panY
                return true
            }
            override fun onScaleEnd(detector: ScaleGestureDetector) {
                super.onScaleEnd(detector)
                mLabelZoom!!.text = getString(R.string.sZoom).format((mZoom * 100).toInt())
                mIsScaling = false
                updateTextLabels()
                restartUITimer()
            }
        })

        mDoubleTapDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                super.onDoubleTap(e)
                resetZoomAndPan()
                showUI()
                return true
            }
        })
    }

    private fun setupAnimation() {
        mAnimeHideToolPanel = TranslateAnimation(0.0f, 0.0f, 0.0f, tool_panel.layoutParams.height.toFloat())
        mAnimeHideToolPanel!!.duration = PANEL_HIDDEN_DURATION
        mAnimeShowToolPanel = TranslateAnimation(0.0f, 0.0f, tool_panel.layoutParams.height.toFloat(), 0.0f)
        mAnimeShowToolPanel!!.duration = PANEL_HIDDEN_DURATION
    }

    private fun showUI() {
        if (!mIsShowingUI) {
            tool_panel.startAnimation(mAnimeShowToolPanel)
            tool_panel!!.visibility = View.VISIBLE
            mColorVisionTypeList.forEachIndexed { index, _ ->
                mLabelTypes[index]!!.visibility = View.VISIBLE
            }
            mLabelZoom!!.visibility = View.VISIBLE
        }
        updateTextLabels()
        restartUITimer()
        mIsShowingUI = true
    }

    private fun hideUI() {
        if (mIsShowingUI) {
            tool_panel.startAnimation(mAnimeHideToolPanel)
            tool_panel!!.visibility = View.INVISIBLE
            closePopups()
            mLabelTypes.forEach { it!!.visibility = View.INVISIBLE  }
            mLabelZoom!!.visibility = View.INVISIBLE
        }
        mIsShowingUI = false
    }

    private fun setupTextLabels() {
        mLabelTypes = Array(4) { TextView(this) }
        mLabelTypes.forEach {
            it!!.setTextColor(Color.WHITE)
            it.setShadowLayer(5.0f, 2.0f, 2.0f, Color.BLACK)
            it.visibility = View.INVISIBLE
            frame_layout.addView(it, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        mLabelZoom = TextView(this)
        mLabelZoom!!.setTextColor(Color.WHITE)
        mLabelZoom!!.setShadowLayer(5.0f, 2.0f, 2.0f, Color.BLACK)
        mLabelZoom!!.text = getString(R.string.sZoom).format((mZoom * 100).toInt())
        mLabelZoom!!.visibility = View.INVISIBLE

        frame_layout.addView(mLabelZoom, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
    }

    private fun setupFileSaveIntent() {
        mBroadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val bitmap: Bitmap? = if (!mIsImageFromFile) mRendererCamera!!.mCaptureBitmap!! else mRendererFile!!.mCaptureBitmap!!
                if (bitmap == null) {
                    mError!!.log(tag, "File save error - bitmap == null(1).")
                    mError!!.show(R.string.sFileSaveError)
                    enableAllButton(true)
                    return
                }
                if (mHasStoragePermission) {
                    realSave(bitmap)
                } else {
                    requestPermission(false, true) { realSave(bitmap) }
                }
            }
        }
        registerReceiver(mBroadCastReceiver, IntentFilter(CVGLRenderer.INTENT_FILTER))
        startService(Intent(application, MainActivity::class.java))
    }

    private fun releaseFileSaveIntent() {
        if (mBroadCastReceiver != null) {
            try {
                unregisterReceiver(mBroadCastReceiver)
            } catch (e: IllegalArgumentException) {
                // Sometimes error is occurred in unregisterReceiver(mBroadCastReceiver)
            }
        }
    }

    private fun realSave(bitmap: Bitmap?) {
        if (bitmap == null) {
            mError!!.log(tag, "File save error - bitmap == null(2).")
            mError!!.show(R.string.sFileSaveError)
            enableAllButton(true)
            return
        }
        if (saveBitmapToFile(bitmap)) {
            fileSaveAnimation(bitmap)
        } else {
            mError!!.show(R.string.sFileSaveError)
        }
        enableAllButton(true)
    }

    private fun saveBitmapToFile(bitmap: Bitmap?): Boolean {
        if (bitmap == null) {
            mError!!.log(tag, "File save error - bitmap == null(3).")
            mError!!.show(R.string.sFileSaveError)
            return false
        }

        val storageDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!storageDir.isDirectory) {
            storageDir.mkdir()
            if (!storageDir.isDirectory) {
                mError!!.log(tag, "File save error - Cannot make directory.")
                return false
            }
        }

        // Determination of file name
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US)
        val fileName = "cvs_${dateFormat.format(Date())}.jpg"
        val file = File(storageDir, fileName)

        // Save JPEG image to file
        try {
            val fos = FileOutputStream(file)
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos)) {
                mError!!.log(tag, "File save error - bitmap.compress failed.")
                return false
            }
            fos.close()
        } catch (e: FileNotFoundException) {
            mError!!.log(tag, "File save error - FileNotFoundException. Maybe don't have permission.")
            return false
        } catch (e: SecurityException) {
            mError!!.log(tag, "File save error - SecurityException")
            return false
        } catch (e: IOException) {
            mError!!.log(tag, "File save error - IOException.")
            return false
        }
        // Request to register to gallery (MediaScan)
        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))

        mError!!.log(tag,"Saved \"$fileName\".")
        return true
    }

    private fun fileSaveAnimation(bitmap: Bitmap?) {
        if (bitmap == null) {
            mError!!.log(tag, "File save error - bitmap == null(4).")
            mError!!.show(R.string.sFileSaveError)
            return
        }
        val imageview = ImageView(this)
        imageview.setImageBitmap(bitmap)
        imageview.z = 1.0f
        tool_panel.z = 2.0f

        val location = intArrayOf(0, 0)
        button_save.getLocationInWindow(location)
        val animation = ScaleAnimation(
                1.0f, 0.0f, 1.0f, 0.0f,
                Animation.ABSOLUTE, location[0] + button_save.measuredWidth / 2.0f,
                Animation.ABSOLUTE, (location[1] + button_save.measuredHeight / 2.0f))
        animation.duration = CAPTURE_ANIMATION_TIME
        animation.repeatCount = 0
        animation.fillAfter = true
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }
            override fun onAnimationEnd(p0: Animation?) {
                frame_layout.removeView(imageview)
            }
            override fun onAnimationRepeat(p0: Animation?) {
            }
        })
        frame_layout.addView(imageview, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        imageview.startAnimation(animation)
    }

    private fun setupTimers() {
        // Hide Tool Panel and Status when there is no touch for 10 seconds
        mUITimerHandler = Handler()
        mUITimerRunnable = Runnable { hideUI() }
        mUITimerHandler!!.postDelayed(mUITimerRunnable, PANEL_HIDDEN_INTERVAL)

        // Update status on every one second
        mStatusTimerHandler = Handler()
        mStatusTimerRunnable = Runnable {
            updateTextLabels()
            mStatusTimerHandler!!.postDelayed(mStatusTimerRunnable, STATUS_INTERVAL)
        }
        mStatusTimerHandler!!.postDelayed(mStatusTimerRunnable, STATUS_INTERVAL)
    }

    private fun restartUITimer() {
        mUITimerHandler ?: return
        mUITimerRunnable ?: return
        mUITimerHandler!!.removeCallbacks(mUITimerRunnable)
        mUITimerHandler!!.postDelayed(mUITimerRunnable, PANEL_HIDDEN_INTERVAL)
    }

    private fun releaseTimers() {
        mUITimerHandler!!.removeCallbacks(mUITimerRunnable)
        mStatusTimerHandler!!.removeCallbacks(mStatusTimerRunnable)
    }

    private fun setupSound() {
        mSound = MediaActionSound()
        mSound!!.load(MediaActionSound.SHUTTER_CLICK)
    }

    private fun releaseSound() {
        if (mSound != null) {
            mSound!!.release()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        event ?: return false

        //Pinch in/out event
        mScaleDetector!!.onTouchEvent(event)

        // DoubleTap event
        mDoubleTapDetector!!.onTouchEvent(event)

        // Pan
        if (event.pointerCount == 1) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mPreX = event.rawX
                    mPreY = event.rawY
                    showUI()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mIsImageFromFile &&  (mPrePointercount == 1)) { // only When "Image From File"
                        val newX: Float = event.rawX
                        val newY: Float = event.rawY
                        val displaySize: Size = getDisplaySize()
                        val moveX: Float = (newX - mPreX) / displaySize.width
                        val moveY: Float = (newY - mPreY) / displaySize.height
                        val (newPanX, newPanY) = adjustTextureOffset(mPanX + moveX, mPanY - moveY, mZoom)
                        mPanX = newPanX
                        mPanY = newPanY
                        mPreX = newX
                        mPreY = newY
                    } else {
                        mPreX = event.rawX
                        mPreY = event.rawY
                    }
                }
                MotionEvent.ACTION_UP -> {
                    mPreX = event.rawX
                    mPreY = event.rawY
                    updateTextLabels()
                    restartUITimer()
                }
            }
        }
        mPrePointercount = event.pointerCount
        return false
    }

    private fun adjustTextureOffset(offsetX: Float, offsetY: Float, zoom: Float): Pair<Float, Float> {
        val (hvAdjustingRatioX: Float, hvAdjustingRatioY: Float) = if (!mIsImageFromFile) mRendererCamera!!.getHvAdjustingRatio() else mRendererFile!!.getHvAdjustingRatio()
        val ratioX: Float = kotlin.math.max(1.0f, hvAdjustingRatioX)
        val ratioY: Float = kotlin.math.max(1.0f, hvAdjustingRatioY)
        var newOffsetX: Float = kotlin.math.min(kotlin.math.max(0.5f - zoom / 2.0f * ratioX, offsetX), zoom / 2.0f * ratioX - 0.5f)
        var newOffsetY: Float = kotlin.math.min(kotlin.math.max(0.5f - zoom / 2.0f * ratioY, offsetY), zoom / 2.0f * ratioY - 0.5f)
        if (zoom < 1.0f) {
            val plusOffsetX: Float = if (ratioX == 1.0f) (1.0f - zoom) / 2.0f else 0.0f
            val plusOffsetY: Float = if (ratioY == 1.0f) (1.0f - zoom) / 2.0f else 0.0f
            newOffsetX += plusOffsetX
            newOffsetY += plusOffsetY
        }
        return Pair(newOffsetX, newOffsetY)
    }

    private fun updateTextLabels() {
        if (!mIsShowingUI) return

        val displaySize: Size = getDisplaySize()
        val width: Int = displaySize.width
        val height: Int = displaySize.height

        if ((width <= 0) || (height <= 0)) return

        val visible = IntArray(4) { View.INVISIBLE }
        mColorVisionTypeList.forEachIndexed { index, type ->
            mLabelTypes[index]!!.text = mColorVisionTypeLabels[type]
            mLabelTypes[index]!!.measure(0, 0)
            visible[index] = View.VISIBLE
        }
        mLabelTypes.forEachIndexed { index, _ ->
            mLabelTypes[index]!!.visibility = visible[index]
        }

        mLabelZoom!!.text = getString(R.string.sZoom).format((mZoom * 100).toInt())
        mLabelZoom!!.measure(0, 0)
        val lzwidth: Float = mLabelZoom!!.measuredWidth.toFloat()
        mLabelZoom!!.translationX = width - lzwidth
        mLabelZoom!!.translationY = 0.0f

        val width12: Float = width / 2.0f
        val width13: Float = width / 3.0f
        val width14: Float = width / 4.0f
        val width34: Float = width * 3.0f / 4.0f
        val width16: Float = width / 6.0f
        val width56: Float = width * 5.0f / 6.0f
        val height12: Float = height / 2.0f
        val height13: Float = height / 3.0f
        val height23: Float = height * 2.0f / 3.0f
        val l0width: Float = mLabelTypes[0]!!.measuredWidth.toFloat()
        val l1width: Float = mLabelTypes[1]!!.measuredWidth.toFloat()
        val l2width: Float = mLabelTypes[2]!!.measuredWidth.toFloat()
        val l3width: Float = mLabelTypes[3]!!.measuredWidth.toFloat()
        var l0width12: Float = l0width / 2.0f
        var l1width12: Float = l1width / 2.0f
        var l2width12: Float = l2width / 2.0f
        var l3width12: Float = l3width / 2.0f

        val numTypes: Int = mColorVisionTypeList.size

        // If device has small screen, use short string
        var divX = 1
        if ((numTypes == 2 && width > height) || numTypes == 4) {
            divX = 2
        } else if (numTypes == 3 && width > height) {
            divX = 3
        }
        val maxWidth: Float = width / divX.toFloat()
        var isWidthTooShort = false
        when (divX) {
            1 -> {
                isWidthTooShort = (maxWidth < width12 - l0width12 + l0width + lzwidth)
            }
            2 -> {
                if (numTypes == 2) {
                    isWidthTooShort =  ((maxWidth < l0width)
                            || (maxWidth < width14 - l1width12 + l1width + lzwidth))
                } else if (numTypes == 4) {
                    isWidthTooShort = ((maxWidth < l0width)
                            || (maxWidth < width14 - l1width12 + l1width + lzwidth)
                            || (maxWidth < l2width)
                            || (maxWidth < l3width))
                }
            }
            3 -> {
                isWidthTooShort = ((maxWidth < l0width)
                        || (maxWidth < l1width)
                        || (maxWidth < width13 - l3width12 + l3width + lzwidth))
            }
        }

        if (isWidthTooShort) {
            mColorVisionTypeList.forEachIndexed { index, type ->
                mLabelTypes[index]!!.text = mColorVisionTypeLabelsShort[type]
                mLabelTypes[index]!!.measure(0, 0)
            }
            l0width12 = mLabelTypes[0]!!.measuredWidth / 2.0f
            l1width12 = mLabelTypes[1]!!.measuredWidth / 2.0f
            l2width12 = mLabelTypes[2]!!.measuredWidth / 2.0f
            l3width12 = mLabelTypes[3]!!.measuredWidth / 2.0f
        }

        when (numTypes) {
            1 -> {
                mLabelTypes[0]!!.translationX = width12 - l0width12
                mLabelTypes[0]!!.translationY = 0.0f
            }
            2 -> {
                if (width < height) {
                    mLabelTypes[0]!!.translationX = width12 - l0width12
                    mLabelTypes[0]!!.translationY = 0.0f
                    mLabelTypes[1]!!.translationX = width12 - l1width12
                    mLabelTypes[1]!!.translationY = height / 2.0f
                } else {
                    mLabelTypes[0]!!.translationX = width14 - l0width12
                    mLabelTypes[0]!!.translationY = 0.0f
                    mLabelTypes[1]!!.translationX = width34 - l1width12
                    mLabelTypes[1]!!.translationY = 0.0f
                }
            }
            3 -> {
                if (width < height) {
                    mLabelTypes[0]!!.translationX = width12 - l0width12
                    mLabelTypes[0]!!.translationY = 0.0f
                    mLabelTypes[1]!!.translationX = width12 - l1width12
                    mLabelTypes[1]!!.translationY = height13
                    mLabelTypes[2]!!.translationX = width12 - l2width12
                    mLabelTypes[2]!!.translationY = height23
                } else {
                    mLabelTypes[0]!!.translationX = width16 - l0width12
                    mLabelTypes[0]!!.translationY = 0.0f
                    mLabelTypes[1]!!.translationX = width12 - l1width12
                    mLabelTypes[1]!!.translationY = 0.0f
                    mLabelTypes[2]!!.translationX = width56 - l2width12
                    mLabelTypes[2]!!.translationY = 0.0f
                }
            }
            4 -> {
                mLabelTypes[0]!!.translationX = width14 - l0width12
                mLabelTypes[0]!!.translationY = 0.0f
                mLabelTypes[1]!!.translationX = width34 - l1width12
                mLabelTypes[1]!!.translationY = 0.0f
                mLabelTypes[2]!!.translationX = width14 - l2width12
                mLabelTypes[2]!!.translationY = height12
                mLabelTypes[3]!!.translationX = width34 - l3width12
                mLabelTypes[3]!!.translationY = height12
            }
        }

        if (badge_number!!.visibility == View.VISIBLE) {
            badge_number.text = (mSimulationRatio * 100).toInt().toString()
        }
    }

    private fun closePopups() {
        mPopupImageSource!!.dismiss()
        mPopupSimulationRatio!!.dismiss()
    }

    private fun getDisplaySize(): Size {
        return(Size(gl_surface_view_camera.width, gl_surface_view_camera.height))
    }

    private fun getDefaultDisplaySize(): Size {
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        return Size(point.x, point.y)
    }

    override fun onResume() {
        super.onResume()

        if (gl_surface_view_camera != null) {
            gl_surface_view_camera.onResume()
        }
        if (gl_surface_view_file != null) {
            gl_surface_view_file.onResume()
        }

        // Setup "Ready To Save Image" intent from CVGLRenderer
        setupFileSaveIntent()

        // Setup Timer
        setupTimers()

        // Setup sound
        setupSound()

        enableAllButton(true)

        // showUI
        showUI()
    }

    override fun onPause() {
        if (gl_surface_view_camera != null) {
            gl_surface_view_camera.onPause()
        }
        if (gl_surface_view_file != null) {
            gl_surface_view_file.onPause()
        }

        // Close camera
        if (!mIsImageFromFile) {
            mRendererCamera!!.closeCamera()
        }

        // Release "Ready To Save Image" intent
        releaseFileSaveIntent()

        // Release timers
        releaseTimers()

        // Release sound
        releaseSound()

        super.onPause()
    }

    override fun onDestroy() {
        if (mFileTextureBitmap != null) {
            mFileTextureBitmap!!.recycle()
        }
        super.onDestroy()
    }

    override fun onSaveInstanceState(saveInstanceState: Bundle) {
        super.onSaveInstanceState(saveInstanceState)
        saveInstanceState.putBooleanArray("CPDT", mCPDT)
        saveInstanceState.putFloat("SimulationRatio", mSimulationRatio)
        saveInstanceState.putFloat("PanX", mPanX)
        saveInstanceState.putFloat("PanY", mPanY)
        saveInstanceState.putFloat("Zoom", mZoom)

        saveInstanceState.putInt("CameraPosition", mRendererCamera!!.mCameraPosition)
        saveInstanceState.putBoolean("IsImageFromFile", mIsImageFromFile)
        if (mIsImageFromFile) {
            saveInstanceState.putParcelable("IntentData", mIntentData)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mCPDT = savedInstanceState.getBooleanArray("CPDT")
        makeColorVisionTypeList(mCPDT)
        mSimulationRatio = savedInstanceState.getFloat("SimulationRatio")
        mPanX = savedInstanceState.getFloat("PanX")
        mPanY = savedInstanceState.getFloat("PanY")
        mZoom = savedInstanceState.getFloat("Zoom")
        button_simulation_ratio.isEnabled = !(mCPDT[0] && !mCPDT[1] && !mCPDT[2] && !mCPDT[3])
        badge_number.visibility = if (button_simulation_ratio.isEnabled && (mSimulationRatio < 1.0f)) View.VISIBLE else View.INVISIBLE

        mRendererCamera!!.mCameraPosition = savedInstanceState.getInt("CameraPosition")
        if (mRendererCamera!!.mCamera == null) {
            mRendererCamera!!.setupTextureCameraShader()
        }

        mIsImageFromFile = savedInstanceState.getBoolean("IsImageFromFile")
        if (mIsImageFromFile) {
            mIntentData = savedInstanceState.getParcelable("IntentData")
            mIsImageFromFile = false
            mIsCallFromRestoreInstanceState = true
            onActivityResult(INTENT_RESULT, Activity.RESULT_OK, mIntentData)
            mIsCallFromRestoreInstanceState = false
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        resetPan()
        if (mIsImageFromFile) {
            resetZoom()
        }
        closePopups()
        showUI()
    }

    private fun resetZoom() {
        mZoom = 1.0f
    }

    private fun resetPan() {
        mPanX = 0.0f
        mPanY = 0.0f
    }

    private fun resetZoomAndPan() {
        resetZoom()
        resetPan()
    }

    private fun setImageFromFile(bitmap: Bitmap?) {
        if (!mIsImageFromFile) {
            mRendererCamera!!.closeCamera()
        }
        if (bitmap != null) {
            mIsImageFromFile = true
            mRendererFile!!.setFileTextureBitmap(bitmap)
            showFileView()
        }
    }

    private fun setImageFromCamera() {
        mIsImageFromFile = false
        mRendererFile!!.unsetFileTextureBitmap()
        showCameraView()
    }

    private fun showCameraView() {
        if (gl_surface_view_camera != null) {
            gl_surface_view_camera.visibility = View.VISIBLE
        }

        if (gl_surface_view_file != null) {
            gl_surface_view_file.visibility = View.INVISIBLE
        }
    }

    private fun showFileView() {
        if (gl_surface_view_camera != null) {
            gl_surface_view_camera.visibility = View.INVISIBLE
        }

        if (gl_surface_view_file != null) {
            gl_surface_view_file.visibility = View.VISIBLE
        }
    }
}