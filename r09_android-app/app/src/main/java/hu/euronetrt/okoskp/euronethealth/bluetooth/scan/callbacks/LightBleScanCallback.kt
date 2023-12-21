package hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.adapter.LightDeviceAdapter
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.dataClass.LightDeviceData

@SuppressLint("StaticFieldLeak")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object LightBleScanCallback : ScanCallback() {

    @SuppressLint("StaticFieldLeak")
    var lightArrayOfFoundBTDevices = ArrayList<LightDeviceData>()
    lateinit var lightContextMain: Context
    private lateinit var recycleDeviceList: RecyclerView
    lateinit var lightAdapter: LightDeviceAdapter

    fun setContext(context: Context, recycleView: RecyclerView) {
        lightContextMain = context
        recycleDeviceList = recycleView
    }

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        Log.d("BleScanCallback", "onScanResult(): ${result?.device?.address} - ${result?.device?.name}")


        if (!GlobalRes.connectedDevice) {
            var devicefound = false

            /*
             * Duplikáció elkerülése
             * */
            lightArrayOfFoundBTDevices.forEach {
                Log.d("arrayOfFoundBTDevices", "arrayOfFoundBTDevices" + it.bluetooth_address)
                if (it.bluetooth_address == result?.device?.address) {
                    //    it.bluetooth_rssi = result.rssi
                    // refreshView.refreshRecycleView(arrayOfFoundBTDevices)
                    recycleDeviceList.adapter?.notifyDataSetChanged()
                    devicefound = true
                }
            }

            if (!devicefound) {
                val items = LightDeviceData()

                if (result?.device?.name != null) {
                    items.bluetooth_name = result.device!!.name
                    items.bluetooth_address = result.device!!.address
                    //    items.bluetooth_rssi = result.rssi

                    /*    if (all_devices.size > 0) {
                         all_devices.forEach {
                             Log.d("Device Name ", "Device Name " + it.name)
                             if (it.address == result.device!!.address) {
                                 items.bluetooth_pair = 1
                             } else {
                                 items.bluetooth_pair = 0
                             }
                         }
                     }*/

                    Log.d("DeviceScan +BLESC", items.bluetooth_address)

                    Log.d("DeviceScan +BLESC", lightArrayOfFoundBTDevices.toString())
                    lightArrayOfFoundBTDevices.add(items)

                    lightAdapter = LightDeviceAdapter(lightContextMain, lightArrayOfFoundBTDevices)
                    recycleDeviceList.adapter = lightAdapter

                    lightAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}