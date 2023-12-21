package hu.euronetrt.okoskp.euronethealth.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity

class ErrorLicenseBroadcastReciever : BroadcastReceiver() {

    companion object {
        val TAG = "ErrorLicenseBroadcastReciever"

    }

    private lateinit var deviceMainActivity: DeviceMainActivity

    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.action
        when (action) {
            "ERROR_LICENSE" -> {
                if (context is DeviceMainActivity) {
                    deviceMainActivity = context
                    deviceMainActivity.errorLicense()
                }
            }
        }
    }
}