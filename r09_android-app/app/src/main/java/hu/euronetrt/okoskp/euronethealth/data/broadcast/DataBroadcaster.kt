package hu.euronetrt.okoskp.euronethealth.data.broadcast

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.bluetooth.BindServiceClass
import hu.euronetrt.okoskp.euronethealth.bluetooth.BluetoothServiceNotificationType
import hu.euronetrt.okoskp.euronethealth.data.AbstractData
import hu.euronetrt.okoskp.euronethealth.data.CollectableType
import hu.euronetrt.okoskp.euronethealth.data.LocalStorage
import java.util.*
import kotlin.collections.ArrayList

class DataBroadcaster {

    private var gotIBI: Boolean = false
    private var gotPPG: Boolean = false
    private var gotHR: Boolean = false
    private lateinit var date: Calendar
    private var started: Boolean = false
    private var mContext: Context
    private var wasError = false

    constructor(mContext: Context) {
        this.mContext = mContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: DataBroadcaster? = null
        private val TAG = "DataBroadcaster"

        @Synchronized
        fun getInstance(mContext: Context): DataBroadcaster {
            if (INSTANCE == null) {
                INSTANCE = DataBroadcaster(mContext)
            }
            return INSTANCE!!
        }
    }

    private val lock = Object()
    private var listeners: TreeMap<CollectableType, ArrayList<DataListener<AbstractData>>> = TreeMap()

    fun addListener(type: CollectableType, listener: DataListener<AbstractData>) {
        synchronized(lock) {
            if (!listeners.containsKey(type)) {
                listeners[type] = ArrayList()
            }
            listeners[type]!!.add(listener)
        }
    }

    fun removeListener(type: CollectableType, listener: DataListener<AbstractData>) {
        synchronized(lock) {
            if (!listeners.containsKey(type)) {
                return
            }
            listeners[type]!!.remove(listener)
        }
    }

    /**
     * send
     *
     * @param data AbstractData
     */
    fun send(data: AbstractData) {
        if (!GlobalRes.applicationVersionModeIsLight) {
            if (started) {

                if (!::date.isInitialized) {
                    date = Calendar.getInstance()
                    date.add(Calendar.SECOND, 10)
                }

                when (data.getType()) {
                    CollectableType.PPG -> {
                        gotPPG = true
                        Log.d(TAG, "PPG OK!")
                    }
                    CollectableType.IBI -> {
                        gotIBI = true
                        Log.d(TAG, "IBI OK!")
                    }
                    CollectableType.HEARTRATE -> {
                        gotHR = true
                        Log.d(TAG, "HR OK!")
                    }
                    else -> {
                        //return
                    }
                }

                val calendar: Calendar = Calendar.getInstance()

                if (date.time < calendar.time) {
                    Log.d(TAG, "started time +10 sec  < now ${date.time} , ${calendar.time}")
                    //indulást követően 10 másodperc alatt mindennek jönnie kellett!
                    if (!gotHR || !gotIBI || !gotPPG) {
                        wasError = true
                        Log.d(TAG, "Invalid License: $gotHR, $gotIBI, $gotPPG")
                        val bleService = BindServiceClass.getInstance(mContext).getBleServiceBind()
                        // stop hr
                        //bleService.heartRateValueNotification(false)
                        bleService.serviceNotificationsSet(BluetoothServiceNotificationType.HEARTRATE_SERVICE.type, false)
                        // stop nordic
                        bleService.serviceNotificationsSet(BluetoothServiceNotificationType.NORDIC_SERVICE.type, false)
                        // stop battery
                        bleService.serviceNotificationsSet(BluetoothServiceNotificationType.BATTERY_SERVICE.type, false)

                        //Liseners stop
                        LocalStorage.getInstance(mContext).stop()

                        bleService.unSubscribeTriangleSignal()
                        bleService.getGatt().disconnect()
                        bleService.getGatt().close()
                        GlobalRes.connectedDevice = false
                        //unbind service
                        BindServiceClass.getInstance(mContext).unBind()
                        //service stop
                        bleService.stopForgroundService()

                        val errorLicense = Intent()
                        errorLicense.action = "ERROR_LICENSE"
                        mContext.sendBroadcast(errorLicense)
                    }
                } else {
                    Log.d(TAG, "nem szolgáltatunk addig adatot míg a vizsgálat fut.")
                    return // nem szolgáltatunk addig adatot mig a vizsgálat fut.
                }
            }
        }

        if (!wasError) {
            started = false
            //  Log.d(TAG, "Letelt a vizsgálati idő és jött minden mehet az adat")
            synchronized(lock) {
                if (listeners.containsKey(data.getType())) {
                    listeners[data.getType()]!!.forEach {
                        it.onDataArrived(data)
                    }
                }
            }
        }
    }

    fun getStartedApp(): Boolean {
        return started
    }
}