package hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects

import com.google.gson.annotations.SerializedName
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.multipleObjects.IDObject


class QuestionnaireGetModel(
        @SerializedName("_id")
        var _id: IDObject,

        @SerializedName("seq")
        var seq: Int,

        @SerializedName("creator")
        var creator: String,

        @SerializedName("createdAt")
        var created_at: QuestionnaireGetDateModel,

        @SerializedName("modifier")
        var modifier: String,

        @SerializedName("modifiedAt")
        var modified_at: QuestionnaireGetDateModel?,

        @SerializedName("active")
        var active: Boolean,

        @SerializedName("id")
        var id: String,

        @SerializedName("template")
        var template: TemplateModel,

        @SerializedName("state")
        var state : Int,    // QuestionnaireResultSuspend.type

        @SerializedName("name")
        var name: String,

        @SerializedName("description")
        var description: String,

        @SerializedName("anonym")
        var anonym: Boolean,

        @SerializedName("activeFrom")
        var activeFrom: QuestionnaireGetDateModel,

        @SerializedName("activeTo")
        var activeTo: QuestionnaireGetDateModel,

        @SerializedName("userGroups")
        var userGroups: Array<Any?>,

        @SerializedName("scheduled")
        var scheduled: Boolean,

        @SerializedName("schedules")
        var schedules: Array<SchedulesModel?>
)