package hu.euronetrt.okoskp.euronethealth.data

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import hu.euronetrt.okoskp.euronethealth.data.exceptionClasses.EuronetErrorDataPackageException

abstract class AbstractData{

    @PrimaryKey(
            autoGenerate = true
    )
    @ColumnInfo( name = "uid")
    private var uid: Long?
    @ColumnInfo( name = "macId")
    private var macId: String
    @ColumnInfo( name = "firmwareVersion")
    private var firmwareVersion: String
    @ColumnInfo( name = "softwareVersion")
    private var softwareVersion: String
    @ColumnInfo( name = "hardwareVersion")
    private var hardwareVersion: String
    @ColumnInfo( name = "deviceId")
    private var deviceId: String
    @ColumnInfo( name = "userId")
    private var userId: String
    private var type: CollectableType
    @ColumnInfo( name = "measuredAt")
    private var measuredAt: Long
    @ColumnInfo( name = "arrivedAt")
    private var arrivedAt: Long
    @ColumnInfo( name = "sentAt")
    private var sentAt: Long?
    @ColumnInfo( name = "value", typeAffinity = ColumnInfo.BLOB)
    private var value: ByteArray

    constructor(uid: Long?,
                macId: String,
                firmwareVersion: String,
                softwareVersion: String,
                hardwareVersion: String,
                deviceId: String,
                userId: String,
                type: CollectableType,
                measuredAt: Long,
                arrivedAt: Long,
                sentAt: Long?,
                value: ByteArray){
        this.uid = uid
        this.macId = macId
        this.firmwareVersion = firmwareVersion
        this.softwareVersion = softwareVersion
        this.hardwareVersion = hardwareVersion
        this.deviceId = deviceId
        this.userId = userId
        this.type = type
        this.measuredAt = measuredAt
        this.arrivedAt = arrivedAt
        this.sentAt = sentAt
        this.value = value
    }

    companion object {
        private val TAGDATAPROC = "AbstractData"
        private val TAG = "TAGDATAPROC"
    }

    @Throws(EuronetErrorDataPackageException::class)
    abstract fun prepareParsedValues()

    fun getUid(): Long? = uid

    fun setUid(uid: Long?) {
        this.uid = uid
    }

    fun getType(): CollectableType = type

    fun setType(type: CollectableType) {
        this.type = type
    }

    fun getMeasuredAt(): Long = measuredAt

    fun setMeasuredAt(measuredAt: Long) {
        this.measuredAt = measuredAt
    }

    fun getArrivedAt(): Long = arrivedAt

    fun setArrivedAt(arrivedAt: Long) {
        this.arrivedAt = arrivedAt
    }

    fun getSentAt(): Long? = sentAt

    fun setSentAt(sentAt: Long) {
        this.sentAt = sentAt
    }

    fun getValue(): ByteArray = this.value

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


    fun getUserId(): String = userId

    fun setUserId(userId: String) {
        this.userId = userId
    }
}
