package hu.aut.android.dm01_v11.ui.fragments.userInfo

import com.google.gson.annotations.SerializedName

class EmailObjectModel(
        @SerializedName("address")
        var address: String,
        @SerializedName("verified")
        var verified: Boolean
)