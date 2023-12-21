package hu.euronetrt.okoskp.euronethealth.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.PPGDataValue
import hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.rabbitMQClasses.PPGDataFromDBToRabbitMQ
import io.reactivex.Flowable

@Dao
interface PpgValueDatabaseDAO {

    /*Figyelem! az adat stream adatcsomagonként egyszer hívódik, mivel egy insert alatt kerül be az egy adathoz tartozó x  db érték!*/
    @Query("SELECT  PPGDataValue.* FROM PPGDataValue " +
            "where PPGDataValue.PPGDataId = (SELECT uid FROM PPGData order by uid desc limit 1)")
    fun getAll(): Flowable<List<PPGDataValue>>

    @Query("SELECT " +
            "macId, " +
            "firmwareVersion, " +
            "softwareVersion, " +
            "hardwareVersion, " +
            "deviceId, " +
            "userId, " +
            "'PPG' AS type,  " +
            "(CASE " +
            "when PPGDataValue.id IS NULL " +
            "THEN -1 " +
            "ELSE PPGDataValue.id " +
            "END) AS uid, " +
            "PPGData.measuredAt AS measuredAt, " +
            "PPGData.arrivedAt AS arrivedAt, " +
            "(CASE " +
            "when PPGData.sentAt IS NULL " +
            "THEN 0 " +
            "ELSE PPGData.sentAt " +
            "END) AS sentAt, " +
            "PPGDataValue.Value AS value, " +
            "PPGData.uid AS parentDataUid " +
            "FROM PPGData  " +
            "INNER JOIN PPGDataValue ON (PPGDataValue.PPGDataId =  PPGData.uid) " +
            "WHERE PPGData.uid IN (SELECT uid " +
            "FROM PPGData " +
            "WHERE sentAt IS NULL " +
            "AND uid IN (SELECT PPGDataId " +
            "FROM PPGDataValue) " +
            "ORDER BY uid ASC " +
            "LIMIT 25)")
    fun getDataWaitingToBeSend(): List<PPGDataFromDBToRabbitMQ>


    @Query("SELECT * FROM PPGDataValue")
    fun findAllItemsPPGDataValue(): List<PPGDataValue>

    @Insert
    fun insertItem(item: PPGDataValue): Long  //--> visszaadott long itt lehet a friss uid.

    /**
     * insertAll fun in PpgValueDatabaseDAO interface
     *
     * @param item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(item: List<PPGDataValue>)
}