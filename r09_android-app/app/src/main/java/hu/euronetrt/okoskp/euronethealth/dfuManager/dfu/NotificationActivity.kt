package hu.euronetrt.okoskp.euronethealth.dfuManager.dfu

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import hu.euronetrt.okoskp.euronethealth.dfuManager.dfu.FirmwareUpdateActvity.Companion.DFUTAG

class NotificationActivity : Activity() {
    companion object {
        val TAG = "NOTIFICATIONACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(DFUTAG, "NotificationActivity start")
        // If this activity is the root activity of the task, the app is not running
        if (isTaskRoot) {
            // Start the app before finishing
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtras(intent.extras!!) // copy all extras
            Log.d(DFUTAG, "NotificationActivity call activity start")
            startActivity(intent)
        }
        // Now finish, which will drop you to the activity at which you were at the top of the task stack
        finish()
    }
}