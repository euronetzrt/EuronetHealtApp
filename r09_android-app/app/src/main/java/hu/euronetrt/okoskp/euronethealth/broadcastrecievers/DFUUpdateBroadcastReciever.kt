package hu.euronetrt.okoskp.euronethealth.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.dfuManager.dfu.DFUUpdateActivity

class DFUUpdateBroadcastReciever : BroadcastReceiver() {
    companion object {
        val TAG = "BLE_BATTERY_BROADCAST"
    }

    private lateinit var dfuUpdateActivity: DFUUpdateActivity

    override fun onReceive(context: Context, intent: Intent) {

        val action: String? = intent.action
        when (action) {
        /*    GlobalRes.DFU_START -> {
                Log.d(TAG, "DFU_START")
                if (context is DFUUpdateActivity) {
                    dfuUpdateActivity = context
                    dfuUpdateActivity.dfuStart()
                } else {
                    Log.d(TAG, "Not implement context fragment")
                }
            }*/
            GlobalRes.DFU_UPDATE_PROGRESS_REFRESH -> {
                Log.d(TAG, "DFU_START")
                if (context is DFUUpdateActivity) {
                    dfuUpdateActivity = context
                    val extras =intent.extras
                    if(extras != null && !extras.isEmpty){
                        val newPercent = extras.getInt("NEW_PERCENT")
                        dfuUpdateActivity.dfuUpdate(newPercent)
                    }
                } else {
                    Log.d(TAG, "Not implement context fragment")
                }
            }
            GlobalRes.DFU_UPDATE_SUCCESSFULLY -> {
                Log.d(TAG, "DFU_UPDATE_SUCCESSFULLY ")
                if (context is DFUUpdateActivity) {
                    dfuUpdateActivity = context
                    dfuUpdateActivity.dfuSuccess()
                } else {
                    Log.d(TAG, "Not implement context fragment")
                }
            }
            GlobalRes.DFU_UPDATE_ERROR -> {
                Log.d(TAG, "DFU_UPDATE_ERROR ")
                if (context is DFUUpdateActivity) {
                    dfuUpdateActivity = context
                    dfuUpdateActivity.dfuError()
                } else {
                    Log.d(TAG, "Not implement context fragment")
                }
            }
        }
    }
}
