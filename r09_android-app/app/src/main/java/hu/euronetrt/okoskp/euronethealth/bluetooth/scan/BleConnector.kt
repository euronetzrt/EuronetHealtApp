package hu.euronetrt.okoskp.euronethealth.bluetooth.scan

import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import hu.euronetrt.okoskp.euronethealth.GlobalRes.BLUETOOTHGATT
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.BleScanCallback.getBLEServiceReferenceInterface
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.BleScanCallbackConnectionCB

/**
 * BleConnector
 *
 */
class BleConnector : Thread() {

    companion object {
        private val TAG = "BLECONNECTOR"
    }

    private val lock = Object()
    private val waitObj = Object()
    private var enabled = true

    private var devicesToConnect: ArrayList<ScanResult> = ArrayList()

    private var myTimeShower: MyTimeShower? = null
    private var currentScanResult: ScanResult? = null
    private var gattObject: BluetoothGatt? = null
    private var currentConnected = false
    private var inWork = false
    private lateinit var cb : BleScanCallbackConnectionCB

    /**
     * add Result
     *
     * @param scanResult
     */
    fun addResult(scanResult: ScanResult) {
        synchronized(lock) {
            devicesToConnect.add(scanResult)
        }
    }

    fun isInWork(): Boolean {
        var result: Boolean
        synchronized(lock) {
            result = inWork
        }
        return result
    }

    /**
     * set In Work
     *
     * @param inWork
     */
    fun setInWork(inWork: Boolean) {
        synchronized(lock) {
            this.inWork = inWork
            if (inWork) {
                if(gattObject != null){
                    myTimeShower = MyTimeShower(gattObject!!, this)
                    myTimeShower!!.start()
                }
            } else {
                myTimeShower!!.interrupt()
                myTimeShower = null
            }
        }
    }

    /**
     * get Result
     *
     * @return
     */
    private fun getResult(): ScanResult? {
        var result: ScanResult? = null
        val sortedList = devicesToConnect.sortedWith(compareByDescending<ScanResult> { it.device.address }
                .thenByDescending { it.rssi })
        devicesToConnect.clear()
        devicesToConnect.addAll(sortedList)

        synchronized(lock) {
            if (devicesToConnect.isNotEmpty()) {
                result = devicesToConnect.removeAt(0)
            }
        }
        return result
    }

    fun timedOutConnection() {
        synchronized(lock) {
            currentScanResult = null
            currentConnected = false
            myTimeShower = null
        }
    }

    fun connectionClosed() {
        synchronized(lock) {
            if (myTimeShower != null) {
                myTimeShower!!.interrupt()
            }
            this.timedOutConnection()
            gattObject = null
        }
    }

    fun setConnected() {
        synchronized(lock) {
            currentConnected = true
            setInWork(false)
        }
    }

    override fun run() {
        while (enabled) {
            var getNewJob = true
            synchronized(lock) {
                if (currentScanResult != null) {
                    getNewJob = false
                }
            }
            if (!getNewJob) {
                synchronized(waitObj) {
                    try {
                        waitObj.wait(1000)
                    } catch (e: InterruptedException) {

                    }
                }
                continue
            }

            val scanResult = getResult()
            if (scanResult == null) {
                synchronized(waitObj) {
                    try {
                        waitObj.wait(1000)
                    } catch (e: InterruptedException) {

                    }
                }
            } else {
                cb = BleScanCallbackConnectionCB(scanResult, getBLEServiceReferenceInterface(), this)
                val device = getBLEServiceReferenceInterface().getBLEServiceReference().getBluetoothAdapter()!!.getRemoteDevice(scanResult.device.address)
                this.currentScanResult = scanResult
                gattObject = device.connectGatt(getBLEServiceReferenceInterface() as Context, false, cb)

                setInWork(true)
            }
        }
        Log.d(BLUETOOTHGATT, " $TAG --> BleConnector Quit  ")
    }

    fun bleConnectorThreadStop() {
        enabled = false
    }
}
