package hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks

import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hu.euronetrt.okoskp.euronethealth.GlobalRes.BLUETOOTHGATT
import hu.euronetrt.okoskp.euronethealth.GlobalRes.DEVICEINFORMATION_SERVICE_UUID
import hu.euronetrt.okoskp.euronethealth.GlobalRes.FIRMWARE_REVISION_STRING
import hu.euronetrt.okoskp.euronethealth.GlobalRes.HARDWARE_REVISION_STRING
import hu.euronetrt.okoskp.euronethealth.GlobalRes.MANUFACTURER_NAME_STRING
import hu.euronetrt.okoskp.euronethealth.GlobalRes.MODEL_NUMBER_STRING
import hu.euronetrt.okoskp.euronethealth.GlobalRes.SERIAL_NUMBER_STRING
import hu.euronetrt.okoskp.euronethealth.GlobalRes.dataMultipleList
import hu.euronetrt.okoskp.euronethealth.bluetooth.bleInterfaces.BLEServiceReferenceInterface
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.BleConnector
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.BleScanCallback.adapter
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.BleScanCallback.arrayOfFoundBTDevices
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.BleScanCallback.contextIsDeviceMainAct
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.BleScanCallback.recycleDeviceList
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.adapter.DeviceAdapter
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.dataClass.DeviceData
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.MultipleDeviceInfo
import im.delight.android.ddp.ResultListener

/**
 * BleScanCallbackConnectionCB class
 *
 * @property result
 * @property bLEServiceReferenceInterface
 * @property connector
 */
class BleScanCallbackConnectionCB(val result: ScanResult, private val bLEServiceReferenceInterface: BLEServiceReferenceInterface, private val connector: BleConnector?) : BluetoothGattCallback() {

    companion object {
        private val TAG = "BleScanCallbackConnectionCB"
    }
   // private lateinit var myGatt : BluetoothGatt
    private var deviceInfoService: BluetoothGattService? = null
    private var deviceInfoChar_Manufacturer_name: BluetoothGattCharacteristic? = null
    private var deviceInfoChar_Model_number: BluetoothGattCharacteristic? = null
    private var deviceInfoChar_Serial_number: BluetoothGattCharacteristic? = null
    private var deviceInfoChar_Hardware_revision: BluetoothGattCharacteristic? = null
    private var deviceInfoChar_Firmware_revision: BluetoothGattCharacteristic? = null
    private var manName: String? = null
    private var seriNum: String? = null
    private var modelNum: String? = null
    private var hardwRev: String? = null
    private var firmRev: String? = null

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {

        //super.onConnectionStateChange(gatt, status, newState)

        Log.e(BLUETOOTHGATT, "$TAG  --> ${result.device.address} onConnectionStateChange run")

        if (status == BluetoothGatt.GATT_SUCCESS) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTING -> {
                    Log.e(BLUETOOTHGATT, "$TAG  --> STATE_CONNECTING  ${result.device.address}")
                }
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.e(BLUETOOTHGATT, "$TAG  --> STATE_CONNECTED run ${result.device.address}")
                    connector!!.setConnected()
                    Log.e(BLUETOOTHGATT, "$TAG  --> STATE_CONNECTED connector ${result.device.address}")
                    gatt!!.discoverServices()
                    connector.setInWork(true)
                    Log.e(BLUETOOTHGATT, "$TAG  --> STATE_CONNECTED discover ${result.device.address}")
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    Log.e(BLUETOOTHGATT, "$TAG  --> STATE_DISCONNECTING run ${result.device.address}")
                    Log.e(BLUETOOTHGATT, "$TAG  --> STATE_DISCONNECTED AFTER CONNECTOR RESET ${result.device.address}")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.e(BLUETOOTHGATT, "$TAG  --> STATE_DISCONNECTED run ${result.device.address}")
                    connector!!.connectionClosed()
                    Log.e(BLUETOOTHGATT, "$TAG  --> STATE_DISCONNECTED AFTER CONNECTOR RESET ${result.device.address}")
                    gatt!!.close()
                    return
                }
            }
        } else {
            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.e(BLUETOOTHGATT, "$TAG  --> Statusz: $status ,///onConnectionStateChange BluetoothGatt.GATT_FAILURE   ${result.device.address}")
            } else {
                Log.e(BLUETOOTHGATT, "$TAG  --> Statusz: $status ,///onConnectionStateChange Gatt nem succes!!   ${result.device.address}")
            }

            gatt!!.close()
            return
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        Log.e(BLUETOOTHGATT, "$TAG  --> onServicesDiscovered run ${result.device.address}")
        connector!!.setInWork(false)

        /*GATT_INTERNAL_ERROR */
        if (status == 129) {
            Log.e(BLUETOOTHGATT, "$TAG  --> Service discovery failed GATT_INTERNAL_ERROR ${result.device.address}")
            gatt.disconnect()
            return
        }

        if (status == BluetoothGatt.GATT_SUCCESS) {

            deviceInfoService = gatt.getService(DEVICEINFORMATION_SERVICE_UUID)

            deviceInfoChar_Manufacturer_name = deviceInfoService?.getCharacteristic(MANUFACTURER_NAME_STRING)
            deviceInfoChar_Model_number = deviceInfoService?.getCharacteristic(MODEL_NUMBER_STRING)
            deviceInfoChar_Serial_number = deviceInfoService?.getCharacteristic(SERIAL_NUMBER_STRING)
            deviceInfoChar_Hardware_revision = deviceInfoService?.getCharacteristic(HARDWARE_REVISION_STRING)
            deviceInfoChar_Firmware_revision = deviceInfoService?.getCharacteristic(FIRMWARE_REVISION_STRING)

            manName = ""
            modelNum = ""
            seriNum = ""
            hardwRev = ""
            firmRev = ""

            if (deviceInfoService == null) {
                Log.e(BLUETOOTHGATT, "$TAG  --> Device information service not found! call disconnect() ${result.device.address}")
                gatt.disconnect()
                return
            }
            if (deviceInfoChar_Manufacturer_name == null) {
                Log.e(BLUETOOTHGATT, "$TAG  --> Manufacturer name not found! call disconnect() ${result.device.address}")
                gatt.disconnect()
                return
            }
            if (deviceInfoChar_Model_number == null) {
                Log.e(BLUETOOTHGATT, "$TAG  --> model name not found! ${result.device.address}")
                gatt.disconnect()
                return
            }
            if (deviceInfoChar_Serial_number == null) {
                Log.e(BLUETOOTHGATT, "$TAG  --> Serial_number not found! ${result.device.address}")
                gatt.disconnect()
                return
            }
            if (deviceInfoChar_Hardware_revision == null) {
                Log.e(BLUETOOTHGATT, "$TAG  --> Hardware_revision not found! ${result.device.address}")
                gatt.disconnect()
                return
            }
            if (deviceInfoChar_Firmware_revision == null) {

                Log.e(BLUETOOTHGATT, "$TAG  --> Firmware_revision not found! ${result.device.address}")
                gatt.disconnect()
                return
            }

            connector.setInWork(true)
            gatt.readCharacteristic(deviceInfoChar_Manufacturer_name)
        } else {
            Log.e(BLUETOOTHGATT, "$TAG  --> onServicesDiscovered error! status: $status - addr: ${result.device.address}")
            gatt.disconnect()
            return
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt,
                                      characteristic: BluetoothGattCharacteristic,
                                      status: Int) {

        connector!!.setInWork(false)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.e(BLUETOOTHGATT, "$TAG  --> onCharacteristicRead ${result.device.address}")

            when (characteristic.uuid) {
                MANUFACTURER_NAME_STRING -> {
                    manName = characteristic.getStringValue(0)
                    // Log.e(BLUETOOTHGATT, "$TAG  --> MANUFACTURER_NAME_STRING ${characteristic.getStringValue(0)} ------  ${result.device.address}}")
                    gatt.readCharacteristic(deviceInfoChar_Model_number!!)
                    connector.setInWork(true)

                }
                SERIAL_NUMBER_STRING -> {
                    seriNum = characteristic.getStringValue(0)
                    //Log.e(BLUETOOTHGATT, "$TAG  --> SERIAL_NUMBER_STRING ${characteristic.getStringValue(0)} ------  ${result.device.address}}")
                    gatt.readCharacteristic(deviceInfoChar_Hardware_revision!!)
                    connector.setInWork(true)

                }
                MODEL_NUMBER_STRING -> {
                    modelNum = characteristic.getStringValue(0)
                    //Log.e(BLUETOOTHGATT, "$TAG  --> MODEL_NUMBER_STRING ${characteristic.getStringValue(0)} ------  ${result.device.address}}")
                    val devices = bLEServiceReferenceInterface.getBLEServiceReference().getAvailableDevice()
                    var ok = false
                    devices.addressJson.forEach {
                         Log.e(BLUETOOTHGATT, "$TAG  -->  ${it.manufacturerName}  $manName     ${it.modelName}  $modelNum")
                        if (it.manufacturerName == manName && it.modelName == modelNum) {
                            ok = true
                        }
                    }
                    if (ok) {
                        //ha true akkor a scan eszköz szerepel a servertől kapott listában
                        gatt.readCharacteristic(deviceInfoChar_Serial_number!!)
                        connector.setInWork(true)
                    } else {
                        Log.e(BLUETOOTHGATT, "$TAG  --> Nem található az eszköz a szerver listájában!  ------  ${result.device.address}}")
                        gatt.disconnect()
                        return
                    }
                }
                HARDWARE_REVISION_STRING -> {
                    hardwRev = characteristic.getStringValue(0)
                    //   Log.e(BLUETOOTHGATT, "$TAG  --> HARDWARE_REVISION_STRING ${characteristic.getStringValue(0)}")
                    gatt.readCharacteristic(deviceInfoChar_Firmware_revision!!)
                    connector.setInWork(true)
                }
                FIRMWARE_REVISION_STRING -> {
                    firmRev = characteristic.getStringValue(0)
                    //   Log.e(BLUETOOTHGATT, "$TAG  --> FIRMWARE_REVISION_STRING ${characteristic.getStringValue(0)}")
                    /*ő az utolsó lekért adat a soros kérések közt ,mehet a server ellenőrzés*/
                    val devName = gatt.device.name
                    val devType = gatt.device.type

                    getDevicesListByMultiple(gatt, manName, modelNum, seriNum, hardwRev, gatt.device.address, devName, devType)
                }
            }
        } else {
            Log.e(BLUETOOTHGATT, "$TAG  --> Characteristic read error: status + $status  ------  ${result.device.address}}")
            gatt.disconnect()
            gatt.close()
            return
        }
    }

    /**
     * getDevicesListByMultiple
     *
     * @param gatt
     * @param manName
     * @param modelNum
     * @param seriNum
     * @param hardwRev
     * @param macID
     * @param deviceName
     * @param deviceType
     */
    private fun getDevicesListByMultiple(gatt: BluetoothGatt, manName: String?, modelNum: String?, seriNum: String?, hardwRev: String?, macID: String?, deviceName: String?, deviceType: Int?) {

        val meteor = bLEServiceReferenceInterface.getBLEServiceReference().getMeteor()
        //  Log.e(BLUETOOTHGATT, "$TAG  --> getDevicesListByMultiple run ${meteor.isConnected}")

        val method = "devices.listByMultiple"
        val params = Array(5) { "" }
        params[0] = manName ?: ""
        params[1] = modelNum ?: ""
        params[2] = seriNum ?: ""
        params[3] = macID ?: ""
        params[4] = hardwRev ?: ""

        Log.e(BLUETOOTHGATT, "$TAG  --> params $manName $modelNum $seriNum $macID $hardwRev")

        meteor.call(method, params, object : ResultListener {
            override fun onSuccess(result: String?) {
                try {
                    Log.e(BLUETOOTHGATT, "$TAG  --> onSuccess result $result $macID")
                    val notValidResult = "[]"
                    if (!result.isNullOrEmpty() && result != notValidResult) {
                        val REVIEW_TYPE = object : TypeToken<Array<MultipleDeviceInfo>>() {}.type
                        val gson = Gson()
                        dataMultipleList = gson.fromJson(result, REVIEW_TYPE)

                        val items = DeviceData()

                        items.bluetooth_name = deviceName!!
                        items.bluetooth_manufacturer =  manName!!
                        items.bluetooth_type = deviceType!!
                        items.bluetooth_address = dataMultipleList[0].hw_mac_id
                        items.bluetooth_device_Object = dataMultipleList
                        deviceToUI(items,gatt)

                    }else{
                        gatt.disconnect()
                    }

                } catch (e: Exception) {
                    Log.e(BLUETOOTHGATT, "$TAG  --> ${e.message} $macID")
                    gatt.disconnect()
                }
            }

            override fun onError(error: String?, reason: String?, details: String?) {
                Log.e(BLUETOOTHGATT, "$TAG  --> $error $macID")
                Log.e(BLUETOOTHGATT, "$TAG  --> $reason $macID")
                Log.e(BLUETOOTHGATT, "$TAG  --> $details $macID")
                gatt.disconnect()
            }
        })
    }

    private fun deviceToUI(items: DeviceData, gatt: BluetoothGatt) {
        Log.e(BLUETOOTHGATT, "$TAG  --> deviceToUI ${items.bluetooth_address} ")
        Log.e(BLUETOOTHGATT, "$TAG  --> ADATOK ${items.bluetooth_device_Object!![0].id} " +
                "${items.bluetooth_device_Object!![0].seq} "+
                "${items.bluetooth_device_Object!![0].serialnumber} "+
                items.bluetooth_device_Object!![0].hw_mac_id)

        if (!arrayOfFoundBTDevices.contains(items)) {
            arrayOfFoundBTDevices.add(items)

            adapter = DeviceAdapter(contextIsDeviceMainAct, arrayOfFoundBTDevices)
            recycleDeviceList.adapter = adapter

            adapter.notifyDataSetChanged()
        }
        gatt.disconnect()
    }

   /* fun setGatt(mGatt: BluetoothGatt){
        myGatt = mGatt
    }*/
}
