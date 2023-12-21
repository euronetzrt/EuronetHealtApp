package hu.euronetrt.okoskp.euronethealth.data.dataClasses

import androidx.room.*

/**
 * IMU Data Value
 *
 * @property id
 * @property imuDataId
 * @property accelerometer_x
 * @property accelerometer_y
 * @property accelerometer_z
 * @property gyroscope_x
 * @property gyroscope_y
 * @property gyroscope_z
 * @property magnetometer_x
 * @property magnetometer_y
 * @property magnetometer_z
 */
@Entity(tableName = "IMUDataValue",
        indices = [Index("IMUDataId")],
        foreignKeys = [ForeignKey(entity = IMUData::class,
                parentColumns = arrayOf("uid"),
                childColumns = arrayOf("IMUDataId"))])
class IMUDataValue(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        private var id: Long?,
        @ColumnInfo(name = "IMUDataId")
        private var imuDataId: Long?,
        @ColumnInfo(name = "Accelerometer_X")
        private var accelerometer_x: Long,
        @ColumnInfo(name = "Accelerometer_Y")
        private var accelerometer_y: Long,
        @ColumnInfo(name = "Accelerometer_Z")
        private var accelerometer_z: Long,
        @ColumnInfo(name = "Gyroscope_X")
        private var gyroscope_x: Long,
        @ColumnInfo(name = "Gyroscope_Y")
        private var gyroscope_y: Long,
        @ColumnInfo(name = "Gyroscope_Z")
        private var gyroscope_z: Long,
        @ColumnInfo(name = "Magnetometer_X")
        private var magnetometer_x: Long,
        @ColumnInfo(name = "Magnetometer_Y")
        private var magnetometer_y: Long,
        @ColumnInfo(name = "Magnetometer_Z")
        private var magnetometer_z: Long
) {
    fun getId(): Long? = id
    fun getImuDataId(): Long? = imuDataId

    fun getAccelerometer_x() : Long? = accelerometer_x
    fun getAccelerometer_y() : Long? = accelerometer_y
    fun getAccelerometer_z() : Long? = accelerometer_z

    fun getGyroscope_x() : Long? = gyroscope_x
    fun getGyroscope_y() : Long? = gyroscope_y
    fun getGyroscope_z() : Long? = gyroscope_z

    fun getMagnetometer_x() : Long? = magnetometer_x
    fun getMagnetometer_y() : Long? = magnetometer_y
    fun getMagnetometer_z() : Long? = magnetometer_z

    fun setId(id: Long) {
        this.id = id
    }

    fun setImuDataId(imuDataId: Long) {
        this.imuDataId = imuDataId
    }

    fun setAccelerometer_x(accelerometer_x :Long){
        this.accelerometer_x = accelerometer_x
    }

    fun setAccelerometer_y(accelerometer_y :Long){
        this.accelerometer_y = accelerometer_y
    }
    fun setAccelerometer_z(accelerometer_z: Long){
        this.accelerometer_z = accelerometer_z
    }

    fun setGyroscope_x(gyroscope_x : Long){
        this.gyroscope_x = gyroscope_x
    }
    fun setGyroscope_y(gyroscope_y: Long){
        this.gyroscope_y = gyroscope_y
    }
    fun setGyroscope_z(gyroscope_z: Long){
        this.gyroscope_z = gyroscope_z
    }

    fun setMagnetometer_x(magnetometer_x: Long){
        this.magnetometer_x = magnetometer_x
    }
    fun setMagnetometer_y(magnetometer_y: Long){
        this.magnetometer_y = magnetometer_y
    }
    fun setMagnetometer_z(magnetometer_z: Long){
        this.magnetometer_z = magnetometer_z
    }
}
