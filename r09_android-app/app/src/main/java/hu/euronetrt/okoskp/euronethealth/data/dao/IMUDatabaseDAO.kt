package hu.euronetrt.okoskp.euronethealth.data.dao

import androidx.room.*
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.IMUData
import io.reactivex.Flowable

@Dao
interface IMUDatabaseDAO {

    @Query("SELECT * FROM IMUData")
    fun getAll(): Flowable<List<IMUData>>

    /*@Query("SELECT * FROM HRData")
    fun getLast(): LiveData<List<HRData>>
*/
    @Query("SELECT * FROM IMUData")
    fun findAllItems(): List<IMUData>

    @Transaction
    @Query("UPDATE IMUData SET sentAt = :newTime WHERE uid = :itemID")
    fun itemDataSentAtUpdate(newTime: Long, itemID : Long)

    @Insert
    fun insertItem(item: IMUData): Long  //--> visszaadott long itt lehet a friss uid.

    @Delete
    fun deleteItem(item: IMUData)

    @Update
    fun updateItem(item: IMUData)
}
