package hu.euronetrt.okoskp.euronethealth.data.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import hu.euronetrt.okoskp.euronethealth.data.TableName
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.PPGData

@Dao
interface PPGDatabaseDAO {

    @RawQuery
    fun getTableNames(query: SupportSQLiteQuery) : List<TableName>

    @Transaction
    @Query("SELECT * FROM PPGData")
    fun findAllItemsPPGData(): List<PPGData>

    @Transaction
    @Query("SELECT * FROM PPGData WHERE uid IN (:itemsIds)")
    fun findByIdsItemsPPGData(itemsIds: ArrayList<Long>): List<PPGData>

    @Transaction
    @Query("UPDATE PPGData SET sentAt = :newTime WHERE uid = :itemID")
    fun itemDataSentAtUpdate(newTime: Long, itemID : Long)

    /**
     * updateItemPlaces
     *
     * @param newTime
     * @param itemIDs
     */
    @Query("UPDATE PPGData SET sentAt = :newTime WHERE uid IN (:itemIDs)")
    fun updateItemPlaces(newTime:Long, itemIDs :ArrayList<Long?>)

    @Insert
    fun insertItem(item: PPGData): Long  //--> visszaadott long itt lehet a friss uid.

    @Delete
    fun deleteItem(item: PPGData)

    @Update
    fun updateItem(item: PPGData)
}