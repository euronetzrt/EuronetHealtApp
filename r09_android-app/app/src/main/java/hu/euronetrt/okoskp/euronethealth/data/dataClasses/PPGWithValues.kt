package hu.euronetrt.okoskp.euronethealth.data.dataClasses

import androidx.room.Embedded
import androidx.room.Relation

/**
 * PPG With Values
 *
 * @property rawData
 * @property values
 */
class PPGWithValues (
    @Embedded
    private var rawData : PPGData,
    @Relation(parentColumn = "uid", entityColumn = "PPGDataID", entity = PPGDataValue::class)
    private var values : List<PPGDataValue>
) {
    fun getRawData() : PPGData = rawData
    fun getValues() : List<PPGDataValue> = values

    fun setRawData(rawData : PPGData) {
        this.rawData = rawData
    }
    fun setValues(values : List<PPGDataValue>) {
        this.values = values
    }
}