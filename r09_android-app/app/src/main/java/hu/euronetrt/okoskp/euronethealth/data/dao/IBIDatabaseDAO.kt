package hu.euronetrt.okoskp.euronethealth.data.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import hu.euronetrt.okoskp.euronethealth.data.TableName
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.IBIData

@Dao
interface IBIDatabaseDAO {

    @RawQuery
    fun getTableNames(query: SupportSQLiteQuery) : List<TableName>

    @Transaction
    @Query("SELECT * FROM IBIData")
    fun findAllItems(): List<IBIData>

    @Transaction
    @Query("UPDATE IBIData SET sentAt = :newTime WHERE uid = :itemID")
    fun itemDataSentAtUpdate(newTime: Long, itemID : Long)

    @Insert
    fun insertItem(item: IBIData): Long  //--> visszaadott long itt lehet a friss uid.

    @Delete
    fun deleteItem(item: IBIData)

    @Update
    fun updateItem(item: IBIData)
}