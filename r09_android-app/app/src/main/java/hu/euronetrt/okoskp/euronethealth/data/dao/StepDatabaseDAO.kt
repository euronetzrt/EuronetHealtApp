package hu.euronetrt.okoskp.euronethealth.data.dao

import androidx.room.*
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.StepData
import hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.rabbitMQClasses.STEPDataFromDBToRabbitMQ
import io.reactivex.Flowable

@Dao
interface StepDatabaseDAO {

    @Query("SELECT * FROM StepData")
    fun getAll(): Flowable<List<StepData>>

    @Query("SELECT * FROM StepData  ORDER BY uid DESC LIMIT 1")
    fun getLastOne(): StepData

    @Query("SELECT * FROM StepData  ORDER BY uid DESC LIMIT 1")
    fun getLast(): Flowable<List<StepData>>

    @Query("SELECT " +
            "macId, " +
            "firmwareVersion, " +
            "softwareVersion, " +
            "hardwareVersion, " +
            "deviceId, " +
            "userId, " +
            "STEPData.uid AS uid, " +
            "'STEP' AS type, " +
            "STEPData.measuredAt AS measuredAt, " +
            "STEPData.arrivedAt AS arrivedAt, " +
            "(CASE " +
            "when STEPData.sentAt IS NULL " +
            "THEN 0 ELSE STEPData.sentAt END) AS sentAt, " +
            "STEPData.stepValue AS value " +
            "FROM STEPData " +
            "WHERE STEPData.sentAt IS NULL ")
    fun getDataWaitingToBeSend(): List<STEPDataFromDBToRabbitMQ>

    @Transaction
    @Query("UPDATE STEPData SET sentAt = :newTime WHERE uid = :itemID")
    fun itemDataSentAtUpdate(newTime: Long, itemID: Long)

    /*@Query("SELECT * FROM HRData")
    fun getLast(): LiveData<List<HRData>>
*/
    @Query("SELECT * FROM StepData")
    fun findAllItems(): List<StepData>

    @Insert
    fun insertItem(item: StepData): Long  //--> visszaadott long itt lehet a friss uid.

    @Delete
    fun deleteItem(item: StepData)

    @Update
    fun updateItem(item: StepData)
}
