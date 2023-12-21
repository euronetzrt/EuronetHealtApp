package hu.euronetrt.okoskp.euronethealth.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService

class BOOTReciever : BroadcastReceiver() {

    companion object {
        val TAG = "BOOTReciever"
    }

    override fun onReceive(context: Context, intent: Intent) {
       Log.d(TAG,"${PreferenceManager.getDefaultSharedPreferences(context.applicationContext).getBoolean("autoStart", true)}")

        if(PreferenceManager.getDefaultSharedPreferences(context.applicationContext).getBoolean("autoStart", true)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, BluetoothLeService::class.java))
            } else {
                context.startService(Intent(context, BluetoothLeService::class.java))
            }
        }
    }
}
