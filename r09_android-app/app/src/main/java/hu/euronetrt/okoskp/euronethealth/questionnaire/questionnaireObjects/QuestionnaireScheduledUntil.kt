package hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects

import com.google.gson.annotations.SerializedName

class QuestionnaireScheduledUntil(
        @SerializedName("\$date")
        var date: Long?
)