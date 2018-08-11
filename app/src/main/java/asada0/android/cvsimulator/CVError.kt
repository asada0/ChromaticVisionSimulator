//
//  CVError.kt
//  Chromatic Vision Simulator
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2018/08/10.
//  Copyright 2010-2018 Kazunori Asada. All rights reserved.
//

package asada0.android.cvsimulator

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast

class CVError(context: Context) {
    private var mContext: Context = context
    var mShowDebugAlert = MainActivity.SHOW_ALERT

    fun show(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(mContext, message as CharSequence, Toast.LENGTH_LONG).show()
        }
    }

    fun show(messageIndex: Int) {
        show(mContext.getString((messageIndex)))
    }

    fun log(tag: String, message: String) {
        Log.d(tag, message)

        // for beta test only
        if (mShowDebugAlert) {
            Handler(Looper.getMainLooper()).post {
                AlertDialog.Builder(mContext, R.style.AlertDialogStyle).setTitle("DEBUG")
                        .setMessage("$tag: $message")
                        .setPositiveButton("OK", null)
                        .show()
            }
        }
    }

    fun log(tag: String, messageIndex: Int) {
        log(tag, mContext.getString(messageIndex))
    }
}
