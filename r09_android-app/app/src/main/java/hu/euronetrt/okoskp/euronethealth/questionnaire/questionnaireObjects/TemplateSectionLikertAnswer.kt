package hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects

import com.google.gson.annotations.SerializedName


class TemplateSectionLikertAnswer(
        @SerializedName("value")
        var value: Int,

        @SerializedName("text")
        var text: String?
)