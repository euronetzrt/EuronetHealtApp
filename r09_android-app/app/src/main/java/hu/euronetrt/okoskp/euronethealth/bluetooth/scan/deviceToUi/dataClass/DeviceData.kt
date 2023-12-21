package hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.dataClass

import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.MultipleDeviceInfo

data class DeviceData(
        var bluetooth_name: String = "",
        var bluetooth_manufacturer: String = "",
        var bluetooth_type: Int = 0,
        var bluetooth_address: String = "",
        var bluetooth_device_Object: Array<MultipleDeviceInfo>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceData

        if (bluetooth_name != other.bluetooth_name) return false
        if (bluetooth_manufacturer != other.bluetooth_manufacturer) return false
        if (bluetooth_type != other.bluetooth_type) return false
        if (bluetooth_address != other.bluetooth_address) return false
        if (bluetooth_device_Object != null) {
            if (other.bluetooth_device_Object == null) return false
            if (!bluetooth_device_Object!!.contentEquals(other.bluetooth_device_Object!!)) return false
        } else if (other.bluetooth_device_Object != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bluetooth_name.hashCode()
        result = 31 * result + bluetooth_manufacturer.hashCode()
        result = 31 * result + bluetooth_type
        result = 31 * result + bluetooth_address.hashCode()
        result = 31 * result + (bluetooth_device_Object?.contentHashCode() ?: 0)
        return result
    }
}