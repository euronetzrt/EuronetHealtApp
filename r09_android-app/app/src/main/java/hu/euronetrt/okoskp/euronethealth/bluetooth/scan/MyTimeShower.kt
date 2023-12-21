package hu.euronetrt.okoskp.euronethealth.bluetooth.scan

import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.DeadObjectException
import android.os.Handler
import android.util.Log
import hu.euronetrt.okoskp.euronethealth.GlobalRes.BLUETOOTHGATT
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.BleScanCallback

class MyTimeShower(private val gatt: BluetoothGatt, private val connectorThread: BleConnector) : Thread() {

    companion object{
      private val TAG = "MyTimeShower"
    }

    override fun run() {
        val h = Handler((BleScanCallback.getBLEServiceReferenceInterface() as Context).mainLooper)
        var interrupted = false
        try {
            Log.d(BLUETOOTHGATT, "$TAG  --> sleep(3000) ${gatt.device.address}")
            sleep(3000)
        } catch (e: InterruptedException) {
            Log.d(BLUETOOTHGATT, "$TAG  --> InterruptedException ${gatt.device.address}")
            e.printStackTrace()
            interrupted = true
        }
        if (!interrupted) {
            try {
                h.post {
                    val inWork: Boolean? = connectorThread.isInWork()
                    if (inWork!!) {
                        Log.d(BLUETOOTHGATT, "$TAG  --> Disconnect by timeout ${gatt.device.address}")
                        gatt.disconnect()
                        Log.d(BLUETOOTHGATT, "$TAG  --> call gatt.close ${gatt.device.address}")
                        gatt.close()
                        Log.d(BLUETOOTHGATT, "$TAG  --> stop connect ${gatt.device.address}")
                        //timeout eldobáskor újra próbálkozhat a scan során.
                        //searchingDeviceList.remove(gatt.device.address)
                        connectorThread.timedOutConnection()
                    }
                }
            }catch (e : DeadObjectException){
                Log.d(TAG,"Exceptiont fogtunk a scan során : ${e.message}")
            }
        }
    }
}
