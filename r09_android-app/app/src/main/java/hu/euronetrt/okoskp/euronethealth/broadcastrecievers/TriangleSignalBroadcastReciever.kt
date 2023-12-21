package hu.euronetrt.okoskp.euronethealth.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity
import hu.euronetrt.okoskp.euronethealth.GlobalRes

class TriangleSignalBroadcastReciever : BroadcastReceiver() {

    companion object {
        val TAG = "TriangleSignalBroadcastReciever"
    }

    private lateinit var deviceMainActivity: DeviceMainActivity

    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.action
        when (action) {
            GlobalRes.ACTION_TRIANGLESIGNAL_DATA_AVAILABLE -> {
                Log.d(TAG, "ACTION_TRIANGLESIGNAL_DATA_AVAILABLE" + intent.extras!![GlobalRes.EXTRA_DATA])
                if (context is DeviceMainActivity) {
                    deviceMainActivity = context
                    deviceMainActivity.setTriangleValue(intent.extras!![GlobalRes.EXTRA_DATA]!! as Int)
                } else {
                    Log.d(TAG, "Not implement context fragment")
                }
            }
        }
    }
}
