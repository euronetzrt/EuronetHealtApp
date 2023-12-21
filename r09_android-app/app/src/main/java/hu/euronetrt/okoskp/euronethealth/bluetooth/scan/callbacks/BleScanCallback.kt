package hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import hu.euronetrt.okoskp.euronethealth.GlobalRes.BLUETOOTHGATT
import hu.euronetrt.okoskp.euronethealth.GlobalRes.searchingDeviceList
import hu.euronetrt.okoskp.euronethealth.bluetooth.bleInterfaces.BLEServiceReferenceInterface
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.adapter.DeviceAdapter
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.dataClass.DeviceData
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService

@SuppressLint("StaticFieldLeak")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object BleScanCallback : ScanCallback() {

    lateinit var contextIsDeviceMainAct: Context
    lateinit var recycleDeviceList: RecyclerView
    lateinit var adapter: DeviceAdapter
    private lateinit var bLEServiceReferenceInterface: BLEServiceReferenceInterface
    private lateinit var contextBLE: Context
    var arrayOfFoundBTDevices = ArrayList<DeviceData>()
    private val TAG = "BleScanCallback"

    fun setContextFromDeviceMainAct(context: Context, recycleView: RecyclerView) {
        contextIsDeviceMainAct = context
        recycleDeviceList = recycleView
    }

    fun setBleContext(context: Context) {
        contextBLE = context
        setInterfaceContext(contextBLE)
    }

    private fun setInterfaceContext(contextBLE: Context) {
        if (contextBLE is BluetoothLeService) {
            bLEServiceReferenceInterface = contextBLE
        }
    }

    fun getBLEServiceReferenceInterface(): BLEServiceReferenceInterface {
        return bLEServiceReferenceInterface
    }

    /**
     * onScanResult
     *
     * @param callbackType
     * @param result
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onScanResult(callbackType: Int, result: ScanResult) {
        /*ha egyszer megtaláltuk mégegyszer nem vizsgáljuk, név nélkül nem kell az eszköz.*/
        if (result.device.name != null || result.device.name != "") {
            /** Duplikáció elkerülése
             * */
            arrayOfFoundBTDevices.forEach {
                if (it.bluetooth_address == result.device?.address) {
                    Log.d(BLUETOOTHGATT + 2, "arrayOfFoundBTDevices eleme " + it.bluetooth_address)
                    recycleDeviceList.adapter?.notifyDataSetChanged()
                    return
                }
            }

            if (!searchingDeviceList.contains(result.device?.address)) {
                searchingDeviceList.add(result.device.address)
                try {
                    Log.e(BLUETOOTHGATT, " $TAG --> getConnectorThread()!!.addResult(result) ${result.device.address}")
                    bLEServiceReferenceInterface.getConnectorThread()!!.addResult(result)
                } catch (e: KotlinNullPointerException) {
                    Log.e(BLUETOOTHGATT, " $TAG -->${e.message} ${result.device?.address}")
                }
            }
        }
    }
}