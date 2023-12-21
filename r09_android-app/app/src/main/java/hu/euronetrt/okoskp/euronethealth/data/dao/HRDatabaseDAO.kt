package hu.euronetrt.okoskp.euronethealth.data.dao

import androidx.room.*
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.HRData
import hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.rabbitMQClasses.HRDataFromDBToRabbitMQ
import io.reactivex.Flowable

@Dao
interface HRDatabaseDAO {

    @Query("SELECT * FROM HRData")
    fun getAll(): Flowable<List<HRData>>

    @Query("SELECT * FROM HRData ORDER BY uid DESC LIMIT 1")
    fun getLast(): Flowable<List<HRData>>

    /*@Query("SELECT * FROM HRData")
    fun getLast(): LiveData<List<HRData>>
*/

    @Query("SELECT " +
            "macId, " +
            "firmwareVersion, " +
            "softwareVersion, " +
            "hardwareVersion, " +
            "deviceId, " +
            "userId, " +
            "HRData.uid AS uid, " +
            "'HR' AS type, " +
            "HRData.measuredAt AS measuredAt, " +
            "HRData.arrivedAt AS arrivedAt, " +
            "(CASE " +
            "when HRData.sentAt IS NULL " +
            "THEN 0 ELSE HRData.sentAt END) AS sentAt, " +
            "HRData.hrValue AS value " +
            "FROM HRData "+
            "WHERE HRData.sentAt IS NULL ")
    fun getDataWaitingToBeSend(): List<HRDataFromDBToRabbitMQ>

    @Transaction
    @Query("UPDATE HRData SET sentAt = :newTime WHERE uid = :itemID")
    fun itemDataSentAtUpdate(newTime: Long, itemID : Long)

    @Query("SELECT * FROM HRData")
    fun findAllItems(): List<HRData>

    @Insert
    fun insertItem(item: HRData): Long  //--> visszaadott long itt lehet a friss uid.

    @Delete
    fun deleteItem(item: HRData)

    @Update
    fun updateItem(item: HRData)

}