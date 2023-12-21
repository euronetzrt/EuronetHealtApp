package hu.euronetrt.okoskp.euronethealth.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class MBroadcastDeviceConnChangeReceiver : BroadcastReceiver() {
    companion object {
        val TAG = "DEVICECONNCHANGEREC"
    }

    override fun onReceive(context: Context, intent: Intent) {

     //   val action: String? = intent.action
    //    when (action) {
   //         CONNECT_AVAILABLE -> {
                Log.d(TAG, "Connected Log:" + intent.extras)
   //             DeviceActivity.myDeviceActivity.reconnect()
 //           }
    //        DISCONNECT_AVAILABLE -> {
                Log.d(TAG, "DISCONNECTET Log ")
    //            DeviceActivity.myDeviceActivity.disconnectDevice()
            }
   //     }
   // }
}