package hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects

import com.google.gson.annotations.SerializedName

class TemplateQuestion(
        @SerializedName("order")
        var order: Int,

        @SerializedName("type")
        var type: Int,

        @SerializedName("text")
        var text: String,

        @SerializedName("image")
        var image: String?,

        @SerializedName("answerMaxLength")
        var answerMaxLength: Int,

        @SerializedName("answerMultiline")
        var answerMultiline: Boolean,

        @SerializedName("answers")
        var answers: Array<TemplateAnswerdModel?>
)