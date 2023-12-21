package hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects

import com.google.gson.annotations.SerializedName

class TemplateSectionModel(
        @SerializedName("name")
        var name: String,

        @SerializedName("order")
        var order: Int,

        @SerializedName("type")
        var type: Int,

        @SerializedName("required")
        var required: Boolean,

        @SerializedName("resumable")
        var resumable: Boolean,

        @SerializedName("questions")
        var questions: Array<TemplateQuestion>,

        @SerializedName("likertAnswers")
        var likertAnswers : Array<TemplateSectionLikertAnswer?>
)