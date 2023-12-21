package hu.euronetrt.okoskp.euronethealth.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.IMUDataValue
import hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.rabbitMQClasses.IMUDataFromDBToRabbitMQ
import io.reactivex.Flowable

@Dao
interface IMUValueDatabaseDAO {

    /*Figyelem! az adat stream adatcsomagonként egyszer hívódik, mivel egy insert alatt kerül be az egy adathoz tartozó x  db érték!*/
    @Query("SELECT  IMUDataValue.* FROM IMUDataValue " +
            "where IMUDataValue.IMUDataId = (SELECT uid FROM IMUData order by uid desc limit 1) " +
            "LIMIT 1")
    fun getLast(): Flowable<List<IMUDataValue>>

    @Query("SELECT " +
            "macId, " +
            "firmwareVersion, " +
            "softwareVersion, " +
            "hardwareVersion, " +
            "deviceId, " +
            "userId, " +
            "'IMU' AS type,  " +
            "(CASE " +
            "when IMUDataValue.id IS NULL " +
            "THEN -1 " +
            "ELSE IMUDataValue.id " +
            "END) AS uid, " +
            "IMUData.measuredAt AS measuredAt, " +
            "IMUData.arrivedAt AS arrivedAt, " +
            "(CASE " +
            "when IMUData.sentAt IS NULL " +
            "THEN 0 " +
            "ELSE IMUData.sentAt " +
            "END) AS sentAt, " +
            "IMUDataValue.Accelerometer_X AS acceleroX, " +
            "IMUDataValue.Accelerometer_Y AS acceleroY, " +
            "IMUDataValue.Accelerometer_Z AS acceleroZ, " +
            "IMUDataValue.Gyroscope_X AS gyroscopeX, " +
            "IMUDataValue.Gyroscope_Y AS gyroscopeY, " +
            "IMUDataValue.Gyroscope_Z AS gyroscopeZ, " +
            "IMUDataValue.Magnetometer_X AS magnetometerX, " +
            "IMUDataValue.Magnetometer_Y AS magnetometerY, " +
            "IMUDataValue.Magnetometer_Z AS magnetometerZ, " +
            "IMUData.uid AS parentDataUid " +
            "FROM IMUData " +
            "INNER JOIN IMUDataValue ON (IMUDataValue.IMUDataId =  IMUData.uid) " +
            "WHERE IMUData.uid IN (SELECT uid " +
            "FROM IMUData " +
            "WHERE sentAt IS NULL " +
            "AND uid IN (SELECT IMUDataId " +
            "FROM IMUDataValue) " +
            "ORDER BY uid ASC " +
            "LIMIT 25)")
    fun getDataWaitingToBeSend(): List<IMUDataFromDBToRabbitMQ>

    @Query("SELECT * FROM IMUDataValue")
    fun findAllItemsIMUDataValue(): List<IMUDataValue>

    @Insert
    fun insertItem(item: IMUDataValue): Long  //--> visszaadott long itt lehet a friss uid.

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(item: List<IMUDataValue>)

}