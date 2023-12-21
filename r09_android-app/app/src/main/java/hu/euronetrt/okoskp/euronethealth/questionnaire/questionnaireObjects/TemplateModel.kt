package hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects

import com.google.gson.annotations.SerializedName

class TemplateModel(
        @SerializedName("_id")
        var _id: TemplateIDObject,
        @SerializedName("seq")
        var seq: String?,
        @SerializedName("creator")
        var creator: String,
        @SerializedName("createdAt")
        var created_at: TemplateCreatedAT,
        @SerializedName("modifiedAt")
        var modified_at: TemplateModifiedAt,
        @SerializedName("modifier")
        var modifier: String,
        @SerializedName("active")
        var active: Boolean,
        @SerializedName("id")
        var id: String,
        @SerializedName("name")
        var name: String,
        @SerializedName("description")
        var description: String?,
        @SerializedName("type")
        var type: Int,
        @SerializedName("repeatable")
        var repeatable: Boolean,
        @SerializedName("required")
        var required: Boolean,
        @SerializedName("cancelOverlapped")
        var cancelOverlapped: Boolean,
        @SerializedName("cancelledUsable")
        var cancelledUsable: Boolean,
        @SerializedName("sections")
        var sections: Array<TemplateSectionModel>
)