package hu.euronetrt.okoskp.euronethealth.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity

class BluetoothOFFBroadcastReciever : BroadcastReceiver() {

    companion object {
        val TAG = "BluetoothOFFBroadcastReciever"
    }

    private lateinit var deviceMainActivity: DeviceMainActivity

    override fun onReceive(context: Context, intent: Intent) {

        val action: String? = intent.action
        when (action) {
           "android.bluetooth.adapter.action.STATE_CHANGED" -> {
                Log.d(TAG, "bluetooth.adapter.action.STATE_CHANGED")
                if (context is DeviceMainActivity) {
                    deviceMainActivity = context
                    deviceMainActivity.bluetoothChange()
                } else {
                    Log.d(TAG, "Not implement context fragment")
                }
            }
       }
    }
}
