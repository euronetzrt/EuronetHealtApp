package hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.rabbitMQClasses.subIMUData

import hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.rabbitMQClasses.AbstractDataRabbitMQ

/**
 * Gyroscope Data From DB To RabbitMQ
 *
 * @property parentDataUid
 * @property value
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
class GyroscopeDataFromDBToRabbitMQ(
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
        private var value : HashMap<String,Long>
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

    fun getValue(): HashMap<String,Long> = value

    fun setValue(value: HashMap<String,Long>) {
        this.value = value
    }

    fun getParentDataUid(): Long = parentDataUid

    fun setParentDataUid(parentDataUid: Long) {
        this.parentDataUid = parentDataUid
    }
}