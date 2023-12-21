package hu.aut.android.dm01_v11.ui.fragments.userInfo

import com.google.gson.annotations.SerializedName

class ProfileObjectModel(
        @SerializedName("firstName")
        var firstName: String,
        @SerializedName("lastName")
        var lastName: String,
        @SerializedName("title")
        var title : String?,
        @SerializedName("displayName")
        var displayName: String,
        @SerializedName("birthday")
        var birthday: String,
        @SerializedName("sex")
        var sex: String,
        @SerializedName("lang")
        var lang: String
)