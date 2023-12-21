package hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.rabbitMQClasses

/**
 * IMU Data From DB To RabbitMQ
 *
 * @property parentDataUid
 * @property acceleroX
 * @property acceleroY
 * @property acceleroZ
 * @property gyroscopeX
 * @property gyroscopeY
 * @property gyroscopeZ
 * @property magnetometerX
 * @property magnetometerY
 * @property magnetometerZ
 *
 * @constructor
 * @param macId
 * @param firmwareVersion
 * @param softwareVersion
 * @param hardwareVersion
 * @param deviceId
 * @param userId
 * @param type
 * @param uid
 * @param measuredAt
 * @param arrivedAt
 * @param sentAt
 */
class IMUDataFromDBToRabbitMQ(
        macId: String,
        firmwareVersion: String,
        softwareVersion: String,
        hardwareVersion: String,
        deviceId: String,
        userId: String,
        type: String,
        uid: Long,
        measuredAt:Long,
        arrivedAt: Long,
        sentAt: Long,
        private var parentDataUid: Long,
        private var acceleroX: Long,
        private var acceleroY: Long,
        private var acceleroZ: Long,
        private var gyroscopeX: Long,
        private var gyroscopeY: Long,
        private var gyroscopeZ: Long,
        private var magnetometerX: Long,
        private var magnetometerY: Long,
        private var magnetometerZ: Long
) : AbstractDataRabbitMQ(
        macId,
        firmwareVersion,
        softwareVersion,
        hardwareVersion,
        deviceId,
        userId,
        type,
        uid,
        measuredAt,
        arrivedAt,
        sentAt) {

    fun getAcceleroX(): Long = acceleroX

    fun setAcceleroX(acceleroX: Long) {
        this.acceleroX = acceleroX
    }

    fun getAcceleroY(): Long = acceleroY

    fun setAcceleroY(acceleroY: Long) {
        this.acceleroY = acceleroY
    }

    fun getAcceleroZ(): Long = acceleroZ

    fun setAcceleroZ(acceleroZ: Long) {
        this.acceleroZ = acceleroZ
    }

    fun getGyroscopeX(): Long = gyroscopeX

    fun setGyroscopeX(gyroscopeX: Long) {
        this.gyroscopeX = gyroscopeX
    }

    fun getGyroscopeY(): Long = gyroscopeY

    fun setGyroscopeY(gyroscopeY: Long) {
        this.gyroscopeY = gyroscopeY
    }

    fun getGyroscopeZ(): Long = gyroscopeZ

    fun setGyroscopeZ(gyroscopeZ: Long) {
        this.gyroscopeZ = gyroscopeZ
    }

    fun getMagnetometerX(): Long = magnetometerX

    fun setMagnetometerX(magnetometerX: Long) {
        this.magnetometerX = magnetometerX
    }

    fun getMagnetometerY(): Long = magnetometerY

    fun setMagnetometerY(magnetometerY: Long) {
        this.magnetometerY = magnetometerY
    }

    fun getMagnetometerZ(): Long = magnetometerZ

    fun setMagnetometerZ(magnetometerZ: Long) {
        this.magnetometerZ = magnetometerZ
    }

    fun getParentDataUid(): Long = parentDataUid

    fun setParentDataUid(parentDataUid: Long) {
        this.parentDataUid = parentDataUid
    }
}