package hu.euronetrt.okoskp.euronethealth.data

import androidx.room.ColumnInfo

data class TableName (
    @ColumnInfo(name = "name")
    val name : String?
)