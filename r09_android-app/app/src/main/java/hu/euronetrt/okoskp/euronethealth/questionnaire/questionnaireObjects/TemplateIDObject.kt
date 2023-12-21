package hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects

import com.google.gson.annotations.SerializedName

class TemplateIDObject(
        @SerializedName("_bsontype")
        var _bsontype: String,
        @SerializedName("id")
        var _idObj_idField: TemplateIDObjIDField
)