package hu.euronetrt.okoskp.euronethealth.dfuManager.model

import com.google.gson.annotations.SerializedName
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.multipleObjects.CreatedAT
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.multipleObjects.IDObject
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.multipleObjects.ModifiedAt

class FirmwareDeviceTypeResultModel(
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
        var deviceIDFrom_idObj: String,

        @SerializedName("name")
        val modelName: String,

        @SerializedName("vendor")
        val manufacturerName: String,

        @SerializedName("versions")
        val versions: Array<VersionObj>,

        @SerializedName("firmwares")
        val firmwareArray: Array<FirmwareModel>,

        @SerializedName("collectables")
        val collectables: Array<String?>
)