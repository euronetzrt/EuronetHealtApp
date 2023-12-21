package hu.aut.android.dm01_v11.ui.fragments.userInfo

import com.google.gson.annotations.SerializedName

class UserInfoModel (
        @SerializedName("emails")
        var emails: ArrayList<EmailObjectModel>,
        @SerializedName("profile")
        var profile: ProfileObjectModel
)