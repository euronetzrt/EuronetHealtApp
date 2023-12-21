package hu.euronetrt.okoskp.euronethealth.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import hu.euronetrt.okoskp.euronethealth.GlobalRes.enabled
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService

class BleServiceStartBroadcastReceiver : BroadcastReceiver() {

    companion object {
        val TAG = "STARTSERVICE"
        val START_SERVICE = "hu.aut.android.dm01_v11.action.START_SERVICE"
        val STARTDEVACTIVITY = "startDevActivity"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive run")
        val action: String? = intent.action

        when (action) {
            START_SERVICE -> {
                Log.d(TAG, "START SERVICE" + intent.extras)

                if (!enabled) {

                    val mIntent = Intent()

                    if (intent.hasExtra(STARTDEVACTIVITY) && intent.getBooleanExtra(STARTDEVACTIVITY, false)) {
                        mIntent.putExtra(STARTDEVACTIVITY, intent.getBooleanExtra(STARTDEVACTIVITY, false))
                    }
                    mIntent.setClass(context, BluetoothLeService::class.java)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(Intent(mIntent))
                    } else {
                        context.startService(Intent(mIntent))
                    }
                }
            }
        }
    }
}


