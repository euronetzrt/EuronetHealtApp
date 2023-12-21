package hu.euronetrt.okoskp.euronethealth.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MHeartRateBroadcastReceiver : BroadcastReceiver() {

  //  private lateinit var deviceAct: DeviceActivity
    private val EXTRA_DATA = "EXTRA_DATA"

    companion object {
        val TAG = "MHEART_RATE_BROADCAST"
    }

    override fun onReceive(context: Context, intent: Intent) {

      //  val action: String? = intent.action
    /*    when (action) {
            ACTION_HEART_DATA_AVAILABLE -> {
                Log.d(TAG, "ACTION_HEART_DATA_AVAILABLE " + intent.extras!![EXTRA_DATA])
                if (context is DeviceActivity) {
                    deviceAct = context
                    val frg = deviceAct.heartRateFragment
                    frg.dataRefresh(intent.extras!![EXTRA_DATA]!!.toString().toInt())
                } else {
                    Log.d(TAG, "Not implement context fragement")
                }
            }
        }*/
    }
}