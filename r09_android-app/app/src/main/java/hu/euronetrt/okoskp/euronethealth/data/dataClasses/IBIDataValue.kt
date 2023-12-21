package hu.euronetrt.okoskp.euronethealth.data.dataClasses

import androidx.room.*

/**
 * IBI Data Value class
 *
 * @property id
 * @property ibiDataId
 * @property value
 */
@Entity(tableName = "IBIDataValue",
        indices = [Index("IBIDataId")],
        foreignKeys = [ForeignKey(entity = IBIData::class,
                parentColumns = arrayOf("uid"),
                childColumns = arrayOf("IBIDataId"))])
class IBIDataValue(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        private var id: Long?,
        @ColumnInfo(name = "IBIDataId")
        private var ibiDataId: Long?,
        @ColumnInfo(name = "Value")
        private var value: Long
){
        fun getId() : Long? = id
        fun getIbiDataId() : Long? = ibiDataId
        fun getValue() : Long = value

        fun setId (id: Long){
                this.id = id
        }

        fun setIbiDataId (ibiDataId: Long){
                this.ibiDataId = ibiDataId
        }

        fun setValue (value: Long){
                this.value = value
        }
}
