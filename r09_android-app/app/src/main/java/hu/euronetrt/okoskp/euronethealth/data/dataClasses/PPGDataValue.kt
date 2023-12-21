package hu.euronetrt.okoskp.euronethealth.data.dataClasses

import androidx.room.*

/**
 * PPG Data Value
 *
 * @property id
 * @property ppgDataId
 * @property value
 */
@Entity(tableName = "PPGDataValue",
        indices = [Index("PPGDataId")],
        foreignKeys = [ForeignKey(entity = PPGData::class,
                parentColumns = arrayOf("uid"),
                childColumns = arrayOf("PPGDataId"))])
class PPGDataValue(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        private var id: Long?,
        @ColumnInfo(name = "PPGDataId")
        private var ppgDataId: Long?,
        @ColumnInfo(name = "Value")
        private var value: Long
){
        fun getId() : Long? = id
        fun getPpgDataId() : Long? = ppgDataId
        fun getValue() : Long = value

        fun setId (id: Long){
                this.id = id
        }

        fun setPpgDataId (ppgDataId: Long){
                this.ppgDataId = ppgDataId
        }

        fun setValue (value: Long){
                this.value = value
        }
}
