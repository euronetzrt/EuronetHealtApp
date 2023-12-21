package hu.euronetrt.okoskp.euronethealth.dfuManager.model

import com.google.gson.annotations.SerializedName

class FirmwareArrayModel(
    @SerializedName("firmwares")
    val firmwareArray: Array<FirmwareModel>
)