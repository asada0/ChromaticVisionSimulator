//
//  CVGLRendererFile.kt
//  Chromatic Vision Simulator
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2018/08/10.
//  Copyright 2010-2018 Kazunori Asada. All rights reserved.
//

package asada0.android.cvsimulator

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.Size
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class CVGLRendererFile(context: Context, activity: Activity): CVGLRenderer(context, activity) {
    override
    val tag: String = "CVS-CVGLRendererFile"
    private var mFileTextureBitmap: Bitmap? = null

    init {
        init()
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        mTexture = CVTexture()
        mOffscreenShader = CVShader(mContext)
        mOffscreenShader!!.setProgram(R.raw.cvvshader, R.raw.cvfhader_2d)
        mOffscreenShader!!.useProgram()
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        mFileTextureBitmap ?: return
        mSourceSize = getImageSize()
        mScreenSize = Size(width, height)
        adjustTexture()
    }

    override fun onDrawFrame(gl: GL10) {
        if (!mIsImageFromFile) return
        mFileTextureBitmap ?: return
        mTexture!!.bindFileTexture(mFileTextureBitmap!!)
        drawScreen()
    }

    fun setFileTextureBitmap(bitmap: Bitmap) {
        mFileTextureBitmap = bitmap
        adjustTexture()
    }

    fun unsetFileTextureBitmap() {
        mFileTextureBitmap = null
    }

    private fun getImageSize(): Size {
        if (mFileTextureBitmap == null) {
            return Size(0, 0)
        }
        return Size(mFileTextureBitmap!!.width, mFileTextureBitmap!!.height)
    }
}