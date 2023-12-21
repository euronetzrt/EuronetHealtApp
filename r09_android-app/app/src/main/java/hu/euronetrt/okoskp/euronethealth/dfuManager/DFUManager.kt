package hu.euronetrt.okoskp.euronethealth.dfuManager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.util.Base64
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.GlobalRes.dfuRefreshTryAgain
import hu.euronetrt.okoskp.euronethealth.bluetooth.BluetoothServiceNotificationType
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_DEVICEIMAGE
import hu.euronetrt.okoskp.euronethealth.dfuManager.dfu.OwnDfuService
import hu.euronetrt.okoskp.euronethealth.dfuManager.model.FirmwareDeviceTypeResultModel
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.meteor.EuronetMeteorSingleton
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.MultipleDeviceInfo
import im.delight.android.ddp.MeteorCallback
import im.delight.android.ddp.ResultListener
import im.delight.android.ddp.db.memory.InMemoryDatabase
import no.nordicsemi.android.dfu.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class DFUManager(private val mContext: Context, private val bluetoothLeService: BluetoothLeService) : Thread(), MeteorCallback {

    companion object {
        private val TAG = "DFUManager"
    }

    private lateinit var dataMultipleList: MultipleDeviceInfo
    private var lock: Object = Object()
    private lateinit var controller: DfuController
    private lateinit var devicetypesModel: FirmwareDeviceTypeResultModel
    private lateinit var meteor: EuronetMeteorSingleton
    private val WAITING_INTERVAL: Long = 15000

    override fun run() {
        Log.d(TAG, "run thread")
        doWork()
    }

    private fun doWork() {

        Log.d(TAG, "doWork DFU running")
        if (GlobalRes.connectedDevice) {
            Log.d(TAG, "doWork DFU connected device")

            try {
                if (EuronetMeteorSingleton.hasInstance()) {
                    Log.d(TAG, "EuronetMeteorSingleton.getInstance()")
                    meteor = EuronetMeteorSingleton.getInstance()
                } else {
                    Log.d(TAG, "EuronetMeteorSingleton.getInstance()   else")
                    meteor = EuronetMeteorSingleton.createInstance(mContext, "ws://${AccountGeneral.mHost}:3000/websocket", InMemoryDatabase())
                    meteor.addCallback(this)
                }
                var tryCounter = 0
                while(tryCounter <=3) {
                    if (!meteor.isConnected) {
                        meteor.connect()
                        synchronized(lock) {
                            Log.d(TAG, "Lock dfu meteor miatt")
                            lock.wait(WAITING_INTERVAL)
                        }
                    }
                    tryCounter++
                }

                if (!meteor.isConnected) {
                    // nem tudtunk connectálni újra próbáljuk majd, csak ebben az egy esetben minden egyéb hiba esetén nem indítjuk újra.
                    dfuRefreshTryAgain = true
                    Log.d(TAG, "thread close")
                    threadStop()
                } else {
                    Log.d(TAG, " meteor connected!!")
                }

                val methodGet = "devices.get"
                val paramsGet = Array(1) { "" }
                val devId = PreferenceManager.getDefaultSharedPreferences(mContext).getString(BluetoothLeService.KEY_DEVICE_ID, null)!!

                Log.d(TAG, "devId   --> $devId")

                paramsGet[0] = devId

                var resultOK: Boolean? = null

                meteor.call(methodGet, paramsGet, object : ResultListener {
                    override fun onSuccess(result: String?) {
                        Log.d(TAG, "$TAG  --> onSuccess result devices.get $result")
                        resultOK = true
                        try {
                            val notValidResult = "[]"
                            if (!result.isNullOrEmpty() && result != notValidResult) {

                                val REVIEW_TYPE = object : TypeToken<MultipleDeviceInfo>() {}.type
                                val gson = Gson()
                                dataMultipleList = gson.fromJson(result, REVIEW_TYPE)

                                getFirmware(dataMultipleList)
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "$TAG  --> ${e.message}")
                            resultOK = false
                        }
                    }

                    override fun onError(error: String?, reason: String?, details: String?) {
                        Log.d(TAG, "$TAG  --> error $error ")
                        Log.d(TAG, "$TAG  --> error $reason ")
                        Log.d(TAG, "$TAG  --> error $details ")
                        resultOK = false
                    }
                })

                val handler = Handler()
                handler.postDelayed({
                    if (resultOK == null) {
                        Log.d(TAG, "időkeret lejárt a deviceget hívása során")
                        threadStop()
                    } else if (!resultOK!!) {
                        Log.d(TAG, "hiba volt a deviceget hívása során")
                        threadStop()
                    }
                }, WAITING_INTERVAL)

            } catch (e: Exception) {
                Log.d(TAG, "result: ${e.message}")
                threadStop()
            }
        } else {
            Log.d(TAG, "result: Device not connected!")
            threadStop()
        }
    }

    /**
     * get Firmware info
     *
     * @param dataMultipleList
     */
    private fun getFirmware(dataMultipleList: MultipleDeviceInfo) {
        val paramsGet = Array(1) { "" }

        paramsGet[0] = dataMultipleList.devicetype_id

        var resultOK: Boolean? = null

        Log.d(TAG, "call devicetypes.get ${paramsGet[0]}")

        if (EuronetMeteorSingleton.hasInstance()) {
            Log.d(TAG, "EuronetMeteorSingleton.getInstance()")
            meteor = EuronetMeteorSingleton.getInstance()
        } else {
            Log.d(TAG, "EuronetMeteorSingleton.getInstance()   else")
            meteor = EuronetMeteorSingleton.createInstance(mContext, "ws://${AccountGeneral.mHost}:3000/websocket", InMemoryDatabase())
            meteor.addCallback(this)
        }

        if (!meteor.isConnected) {
            meteor.connect()
            synchronized(lock) {
                Log.d(TAG, "Lock dfu meteor miatt")
                lock.wait(WAITING_INTERVAL)
            }
        }

        if (!meteor.isConnected) {
            // nem tudtunk connectálni újra próbáljuk majd, csak ebben az egy esetben minden egyéb hiba esetén nem indítjuk újra.
            dfuRefreshTryAgain = true
            Log.d(TAG, "thread close")
            threadStop()
        } else {
            Log.d(TAG, " meteor connected!!")
        }

        meteor.call("devicetypes.get", paramsGet, object : ResultListener {
            override fun onSuccess(result: String?) {
               Log.d(TAG, "result devicetypes write")
                try {

                    val REVIEW_TYPE = object : TypeToken<FirmwareDeviceTypeResultModel>() {}.type
                    val gson = Gson()

                    devicetypesModel = gson.fromJson(result, REVIEW_TYPE)

                    Log.e(TAG, "photo: ${devicetypesModel.firmwareArray[devicetypesModel.firmwareArray.lastIndex]}")

                    val DEVICEIMAGE = PreferenceManager.getDefaultSharedPreferences(mContext.applicationContext)
                    DEVICEIMAGE.edit()
                            .putString(KEY_DEVICEIMAGE, devicetypesModel.versions[devicetypesModel.versions.lastIndex].imageContent)
                            .apply()


                    val currentFirmwareVersion = bluetoothLeService.getDeviceInformation()

                    val currentFirmwareData = devicetypesModel.firmwareArray[devicetypesModel.firmwareArray.lastIndex]
                    Log.d(TAG, "firmware check  ---> firmwareModell.version:  ${devicetypesModel.firmwareArray[devicetypesModel.firmwareArray.lastIndex].version}  currentFirmwareVersion[FIRMWARE]-- > ${currentFirmwareVersion["FIRMWARE"]}  ")

                    if (currentFirmwareData.version > currentFirmwareVersion["FIRMWARE"]!! && !currentFirmwareData.content.isEmpty()) {
                        resultOK = true
                        /**
                         * ASk User
                         * */
                        (mContext as DeviceMainActivity).dfuStart(this@DFUManager)

                    } else {
                        Log.d(TAG, "Nincs elérhető frissítés")
                        resultOK = true
                        threadStop()
                    }
                } catch (e: java.lang.Exception) {
                    Log.d(TAG, " error devicetype : ${e.message}")
                    resultOK = false
                }
            }

            override fun onError(error: String?, reason: String?, details: String?) {
                Log.d(TAG, "error meteor.call -> $error  -> $reason  --> $details")
                Log.d(TAG, "$TAG  --> wasError  2 ")
                resultOK = false
            }
        })

        val handler = Handler()
        handler.postDelayed({
            if (resultOK == null) {
                Log.d(TAG, "időkeret lejárt a devicetypes.get hívása során")
                threadStop()
            } else if (!resultOK!!) {
                Log.d(TAG, "hiba volt a devicetypes.get  hívása során vagy a user nem akar frissíteni.")
                threadStop()
            }
        }, WAITING_INTERVAL)
    }

    private fun runDFU() {

        val device = bluetoothLeService.getGatt().device
        val currentFirmwareData = devicetypesModel.firmwareArray[devicetypesModel.firmwareArray.lastIndex]

        try {
            val dfuByteArray = Base64.decode(currentFirmwareData.content, Base64.DEFAULT)
            // Log.d(TAG, "dfuString : ${firmwareModell.content}")

            var out: BufferedOutputStream? = null
            try {
                out = BufferedOutputStream(FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/Euronet/EuronetRT_DFU.zip"), 4096)
                out.write(dfuByteArray)
            } finally {
                out?.close()
            }

        } catch (e: IOException) {
            Log.d(TAG, "error dfu zip create-> ${e.message}")
            deleteZipFile()
            threadStop()
        }

        val file = File(Environment.getExternalStorageDirectory().toString() + "/Euronet/EuronetRT_DFU.zip")
        while (!file.exists()){
            Log.d(TAG, "zip create is working ")
            synchronized(lock){
                lock.wait(200)
            }
        }
        Log.d(TAG, "zip create is finish ")
        synchronized(lock){
            lock.notify()
        }

        val starter = DfuServiceInitiator(device.address).setDeviceName(device.name)

        Log.d(TAG, "device.address : ${device.address}  device.name: ${device.name} ")

        // If you want to have experimental buttonless DFU feature supported call additionally:
        starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)


        val uri = Uri.fromFile(file)
        starter.setZip(uri, file.absolutePath)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DfuServiceInitiator.createDfuNotificationChannel(bluetoothLeService)
        }

        controller = starter.start(bluetoothLeService, OwnDfuService::class.java)
    }

    private val mDfuProgressListener: DfuProgressListener = object : DfuProgressListenerAdapter() {

        override fun onDfuProcessStarted(deviceAddress: String) {
            super.onDfuProcessStarted(deviceAddress)
            Log.d(TAG, "DFU process started")
        }

        override fun onProgressChanged(deviceAddress: String, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
            super.onProgressChanged(deviceAddress, percent, speed, avgSpeed, currentPart, partsTotal)
            Log.d(TAG, "DFU process changed --> deviceAddress: $deviceAddress, percent: $percent, speed: $speed, avgSpeed: $avgSpeed, currentPart: $currentPart, partsTotal: $partsTotal")
            val intent = Intent()
            intent.putExtra("NEW_PERCENT",percent)
            intent.action = GlobalRes.DFU_UPDATE_PROGRESS_REFRESH
            mContext.sendBroadcast(intent)
        }

        override fun onDeviceConnected(deviceAddress: String) {
            super.onDeviceConnected(deviceAddress)
            Log.d(TAG, "DFU connecting")
        }

        override fun onDeviceConnecting(deviceAddress: String) {
            super.onDeviceConnecting(deviceAddress)
            Log.d(TAG, "DFU connecting")
        }

        override fun onEnablingDfuMode(deviceAddress: String) {
            super.onEnablingDfuMode(deviceAddress)
            Log.d(TAG, "DFU mode enable")
        }

        override fun onDfuCompleted(deviceAddress: String) {
            super.onDfuCompleted(deviceAddress)
            Log.d(TAG, "DFU completed")
            val intent = Intent()
            intent.action = GlobalRes.DFU_UPDATE_SUCCESSFULLY
            mContext.sendBroadcast(intent)

            (mContext as DeviceMainActivity).dfuUpdateSuccessfully()

            reWriteServer()
            deleteZipFile()
        }

        override fun onDeviceDisconnected(deviceAddress: String) {
            super.onDeviceDisconnected(deviceAddress)
            Log.d(TAG, "DFU device disconnected")
        }

        override fun onError(deviceAddress: String, error: Int, errorType: Int, message: String?) {
            super.onError(deviceAddress, error, errorType, message)
            Log.d(TAG, "DFU device dfuerror $message")
            val intent = Intent()
            intent.action = GlobalRes.DFU_UPDATE_ERROR
            mContext.sendBroadcast(intent)
            deleteZipFile()
        }
    }

    private fun deleteZipFile() {
        val file = File(Environment.getExternalStorageDirectory().toString() + "/Euronet/EuronetRT_DFU.zip")
        if (file.exists()) {
            file.delete()
        }
        threadStop()
    }

    private fun reWriteServer() {
        val paramsGet = Array(7) { "" }
        val userId = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext.applicationContext).getString(BluetoothLeService.KEY_USERID, null)
        dataMultipleList.current_user_id = userId

        paramsGet[0] = dataMultipleList.current_user_id!!
        paramsGet[1] = dataMultipleList.id
        paramsGet[2] = dataMultipleList.seq.toString()
        paramsGet[3] = dataMultipleList.serialnumber
        paramsGet[4] = dataMultipleList.hw_mac_id
        paramsGet[5] = dataMultipleList.current_firmware_version
        paramsGet[6] = devicetypesModel.firmwareArray[devicetypesModel.firmwareArray.lastIndex].version

        Log.d(TAG, "devices.upgradeFirmware params -->  ${paramsGet[0]}  ${paramsGet[1]}  ${paramsGet[2]}  ${paramsGet[3]}  ${paramsGet[4]}  ${paramsGet[5]}  ${paramsGet[6]} ")

        var resultOK: Boolean? = null

        meteor.call("devices.upgradeFirmware", paramsGet, object : ResultListener {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "result server rewrite succes result -- > $result")
                dfuRefreshTryAgain = false
                resultOK = true
            }

            override fun onError(error: String?, reason: String?, details: String?) {
                Log.d(TAG, "error meteor.call -> $error  -> $reason  --> $details")
                Log.d(TAG, "$TAG  --> wasError  2 ")
                resultOK = false
            }
        })

        val handler = Handler()
        handler.postDelayed({
            if (resultOK == null) {
                Log.d(TAG, "időkeret lejárt a firmware szerver visszaírásra")
                threadStop()
            } else if (!resultOK!!) {
                Log.d(TAG, "firmware szerver visszaírás során hiva volt")
                threadStop()
            }else{
                /*true- > sikeres volt a visszaírás kész vagyunk
               */
                Log.d(TAG, "firmware update OK, server rewrite ok!, DONE!")
                threadStop()
            }
        }, WAITING_INTERVAL)
    }


    /**
     * Meteor create instance callback
     */
    override fun onConnect(signedInAutomatically: Boolean) {
        if (signedInAutomatically) {
            // bejelentkeztünk az adatainkkal.
            Log.d(TAG, "DFUManager signedInAutomatically -----> true")
            synchronized(lock) {
                lock.notify()
            }
        } else {
            Log.d(TAG, "DFUManager signedInAutomatically -----> false")
        }
    }

    override fun onDataAdded(collectionName: String?, documentID: String?, newValuesJson: String?) {
        val mCollectionName = "DFUManager onDataAdded $collectionName"
        val mDocumentID = "DFUManager onDataAdded $documentID"
        val mNewValuesJson = "DFUManager onDataAdded$newValuesJson"

        Log.d(TAG, mCollectionName)
        Log.d(TAG, mDocumentID)
        Log.d(TAG, mNewValuesJson)
    }

    override fun onDataRemoved(collectionName: String?, documentID: String?) {
        val mCollectionName = "DFUManager onDataRemoved $collectionName"
        val mDocumentID = "DFUManager onDataRemoved $documentID"

        Log.d(TAG, mCollectionName)
        Log.d(TAG, mDocumentID)
    }

    override fun onException(e: Exception?) {
        val error = "DFUManager onError error:" + e!!.message
        Log.d(TAG, error)
        threadStop()
    }

    override fun onDisconnect() {
        val ondisconnect = "DFUManager onDisconnect"
        Log.d(TAG, ondisconnect)
    }

    override fun onDataChanged(collectionName: String?, documentID: String?, updatedValuesJson: String?, removedValuesJson: String?) {
        val mCollectionName = "DFUManager onDataChanged $collectionName"
        val mDocumentID = "DFUManager onDataChanged $documentID"
        val mNewValuesJson = "DFUManager onDataChanged $updatedValuesJson"
        val mRemovedValuesJson = "DFUManager onDataChanged $removedValuesJson"

        Log.d(TAG, mCollectionName)
        Log.d(TAG, mDocumentID)
        Log.d(TAG, mNewValuesJson)
        Log.d(TAG, mRemovedValuesJson)
    }

    /**
     * Thread stop*/
    private fun threadStop() {
        (mContext as DeviceMainActivity).dfuThreadStop()
    }

    /**
     * User response --> would like update
     */
    fun userResponse() {
        try {
            bluetoothLeService.serviceNotificationsSet(BluetoothServiceNotificationType.BATTERY_SERVICE.type, false)
            bluetoothLeService.serviceNotificationsSet(BluetoothServiceNotificationType.HEARTRATE_SERVICE.type, false)
            bluetoothLeService.serviceNotificationsSet(BluetoothServiceNotificationType.NORDIC_SERVICE.type, false)
            bluetoothLeService.serviceNotificationsSet(BluetoothServiceNotificationType.DEVICEINFO_SERVICE.type, false)
            DfuServiceListenerHelper.registerProgressListener(mContext, mDfuProgressListener)
            runDFU()
        } catch (e: Exception) {
            Log.d(TAG, "$TAG  --> wasError  1   ${e.message}")
        }
    }
}