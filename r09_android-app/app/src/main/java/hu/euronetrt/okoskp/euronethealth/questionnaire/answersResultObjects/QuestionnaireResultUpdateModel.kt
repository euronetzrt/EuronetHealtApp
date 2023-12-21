package hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects

import com.google.gson.annotations.SerializedName
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.multipleObjects.CreatedAT
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.multipleObjects.IDObject
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.multipleObjects.ModifiedAt

class QuestionnaireResultUpdateModel(
        @SerializedName("_id")
        var _id: IDObject,

        @SerializedName("seq")
        var seq: Int,

        @SerializedName("creator")
        var creator: String,

        @SerializedName("createdAt")
        var createdAt: CreatedAT,

        @SerializedName("modifier")
        var modifier: String,

        @SerializedName("modifiedAt")
        var modifiedAt: ModifiedAt?,

        @SerializedName("active")
        var active: Boolean,

        @SerializedName("id")
        var id: String?,

        @SerializedName("questionnaireId")
        var questionnaireId: String,

        @SerializedName("userId")
        var userId: String,

        @SerializedName("scheduleOrder")
        var scheduleOrder: Int,

        @SerializedName("scheduledAt")
        var scheduledAt: QuestionnaireResultAnswerScheduledAt?,

        @SerializedName("scheduledUntil")
        var scheduledUntil: QuestionnaireResultAnswerScheduledUntil?,

        @SerializedName("state")
        var state: Int,    // QuestionnaireResultSuspend.type

        @SerializedName("startedAt")
        var startedAt: QuestionnaireResultAnswerStartedAt?,

        @SerializedName("finishedAt")
        var finishedAt: QuestionnaireResultAnswerFinishedAt?,

        @SerializedName("sections")
        var sections: ArrayList<QuestionnaireResultUpdateSection?>,

        @SerializedName("suspends")
        var suspends: ArrayList<QuestionnaireResultSuspend>?,

        @SerializedName("created_at")
        var created_at: Long?,

        @SerializedName("modified_at")
        var modified_at: Long?
        )