package hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects

import com.google.gson.annotations.SerializedName

class QuestionnaireResultForServer(

        @SerializedName("seq")
        var seq: Int,

        @SerializedName("creator")
        var creator: String,

        @SerializedName("createdAt")
        var created_at: Long?,//CreatedAT,

        @SerializedName("modifier")
        var modifier: String,

        @SerializedName("modifiedAt")
        var modified_at: Long?,//ModifiedAt?,

        @SerializedName("active")
        var active: Boolean,

        @SerializedName("id")
        var id: String,

        @SerializedName("questionnaireId")
        var questionnaireId: String,

        @SerializedName("userId")
        var userId: String, //sha

        @SerializedName("scheduleOrder")
        var scheduleOrder: Int,

        @SerializedName("scheduledAt")
        var scheduledAt: Long?,//QuestionnaireResultAnswerScheduledAt?,

        @SerializedName("scheduledUntil")
        var scheduledUntil: Long?,//QuestionnaireResultAnswerScheduledUntil?,

        @SerializedName("state")
        var state : Int,    // QuestionnaireResultSuspend.type

        @SerializedName("startedAt")
        var startedAt: Long?,// QuestionnaireResultAnswerStartedAt?,

        @SerializedName("finishedAt")
        var finishedAt: Long?,//QuestionnaireResultAnswerFinishedAt?,

        @SerializedName("sections")
        var sections : ArrayList<QuestionnaireResultSection>,

        @SerializedName("suspends")
        var suspends : ArrayList<QuestionnaireResultSuspend>?
)