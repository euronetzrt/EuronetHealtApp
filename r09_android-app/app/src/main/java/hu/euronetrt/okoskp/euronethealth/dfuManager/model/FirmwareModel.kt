package hu.euronetrt.okoskp.euronethealth.dfuManager.model

import com.google.gson.annotations.SerializedName

class FirmwareModel(
        @SerializedName("version")
        val version: String,

        @SerializedName("updateScheduledFrom")
        val update_scheduled_from: UpdateScheduledFromObj,

        @SerializedName("updateActive")
        val update_active: Boolean,

        @SerializedName("size")
        val size: Int,

        @SerializedName("filename")
        val filename: String,

        @SerializedName("content")
        val content: String,

        @SerializedName("releaseDate")
        val releaseDate: ReleaseDateObj
)