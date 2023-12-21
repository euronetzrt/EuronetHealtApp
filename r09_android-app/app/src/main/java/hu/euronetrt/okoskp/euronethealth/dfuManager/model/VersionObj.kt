package hu.euronetrt.okoskp.euronethealth.dfuManager.model

import com.google.gson.annotations.SerializedName

class VersionObj(
        @SerializedName("version")
        val version: String,
        @SerializedName("active")
        val active: Boolean,
        @SerializedName("imageContent")
        val imageContent: String
)