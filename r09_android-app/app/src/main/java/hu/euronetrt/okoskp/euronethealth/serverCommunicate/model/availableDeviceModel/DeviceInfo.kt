package hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.availableDeviceModel

import com.google.gson.annotations.SerializedName

class DeviceInfo(
        @SerializedName("name")
        val modelName: String,
        @SerializedName("vendor")
        val manufacturerName: String
)