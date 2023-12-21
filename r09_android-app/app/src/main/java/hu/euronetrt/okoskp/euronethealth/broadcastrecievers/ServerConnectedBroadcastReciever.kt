package hu.euronetrt.okoskp.euronethealth.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity
import hu.euronetrt.okoskp.euronethealth.GlobalRes.SERVER_CONN


class ServerConnectedBroadcastReciever : BroadcastReceiver() {

    companion object {
        val TAG = "ServerConnBR"
    }

    private lateinit var deviceMainActivity: DeviceMainActivity

    override fun onReceive(context: Context?, intent: Intent?) {
        val action: String? = intent?.action
        when (action) {
            SERVER_CONN -> {
                Log.d(TAG, "SERVER_CONN ")
                if (context is DeviceMainActivity) {
                    deviceMainActivity = context
                    deviceMainActivity.serverConnected()
                } else {
                    Log.d(TAG, "Not implement context fragment")
                }
            }
        }
    }
}