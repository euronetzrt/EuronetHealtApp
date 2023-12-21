package hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.multipleObjects

import com.google.gson.annotations.SerializedName

class UserHistory (
        @SerializedName("userName")
        val userName: String?,
    @SerializedName("userId")
    val user_id: String,
    @SerializedName("pairedAt")
    val paired_at: PairedAt?,
    @SerializedName("releasedAt")
    val released_at: ReleasedAt?
)