package hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects

import com.google.gson.annotations.SerializedName

class QuestionnaireResultAnswerScheduledAt(
        @SerializedName("\$date")
        var date: Long?
)