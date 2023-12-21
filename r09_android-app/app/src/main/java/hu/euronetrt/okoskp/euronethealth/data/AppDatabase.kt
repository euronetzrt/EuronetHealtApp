package hu.euronetrt.okoskp.euronethealth.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import hu.euronetrt.okoskp.euronethealth.data.dao.*
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.*

@Database(entities = [
    PPGData::class,
    PPGDataValue::class,
    IBIData::class,
    IBIDataValue::class,
    HRData::class,
    IMUData::class,
    IMUDataValue::class,
    OTHERData::class,
    StepData::class],
        version = 1,
        exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ppgDatabaseDao(): PPGDatabaseDAO
    abstract fun hrDatabaseDao(): HRDatabaseDAO
    abstract fun ibiDatabaseDao(): IBIDatabaseDAO
    abstract fun ppgValueDatabaseDao(): PpgValueDatabaseDAO
    abstract fun ibiValueDatabaseDao(): IBIValueDatabaseDAO
    abstract fun imuValueDatabaseDao(): IMUValueDatabaseDAO
    abstract fun imuDatabaseDao(): IMUDatabaseDAO
    abstract fun otherDatabaseDao(): OTHERDatabaseDAO
    abstract fun stepDatabaseDao(): StepDatabaseDAO

    companion object {
        private var INSTANCE: AppDatabase? = null

        /*https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929 Migration help!
        * https://www.sqlite.org/draft/datatype3.html
        * */

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new table
                database.execSQL(
                        "CREATE TABLE  IBIData (uid INTEGER PRIMARY KEY AUTOINCREMENT, type INTEGER, measuredAt INTEGER NOT NULL,arrivedAt INTEGER NOT NULL,sendAt INTEGER NOT NULL ,value BLOB)")
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new table with foreign key
                database.execSQL(
                        "CREATE TABLE  PPGDataValue (id INTEGER PRIMARY KEY AUTOINCREMENT, PPGDataId INTEGER, Value INTEGER NOT NULL, FOREIGN KEY (PPGDataId) REFERENCES PPGData (uid))")
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //Create new table
                database.execSQL(
                        "CREATE TABLE IBIDataValue (id INTEGER PRIMARY KEY AUTOINCREMENT,IBIDataId INTEGER , Value INTEGER NOT NULL, FOREIGN KEY (IBIDataId) REFERENCES IBIData (uid))")
            }
        }

        val MIGRATION_4_6: Migration = object : Migration(4, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_IBIDataValue_IBIDataId ON IBIDataValue (IBIDataId)"
                )
            }
        }
        val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_PPGDataValue_PPGDataId ON PPGDataValue (PPGDataId)"
                )
            }
        }

        val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new table
                database.execSQL(
                        "CREATE TABLE  IMUData (uid INTEGER PRIMARY KEY AUTOINCREMENT, type INTEGER, measuredAt INTEGER NOT NULL,arrivedAt INTEGER NOT NULL,sendAt INTEGER NOT NULL ,value BLOB)")
            }
        }
         val MIGRATION_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //Create new table
                database.execSQL(
                        "CREATE TABLE IMUDataValue (id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                                "IMUDataId INTEGER," +
                                "Accelerometer_X INTEGER NOT NULL," +
                                "Accelerometer_Y INTEGER NOT NULL," +
                                "Accelerometer_Z INTEGER NOT NULL," +
                                "Gyroscope_X INTEGER NOT NULL," +
                                "Gyroscope_Y INTEGER NOT NULL," +
                                "Gyroscope_Z INTEGER NOT NULL," +
                                "Magnetometer_X INTEGER NOT NULL," +
                                "Magnetometer_Y INTEGER NOT NULL," +
                                "Magnetometer_Z INTEGER NOT NULL," +
                                "FOREIGN KEY (IMUDataId) REFERENCES IMUData (uid))")
            }
        }

        val MIGRATION_9_10: Migration = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                        "CREATE TABLE  OTHERData (uid INTEGER PRIMARY KEY AUTOINCREMENT, type INTEGER, measuredAt INTEGER NOT NULL,arrivedAt INTEGER NOT NULL,sendAt INTEGER NOT NULL ,value BLOB)")
            }
        }

        val MIGRATION_10_11: Migration = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_IMUDataValue_IMUDataId ON IMUDataValue (IMUDataId)"
                )
            }
        }

        val MIGRATION_11_12: Migration = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new table
                database.execSQL(
                        "CREATE TABLE  StepData (uid INTEGER PRIMARY KEY AUTOINCREMENT, type INTEGER, measuredAt INTEGER NOT NULL,arrivedAt INTEGER NOT NULL,sendAt INTEGER NOT NULL ,value INTEGER)")
            }
        }

        val MIGRATION_12_13: Migration = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_PPGDataValue_PPGDataId ON PPGDataValue (PPGDataId)"
                )
            }
        }
        val MIGRATION_13_14: Migration = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                        "ALTER TABLE StepData ADD COLUMN stepTimestamp INTEGER NOT NULL"
                )
            }
        }

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "euronetDatabase.db")
                        .addMigrations(MIGRATION_1_2)
                        .addMigrations(MIGRATION_2_3)
                        .addMigrations(MIGRATION_3_4)
                        .addMigrations(MIGRATION_4_6)
                        .addMigrations(MIGRATION_6_7)
                        .addMigrations(MIGRATION_7_8)
                        .addMigrations(MIGRATION_8_9)
                        .addMigrations(MIGRATION_9_10)
                        .addMigrations(MIGRATION_10_11)
                        .addMigrations(MIGRATION_11_12)
                        .addMigrations(MIGRATION_12_13)
                        .addMigrations(MIGRATION_13_14)
                        .build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

    fun getHRListStream(): LiveData<List<HRData>> {
        return LiveDataReactiveStreams.fromPublisher(hrDatabaseDao().getAll())
    }

    fun getHRLastListStream(): LiveData<List<HRData>> {
        return LiveDataReactiveStreams.fromPublisher(hrDatabaseDao().getLast())
    }

    fun getPPGValueListStream(): LiveData<List<PPGDataValue>> {
        return LiveDataReactiveStreams.fromPublisher(ppgValueDatabaseDao().getAll())
    }

    /*fun getPPGValueDataWaitingToBeSendListStream(): LiveData<List<PPGDataFromDBToRabbitMQ>> {
       return LiveDataReactiveStreams.fromPublisher(ppgValueDatabaseDao().getDataWaitingToBe())
    }*/

    fun getIBIValueListStream(): LiveData<List<IBIDataValue>> {
        return LiveDataReactiveStreams.fromPublisher(ibiValueDatabaseDao().getAll())
    }

    fun getIMUValueListStream(): LiveData<List<IMUDataValue>> {
        return LiveDataReactiveStreams.fromPublisher(imuValueDatabaseDao().getLast())
    }

    fun getStepListStream(): LiveData<List<StepData>> {
        return LiveDataReactiveStreams.fromPublisher(stepDatabaseDao().getAll())
    }
    fun getStepLastStream(): LiveData<List<StepData>> {
        return LiveDataReactiveStreams.fromPublisher(stepDatabaseDao().getLast())
    }
}
