//
//  CVGLRendererCamera.kt
//  Chromatic Vision Simulator
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2018/08/10.
//  Copyright 2010-2018 Kazunori Asada. All rights reserved.
//

package asada0.android.cvsimulator

import android.app.Activity
import android.content.Context
import android.util.Size
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CVGLRendererCamera(context: Context, activity: Activity): CVGLRenderer(context, activity) {
    override
    val tag: String = "CVS-CVGLRendererCamera"

    init {
        init()
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        setupTextureCameraShader()
    }

    fun setupTextureCameraShader() {
        if ((mCameraPosition != CVCamera.CAMERA_BACK) && (mCameraPosition != CVCamera.CAMERA_FRONT)) {
            return
        }
        if (mCamera != null) {
            mCamera!!.closeCamera()
        }
        mTexture = CVTexture()
        mCamera = CVCamera(mActivity!!, mTexture!!.mTextureHandle)
        if (!mCamera!!.open(mCameraPosition)) {
            mCamera!!.closeCamera()
        }
        // Load and Compile shader
        mOffscreenShader = CVShader(mContext)
        mOffscreenShader!!.setProgram(R.raw.cvvshader, R.raw.cvfshader_ex)
        mOffscreenShader!!.useProgram()
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        mCamera ?: return
        mSourceSize = mCamera!!.mCameraSize
        mScreenSize = Size(width, height)
        adjustTexture()
    }

    override fun onDrawFrame(gl: GL10) {
        mCamera ?: return
        if (mIsImageFromFile) return
        mCamera!!.updateTexture()
        mTexture!!.bindCameraTexture()
        drawScreen()
    }

    private fun setCameraPosition(cameraPosition: Int): Boolean {
        mCamera ?: return false
        if (!mCamera!!.open(cameraPosition)) {
            mCamera!!.closeCamera()
            return false
        }
        mSourceSize = mCamera!!.mCameraSize
        mCameraPosition = cameraPosition
        adjustTexture()
        return true
    }

    fun changeCameraToRear(): Boolean {
        mCamera ?: return false
        return setCameraPosition(CVCamera.CAMERA_BACK)
    }

    fun changeCameraToFront(): Boolean {
        mCamera ?: return false
        return setCameraPosition(CVCamera.CAMERA_FRONT)
    }

    fun closeCamera() {
        mCamera ?: return
        mCamera!!.closeCamera()
    }
}