//
//  CVGLRenderer.kt
//  Chromatic Vision Simulator
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2018/08/10.
//  Copyright 2010-2018 Kazunori Asada. All rights reserved.
//

package asada0.android.cvsimulator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Size
import android.view.Surface
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

open class CVGLRenderer(context: Context, activity: Activity) : GLSurfaceView.Renderer {
    open val tag: String = "CVS-CVGLRenderer"

    companion object {
        const val FLOATSIZE: Int = 4
        const val INTENT_FILTER = "READY_TO_SAVE_ACTION"
    }

    var mContext: Context = context
    var mActivity: Activity? = activity
    var mTexture: CVTexture? = null
    var mCamera: CVCamera? = null
    var mOffscreenShader:CVShader? = null
    var mCaptureBitmap: Bitmap? = null
    var mCameraPosition: Int = CVCamera.CAMERA_BACK
    private var mError = CVError(context)

    // Simulation Parameters are set by U/I
    var mSimulationRatio: Float = 1.0f
    var mZoom: Float = 1.0f
    var mPanX: Float = 0.0f
    var mPanY: Float = 0.0f
    var mColorVisionTypeList: List<Int> = listOf(0) // Color Vision Types list of multi screen
    var mIsImageFromFile: Boolean = false

    // Public Variables
    var mScreenSize: Size = Size(0, 0)
    var mSourceSize: Size = Size(0, 0)

    // Useful info but not used currently
    private var mIsPortraitDevice: Boolean = true // Is this device's default orientation Portrait?
    private var mIsDisplayOrientationPortrait: Boolean = true // Is this device's current orientation Portrait?

    // Private Variables
    private var mDisplayRotation = Surface.ROTATION_0
    private var mCapturing: Boolean = false
    private var mMirror: Boolean = false


    // Vertices Matrix
    private val mSquareVerticesMat = floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f,  1.0f,
            1.0f,  1.0f
    )

    // Texture Matrix
    private val mTextureCoordinatesMat0 = floatArrayOf(
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    )

    private val mTextureCoordinatesMat90 = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    )

    private val mTextureCoordinatesMat180 = floatArrayOf(
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
    )

    private val mTextureCoordinatesMat270 = floatArrayOf(
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f
    )

    private var mZoomVerticesMat = mSquareVerticesMat.copyOf()
    private val mZoomVerticesBuf = ByteBuffer.allocateDirect(FLOATSIZE * mZoomVerticesMat.count()).order(ByteOrder.nativeOrder()).asFloatBuffer()
    private val mTextureCoordinatesBuf = ByteBuffer.allocateDirect(FLOATSIZE * mTextureCoordinatesMat0.count()).order(ByteOrder.nativeOrder()).asFloatBuffer()
    private var mTextureCoordinatesMat = mTextureCoordinatesMat0

    private val divPortrait = arrayOf(Pair(1, 1), Pair(1, 2), Pair(1, 3), Pair(2, 2))
    private val divLandscape = arrayOf(Pair(1, 1), Pair(2, 1), Pair(3, 1), Pair(2, 2))

    init {
        init()
    }

    fun init() {
        mIsPortraitDevice = isPortraitDevice()
        // mError.log(tag, if (mIsPortraitDevice) "Device type : Smartphone" else "Device type : Tablet")
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
    }

    protected fun adjustTexture() {
        mDisplayRotation = getDisplayRotation()

        var textureRotation = 0
        if (!mIsImageFromFile) {
            if (mCamera != null) {
                textureRotation = calcTextureRotation(mDisplayRotation, mCamera!!.mSensorOrientation, mCamera!!.mCameraPosition == CVCamera.CAMERA_FRONT)
            } else {
                mError.log(tag, "Camera error - mCamera == null.")
            }
        }
        mTextureCoordinatesMat = getTextureMatrix(textureRotation)

        // Is isDisplay Orientation Portrait?
        mIsDisplayOrientationPortrait = (mContext.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
    }

    private fun getDisplayRotation(): Int {
        val rotation:Int = (mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
        return when (rotation) {
            Surface.ROTATION_0 -> 0 // Display rotation: 0 (Portrait on Smartphone)
            Surface.ROTATION_90 -> 1 // Display rotation: 90 (Landscape Left on Smartphone)
            Surface.ROTATION_180 -> 2 // Display rotation: 180 (Portrait Upside down on Smartphone)
            Surface.ROTATION_270 -> 3 // Display rotation: 270 (Landscape Right on Smartphone)
            else -> 0
        }
    }

    private fun calcTextureRotation(displayRotation: Int, cameraSensorOrientation: Int, isCameraPositionFront: Boolean): Int {
        var rotateIndex: Int = displayRotation
        if (isCameraPositionFront) {
            if (displayRotation == 1) rotateIndex = 3
            else if (displayRotation == 3) rotateIndex = 1
        }
        val cameraRotateIndex: Int = cameraSensorOrientation / 90
        return (rotateIndex + 4 - cameraRotateIndex) % 4
    }

    private fun getTextureMatrix(textureRotation: Int):  FloatArray {
        return when (textureRotation) {
            0 -> mTextureCoordinatesMat0 // Display vs Source: 0
            1 -> mTextureCoordinatesMat90 // Display vs Source: 90
            2 -> mTextureCoordinatesMat180 // Display vs Source: 180
            3 -> mTextureCoordinatesMat270 // Display vs Source: 270
            else -> mTextureCoordinatesMat0 // Display vs Source: (unknown)
        }
    }

    private fun isPortraitDevice(): Boolean {
        val pIsDisplayOrientationPortrait: Boolean = mContext.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val pDisplayRotation: Int = (mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
        // Is this device's default orientation Portrait? Smart phone type device -> true, Tablet type device -> false
        return if (pIsDisplayOrientationPortrait) pDisplayRotation == Surface.ROTATION_0 || pDisplayRotation == Surface.ROTATION_180 else pDisplayRotation == Surface.ROTATION_90 || pDisplayRotation == Surface.ROTATION_270
    }

    override fun onDrawFrame(gl: GL10){
        drawScreen()
    }

    protected fun drawScreen() {
        // Clear Screen by dark gray
        GLES20.glClearColor(0.333f, 0.333f, 0.333f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        mOffscreenShader ?: return

        // Measures for that onSurfaceChanged() is not called when the device rotation in the front and back.
        if (mDisplayRotation != getDisplayRotation()) {
            // mError.log(tag, "Rotation in the forward/backward direction.")
            adjustTexture()
        }

        // Setup vertex parameters
        val aVertexPosition: Int = mOffscreenShader!!.getHandle("aVertexPosition")
        mZoomVerticesBuf.put(mZoomVerticesMat).position(0)
        GLES20.glVertexAttribPointer(aVertexPosition, 2, GLES20.GL_FLOAT, false, 0, mZoomVerticesBuf)
        GLES20.glEnableVertexAttribArray(aVertexPosition)

        val aVertexTextureCoord: Int = mOffscreenShader!!.getHandle("aVertexTextureCoord")
        mTextureCoordinatesBuf.put(mTextureCoordinatesMat).position(0)
        GLES20.glVertexAttribPointer(aVertexTextureCoord, 2, GLES20.GL_FLOAT, false, 0, mTextureCoordinatesBuf)
        GLES20.glEnableVertexAttribArray(aVertexTextureCoord)

        mZoomVerticesMat = mSquareVerticesMat.copyOf()

        val displaySize: Size = getDefaultDisplaySize()
        val numOfTypes: Int = mColorVisionTypeList.count()
        val (xDiv, yDiv) = if (displaySize.width < displaySize.height) divPortrait[numOfTypes - 1] else divLandscape[numOfTypes - 1]
        val (hvAdjustingRatioX: Float, hvAdjustingRatioY: Float) = getHvAdjustingRatio()

        // zoom and pan
        // zoom : default 1.0
        // panX, panY : default 0.0. Set panY = -0.5 to shift the image halfway up the screen upwards.
        for (i in 0..7 step 2) {
            mZoomVerticesMat[i] = mSquareVerticesMat[i] * mZoom * hvAdjustingRatioX + mPanX * 2.0f
            mZoomVerticesMat[i + 1] = mSquareVerticesMat[i + 1] * mZoom * hvAdjustingRatioY + mPanY * 2.0f
        }

        // Mirror image when using Front Camera
        mMirror = (!mIsImageFromFile && (mCameraPosition == CVCamera.CAMERA_FRONT)) // Mirror
        if (mMirror) {
            for (i in 0..7 step 2) {
                mZoomVerticesMat[i] = -mZoomVerticesMat[i]
            }
        }

        // set Simulation Ratio
        mOffscreenShader!!.setSimulationRatio(mSimulationRatio)

        // Draw screen
        for (y in 0..(yDiv - 1)) {
            for (x in 0..(xDiv - 1)) {
                // Set viewport
                GLES20.glViewport(mScreenSize.width / xDiv * x + x, (mScreenSize.height / yDiv) * y + y, mScreenSize.width / xDiv, mScreenSize.height / yDiv)
                // Set Color Vision Types
                val cvTypeIndex: Int = (yDiv - 1 - y) * xDiv + x
                if (cvTypeIndex < mColorVisionTypeList.count()) {
                    if (numOfTypes == 1) {
                        mOffscreenShader!!.setColorVisionType(mColorVisionTypeList[0])
                    } else {
                        mOffscreenShader!!.setColorVisionType(mColorVisionTypeList[cvTypeIndex])
                    }
                    // Draw a color vision image
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
                }
            }
        }

        if (mCapturing) { // Capture GLView and save it to gallery (Gallery button is pushed)
            capture()
            // Send Ready To Save Image intent to MainActivity
            sendReadyToSaveIntent()
        }
    }

    private fun getDefaultDisplaySize(): Size {
        return(Size(mActivity!!.gl_surface_view_camera.width, mActivity!!.gl_surface_view_camera.height))
    }

    private fun isSourceDisplayTwisted(): Boolean {
        // Twisted or Not?: Camera orientation and Display dimension
        //  portrait & landscape | landscape & landscape -> true
        //  portrait & portrait | landscape & landscape -> false
        mCamera ?: return false
        val sourceAspect: Float = mSourceSize.width.toFloat() / mSourceSize.height
        val screenAspect: Float = mScreenSize.width.toFloat() / mScreenSize.height
        return ((sourceAspect > 1.0f) && (screenAspect < 1.0f)) || ((sourceAspect < 1.0f) && (screenAspect > 1.0f))
    }

    fun getHvAdjustingRatio(): Pair<Float, Float> {
        // Get hvAdjustingRatio for texture aspect
        val displaySize: Size = getDefaultDisplaySize()
        val numOfTypes: Int = mColorVisionTypeList.count()
        val (xDiv, yDiv) = if (displaySize.width < displaySize.height) divPortrait[numOfTypes - 1] else divLandscape[numOfTypes - 1]
        val twisted = isSourceDisplayTwisted()
        val sourceWidthTwist = if (!twisted) mSourceSize.width.toFloat() else mSourceSize.height.toFloat()
        val sourceHeightTwist = if (!twisted) mSourceSize.height.toFloat() else mSourceSize.width.toFloat()
        val screenWidthDiv = mScreenSize.width.toFloat() / xDiv
        val screenHeightDiv = mScreenSize.height.toFloat() / yDiv
        val hvAdjustingRatioX = Math.max((sourceWidthTwist / sourceHeightTwist) / (screenWidthDiv / screenHeightDiv), 1.0f)
        val hvAdjustingRatioY = Math.max((screenWidthDiv / screenHeightDiv) / (sourceWidthTwist / sourceHeightTwist), 1.0f)
        return Pair(hvAdjustingRatioX, hvAdjustingRatioY)
    }

    fun captureRequest() {
        mCapturing = true
    }

    private fun capture() {
        val pixels = IntArray(mScreenSize.width * mScreenSize.height)
        val buffer: Buffer = IntBuffer.wrap(pixels)
        buffer.position(0)

        // Read image data from OpenGL
        GLES20.glReadPixels(0, 0, mScreenSize.width, mScreenSize.height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)

        val glBitmap: Bitmap
        try {
            glBitmap = Bitmap.createBitmap(mScreenSize.width, mScreenSize.height, Bitmap.Config.ARGB_8888)
        } catch (e: OutOfMemoryError) {
            mError.show(R.string.sFileSaveError)
            mError.log(tag, "Out of memory error - Capture() glBitmap.")
            mCapturing = false
            return
        }
        glBitmap.copyPixelsFromBuffer(buffer)
        val transMatrix = Matrix()
        transMatrix.postScale(1.0f, -1.0f)
        if (mMirror) {
            transMatrix.postScale(-1.0f, 1.0f)
        }

        // Set vertical flipped image to newBitmap
        try {
            mCaptureBitmap = Bitmap.createBitmap(glBitmap, 0, 0, mScreenSize.width, mScreenSize.height, transMatrix, false)
        } catch (e: OutOfMemoryError) {
            mError.show(R.string.sFileSaveError)
            mError.log(tag, "Out of memory error - Capture() mCaptureBitmap.")
            mCapturing = false
            return
        }

        // Embed Color Vision Type Labels in bitmap
        embedTypesLabels(mCaptureBitmap!!)
        mCapturing = false
    }

    private fun embedTypesLabels(bitmap: Bitmap) {
        val displaySize: Size = getDefaultDisplaySize()
        val mColorVisionTypeChars = arrayOf(mContext.getString(R.string.sCChar), mContext.getString(R.string.sPChar), mContext.getString(R.string.sDChar), mContext.getString(R.string.sTChar))
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.argb((255 * 0.8).toInt(), 255, 255, 255)
        paint.isAntiAlias = true
        paint.setShadowLayer(5.0f, 3.0f, 3.0f, Color.BLACK)
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = determinEmbeddedFontSize()

        val offsetX: Float = expectedEmbeddedLabelWidth() * 0.5f
        val offsetY: Float = paint.measureText("D") * 1.5f

        // Draw Color Vision Types
        when (mColorVisionTypeList.size) {
            1 -> {
                canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[0]], offsetX, offsetY, paint)
            }
            2 -> {
                if (displaySize.width < displaySize.height) {
                    canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[0]], offsetX, offsetY, paint)
                    canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[1]], offsetX, offsetY + displaySize.height / 2.0f, paint)
                } else {
                    canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[0]], offsetX, offsetY, paint)
                    canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[1]], offsetX + displaySize.width / 2.0f, offsetY, paint)
                }
            }
            3 -> {
                if (displaySize.width < displaySize.height) {
                    canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[0]], offsetX, offsetY, paint)
                    canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[1]], offsetX, offsetY + displaySize.height / 3.0f, paint)
                    canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[2]], offsetX, offsetY + displaySize.height / 3.0f * 2.0f, paint)
                } else {
                    canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[0]], offsetX, offsetY, paint)
                    canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[1]], offsetX + displaySize.width / 3.0f, offsetY, paint)
                    canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[2]], offsetX + displaySize.width / 3.0f * 2.0f, offsetY, paint)
                }
            }
            4 -> {
                canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[0]], offsetX, offsetY, paint)
                canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[1]], offsetX + displaySize.width / 2.0f, offsetY, paint)
                canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[2]], offsetX, offsetY + displaySize.height / 2.0f, paint)
                canvas.drawText(mColorVisionTypeChars[mColorVisionTypeList[3]], offsetX + displaySize.width / 2.0f, offsetY + displaySize.height / 2.0f, paint)
            }
        }

        // Draw Simulation Ratio
        if (mSimulationRatio != 1.0f) { // Don't show Simulation Ratio when it is 1.0
            paint.textSize *= 0.666f // 2/3 size of Chromatic Vision Type Labels
            val zoomString: String = mContext.getString(R.string.sRatio).format((mSimulationRatio * 100).toInt())
            val zoomWidth: Float = paint.measureText(zoomString)
            val zoomTop = paint.fontMetrics.top
            canvas.drawText(zoomString, displaySize.width - zoomWidth, -zoomTop, paint)
        }
    }

    private fun expectedEmbeddedLabelWidth(): Float {
        return kotlin.math.min(mScreenSize.width, mScreenSize.height) / 20.0f
    }

    private fun determinEmbeddedFontSize(): Float {
        val paint = Paint()
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 0.0f
        do {
            paint.textSize += 1.0f
            val width: Float = paint.measureText("D")
        } while (width < expectedEmbeddedLabelWidth())
        return paint.textSize
    }

    private fun sendReadyToSaveIntent() {
        mContext.sendBroadcast(Intent(INTENT_FILTER))
    }
}