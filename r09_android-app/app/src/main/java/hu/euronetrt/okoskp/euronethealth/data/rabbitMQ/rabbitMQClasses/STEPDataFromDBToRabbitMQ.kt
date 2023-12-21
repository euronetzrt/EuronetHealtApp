package hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.rabbitMQClasses

/**
 * STEP Data From DB To RabbitMQ
 *
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
class STEPDataFromDBToRabbitMQ(
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
        private var value: Long
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


    fun getValue(): Long = value

    fun setValue(value: Long) {
        this.value = value
    }

}