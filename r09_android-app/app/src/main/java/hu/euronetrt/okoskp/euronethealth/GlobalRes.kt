package hu.euronetrt.okoskp.euronethealth

import hu.euronetrt.okoskp.euronethealth.bluetooth.objects.BleServiceAndCharacteristicNameTranslate.convertFromInteger
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.MultipleDeviceInfo
import java.util.*

object GlobalRes {

    val applicationVersionModeIsLight = false
    val writeFileActive = false
    val writeFileQuestionnaireActive = true
    val writeFileCrashRiportActive = true
    var enabled = false
    var FullScreenActivityActive = false
    var dfuRefreshTryAgain = true
    var connectedDevice = false
    const val EXTRA_DATA = "EXTRA_DATA"
    const val SERVER_NOT_CONN = "SERVER_NOT_CONNECTED"
    const val SERVER_CONN = "SERVER_CONNECTED"
    const val ACTION_ACL_CONNECTED = "hu.aut.android.dm01_v11.action.ACL_CONNECTED"
    const val ACTION_ACL_DISCONNECTED = "hu.aut.android.dm01_v11.action.ACL_DISCONNECTED"
    const val ACTION_SERVICE_DISCOVER_DONE  = "hu.aut.android.dm01_v11.action.SERVICE_DISCOVER_DONE"
    const val ACTION_HEART_RATE_DATA_AVAILABLE = "hu.aut.android.dm01_v11.action.HEART_DATA_AVAILABLE"
    const val ACTION_BATTERY_DATA_AVAILABLE = "hu.aut.android.dm01_v11.action.BATTERY_DATA_AVAILABLE"
    const val ACTION_NORDIC_DATA_AVAILABLE = "hu.aut.android.dm01_v11.action.NORDIC_DATA_AVAILABLE"
    const val ACTION_DEVICEINFO_DATA_AVAILABLE = "hu.aut.android.dm01_v11.action.NORDIC_DATA_AVAILABLE"
    const val DFU_START = "hu.aut.android.dm01_v11.action.DFU_START"
    const val DFU_UPDATE_SUCCESSFULLY = "hu.aut.android.dm01_v11.action.DFU_UPDATE_SUCCESSFULLY"
    const val DFU_UPDATE_ERROR = "hu.aut.android.dm01_v11.action.DFU_UPDATE_ERROR"
    const val DFU_UPDATE_PROGRESS_REFRESH = "hu.aut.android.dm01_v11.action.DFU_UPDATE_PROGRESS_REFRESH"
    const val ACTION_TRIANGLESIGNAL_DATA_AVAILABLE = "hu.aut.android.dm01_v11.action.TRIANGLESIGNAL_DATA_AVAILABLE"
    const val ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON"
    const val ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF"
    const val ACTION_USER_PRESENT = "android.intent.action.USER_PRESENT"


    val BATTERY_SERVICE_UUID: UUID = convertFromInteger(0x180F)
    val BATTERY_LEVEL_CHAR_UUID: UUID = convertFromInteger(0x2A19)
    val HEART_RATE_MEASUREMENT_CHAR_UUID = convertFromInteger(0x2A37)
    val HEART_RATE_SERVICE_UUID: UUID = convertFromInteger(0x180D)

    val DEVICEINFORMATION_SERVICE_UUID: UUID = convertFromInteger(0x180A)
    val GENERIC_ACCESS: UUID = convertFromInteger(0x1800)
    val DEVICENAME :UUID= convertFromInteger(0x2A00)
    val MANUFACTURER_NAME_STRING:UUID= convertFromInteger(0x2A29)
    val HARDWARE_REVISION_STRING:UUID= convertFromInteger(0x2A27)
 // val SOFTWARE_REVISION_STRING:UUID= convertFromInteger(0x2A28)
    val SERIAL_NUMBER_STRING:UUID= convertFromInteger(0x2A25)
    val MODEL_NUMBER_STRING:UUID= convertFromInteger(0x2A24)
    val FIRMWARE_REVISION_STRING:UUID= convertFromInteger(0x2A26)

    const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
    val UUID_CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG)

    val RX_CHAR_UUID: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
    val TX_CHAR_UUID: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")

    val NORDIC_UART_SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    var registredNordicBroadcast = false
    var searchingDeviceList = ArrayList<String?>()
    lateinit var dataMultipleList: Array<MultipleDeviceInfo>
    val BLUETOOTHGATT = "BluetoothGatt"
}