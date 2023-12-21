package hu.euronetrt.okoskp.euronethealth.bluetooth

/**
 * Bluetooth Service NotificationT ype
 *
 * @property type
 */
enum class BluetoothServiceNotificationType (val type : Int) {
    BATTERY_SERVICE (1),
    HEARTRATE_SERVICE (2),
    NORDIC_SERVICE (3),
    DEVICEINFO_SERVICE (4)
}