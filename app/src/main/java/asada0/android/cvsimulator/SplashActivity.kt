//
//  SplashActivity.kt
//  Chromatic Vision Simulator
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2018/08/10.
//  Copyright 2010-2018 Kazunori Asada. All rights reserved.
//

package asada0.android.cvsimulator

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View

class SplashActivity : AppCompatActivity() {
    companion object {
        const val SPLASH_DELAY_TIME = 1000L
    }
    private val handler = Handler()
    private val runnable = Runnable {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        setContentView(R.layout.activity_splash)
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, SPLASH_DELAY_TIME)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }
}
