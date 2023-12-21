package hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects

import com.google.gson.annotations.SerializedName

class TemplateAnswerdModel(

        @SerializedName("order")
        var order: Int,

        @SerializedName("text")
        var text: String,

        @SerializedName("image")
        var image: String?,

        @SerializedName("imageSize")
        var imageSize: Int?
)