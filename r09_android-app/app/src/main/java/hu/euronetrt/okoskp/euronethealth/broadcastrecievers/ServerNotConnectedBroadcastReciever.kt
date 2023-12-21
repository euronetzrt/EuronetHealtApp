package hu.euronetrt.okoskp.euronethealth.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity
import hu.euronetrt.okoskp.euronethealth.GlobalRes.SERVER_NOT_CONN
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.TAGSCAN

class ServerNotConnectedBroadcastReciever : BroadcastReceiver() {

    companion object {
        val TAG = "ServerNotConnBR"
    }

    private lateinit var deviceMainActivity: DeviceMainActivity

    override fun onReceive(context: Context?, intent: Intent?) {
        val action: String? = intent?.action
        when (action) {
            SERVER_NOT_CONN -> {
                Log.d(TAGSCAN, "SERVER_NOT_CONN ")
                if (context is DeviceMainActivity) {
                    deviceMainActivity = context
                    deviceMainActivity.serverNotConnected()
                } else {
                    Log.d(TAGSCAN, "Not implement context fragment")
                }
            }
        }
    }
}