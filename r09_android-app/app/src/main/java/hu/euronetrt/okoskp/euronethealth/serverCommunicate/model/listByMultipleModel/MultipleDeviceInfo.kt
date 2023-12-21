package hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel

import com.google.gson.annotations.SerializedName
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.multipleObjects.*

class MultipleDeviceInfo(
        @SerializedName("_id")
        val _id: IDObject,

        @SerializedName("seq")
        val seq: Int,

        @SerializedName("creator")
        val creator: String,

        @SerializedName("createdAt")
        val created_at: CreatedAT,

        @SerializedName("modifier")
        val modifier: String,

        @SerializedName("modifiedAt")
        val modified_at: ModifiedAt,

        @SerializedName("active")
        val active: String,

        @SerializedName("id")
        var id: String,


        @SerializedName("devicetypeId")
        val devicetype_id: String,

        @SerializedName("serialnumber")
        val serialnumber: String,

        @SerializedName("hwMacId")
        val hw_mac_id: String,

        @SerializedName("currentFirmwareVersion")
        val current_firmware_version: String,

        @SerializedName("maximum")
        var maximum: Int,

        @SerializedName("firmwareHistory")
        val firmware_history: Array<FirmwareHistory>,

        @SerializedName("userHistory")
        val user_history: Array<UserHistory?>,

        @SerializedName("version")
        val version: String,

        @SerializedName("assignedToUser")
        var assignedToUser: Boolean,

        @SerializedName("current_user_id")
        var current_user_id: String?
)