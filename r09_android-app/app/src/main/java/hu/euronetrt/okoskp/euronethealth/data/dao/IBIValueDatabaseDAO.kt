package hu.euronetrt.okoskp.euronethealth.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.IBIDataValue
import hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.rabbitMQClasses.IBIDataFromDBToRabbitMQ
import io.reactivex.Flowable

@Dao
interface IBIValueDatabaseDAO {

    /*Figyelem! az adat stream adatcsomagonként egyszer hívódik, mivel egy insert alatt kerül be az egy adathoz tartozó x  db érték!*/
    @Query("SELECT  IBIDataValue.* FROM IBIDataValue \n" +
            " where IBIDataValue.IBIDataId = (SELECT uid FROM IBIData order by uid desc limit 1) LIMIT 1")
    fun getAll(): Flowable<List<IBIDataValue>>


    @Query("SELECT " +
            "macId, " +
            "firmwareVersion, " +
            "softwareVersion, " +
            "hardwareVersion, " +
            "deviceId, " +
            "userId, " +
            "'IBI' AS type,  " +
            "(CASE " +
            "when IBIDataValue.id IS NULL " +
            "THEN -1 " +
            "ELSE IBIDataValue.id " +
            "END) AS uid, " +
            "IBIData.measuredAt AS measuredAt, " +
            "IBIData.arrivedAt AS arrivedAt, " +
            "(CASE " +
            "when IBIData.sentAt IS NULL " +
            "THEN 0 " +
            "ELSE IBIData.sentAt " +
            "END) AS sentAt, " +
            "IBIDataValue.Value AS value, " +
            "IBIData.uid AS parentDataUid " +
            "FROM IBIData  " +
            "INNER JOIN IBIDataValue ON (IBIDataValue.IBIDataId =  IBIData.uid) " +
            "WHERE IBIData.uid IN (SELECT uid " +
            "FROM IBIData " +
            "WHERE sentAt IS NULL " +
            "AND uid IN (SELECT IBIDataId " +
            "FROM IBIDataValue) " +
            "ORDER BY uid ASC " +
            "LIMIT 25)")
    fun getDataWaitingToBeSend(): List<IBIDataFromDBToRabbitMQ>

    @Query("SELECT * FROM IBIDataValue")
    fun findAllItemsIBIDataValue(): List<IBIDataValue>

    @Insert
    fun insertItem(item: IBIDataValue): Long  //--> visszaadott long itt lehet a friss uid.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(item: List<IBIDataValue>)
}
