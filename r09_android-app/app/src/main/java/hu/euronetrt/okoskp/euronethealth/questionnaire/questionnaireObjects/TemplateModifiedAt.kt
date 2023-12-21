package hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects

import com.google.gson.annotations.SerializedName

class TemplateModifiedAt(
        @SerializedName("\$date")
        var date: Long
)