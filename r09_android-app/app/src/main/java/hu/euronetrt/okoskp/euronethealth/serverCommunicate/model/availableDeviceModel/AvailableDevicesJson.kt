package hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.availableDeviceModel

import com.google.gson.annotations.SerializedName

class AvailableDevicesJson (
    @SerializedName("array")
    val addressJson: Array<DeviceInfo>
)