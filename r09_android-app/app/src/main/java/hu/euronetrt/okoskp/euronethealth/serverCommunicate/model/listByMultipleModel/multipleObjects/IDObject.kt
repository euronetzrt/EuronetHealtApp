package hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.multipleObjects

import com.google.gson.annotations.SerializedName

class IDObject (
        @SerializedName("_bsontype")
        val _bsontype : String,
        @SerializedName("id")
        val _idObj_idField: IDObjIDField
)