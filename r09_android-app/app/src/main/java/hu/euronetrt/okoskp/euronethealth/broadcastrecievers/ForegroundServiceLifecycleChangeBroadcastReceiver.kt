package hu.euronetrt.okoskp.euronethealth.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import hu.euronetrt.okoskp.euronethealth.GlobalRes


class ForegroundServiceLifecycleChangeBroadcastReceiver : BroadcastReceiver() {

    companion object {
        val TAG = "BLE_BATTERY_BROADCAST"
    }

   // private lateinit var deviceMainActivity: DeviceMainActivity

    override fun onReceive(context: Context, intent: Intent) {

        val action: String? = intent.action
        when (action) {
            GlobalRes.ACTION_BATTERY_DATA_AVAILABLE -> {
                Log.d(TAG, "ACTION_BATTERY_DATA_AVAILABLE" + intent.extras!![GlobalRes.EXTRA_DATA])
     //           if (context is DeviceMainActivity) {
       //             deviceMainActivity = context
         //           deviceMainActivity.changeDeviceBatteryValue(intent.extras!![GlobalRes.EXTRA_DATA]!!.toString().toInt())
           //     } else {
             //       Log.d(TAG, "Not implement context fragment")
               /// }
            }
        }
    }
}
