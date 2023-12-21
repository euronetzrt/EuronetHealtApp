package hu.euronetrt.okoskp.euronethealth.bluetooth.services

import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.*
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import hu.aut.android.dm01_v11.BuildConfig
import hu.aut.android.dm01_v11.R
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.GlobalRes.ACTION_BATTERY_DATA_AVAILABLE
import hu.euronetrt.okoskp.euronethealth.GlobalRes.ACTION_DEVICEINFO_DATA_AVAILABLE
import hu.euronetrt.okoskp.euronethealth.GlobalRes.ACTION_HEART_RATE_DATA_AVAILABLE
import hu.euronetrt.okoskp.euronethealth.GlobalRes.ACTION_NORDIC_DATA_AVAILABLE
import hu.euronetrt.okoskp.euronethealth.GlobalRes.ACTION_SCREEN_OFF
import hu.euronetrt.okoskp.euronethealth.GlobalRes.ACTION_SCREEN_ON
import hu.euronetrt.okoskp.euronethealth.GlobalRes.ACTION_TRIANGLESIGNAL_DATA_AVAILABLE
import hu.euronetrt.okoskp.euronethealth.GlobalRes.ACTION_USER_PRESENT
import hu.euronetrt.okoskp.euronethealth.GlobalRes.BATTERY_LEVEL_CHAR_UUID
import hu.euronetrt.okoskp.euronethealth.GlobalRes.BATTERY_SERVICE_UUID
import hu.euronetrt.okoskp.euronethealth.GlobalRes.DEVICEINFORMATION_SERVICE_UUID
import hu.euronetrt.okoskp.euronethealth.GlobalRes.DEVICENAME
import hu.euronetrt.okoskp.euronethealth.GlobalRes.EXTRA_DATA
import hu.euronetrt.okoskp.euronethealth.GlobalRes.FIRMWARE_REVISION_STRING
import hu.euronetrt.okoskp.euronethealth.GlobalRes.GENERIC_ACCESS
import hu.euronetrt.okoskp.euronethealth.GlobalRes.HARDWARE_REVISION_STRING
import hu.euronetrt.okoskp.euronethealth.GlobalRes.HEART_RATE_SERVICE_UUID
import hu.euronetrt.okoskp.euronethealth.GlobalRes.MANUFACTURER_NAME_STRING
import hu.euronetrt.okoskp.euronethealth.GlobalRes.MODEL_NUMBER_STRING
import hu.euronetrt.okoskp.euronethealth.GlobalRes.NORDIC_UART_SERVICE_UUID
import hu.euronetrt.okoskp.euronethealth.GlobalRes.RX_CHAR_UUID
import hu.euronetrt.okoskp.euronethealth.GlobalRes.SERIAL_NUMBER_STRING
import hu.euronetrt.okoskp.euronethealth.GlobalRes.SERVER_NOT_CONN
import hu.euronetrt.okoskp.euronethealth.GlobalRes.TX_CHAR_UUID
import hu.euronetrt.okoskp.euronethealth.GlobalRes.UUID_CLIENT_CHARACTERISTIC_CONFIG
import hu.euronetrt.okoskp.euronethealth.GlobalRes.applicationVersionModeIsLight
import hu.euronetrt.okoskp.euronethealth.GlobalRes.connectedDevice
import hu.euronetrt.okoskp.euronethealth.GlobalRes.enabled
import hu.euronetrt.okoskp.euronethealth.GlobalRes.searchingDeviceList
import hu.euronetrt.okoskp.euronethealth.TriangleSignal.TriangleSignalObject
import hu.euronetrt.okoskp.euronethealth.asyncTasks.DataFileWrite
import hu.euronetrt.okoskp.euronethealth.bluetooth.BindServiceClass
import hu.euronetrt.okoskp.euronethealth.bluetooth.BluetoothServiceNotificationType
import hu.euronetrt.okoskp.euronethealth.bluetooth.bleInterfaces.BLEServerFunctionsInterfaces
import hu.euronetrt.okoskp.euronethealth.bluetooth.bleInterfaces.BLEServiceReferenceInterface
import hu.euronetrt.okoskp.euronethealth.bluetooth.callbacks.MeteorCallbackInBleService
import hu.euronetrt.okoskp.euronethealth.bluetooth.objects.BleServiceAndCharacteristicNameTranslate.convertFromInteger
import hu.euronetrt.okoskp.euronethealth.bluetooth.objects.NordicObject.datasToCSVIBI
import hu.euronetrt.okoskp.euronethealth.bluetooth.objects.NordicObject.datasToCSVIMU
import hu.euronetrt.okoskp.euronethealth.bluetooth.objects.NordicObject.datasToCSVPPG
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.BleConnector
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.BleScanCallback
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.LightBleScanCallback
import hu.euronetrt.okoskp.euronethealth.broadcastrecievers.ScreenUnlockBroadcastReciever
import hu.euronetrt.okoskp.euronethealth.data.CollectableType
import hu.euronetrt.okoskp.euronethealth.data.LocalStorage
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataBroadcaster
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.HRData
import hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.dbThreads.*
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral
import hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects.QuestionnaireResultListFillable
import hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects.QuestionnaireGetModel
import hu.euronetrt.okoskp.euronethealth.questionnaire.timer.QuestionnaireOneThread
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.meteor.EuronetMeteorSingleton
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.availableDeviceModel.AvailableDevicesJson
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.MultipleDeviceInfo
import im.delight.android.ddp.MeteorCallback
import im.delight.android.ddp.ResultListener
import im.delight.android.ddp.SubscribeListener
import im.delight.android.ddp.UnsubscribeListener
import im.delight.android.ddp.db.memory.InMemoryDatabase
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class BluetoothLeService : Service(), BLEServiceReferenceInterface, BLEServerFunctionsInterfaces, LifecycleOwner {

    override fun getBLEServiceReference(): BluetoothLeService {
        return this@BluetoothLeService
    }

    override fun onTrimMemory(level: Int) {
        Log.d(TAG, " onTrimMemory level : $level")
        super.onTrimMemory(level)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, " onTaskRemoved rootIntent ")
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        val TAG = "BluetoothLeService"
        val TAGMEASUREMENT = "TAGMEASUREMENT"
        val TAGQUESTION = "TAGQUESTION"
        val TAGSCAN = "BLUETOOTH_SCAN"
        val TAGPAIR = "TAGPAIR"
        val TAGSUBS = "TAGSUBS"
        val KEY_DEVICE_ID = "KEY_DEVICE_ID"
        val KEY_HWMAC_ID = "KEY_HWMAC_ID"
        val KEY_FW_VERSION = "KEY_FW_VERSION"
        val KEY_USERID = "KEY_USERID"
        val KEY_MANUFACTURER_NAME_STRING = "KEY_MANUFACTURER_NAME_STRING"
        val KEY_DEVICENAME = "KEY_DEVICENAME"
        val KEY_MODEL_NUMBER_STRING = "KEY_MODEL_NUMBER_STRING"
        val KEY_HARDWARE_VERSION = "KEY_HARDWARE_VERSION"
        val KEY_SERIAL_NUMBER_STRING = "KEY_SERIAL_NUMBER_STRING"
        val KEY_DEVICEIMAGE = "KEY_DEVICEIMAGE"
        val KEY_HRTHREADRUN = "KEY_HRTHREADRUN"
        val KEY_IBITHREADRUN = "KEY_IBITHREADRUN"
        val KEY_IMUTHREADRUN = "KEY_IMUTHREADRUN"
        val KEY_PPGTHREADRUN = "KEY_PPGTHREADRUN"
        val KEY_STEPTHREADRUN = "KEY_STEPTHREADRUN"
        val KEY_NEW_QUESTIONNAIRECOUNTER = "KEY_NEW_QUESTIONNAIRECOUNTER"

        val KEY_USEREMAIL = "KEY_USEREMAIL"
        val KEY_USERNAME = "KEY_USERNAME"
        val KEY_USERSEX = "KEY_USERSEX"
        val KEY_BIRTHDAY = "KEY_BIRTHDAY"
        val KEY_USERIMAGE = "KEY_USERIMAGE"
    }

    private var counter = 0
    private var runtryConnect = false
    private var connectorThread: BleConnector? = null
    private var charaPropTX: Int = -1
    private lateinit var tx: BluetoothGattCharacteristic
    private lateinit var data: AvailableDevicesJson
    private lateinit var thisIsMyDevice: Array<MultipleDeviceInfo>
    private var arriveingData = false
    private var deviceInformation: HashMap<String, String> = HashMap()
    private var deviceBatteryValue: Int = 0
    private var bluetoothPackageCounter = 0
    private var interval: Long = 1000000
    private lateinit var fullScreanActData : Pair<QuestionnaireResultListFillable,QuestionnaireGetModel>

    var HEART_RATE_MEASUREMENT_CHARACTERISTIC = "00002a37-0000-1000-8000-00805f9b34fb"
    val UUID_HEART_RATE_MEASUREMENT_CHARACTERISTIC = UUID.fromString(HEART_RATE_MEASUREMENT_CHARACTERISTIC)


    /*Forground ids*/
    private val NOTIFICATION_CHANNEL_ID = "euronet_service_notifications"
    private val NOTIFICATION_CHANNEL_NAME = "Euronet Service notifications"
    private val ANY_ACTION = "ANY_ACTION"
    private val NOTIF_FOREGROUND_ID = 1

    private val screenOnBroadcastReciever = ScreenUnlockBroadcastReciever()
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var connectionGatt: BluetoothGatt? = null
    private var mNotifyCharacteristicBattery: BluetoothGattCharacteristic? = null
    private var mNotifyCharacteristicHeartRate: BluetoothGattCharacteristic? = null
    private var mNotifyCharacteristicNordic: BluetoothGattCharacteristic? = null
    private lateinit var meteor: EuronetMeteorSingleton
    private lateinit var meteorSubscribe: EuronetMeteorSingleton
    private val method = "devicetypes.availableTypes"
    private var runningWrtieFile = false
    private val cb = BleScanCallback
    private val lightCB = LightBleScanCallback
    private val STARTDEVACTIVITY = "startDevActivity"
    private var rabbitMQRun = false

    private lateinit var ppgThread: PPGDatabaseThread
    private lateinit var hrThread: HRDatabaseThread
    private lateinit var stepThread: STEPDatabaseThread
    private lateinit var ibiThread: IBIDatabaseThread
    private lateinit var imuThread: IMUDatabaseThread
    private var trianglecounter = 0
    private lateinit var questionnaireOneThread: QuestionnaireOneThread

    private lateinit var lifecycleRegistry: LifecycleRegistry

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    inner class BluetoothLeServiceBinder : Binder() {
        internal val service: BluetoothLeService
            get() = this@BluetoothLeService
    }

    private val bluetoothLeServiceBinder = BluetoothLeServiceBinder()

    override fun onBind(intent: Intent): IBinder? {
        return bluetoothLeServiceBinder
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (bluetoothManager == null) {
            bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (bluetoothManager == null) {
                Log.d(TAG, "Unable to initialize BluetoothManager.")
                Log.d("STARTSERVICE", "return false  initialize")
                return false
            }
        }

        bluetoothAdapter = bluetoothManager!!.adapter
        if (bluetoothAdapter == null) {
            Log.d(TAG, "Unable to obtain a BluetoothAdapter.")
            Log.d("STARTSERVICE", "return false  initialize bluetoothManager!!.adapter")
            return false
        }

        return true
    }

    /**
     * onStartCommand
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        BindServiceClass.getInstance(this)
        LocalStorage.getInstance(this)

        initialize()

        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        if (!enabled) {

            startForeground(NOTIF_FOREGROUND_ID,
                    getMyNotification("Run for your health"))
            enabled = true

            BluetoothThread().start()
            Log.d(TAG, "QuestionnaireOneThread start")
            questionnaireOneThread =QuestionnaireOneThread(this@BluetoothLeService)
            questionnaireOneThread.start()


            /*RabbitMQ - DB */
            if (!applicationVersionModeIsLight) {
                Log.d(TAG, "rabbitMq start")

         /*       ppgThreadRun(PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(KEY_PPGTHREADRUN, true))

                hrThreadRun(PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(KEY_HRTHREADRUN, true))

                stepThreadRun(PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(KEY_STEPTHREADRUN, true))

                ibiThreadRun(PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(KEY_IBITHREADRUN, true))

                imuThreadRun(PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(KEY_IMUTHREADRUN, true))
          */
            }

            val intentFilterScreenOn = IntentFilter()
            intentFilterScreenOn.addAction(ACTION_SCREEN_ON)
            intentFilterScreenOn.addAction(ACTION_SCREEN_OFF)
            intentFilterScreenOn.addAction(ACTION_USER_PRESENT)
            registerReceiver(
                    screenOnBroadcastReciever,
                    intentFilterScreenOn
            )
        }
        return START_STICKY
    }

    fun ppgThreadRun(run: Boolean) {
        Log.d(PPGDatabaseThread.TAG, "ppgThreadRun ")
        try {
            if (run) {
                Log.d(PPGDatabaseThread.TAG, "ppgThread new and start ")
                ppgThread = PPGDatabaseThread(this)
                ppgThread.start()
            } else {
                if (::ppgThread.isInitialized) {
                    ppgThread.stopAndInterrupt()
                    Log.d(PPGDatabaseThread.TAG, "interupted ")
                }
            }

        } catch (e: InterruptedException) {
            Log.d(TAG, "Interupted")
        }
    }

    fun hrThreadRun(run: Boolean) {
        try {
            if (run) {
                Log.d(HRDatabaseThread.TAG, "hrThreadRun new and start ")
                hrThread = HRDatabaseThread(this)
                hrThread.start()
            } else {
                if (::hrThread.isInitialized) {
                    hrThread.stopAndInterrupt()
                    Log.d(HRDatabaseThread.TAG, "interupted ")
                }
            }
        } catch (e: InterruptedException) {
            Log.d(TAG, "Interupted")
        }
    }

    fun stepThreadRun(run: Boolean) {
        try {
            if (run) {
                stepThread = STEPDatabaseThread(this)
                stepThread.start()
            } else {
                if (::stepThread.isInitialized) {
                    stepThread.stopAndInterrupt()
                    Log.d(STEPDatabaseThread.TAG, "interupted ")
                }
            }
        } catch (e: InterruptedException) {
            Log.d(TAG, "Interupted")
        }
    }

    fun imuThreadRun(run: Boolean) {
        try {
            if (run) {
                imuThread = IMUDatabaseThread(this)
                imuThread.start()
            } else {
                if (::imuThread.isInitialized) {
                    imuThread.stopAndInterrupt()
                    Log.d(IMUDatabaseThread.TAG, "interupted ")
                }
            }
        } catch (e: InterruptedException) {
            Log.d(TAG, "Interupted")
        }
    }

    fun ibiThreadRun(run: Boolean) {
        try {
            if (run) {
                ibiThread = IBIDatabaseThread(this)
                ibiThread.start()
            } else {
                if (::ibiThread.isInitialized) {
                    ibiThread.stopAndInterrupt()
                    Log.d(IBIDatabaseThread.TAG, "interupted ")
                }
            }
        } catch (e: InterruptedException) {
            Log.d(TAG, "Interupted")
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    private inner class BluetoothThread : Thread() {
        override fun run() {

            if (!getPairedDeviceAddress().isNullOrEmpty() && bluetoothAdapter!!.isEnabled) {
                Log.d("RECONNE", "bles")
                continousTryConnectDevice()
            }
        }

        private fun getPairedDeviceAddress(): String? {

            return PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(
                    KEY_HWMAC_ID, null
            )
        }
    }

    fun continousTryConnectDevice() {
        //Bluetooth enabled
        val myDevice = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(
                KEY_HWMAC_ID, null)
        connectWithThisDevice(myDevice)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {
        val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)

    }

    private fun getMyNotification(s: String): Notification? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChanel()
        }

        val notificationIntent = Intent(this, DeviceMainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this,
                NOTIF_FOREGROUND_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(
                this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Euronet health")
                .setContentText(s)
                .setSmallIcon(R.mipmap.ic_launcher_my_round)
                .setVibrate(longArrayOf(1000, 2000, 1000))
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .build()

    }

    fun updateNotification(dateString: String) {
        Log.d(TAG, "update notifi call $dateString")
        val notification = getMyNotification(dateString)
        val notifManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        notifManager?.notify(NOTIF_FOREGROUND_ID, notification)
    }

    fun continousTryConnectDeviceForUI() {
        if (!applicationVersionModeIsLight) {
            Log.d(TAGSCAN, "continousTryConnectDeviceForUI run ")
            if (!::meteor.isInitialized) {

                meteor = if (EuronetMeteorSingleton.hasInstance()) {
                    EuronetMeteorSingleton.getInstance()
                } else {
                    EuronetMeteorSingleton.createInstance(this, "ws://${AccountGeneral.mHost}:3000/websocket", InMemoryDatabase())
                }
                val cbMeteor = MeteorCallbackInBleService

                meteor.addCallback(cbMeteor)
                cbMeteor.setBleContect(this@BluetoothLeService)
            }

            if (!meteor.isConnected) {
                Log.d(TAGSCAN, " !meteor.isConnected run ")
                val serverNotConnected = Intent()
                serverNotConnected.action = SERVER_NOT_CONN
                sendBroadcast(serverNotConnected)
                meteor.connect()
            } else {
                getAvailableDeviceListfromServer()
            }
        } else {
            getAvailableDeviceListfromServer()
        }
    }

    private fun getAvailableDeviceListfromServer() {
        if (!applicationVersionModeIsLight) {
            /*ServerCommunicate*/
            //    Log.d(TAGSCAN, "meteor.run ")

            meteor.call(method, object : ResultListener {
                override fun onSuccess(result: String?) {
                    try {
                        Log.d(TAGSCAN, "onSuccess result $result ")

                        /*meglehet oldani simán JsonArrayben is de most marad ez.*/
                        var resultNew = result?.replaceFirst("[", "{\"array\":[", false)
                        resultNew = resultNew?.replaceFirst("}]", "}]}", false)
                        Log.d(TAGSCAN, "onSuccess result new $resultNew ")
                        val REVIEW_TYPE = object : TypeToken<AvailableDevicesJson>() {}.type
                        val convertedObject = Gson().fromJson(resultNew, JsonObject::class.java)
                        val gson = Gson()
                        data = gson.fromJson(convertedObject, REVIEW_TYPE)
                        Log.d(TAGSCAN, "onSuccess result data ${data.addressJson}")
                        bluetoothScanStart()
                    } catch (e: Exception) {
                        Log.d(TAGSCAN, e.message)
                        val serverNotConnected = Intent()
                        serverNotConnected.action = SERVER_NOT_CONN
                        sendBroadcast(serverNotConnected)
                    }
                }

                override fun onError(error: String?, reason: String?, details: String?) {
                    Log.d(TAGSCAN, "error $error ")
                    Log.d(TAGSCAN, "error $reason ")
                    Log.d(TAGSCAN, "error $details ")
                    val serverNotConnected = Intent()
                    serverNotConnected.action = SERVER_NOT_CONN
                    sendBroadcast(serverNotConnected)
                }
            })
        } else {
            bluetoothScanStart()
        }
    }

    /**
     * bluetoothScanStart
     *
     */
    private fun bluetoothScanStart() {

        if (!applicationVersionModeIsLight) {
            cb.setBleContext(this@BluetoothLeService)
            this.connectorThread = BleConnector()
            this.connectorThread!!.start()
        }

        //Bluetooth enabled
        val scanSettings: ScanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .build()

        val allFilter = mutableListOf<ScanFilter>()
        val scanFilter: ScanFilter
        if (!applicationVersionModeIsLight) {
            scanFilter = ScanFilter.Builder()
                    .setManufacturerData(0xFFFF, null)
                    .build()
            allFilter.add(scanFilter)
        } else {
            scanFilter = ScanFilter.Builder()
                    .setManufacturerData(0xFFFF, null)
                    .setDeviceName("PPGo")
                    .build()
            allFilter.add(scanFilter)
        }

        if (!applicationVersionModeIsLight) {
            bluetoothAdapter!!.bluetoothLeScanner.startScan(allFilter, scanSettings, cb)
        } else {
            bluetoothAdapter!!.bluetoothLeScanner.startScan(allFilter, scanSettings, lightCB)
        }
    }

    fun stopScan() {

        if (!applicationVersionModeIsLight) {
            if (bluetoothAdapter != null) {
                bluetoothAdapter!!.bluetoothLeScanner.stopScan(cb)
            }

            if (connectorThread != null) {
                connectorThread!!.bleConnectorThreadStop()
                connectorThread!!.interrupt()
                connectorThread = null
                searchingDeviceList.clear()
            }
        } else {
            bluetoothAdapter!!.bluetoothLeScanner.stopScan(lightCB)
        }
    }

    fun connectWithThisDevice(address: String?, iChooseThisDevice: Array<MultipleDeviceInfo>?) {

        lateinit var device: BluetoothDevice

        if (bluetoothAdapter == null) {
            return
        }

        if (address != null) {
            device = bluetoothAdapter!!.getRemoteDevice(address)
        } else if (iChooseThisDevice != null) {
            Log.d(TAGPAIR, "$TAG --> device identification..")
            device = bluetoothAdapter!!.getRemoteDevice(iChooseThisDevice[0].hw_mac_id)
            thisIsMyDevice = iChooseThisDevice
        }

        Log.d(TAG, "Trying to create a new connection.")
        //     Log.d(TAGSCAN, "Trying to create a new connection. $address $setrealOperationMode")

        connectionGatt = device.connectGatt(this, true, connectGattCallback)
        stopScan()
    }

    /**
     * connect With This Device
     *
     * @param address
     */
    fun connectWithThisDevice(address: String?) {
        if (bluetoothAdapter == null || address == null) {
            return
        }

        reConnect(address)
        //Bluetooth enabled
    }

    /**
     * retry Connect
     *
     * @param address
     */
    private fun reConnect(address: String?) {
        val device = bluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return
        } else {
            if (!runtryConnect) {
                runtryConnect = true
                Log.d(TAG, "Trying to create a new connection. eszköz: $device")
                connectionGatt = device.connectGatt(this, true, connectGattCallback)
            }
        }
    }

    private val connectGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {

            Log.d(TAG, "onConnectionStateChange run notifi call")

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {

                    Log.d(TAG, "STATE_CONNECTED run")
                    if (!applicationVersionModeIsLight) {
                        if (::thisIsMyDevice.isInitialized) {

                            val spId = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                            spId.edit()
                                    .putString(KEY_DEVICE_ID, thisIsMyDevice[0].id)
                                    .apply()

                            val spHwMacId = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                            spHwMacId.edit()
                                    .putString(KEY_HWMAC_ID, thisIsMyDevice[0].hw_mac_id)
                                    .apply()

                        }
                    } else {
                        val spId = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        spId.edit()
                                .putString(KEY_USERID, "lightVersion")
                                .putString(KEY_DEVICE_ID, "lightVersion")
                                .apply()

                        val spHwMacId = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        spHwMacId.edit()
                                .putString(KEY_HWMAC_ID, gatt.device.address)
                                .apply()
                    }

                    Log.d(TAG, "Connected to GATT server.")
                    connectedDevice = true
                    updateNotification("Connecting: $connectedDevice - Measurement active: false")
                    runtryConnect = false
                    broadcastUpdate(GlobalRes.ACTION_ACL_CONNECTED)
                    discoverService()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "STATE_DISCONNECTED called notifi call")
                    connectedDevice = false

                    val myDeviceId = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(
                            KEY_DEVICE_ID, null)
                    if (myDeviceId != null) {
                        broadcastUpdate(GlobalRes.ACTION_ACL_DISCONNECTED)
                        updateNotification("connecting: $connectedDevice measurement active: false")
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d(TAGSCAN, "onServicesDiscovered run")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                serviceNotificationsSet(BluetoothServiceNotificationType.BATTERY_SERVICE.type, true)
            } else {
                //    Log.d(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic,
                                          status: Int) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                //   Log.d(TAG, "GATT_SUCCESS")

                val action = actionDetected(characteristic)
                when {
                    TX_CHAR_UUID == characteristic.uuid -> {
                        //        Log.d(TAG, "TX_CHAR_UUID")
                        dataprocessingStart(characteristic)
                    }
                    DEVICEINFORMATION_SERVICE_UUID == characteristic.uuid -> {

                    }
                    action != ANY_ACTION -> {
                        Log.d(TAG, action)
                        broadcastUpdate(action, characteristic)
                    }
                }
            }
        }

        /**
         * onCharacteristicChanged
         *
         * @param gatt
         * @param characteristic
         */
        override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                             characteristic: BluetoothGattCharacteristic) {

            Log.d(TAG, "onCharacteristicChanged ${characteristic.service}")
            val action = actionDetected(characteristic)
            if (TX_CHAR_UUID == characteristic.uuid) {
                dataprocessingStart(characteristic)
            } else if (action != ANY_ACTION) {
                //  Log.d(TAG, "action != ANY_ACTION")
                broadcastUpdate(action, characteristic)
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
            Log.d(TAG, "onReliableWriteCompleted call státsu : $status")
            super.onReliableWriteCompleted(gatt, status)
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            Log.d(TAG, "onMtuChanged call  mtu: $mtu   státsz : $status")
            super.onMtuChanged(gatt, mtu, status)

        }
    } // ----------- callback end

    private fun discoverService() {
        Log.d(TAG, "discoverService")
        Handler(Looper.getMainLooper()).postDelayed(Runnable() {
            Log.d(TAG, "servicesize :${connectionGatt!!.services.size}")
            if (connectionGatt!!.services.size == 0) {
                connectionGatt!!.discoverServices()
            } else {
                timeSendToDevice()
            }
        }, 1000);
    }

    private fun dataprocessingStart(characteristic: BluetoothGattCharacteristic) {
        if (characteristic.value != null) {
            writeFile(characteristic.value)
            //      Log.d(TAG, "On data processing start")
            bluetoothPackageCounter++

            if (deviceInformation.containsKey("HARDWARE") && !deviceInformation["HARDWARE"].isNullOrEmpty()) {
                val intentDATA = Intent(this, DataFirstProcessIntentService::class.java)
                intentDATA.putExtra(DataFirstProcessIntentService.DEVICEINFORMATION, deviceInformation)
                intentDATA.putExtra(DataFirstProcessIntentService.KEY_DATA, characteristic.value)
                startService(intentDATA)
            }
            // Log.d(TAG, "On data processing end")
        }
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (bluetoothAdapter == null) {
            //     Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        connectionGatt!!.readCharacteristic(characteristic)
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic,
                                      enabled: Boolean) {
        if (bluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        connectionGatt!!.setCharacteristicNotification(characteristic, enabled)

        val descriptor = characteristic.getDescriptor(
                UUID_CLIENT_CHARACTERISTIC_CONFIG)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        Log.w(TAG, "setCharacteristicNotification run")
        connectionGatt!!.writeDescriptor(descriptor)
    }

    @Synchronized
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, dataArray: ByteArray) {
        Log.d(TAG, " writeCharacteristic")
        when (characteristic.uuid) {
            RX_CHAR_UUID -> {
                characteristic.value = dataArray
                connectionGatt!!.writeCharacteristic(characteristic)
            }
        }
    }

    /**
     * service Notifications Set
     *
     * @param type Int
     * @param isListening Boolean
     */
    @Synchronized
    fun serviceNotificationsSet(type: Int, isListening: Boolean) {
        Log.d(TAGMEASUREMENT, " reconnect esemény serviceNotificationsSet call- > $type")
        /**
         *      BATTERY_SERVICE (1),
        HEARTRATE_SERVICE (2),
        NORDIC_SERVICE (3),
        DEVICEINFO_SERVICE (4)
         */
        when (type) {
            BluetoothServiceNotificationType.BATTERY_SERVICE.type -> {
                Log.d(TAGMEASUREMENT, "BATTERY_SERVICE $isListening")
                batteryValueNotification(isListening)
            }
            BluetoothServiceNotificationType.HEARTRATE_SERVICE.type -> {
                Log.d(TAGMEASUREMENT, "HEARTRATE_SERVICE $isListening")
                heartRateValueNotification(isListening)
            }
            BluetoothServiceNotificationType.NORDIC_SERVICE.type -> {
                Log.d(TAGMEASUREMENT, "notifi call NORDIC_SERVICE $isListening")
                nordicValueNotification(isListening)
            }
            BluetoothServiceNotificationType.DEVICEINFO_SERVICE.type -> {
                Log.d(TAGMEASUREMENT, "DEVICEINFO_SERVICE $isListening")
                deviceINFONotification(KEY_FW_VERSION)
            }
        }
    }

    private fun batteryValueNotification(isListening: Boolean) {
        Log.d(TAGMEASUREMENT, "batteryValueNotification run")
        if (connectionGatt!!.services.size > 0) {
            val batteryService = connectionGatt!!.getService(BATTERY_SERVICE_UUID)
            val batteryLevel = batteryService.getCharacteristic(BATTERY_LEVEL_CHAR_UUID)

            if (batteryLevel == null) {
                //           Log.d(TAG, "battery measurement not found!")
                return
            }

            val charaProp = batteryLevel.properties
            if (BluetoothGattCharacteristic.PROPERTY_READ > 0 || (charaProp > 0)) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the dataMultipleList field on the user interface.
                if (mNotifyCharacteristicBattery != null) {
                    setCharacteristicNotification(
                            mNotifyCharacteristicBattery!!, false)
                    //         Log.d(TAG, "batteryValueNotification setNotif: false")
                    mNotifyCharacteristicBattery = null
                }
                readCharacteristic(batteryLevel)
            }
            if (charaProp > 0 || BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                mNotifyCharacteristicBattery = batteryLevel
                setCharacteristicNotification(mNotifyCharacteristicBattery!!, isListening)
                //     Log.d(TAG, "batteryValueNotification setNotif: true")
            }
        }
    }

    private fun heartRateValueNotification(isListening: Boolean) {
        Log.d(TAGMEASUREMENT, "heartRateValueNotification run")
        if (connectionGatt!!.services.size > 0) {
            val heartService = connectionGatt!!.getService(HEART_RATE_SERVICE_UUID)
            val HEART_RATE_MEASUREMENT_UUID = convertFromInteger(0x2A37)
            val heartRateMeasurement = heartService.getCharacteristic(HEART_RATE_MEASUREMENT_UUID)

            if (heartRateMeasurement == null) {
                //     Log.d(TAG, "Heart rate measurement not found!")
                return
            }

            val charaProp = heartRateMeasurement.properties
            if (BluetoothGattCharacteristic.PROPERTY_READ > 0 || (charaProp > 0)) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                if (mNotifyCharacteristicHeartRate != null) {
                    setCharacteristicNotification(
                            mNotifyCharacteristicHeartRate!!, false)
                    mNotifyCharacteristicHeartRate = null
                }
                readCharacteristic(heartRateMeasurement)
            }
            if (charaProp > 0 || BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                mNotifyCharacteristicHeartRate = heartRateMeasurement
                setCharacteristicNotification(heartRateMeasurement, isListening)
            }
        }
    }

    /**
     * nordic Value Notification listening
     *
     * @param isListening
     */
    private fun nordicValueNotification(isListening: Boolean) {
        Log.d(TAGMEASUREMENT, "nordicValueNotification run")
        updateNotification("connecting: $connectedDevice measurement active: $isListening")
        arriveingData = isListening
        if (connectionGatt!!.services.size > 0) {
            val nordicService = connectionGatt!!.getService(NORDIC_UART_SERVICE_UUID)
            tx = nordicService.getCharacteristic(TX_CHAR_UUID)

            charaPropTX = tx.properties
            if (BluetoothGattCharacteristic.PROPERTY_READ > 0 || (charaPropTX > 0)) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the dataMultipleList field on the user interface.
                if (mNotifyCharacteristicNordic != null) {
                    setCharacteristicNotification(
                            tx, false)
                    mNotifyCharacteristicNordic = null
                }
                readCharacteristic(tx)
            }
        }
        if (charaPropTX > 0 || BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
            if (::tx.isInitialized) {
                mNotifyCharacteristicNordic = tx
                setCharacteristicNotification(tx, isListening)
            }
        }
    }

    @Synchronized
    fun deviceINFONotification(charName: String) {
        Log.d(TAGMEASUREMENT, "deviceINFONotification run $charName")
        if (connectionGatt!!.services.size > 0) {

            val deviceINFOService = connectionGatt!!.getService(DEVICEINFORMATION_SERVICE_UUID)

            when (charName) {
                KEY_HARDWARE_VERSION -> {
                    Log.d(TAGMEASUREMENT, "HARDWARE_REVISION")
                    val hardware = deviceINFOService.getCharacteristic(HARDWARE_REVISION_STRING)
                    if (hardware == null) {
                        Log.d(TAGMEASUREMENT, "hardware measurement not found!")
                        return
                    }
                    val charaPropHARDWARE = hardware.properties
                    if (BluetoothGattCharacteristic.PROPERTY_READ > 0 || (charaPropHARDWARE > 0)) {
                        readCharacteristic(hardware)
                    }
                }
                KEY_FW_VERSION -> {
                    val firmware = deviceINFOService.getCharacteristic(FIRMWARE_REVISION_STRING)
                    if (firmware == null) {
                        Log.d(TAGMEASUREMENT, "firmware measurement not found!")
                        return
                    }

                    val charaProp = firmware.properties
                    if (BluetoothGattCharacteristic.PROPERTY_READ > 0 || (charaProp > 0)) {
                        readCharacteristic(firmware)
                    }
                }
                KEY_MANUFACTURER_NAME_STRING -> {
                    val manuf = deviceINFOService.getCharacteristic(MANUFACTURER_NAME_STRING)
                    if (manuf == null) {
                        Log.d(TAGMEASUREMENT, "manuf measurement not found!")
                        return
                    }

                    val charaProp = manuf.properties
                    if (BluetoothGattCharacteristic.PROPERTY_READ > 0 || (charaProp > 0)) {
                        readCharacteristic(manuf)
                    }
                }
                KEY_DEVICENAME -> {

                    val genericAccessService = connectionGatt!!.getService(GENERIC_ACCESS)
                    val DEVICENAME = genericAccessService.getCharacteristic(DEVICENAME)
                    if (DEVICENAME == null) {
                        Log.d(TAGMEASUREMENT, "DEVICENAME measurement not found!")
                        return
                    }

                    val charaProp = DEVICENAME.properties
                    if (BluetoothGattCharacteristic.PROPERTY_READ > 0 || (charaProp > 0)) {
                        readCharacteristic(DEVICENAME)
                    }
                }
                KEY_MODEL_NUMBER_STRING -> {
                    val MODEL_NUMBER = deviceINFOService.getCharacteristic(MODEL_NUMBER_STRING)
                    if (MODEL_NUMBER == null) {
                        Log.d(TAGMEASUREMENT, "MODEL_NUMBER measurement not found!")
                        return
                    }

                    val charaProp = MODEL_NUMBER.properties
                    if (BluetoothGattCharacteristic.PROPERTY_READ > 0 || (charaProp > 0)) {
                        readCharacteristic(MODEL_NUMBER)
                    }
                }
                KEY_SERIAL_NUMBER_STRING -> {
                    val SERIAL_NUMBER = deviceINFOService.getCharacteristic(SERIAL_NUMBER_STRING)
                    if (SERIAL_NUMBER == null) {
                        Log.d(TAGMEASUREMENT, "SERIAL_NUMBER measurement not found!")
                        return
                    }

                    val charaProp = SERIAL_NUMBER.properties
                    if (BluetoothGattCharacteristic.PROPERTY_READ > 0 || (charaProp > 0)) {
                        readCharacteristic(SERIAL_NUMBER)
                    }
                }
            }
        }
    }

    private fun broadcastUpdate(action: String) {
        sendBroadcast(Intent(action))
    }

    @Synchronized
    private fun actionDetected(characteristic: BluetoothGattCharacteristic): String {

        return when (characteristic.uuid) {
            BATTERY_LEVEL_CHAR_UUID -> {
                //Log.d(TAG, "BATTERY_LEVEL_CHAR_UUID actionDetected")
                ACTION_BATTERY_DATA_AVAILABLE
            }
            UUID_HEART_RATE_MEASUREMENT_CHARACTERISTIC -> {
                //Log.d(TAG, "HEART_RATE_MEASUREMENT_CHAR_UUID actionDetected")
                ACTION_HEART_RATE_DATA_AVAILABLE
            }
            TX_CHAR_UUID -> {
                ACTION_NORDIC_DATA_AVAILABLE
            }
            FIRMWARE_REVISION_STRING -> {
                ACTION_DEVICEINFO_DATA_AVAILABLE
            }
            HARDWARE_REVISION_STRING -> {
                ACTION_DEVICEINFO_DATA_AVAILABLE
            }
            DEVICENAME -> {
                ACTION_DEVICEINFO_DATA_AVAILABLE
            }
            MANUFACTURER_NAME_STRING -> {
                ACTION_DEVICEINFO_DATA_AVAILABLE
            }
            MODEL_NUMBER_STRING -> {
                ACTION_DEVICEINFO_DATA_AVAILABLE
            }
            SERIAL_NUMBER_STRING -> {
                ACTION_DEVICEINFO_DATA_AVAILABLE
            }
            else -> {
                ANY_ACTION
            }
        }
    }

    @Synchronized
    fun broadcastUpdate(action: String,
                        characteristic: BluetoothGattCharacteristic) {
        Log.d(TAG, "broadcastUpdate run")
        val intent = Intent()
        intent.action = action
        when (characteristic.uuid) {
            BATTERY_LEVEL_CHAR_UUID -> {
                val data = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                deviceBatteryValue = data

                Log.d(TAGMEASUREMENT, "BATTERY_LEVEL $data")
                //    updateNotification("Battery value: $data%")
                serviceNotificationsSet(BluetoothServiceNotificationType.DEVICEINFO_SERVICE.type, true)

                val batteryChange = Intent()
                batteryChange.putExtra(EXTRA_DATA, deviceBatteryValue)
                batteryChange.action = ACTION_BATTERY_DATA_AVAILABLE
                sendBroadcast(batteryChange)
            }
            UUID_HEART_RATE_MEASUREMENT_CHARACTERISTIC -> {
                Log.d(TAGMEASUREMENT, "HEART_RATE arrived $deviceInformation, ")
                //  if(deviceInformation["MAC"] != null && deviceInformation["FIRMWARE"] != null && deviceInformation["HARDWARE"] != null && deviceInformation["DEVICEID"]  != null && deviceInformation["USERID"] != null) {
                val hrData = HRData(null, deviceInformation["MAC"]!!, deviceInformation["FIRMWARE"]!!, BuildConfig.VERSION_NAME, deviceInformation["HARDWARE"]!!, deviceInformation["DEVICEID"]!!, deviceInformation["USERID"]!!, System.currentTimeMillis(), System.currentTimeMillis(), null, characteristic.value, characteristic)
                DataBroadcaster.getInstance(this).send(hrData)
                // }
            }
            FIRMWARE_REVISION_STRING -> {
                Log.d(TAGMEASUREMENT, "FIRMWARE_REVISION_STRING arrived  $deviceInformation")
                deviceInformation.clear()
                deviceInformation["MAC"] = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(KEY_HWMAC_ID, null)!!
                deviceInformation["DEVICEID"] = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(KEY_DEVICE_ID, null)!!
                deviceInformation["FIRMWARE"] = characteristic.getStringValue(0)
                deviceInformation["USERID"] = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(KEY_USERID, null)!!
                deviceINFONotification(KEY_HARDWARE_VERSION)

                val FIRMWARE = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                FIRMWARE.edit()
                        .putString(KEY_FW_VERSION, characteristic.getStringValue(0))
                        .apply()
            }
            HARDWARE_REVISION_STRING -> {
                Log.d(TAGMEASUREMENT, "HARDWARE_REVISION_STRING arrived")
                deviceInformation["HARDWARE"] = characteristic.getStringValue(0)
                Log.d(TAGMEASUREMENT, "deviceInformation  $deviceInformation")

                val HARDWARE = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                HARDWARE.edit()
                        .putString(KEY_HARDWARE_VERSION, characteristic.getStringValue(0))
                        .apply()

                deviceINFONotification(KEY_DEVICENAME)
            }
            DEVICENAME -> {
                Log.d(TAGMEASUREMENT, "DEVICENAME arrived")
                deviceInformation["DEVICENAME"] = characteristic.getStringValue(0)
                Log.d(TAGMEASUREMENT, "deviceInformation  $deviceInformation")

                val DEVICENAME = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                DEVICENAME.edit()
                        .putString(KEY_DEVICENAME, characteristic.getStringValue(0))
                        .apply()

                deviceINFONotification(KEY_MANUFACTURER_NAME_STRING)
            }
            MANUFACTURER_NAME_STRING -> {
                Log.d(TAGMEASUREMENT, "MANUFACTURER_NAME_STRING arrived")
                deviceInformation["MANUFACTURER"] = characteristic.getStringValue(0)
                Log.d(TAGMEASUREMENT, "deviceInformation  $deviceInformation")

                val MANUFACTURER_NAME_STRING = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                MANUFACTURER_NAME_STRING.edit()
                        .putString(KEY_MANUFACTURER_NAME_STRING, characteristic.getStringValue(0))
                        .apply()
                deviceINFONotification(KEY_SERIAL_NUMBER_STRING)
            }
            SERIAL_NUMBER_STRING -> {
                Log.d(TAGMEASUREMENT, "SERIAL_NUMBER_STRING arrived")
                deviceInformation["SERIAL_NUMBER"] = characteristic.getStringValue(0)
                Log.d(TAGMEASUREMENT, "deviceInformation  $deviceInformation")

                val SERIAL_NUMBER_STRING = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                SERIAL_NUMBER_STRING.edit()
                        .putString(KEY_SERIAL_NUMBER_STRING, characteristic.getStringValue(0))
                        .apply()
                deviceINFONotification(KEY_MODEL_NUMBER_STRING)
            }
            MODEL_NUMBER_STRING -> {
                Log.d(TAGMEASUREMENT, "MODEL_NUMBER_STRING arrived")
                deviceInformation["MODEL_NUMBER"] = characteristic.getStringValue(0)
                Log.d(TAGMEASUREMENT, "deviceInformation  $deviceInformation")

                val MODEL_NUMBER_STRING = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                MODEL_NUMBER_STRING.edit()
                        .putString(KEY_MODEL_NUMBER_STRING, characteristic.getStringValue(0))
                        .apply()

                timeSendToDevice()
            }
        }
    }

    fun timeSendToDevice() {
        /**
         * TIME
         * */

        val time = byteArrayOf(0x00, 0x01) //0x0001


        val timeNow = Calendar.getInstance().time
        val timeNowLong = timeNow.time
        val timeArray = byteArrayOf(
                timeNowLong.toByte(),
                (timeNowLong shr 8).toByte(),
                (timeNowLong shr 16).toByte(),
                (timeNowLong shr 24).toByte(),
                (timeNowLong shr 32).toByte(),
                (timeNowLong shr 40).toByte(),
                (timeNowLong shr 48).toByte(),
                (timeNowLong shr 56).toByte())

        /*ellenőrző fejtés*/
        val l = (timeArray[7].toLong() shl 56
                or (timeArray[6].toLong() and 0xff shl 48)
                or (timeArray[5].toLong() and 0xff shl 40)
                or (timeArray[4].toLong() and 0xff shl 32)
                or (timeArray[3].toLong() and 0xff shl 24)
                or (timeArray[2].toLong() and 0xff shl 16)
                or (timeArray[1].toLong() and 0xff shl 8)
                or (timeArray[0].toLong() and 0xff))

        Log.d(TAG, "WriteChar bytearray to long id_time }" +
                " visszafejtett long: $l" +
                "   visszafejtett date : ${Date(l)}.")

        val sendDataTime = byteArrayOf(
                time[0], time[1],
                timeArray[0],
                timeArray[1],
                timeArray[2],
                timeArray[3],
                timeArray[4],
                timeArray[5],
                timeArray[6],
                timeArray[7],
                /* CRC helye */   0x00
        )

        // CRC számítás ELEJE -------------
        var dataSum = 0
        for (i in 0 until 10) {
            dataSum += sendDataTime[i]
        }

        /*
         * CRC értéket nem vesszük bele a számításba
         */
        val crcValue = dataSum and 0xFF
        // CRC számítás VÉGE -------------

        // CRC az utolsó pozícióra
        sendDataTime[sendDataTime.lastIndex] = crcValue.toByte()

        if (connectionGatt != null && connectedDevice) {
            val nordicService = connectionGatt!!.getService(GlobalRes.NORDIC_UART_SERVICE_UUID)

            val rx = nordicService.getCharacteristic(GlobalRes.RX_CHAR_UUID)
            writeFileNORDIC(sendDataTime, Date(l).toString())
            writeCharacteristic(rx, sendDataTime)

            Handler(Looper.getMainLooper()).postDelayed(Runnable() {
                timeOffsetSendToDevice()
            }, 200);
        }
    }

    fun timeOffsetSendToDevice() {

        /**
         * TIME OFFSET
         * */

        val timeZoneOffset = byteArrayOf(0x00, 0x02)//0x0002
        val tz = TimeZone.getDefault()
        val now = Date()
        val offsetFromUtc = tz.getOffset(now.time) / 1000 / 60

        Log.d(TAG, "WriteChar bytearray to id_time_offset} $offsetFromUtc")

        val offsetArray = byteArrayOf(
                offsetFromUtc.toByte(),
                0,
                0,
                0,
                0,
                0,
                0,
                0
        )

        /*ellenőrző fejtés*/
        val l = (offsetArray[7].toLong() shl 56
                or (offsetArray[6].toLong() and 0xff shl 48)
                or (offsetArray[5].toLong() and 0xff shl 40)
                or (offsetArray[4].toLong() and 0xff shl 32)
                or (offsetArray[3].toLong() and 0xff shl 24)
                or (offsetArray[2].toLong() and 0xff shl 16)
                or (offsetArray[1].toLong() and 0xff shl 8)
                or (offsetArray[0].toLong() and 0xff))

        Log.d(TAG, "WriteChar bytearray to long id_time_offset }" +
                " visszafejtett long: $l")

        val sendDataOffset = byteArrayOf(
                timeZoneOffset[0], timeZoneOffset[1],
                offsetArray[0],
                offsetArray[1],
                offsetArray[2],
                offsetArray[3],
                offsetArray[4],
                offsetArray[5],
                offsetArray[6],
                offsetArray[7],
                /* CRC helye */   0x00
        )

        // CRC számítás ELEJE -------------
        var dataSum = 0
        for (i in 0 until 10) {
            dataSum += sendDataOffset[i]
        }

        /*
         * CRC értéket nem vesszük bele a számításba
         */
        val crcValue = dataSum and 0xFF
        // CRC számítás VÉGE -------------

        // CRC az utolsó pozícióra
        sendDataOffset[sendDataOffset.lastIndex] = crcValue.toByte()

        if (connectionGatt != null && connectedDevice) {
            val nordicService = connectionGatt!!.getService(GlobalRes.NORDIC_UART_SERVICE_UUID)
            val rx = nordicService.getCharacteristic(GlobalRes.RX_CHAR_UUID)
            writeFileNORDIC(sendDataOffset, l.toString())
            writeCharacteristic(rx, sendDataOffset)

            subscribeMeteor()
        }
    }

    private fun subscribeMeteor() {
        Log.d(TAG, "subscribeMeteor")
        /*Külön áltozóban kell maradjon hogy a callback- ide kötődjön ne íródjon felül*/
        meteorSubscribe = if (EuronetMeteorSingleton.hasInstance()) {
            EuronetMeteorSingleton.getInstance()
        } else {
            EuronetMeteorSingleton.createInstance(this, "ws://${AccountGeneral.mHost}:3000/websocket", InMemoryDatabase())
        }
        meteorSubscribe.connect()

        meteorSubscribe.addCallback(object : MeteorCallback {
            override fun onConnect(signedInAutomatically: Boolean) {
                meteorSubscribe.subscribe("triangle-signal", null, object : SubscribeListener {
                    override fun onSuccess() {
                        Log.d(TAGSUBS, "onSuccess  subscribeMeteor")
                    }

                    override fun onError(error: String?, reason: String?, details: String?) {
                        Log.d(TAGSUBS, "error $error ")
                        Log.d(TAGSUBS, "error $reason ")
                        Log.d(TAGSUBS, "error $details ")
                    }
                })
            }

            override fun onDataAdded(collectionName: String?, documentID: String?, newValuesJson: String?) {
                val mCollectionName = "onDataAdded $collectionName"
                val mDocumentID = "onDataAdded $documentID"
                val mNewValuesJson = "onDataAdded$newValuesJson"

                Log.d(TAGSUBS + "   " + TAG, mCollectionName)
                Log.d(TAGSUBS + "   " + TAG, mDocumentID)
                Log.d(TAGSUBS + "   " + TAG, mNewValuesJson)
            }

            override fun onDataRemoved(collectionName: String?, documentID: String?) {
                val mCollectionName = "onDataRemoved $collectionName"
                val mDocumentID = "onDataRemoved $documentID"

                Log.d(TAGSUBS + "   " + TAG, mCollectionName)
                Log.d(TAGSUBS + "   " + TAG, mDocumentID)
            }

            override fun onException(e: Exception?) {
                val error = "onError error" + e!!.message
                Log.d(TAGSUBS + "   " + TAG, error)
            }

            override fun onDisconnect() {
                val ondisconnect = "onDisconnect"
                Log.d(TAGSUBS + "   " + TAG, ondisconnect)
            }

            override fun onDataChanged(collectionName: String?, documentID: String?, updatedValuesJson: String?, removedValuesJson: String?) {
                val mCollectionName = "mCollectionName--> $collectionName"
                val mDocumentID = "mDocumentID--> $documentID"
                val mNewValuesJson = "mNewValuesJson--> $updatedValuesJson"
                val mRemovedValuesJson = "mRemovedValuesJson--> $removedValuesJson"

                /*every five data*/
                if (trianglecounter == 10) {
                    trianglecounter = 0
                    triangleSignalSendToDevice(updatedValuesJson)
                } else {
                    trianglecounter++
                }
            }
        })
    }

    /**
     * triangle Signal Send To Device
     *
     * @param updatedValuesJson
     */
    @Synchronized
    private fun triangleSignalSendToDevice(updatedValuesJson: String?) {
        val vibra = byteArrayOf(0x10, 0x01) //0x1001
        val weariness = byteArrayOf(0x00, 0x10)//0x0010

        val gson = Gson()
        //val jsonString = gson.toJson(updatedValuesJson , JsonObject::class.java)

        val REVIEWTYPE = object : TypeToken<TriangleSignalObject>() {}.type
        val jsonObject: TriangleSignalObject = gson.fromJson(updatedValuesJson, REVIEWTYPE)

        val triangleChange = Intent()
        triangleChange.putExtra(EXTRA_DATA, jsonObject.value)
        triangleChange.action = ACTION_TRIANGLESIGNAL_DATA_AVAILABLE
        sendBroadcast(triangleChange)


        val wearinessLong = jsonObject.value.toLong()
        val wearinessArray = byteArrayOf(
                wearinessLong.toByte(),
                (wearinessLong shr 8).toByte(),
                (wearinessLong shr 16).toByte(),
                (wearinessLong shr 24).toByte(),
                (wearinessLong shr 32).toByte(),
                (wearinessLong shr 40).toByte(),
                (wearinessLong shr 48).toByte(),
                (wearinessLong shr 56).toByte())

        val sendData = byteArrayOf(
                weariness[0], weariness[1],
                wearinessArray[0],
                wearinessArray[1],
                wearinessArray[2],
                wearinessArray[3],
                wearinessArray[4],
                wearinessArray[5],
                wearinessArray[6],
                wearinessArray[7],
                /* CRC helye */   0x00
        )

        Log.d(TAGSUBS + "   " + TAG, "send triangle signal : $wearinessLong")

        // CRC számítás ELEJE -------------
        var dataSum = 0
        for (i in 0 until 10) {
            dataSum += sendData[i]
        }

        /*
         * CRC értéket nem vesszük bele a számításba
         */
        val crcValue = dataSum and 0xFF
        // CRC számítás VÉGE -------------

        // CRC az utolsó pozícióra
        sendData[sendData.lastIndex] = crcValue.toByte()

        if (connectionGatt != null && connectedDevice) {
            val nordicService = connectionGatt!!.getService(GlobalRes.NORDIC_UART_SERVICE_UUID)
            val rx = nordicService.getCharacteristic(GlobalRes.RX_CHAR_UUID)

            writeFileNORDIC(sendData, jsonObject.value.toString())
            writeCharacteristic(rx, sendData)

            if (jsonObject.value <= 20) {
                Handler(Looper.getMainLooper()).postDelayed(Runnable() {
                    Log.d(TAGSUBS + "   " + TAG, "send triangle signal vibra")
                    val sendVibra = byteArrayOf(
                            vibra[0], vibra[1],
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            /* CRC helye */   0x00
                    )

                    Log.d(TAGSUBS + "   " + TAG, "${jsonObject.value}  --- > smaller then 20 -> sendVibra")

                    // CRC számítás ELEJE -------------
                    var dataSumVibra = 0
                    for (i in 0 until 10) {
                        dataSumVibra += sendVibra[i]
                    }

                    /*
                 * CRC értéket nem vesszük bele a számításba
                 */
                    val crcValueVibra = dataSumVibra and 0xFF
                    // CRC számítás VÉGE -------------

                    // CRC az utolsó pozícióra
                    sendVibra[sendVibra.lastIndex] = crcValueVibra.toByte()

                    writeFileNORDIC(sendVibra, "Vibra -> ")
                    writeCharacteristic(rx, sendVibra)
                }, 200);
            }
        }
    }

    override fun getAvailableDevice(): AvailableDevicesJson {
        return data
    }

    override fun getMeteor(): EuronetMeteorSingleton {
        return meteor
    }

    override fun getConnectorThread(): BleConnector {
        return this.connectorThread!!
    }

    fun getBluetoothAdapter(): BluetoothAdapter? {
        return bluetoothAdapter
    }

    fun getGatt(): BluetoothGatt {
        return connectionGatt!!
    }

    fun setGatt() {
        connectionGatt = null
    }

    fun writeLastDatasToCSV() {
        if (!runningWrtieFile) {
            if (datasToCSVPPG.isNotEmpty()) {
                runningWrtieFile = true
                DataFileWrite(this, CollectableType.PPG.type) { result ->
                    when (result) {
                        "RESULT_OK" -> {
                            datasToCSVPPG.clear()
                            runningWrtieFile = false
                            // Log.d(TAG, "Tömb ürítés kész : PPG  méret ellemőrzés: ${datasToCSVPPG.size}")
                        }
                        "RESULT_FAIL" -> {
                            Log.d(TAG, "Hiba keletkezett az írás során! PPG ")
                        }
                        else -> {
                            Log.d(TAG, "Nem definiált result jött vissza fájl írás osztályból")
                        }
                    }
                }.execute(datasToCSVPPG)
            }
            if (datasToCSVIBI.isNotEmpty()) {

                DataFileWrite(this, CollectableType.IBI.type) { result ->
                    when (result) {
                        "RESULT_OK" -> {
                            datasToCSVIBI.clear()
                            //   Log.d(TAG, "Tömb ürítés kész : IBI  méret ellemőrzés: ${datasToCSVIBI.size}")
                        }
                        "RESULT_FAIL" -> {
                            Log.d(TAG, "Hiba keletkezett az írás során! IBI ")
                        }
                        else -> {
                            Log.d(TAG, "Nem definiált result jött vissza fájl írás osztályból")
                        }
                    }
                }.execute(datasToCSVIBI)
            }
            if (datasToCSVIMU.isNotEmpty()) {

                DataFileWrite(this, CollectableType.IMU.type) { result ->
                    when (result) {
                        "RESULT_OK" -> {
                            datasToCSVIMU.clear()
                            //  Log.d(TAG, "Tömb ürítés kész : IMU  méret ellemőrzés: ${datasToCSVIMU.size}")
                        }
                        "RESULT_FAIL" -> {
                            Log.d(TAG, "Hiba keletkezett az írás során! IMU ")
                        }
                        else -> {
                            Log.d(TAG, "Nem definiált result jött vissza fájl írás osztályból")
                        }
                    }
                }.execute(datasToCSVIMU)
            }
        }
    }

    fun ByteArray.toHexString() = joinToString(" ") { "%02x".format(it) }

    /**
     * Bluetooth characteristic byte Array to csv
     *
     * @param data
     */
    @SuppressLint("SimpleDateFormat")
    private fun writeFile(data: ByteArray) {
        // Log.d(TAG, "filewrite->writeFile run")
        val formatted: String
        val CSV_HEADER = "Time;Data"

        if (data.isNotEmpty() /*&& datas.size > MinDataNumber*/) {
            formatted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val current = LocalDateTime.now()

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")
                current.format(formatter)
            } else {
                val current = System.currentTimeMillis()

                val formatter = SimpleDateFormat("yyyy-MM-dd HH")
                formatter.format(current)
            }

            val hasSDCard = Environment.getExternalStorageState()
            val fileName = "Bluetooth_$formatted.csv"

            when (hasSDCard) {
                Environment.MEDIA_MOUNTED -> {

                    // Írható olvasható
                    val fileWriter: FileWriter?
                    val folder = File(Environment.getExternalStorageDirectory().toString() + "/Euronet/")

                    if (!folder.exists())
                        folder.mkdirs()

                    // fix
                    folder.setExecutable(true)
                    folder.setReadable(true)
                    folder.setWritable(true)

                    // initiate media scan and put the new things into the path array to
                    // make the scanner aware of the location and the files you want to see

                    val file = File(folder, fileName)
                    file.setWritable(true)
                    file.setReadable(true)
                    file.setExecutable(true)

                    if (!file.exists()) {
                        fileWriter = FileWriter(file)
                        fileWriter.append(CSV_HEADER)
                        fileWriter.append('\n')
                        //            Log.d(TAG, "filewrite->fileName : $fileName")
                    } else {
                        fileWriter = FileWriter(file, true)
                    }
                    try {
                        val formattedBle: String
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val current = LocalDateTime.now()

                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                            formattedBle = current.format(formatter)
                        } else {
                            val current = System.currentTimeMillis()

                            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                            formattedBle = formatter.format(current)
                        }
                        fileWriter.append(formattedBle)
                        fileWriter.append(';')
                        fileWriter.append(data.toHexString())
                        fileWriter.append('\n')

                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            fileWriter.flush()
                            fileWriter.close()
                        } catch (e: IOException) {
                            e.printStackTrace()

                        }
                    }
                }
            }
        }
    }

    /**
     * characteristic write to file
     *
     * @param data
     * @param value
     */
    @SuppressLint("SimpleDateFormat")
    fun writeFileNORDIC(data: ByteArray, value: String) {
        // Log.d(TAG, "filewrite->writeFile run")
        val formatted: String
        val CSV_HEADER = "Time;Data,ByteArray"

        if (data.isNotEmpty() /*&& datas.size > MinDataNumber*/) {
            formatted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val current = LocalDateTime.now()

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")
                current.format(formatter)
            } else {
                val current = System.currentTimeMillis()

                val formatter = SimpleDateFormat("yyyy-MM-dd HH")
                formatter.format(current)
            }

            val hasSDCard = Environment.getExternalStorageState()
            val fileName = "Nordic_$formatted.csv"

            when (hasSDCard) {
                Environment.MEDIA_MOUNTED -> {

                    // Írható olvasható
                    val fileWriter: FileWriter?
                    val folder = File(Environment.getExternalStorageDirectory().toString() + "/Euronet/Nordic/")

                    if (!folder.exists())
                        folder.mkdirs()

                    // fix
                    folder.setExecutable(true)
                    folder.setReadable(true)
                    folder.setWritable(true)

                    // initiate media scan and put the new things into the path array to
                    // make the scanner aware of the location and the files you want to see

                    val file = File(folder, fileName)
                    file.setWritable(true)
                    file.setReadable(true)
                    file.setExecutable(true)

                    if (!file.exists()) {
                        fileWriter = FileWriter(file)
                        fileWriter.append(CSV_HEADER)
                        fileWriter.append('\n')
                        //            Log.d(TAG, "filewrite->fileName : $fileName")
                    } else {
                        fileWriter = FileWriter(file, true)
                    }
                    try {
                        val formattedBle: String
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val current = LocalDateTime.now()

                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                            formattedBle = current.format(formatter)
                        } else {
                            val current = System.currentTimeMillis()

                            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                            formattedBle = formatter.format(current)
                        }
                        fileWriter.append(formattedBle)
                        fileWriter.append(';')
                        fileWriter.append(value)
                        fileWriter.append(';')
                        fileWriter.append(data.toHexString())
                        fileWriter.append('\n')

                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            fileWriter.flush()
                            fileWriter.close()
                        } catch (e: IOException) {
                            e.printStackTrace()

                        }
                    }
                }
            }
        }
    }

    fun getArriveingData(): Boolean {
        return arriveingData
    }

    fun getDeviceInformation(): HashMap<String, String> {
        return deviceInformation
    }

    override fun onDestroy() {
        Log.d(TAG, "Ondestroy call, bleservice")
        connectedDevice = false

        enabled = false
        if (connectionGatt != null && connectedDevice) {
            serviceNotificationsSet(BluetoothServiceNotificationType.BATTERY_SERVICE.type, false)
            serviceNotificationsSet(BluetoothServiceNotificationType.HEARTRATE_SERVICE.type, false)
            serviceNotificationsSet(BluetoothServiceNotificationType.NORDIC_SERVICE.type, false)
            serviceNotificationsSet(BluetoothServiceNotificationType.DEVICEINFO_SERVICE.type, false)
            Log.d(TAG, "Ondestroy call, connectionGatt.close")
            unSubscribeTriangleSignal()
            connectionGatt!!.disconnect()
            connectionGatt!!.close()
        }

        if (!applicationVersionModeIsLight) {

            ppgThreadRun(false)
            hrThreadRun(false)
            stepThreadRun(false)
            imuThreadRun(false)
            ibiThreadRun(false)
            questionnaireOneThread.stopAndInterrupt()

        }
        unSubscribeTriangleSignal()
        unregisterReceiver(screenOnBroadcastReciever)

        if (EuronetMeteorSingleton.hasInstance()) {
            if (EuronetMeteorSingleton.getInstance().isConnected) {
                EuronetMeteorSingleton.destroyInstance()
            }
        }

        stopForgroundService()
        super.onDestroy()
    }

    fun unSubscribeTriangleSignal() {
        if (::meteorSubscribe.isInitialized) {
            meteorSubscribe.unsubscribe("triangle-signal", object : UnsubscribeListener {
                override fun onSuccess() {
                    Log.d(TAG, "unsubscribe ok.")
                    try {
                        EuronetMeteorSingleton.destroyInstance()
                    } catch (e: IllegalStateException) {
                    }
                    setGatt()
                }
            })
        }
    }

    fun stopForgroundService() {
        stopForeground(true)
        writeLastDatasToCSV()
    }

    fun getDeviceBatteryValue(): Int {
        return deviceBatteryValue
    }

    fun setFullScreanActData (resultQuestionnaireListFillableArray: Pair<QuestionnaireResultListFillable,QuestionnaireGetModel>){
        this.fullScreanActData = resultQuestionnaireListFillableArray
    }

    fun getFullScreanActData() : Pair<QuestionnaireResultListFillable,QuestionnaireGetModel>{
        return fullScreanActData
    }
}
