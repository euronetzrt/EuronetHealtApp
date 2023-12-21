package hu.euronetrt.okoskp.euronethealth.bluetooth

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService

class BindServiceClass {

    private var mContext: Context
    private lateinit var bluetoothLeService: BluetoothLeService
   // private val lock: Object = Object()

    constructor(mContext: Context) {
        this.mContext = mContext
        bind()
    }

    companion object {

        private lateinit var INSTANCE: BindServiceClass
        private val TAG = "BindServiceClass"

        @Synchronized
        fun getInstance(mContext: Context): BindServiceClass {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = BindServiceClass(mContext)
            }
            return INSTANCE
        }
    }

    private fun bind(){
        mContext.bindService(
                Intent(mContext, BluetoothLeService::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        )
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "continousTryConnectDeviceForUI run ")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bluetoothLeService = (service as BluetoothLeService.BluetoothLeServiceBinder).service
        }
    }

    fun unBind() {
        if(::bluetoothLeService.isInitialized){
            mContext.unbindService(serviceConnection)
        }
    }

    fun getBleServiceBind () : BluetoothLeService{
        return bluetoothLeService
    }
}
