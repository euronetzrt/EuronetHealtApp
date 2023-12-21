package hu.euronetrt.okoskp.euronethealth.data.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import hu.euronetrt.okoskp.euronethealth.data.TableName
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.OTHERData

@Dao
interface OTHERDatabaseDAO {

    @RawQuery
    fun getTableNames(query: SupportSQLiteQuery) : List<TableName>

    @Transaction
    @Query("SELECT * FROM OTHERData")
    fun findAllItems(): List<OTHERData>

    @Insert
    fun insertItem(item: OTHERData): Long  //--> visszaadott long itt lehet a friss uid.

    @Delete
    fun deleteItem(item: OTHERData)

    @Update
    fun updateItem(item: OTHERData)
}