package hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.dataClass

import android.os.Parcel
import android.os.Parcelable

data class LightDeviceData(
        var bluetooth_name: String = "",
        var bluetooth_address: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(bluetooth_name)
        parcel.writeString(bluetooth_address)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceData

        if (bluetooth_name != other.bluetooth_name) return false
        if (bluetooth_address != other.bluetooth_address) return false
        return true
    }

    override fun hashCode(): Int {
        var result = bluetooth_name.hashCode()
        result = 31 * result + bluetooth_address.hashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<LightDeviceData> {
        override fun createFromParcel(parcel: Parcel): LightDeviceData {
            return LightDeviceData(parcel)
        }

        override fun newArray(size: Int): Array<LightDeviceData?> {
            return arrayOfNulls(size)
        }
    }
}