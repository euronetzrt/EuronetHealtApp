package hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.multipleObjects

import com.google.gson.annotations.SerializedName

class FirmwareHistory (
    @SerializedName("firmwareVersion")
    val firmware_version: String,

    @SerializedName("upgradeAt")
    val upgrade_at: UpgradeAt
)