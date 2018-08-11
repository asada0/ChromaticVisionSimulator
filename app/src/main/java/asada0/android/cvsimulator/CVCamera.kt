//
//  CVCamera.kt
//  Chromatic Vision Simulator
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2018/08/10.
//  Copyright 2010-2018 Kazunori Asada. All rights reserved.
//

package asada0.android.cvsimulator

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.StreamConfigurationMap
import android.graphics.Point
import android.os.Handler
import android.os.HandlerThread
import android.util.Range
import android.util.Size
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import java.util.*
import java.util.concurrent.Semaphore

class CVCamera(activity: Activity, textureID: Int) {
    private val tag: String = "CVS-CVCamera"

    companion object {
        const val CAMERA_NONE: Int = 0
        const val CAMERA_BACK: Int = 1
        const val CAMERA_FRONT: Int = 2
    }

    private var mActivity: Activity? = activity
    private var mTextureID: Int = textureID
    private var mCamera: CameraDevice? = null
    private var mPreviewSession: CameraCaptureSession? = null
    private var mPreviewBuilder: CaptureRequest.Builder? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mCameraFpsRange: Range<Int> = Range(10, 10)
    private var mError = CVError(activity)
    private var mCameraOpenCloseSemaphore = Semaphore(1)

    var mCameraSize: Size = Size(0, 0)
    var mCameraPosition: Int = CAMERA_BACK
    var mSensorOrientation: Int = 0

    fun open(cameraPosition: Int): Boolean {
        if (mCamera != null) {
            closeCamera()
        }

        // Camera Position
        mCameraPosition = cameraPosition

        val targetFacing: Int = when (cameraPosition) {
            CAMERA_BACK -> CameraCharacteristics.LENS_FACING_BACK
            CAMERA_FRONT -> CameraCharacteristics.LENS_FACING_FRONT
            else -> {
                mError.log(tag, "Camera error - Neither front nor rear camera.")
                return false
            }
        }
        val manager = mActivity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (cameraId: String in manager.cameraIdList) {
            val chars: CameraCharacteristics = manager.getCameraCharacteristics(cameraId)
            val facing: Int? = chars.get(CameraCharacteristics.LENS_FACING)

            if (facing != null && facing == targetFacing) {
                val map: StreamConfigurationMap = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) as StreamConfigurationMap
                mSensorOrientation = chars.get(CameraCharacteristics.SENSOR_ORIENTATION)
                // Choose camera resolution
                val sizes: Array<Size> = map.getOutputSizes(SurfaceTexture::class.java)
                mCameraSize = chooseResolution(sizes)!!
                // Choose camera frequency
                val ranges: Array<Range<Int>> = chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
                mCameraFpsRange = chooseFPS(ranges)!!

                // Start thread
                val thread = HandlerThread("OpenCVSCamera")
                thread.start()
                val backgroundHandler = Handler(thread.looper)

                mCameraOpenCloseSemaphore.acquire()
                try {
                    manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                        override fun onOpened(camera: CameraDevice) {
                            mCamera = camera
                            createCaptureSession()
                        }
                        override fun onDisconnected(camera: CameraDevice) {
                            closeCameraForce()
                        }
                        override fun onError(camera: CameraDevice, error: Int) {
                            mError.log(tag, "Camera error - [$error] in CameraDevice.onError().")
                            closeCameraForce()
                        }
                    }, backgroundHandler)
                } catch (e: CameraAccessException) {
                    mError.log(tag, "Camera error - CameraAccessException in open.")
                    mCameraOpenCloseSemaphore.release()
                    return false
                } catch (e: IllegalStateException) {
                    mError.log(tag, "Camera error - IllegalStateException in open.")
                    mCameraOpenCloseSemaphore.release()
                    return false
                } catch (e: SecurityException) {
                    mError.log(tag, "Camera error - SecurityException in open.")
                    mCameraOpenCloseSemaphore.release()
                    return false
                }
                return true
            }

        }
        return false
    }

    fun closeCamera() {
        mCamera ?: return
        mCameraOpenCloseSemaphore.acquire()
        mCamera!!.close()
        mCameraOpenCloseSemaphore.release()
    }

    fun closeCameraForce() {
        mCamera ?: return
        mCamera!!.close()
        mCameraOpenCloseSemaphore.release()
    }

    private fun createCaptureSession() {
        mSurfaceTexture = SurfaceTexture(mTextureID)
        mSurfaceTexture!!.setDefaultBufferSize(mCameraSize.width, mCameraSize.height)
        val surface = Surface(mSurfaceTexture)

        try {
            mPreviewBuilder = mCamera!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        } catch (e: CameraAccessException) {
            mError.log(tag, "Camera error - CameraAccessException in createCaptureRequest")
            closeCameraForce()
        } catch (e: IllegalStateException) {
            mError.log(tag, "Camera error - IllegalStateException in createCaptureRequest")
            closeCameraForce()
        }

        mPreviewBuilder!!.addTarget(surface)
        try {
            mCamera!!.createCaptureSession(Collections.singletonList(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    mPreviewSession = session
                    updatePreview()
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    mError.log(tag, "Camera error - in CameraCaptureSession.onConfigureFailed().")
                    closeCameraForce()
                }
            }, null)
        } catch (e: CameraAccessException) {
            mError.log(tag, "Camera error - CameraAccessException in createCaptureSession")
            closeCameraForce()
        } catch (e: IllegalStateException) {
            mError.log(tag, "Camera error - IllegalStateException in createCaptureSession")
            closeCameraForce()
        }
    }

    private fun updatePreview() {
        mPreviewBuilder!!.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        mPreviewBuilder!!.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
        mPreviewBuilder!!.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
        mPreviewBuilder!!.set(CaptureRequest.CONTROL_AE_LOCK, false)
        mPreviewBuilder!!.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, mCameraFpsRange)
        val thread = HandlerThread("CameraPreview")
        thread.start()
        val backgroundHandler = Handler(thread.looper)

        try {
            mPreviewSession!!.setRepeatingRequest(mPreviewBuilder!!.build(), null, backgroundHandler)
        } catch (e: CameraAccessException) {
            mError.log(tag, "Camera error - CameraAccessException in setRepeatingRequest")
            closeCameraForce()
        } catch (e: IllegalStateException) {
            mError.log(tag, "Camera error - IllegalStateException in setRepeatingRequest")
            closeCameraForce()
        }
        mCameraOpenCloseSemaphore.release()
    }

    fun updateTexture(): SurfaceTexture? {
        if (mSurfaceTexture != null) {
            mSurfaceTexture!!.updateTexImage()
        }
        return mSurfaceTexture
    }

    private fun isCameraDisplayTwisted(cameraSize: Size, displaySize: Size): Boolean {
        // Twisted or Not?: Camera orientation and Display dimension
        //  portrait & landscape | landscape & landscape -> true
        //  portrait & portrait | landscape & landscape -> false
        val sourceAspect: Float = cameraSize.width.toFloat() / cameraSize.height
        val displayAspect: Float = displaySize.width.toFloat() / displaySize.height
        return ((sourceAspect > 1.0f) && (displayAspect < 1.0f)) || ((sourceAspect < 1.0f) && (displayAspect > 1.0f))
    }

    // Choose a Camera resolution
    private fun chooseResolution(supportedSizes: Array<Size>): Size? {
        var displaySize: Size = getDefaultDisplaySize()
        if (isCameraDisplayTwisted(supportedSizes[0], displaySize)) {
            displaySize = Size(displaySize.height, displaySize.width)
        }

        val larger: List<Size> = supportedSizes.filter { it.width >= displaySize.width && it.height >= displaySize.height }
        if (!larger.isEmpty()) {
            // Choose a resolution whose width and height are larger than that of the display and which has a shortest distance between "width difference" and "height difference".
            return larger.minBy { (it.width - displaySize.width) * (it.width - displaySize.width) + (it.height - displaySize.height) * (it.height - displaySize.height) }
        }
        // If either the width or the height is smaller than that of the display, choose a resolution which has a shortest distance between "width difference" and "height difference".
        return supportedSizes.minBy { (it.width - displaySize.width) * (it.width - displaySize.width) + (it.height - displaySize.height) * (it.height - displaySize.height) }
    }

    // Choose a Camera FPS
    private fun chooseFPS(supportedFPSs: Array<Range<Int>>): Range<Int>? {
        val above10: List<Range<Int>> = supportedFPSs.filter { it.upper >= 10 }
        if (!above10.isEmpty()) {
            // Choose FPS with the smallest upper above 10 FPS. If there are two or more, choose the one with the biggest lower
            return above10.minWith(Comparator { a, b -> (a.upper - b.upper) * 1000 + (b.lower - a.lower) })
        }
        // If there is no upper with 10 FPS or more, choose the one with the biggest upper. If there are two or more, choose the one with the biggest lower
        return supportedFPSs.maxWith(Comparator { a, b -> (a.upper - b.upper) * 1000 + (a.lower - b.lower) })
    }

    private fun getDefaultDisplaySize(): Size {
        val display: Display = (mActivity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        display.getSize(size)
        return Size(size.x, size.y)
    }
}