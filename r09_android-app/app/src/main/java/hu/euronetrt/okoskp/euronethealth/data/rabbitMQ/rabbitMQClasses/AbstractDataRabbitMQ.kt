package hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.rabbitMQClasses

/**
 * Abstract Data RabbitMQ
 *
 * @property macId
 * @property firmwareVersion
 * @property softwareVersion
 * @property hardwareVersion
 * @property deviceId
 * @property userId
 * @property type
 * @property uid
 * @property measuredAt
 * @property arrivedAt
 * @property sentAt
 */
abstract class AbstractDataRabbitMQ(
        private var macId: String,
        private var firmwareVersion: String,
        private var softwareVersion: String,
        private var hardwareVersion: String,
        private var deviceId: String,
        private var userId: String,
        private var type: String,
        private var uid: Long,
        private var measuredAt: Long,
        private var arrivedAt: Long,
        private var sentAt: Long
) {

    fun getMacId(): String {
        return macId
    }

    fun setMacId(macId: String) {
        this.macId = macId
    }

    fun getFirmwareVersion(): String {
        return firmwareVersion
    }

    fun setFirmwareVersion(firmwareVersion: String) {
        this.firmwareVersion = firmwareVersion
    }

    fun getSoftwareVersion(): String {
        return softwareVersion
    }

    fun setSoftwareVersion(softwareVersion: String) {
        this.softwareVersion = softwareVersion
    }

    fun getHardwareVersion(): String {
        return hardwareVersion
    }

    fun setHardwareVersion(hardwareVersion: String) {
        this.hardwareVersion = hardwareVersion
    }

    fun getDeviceId(): String = deviceId

    fun setDeviceId(deviceId: String) {
        this.deviceId = deviceId
    }

    fun getUid(): Long = uid

    fun setUid(uid: Long) {
        this.uid = uid
    }

    fun getType(): String {
        return type
    }

    fun setType(type: String) {
        this.type = type
    }

    fun getUserId(): String = userId

    fun setUserId(userId: String) {
        this.userId = userId
    }

    fun getMeasuredAt(): Long = measuredAt

    fun setMeasuredAt(measuredAt: Long) {
        this.measuredAt = measuredAt
    }

    fun getArrivedAt(): Long = arrivedAt

    fun setArrivedAt(arrivedAt: Long) {
        this.arrivedAt = arrivedAt
    }

    fun getSentAt(): Long {
        return sentAt
    }

    fun setSentAt(sentAt: Long) {
        this.sentAt = sentAt
    }
}

