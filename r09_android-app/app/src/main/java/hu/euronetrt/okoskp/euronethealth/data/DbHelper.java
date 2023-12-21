package hu.euronetrt.okoskp.euronethealth.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.strictmode.InstanceCountViolation;

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper INSTANCE;

    private DbHelper(Context context, String name) {
        super(context, name, null, 1);
    }

    public static DbHelper getInstance(Context context, String name) {
        if (INSTANCE == null) {
            INSTANCE = new DbHelper(context, name);
        }
        return INSTANCE;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE PPGData(uid INTEGER PRIMARY KEY AUTOINCREMENT, macId VARCHAR(100), firmwareVersion VARCHAR(50), softwareVersion VARCHAR(50), hardwareVersion VARCHAR(50), deviceId VARCHAR(50), userId VARCHAR(50), measuredAt INTEGER, arrivedAt INTEGER, sentAt INTEGER, value BLOB)");
        db.execSQL("CREATE TABLE PPGDataValue(id INTEGER PRIMARY KEY AUTOINCREMENT, PPGDataId INTEGER, Value INTEGER NOT NULL, FOREIGN KEY (PPGDataId) REFERENCES PPGData (uid))");

        db.execSQL("CREATE TABLE HRData(uid INTEGER PRIMARY KEY AUTOINCREMENT, macId VARCHAR(100), firmwareVersion VARCHAR(50), softwareVersion VARCHAR(50), hardwareVersion VARCHAR(50), deviceId VARCHAR(50), userId VARCHAR(50), measuredAt INTEGER, arrivedAt INTEGER, sentAt INTEGER, value BLOB)");

        db.execSQL("CREATE TABLE IBIData(uid INTEGER PRIMARY KEY AUTOINCREMENT, macId VARCHAR(100), firmwareVersion VARCHAR(50), softwareVersion VARCHAR(50), hardwareVersion VARCHAR(50), deviceId VARCHAR(50), userId VARCHAR(50), measuredAt INTEGER, arrivedAt INTEGER, sentAt INTEGER, value BLOB)");
        db.execSQL("CREATE TABLE IBIDataValue(id INTEGER PRIMARY KEY AUTOINCREMENT, IBIDataId INTEGER, Value INTEGER NOT NULL, FOREIGN KEY (IBIDataId) REFERENCES IBIData (uid))");

        db.execSQL("CREATE TABLE StepData(uid INTEGER PRIMARY KEY AUTOINCREMENT, macId VARCHAR(100), firmwareVersion VARCHAR(50), softwareVersion VARCHAR(50), hardwareVersion VARCHAR(50), deviceId VARCHAR(50), userId VARCHAR(50), measuredAt INTEGER, arrivedAt INTEGER, sentAt INTEGER, value BLOB)");

        db.execSQL("CREATE TABLE IMUData(uid INTEGER PRIMARY KEY AUTOINCREMENT, macId VARCHAR(100), firmwareVersion VARCHAR(50), softwareVersion VARCHAR(50), hardwareVersion VARCHAR(50), deviceId VARCHAR(50), userId VARCHAR(50), measuredAt INTEGER, arrivedAt INTEGER, sentAt INTEGER, value BLOB)");
        db.execSQL("CREATE TABLE IMUDataValue(id INTEGER PRIMARY KEY AUTOINCREMENT, IMUDataId INTEGER, Accelerometer_X LONG NOT NULL, Accelerometer_Y LONG NOT NULL, Accelerometer_Z LONG NOT NULL, Gyroscope_X LONG NOT NULL, Gyroscope_Y LONG NOT NULL, Gyroscope_Z LONG NOT NULL, FOREIGN KEY (IMUDataId) REFERENCES IMUData (uid))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
