package hu.euronetrt.okoskp.euronethealth.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity
import hu.euronetrt.okoskp.euronethealth.GlobalRes

class BleConnectBroadcastReceiver : BroadcastReceiver() {

    companion object {
        val TAG = "BleConnectBroadcastReceiver"
    }
    private lateinit var deviceMainActivity: DeviceMainActivity

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG,"onReceive")
        val action: String? = intent.action
        when (action) {
            GlobalRes.ACTION_ACL_CONNECTED -> {
                Log.d(TAG,"ACTION_ACL_CONNECTED")
                if (context is DeviceMainActivity) {
                    deviceMainActivity = context
                    deviceMainActivity.deviceConnected()
                } else {
                    Log.d(TAG, "Not implement context fragment")
                }
            }
            GlobalRes.ACTION_ACL_DISCONNECTED -> {
                //ObservableObject.instance.updateValue("Faild")
                if (context is DeviceMainActivity) {
                    deviceMainActivity = context
                    deviceMainActivity.deviceDisconnected()
                } else {
                    Log.d(TAG, "Not implement context fragment")
                }
            }
            GlobalRes.ACTION_SERVICE_DISCOVER_DONE -> {
                //ObservableObject.instance.updateValue("Faild")
                if (context is DeviceMainActivity) {
                    deviceMainActivity = context
                } else {
                    Log.d(TAG, "Not implement context fragment")
                }
            }
        }
    }
}

