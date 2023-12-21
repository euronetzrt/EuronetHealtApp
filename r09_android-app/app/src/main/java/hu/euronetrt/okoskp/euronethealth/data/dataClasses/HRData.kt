package hu.euronetrt.okoskp.euronethealth.data.dataClasses

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.TypeConverters
import hu.euronetrt.okoskp.euronethealth.data.AbstractData
import hu.euronetrt.okoskp.euronethealth.data.CollectableType
import hu.euronetrt.okoskp.euronethealth.data.CollectableTypeConverter

/**
 * HR Data class
 *
 * @constructor
 * @param uid
 * @param macId
 * @param firmwareVersion
 * @param softwareVersion
 * @param hardwareVersion
 * @param deviceId
 * @param userId
 * @param measuredAt
 * @param arrivedAt
 * @param sentAt
 * @param value
 */
@Entity(tableName = "HRData")
@TypeConverters(CollectableTypeConverter::class)
class HRData(
        uid: Long?,
        macId: String,
        firmwareVersion: String,
        softwareVersion: String,
        hardwareVersion: String,
        deviceId: String,
        userId: String,
        measuredAt: Long,
        arrivedAt: Long,
        sentAt: Long?,
        value: ByteArray
) : AbstractData(uid,
        macId,
        firmwareVersion,
        softwareVersion,
        hardwareVersion,
        deviceId,
        userId,
        CollectableType.HEARTRATE,
        measuredAt,
        arrivedAt,
        sentAt,
        value) {

    @Ignore
    @Transient
    private lateinit var characteristic: BluetoothGattCharacteristic

    constructor(uid: Long?,
                macId: String,
                firmwareVersion: String,
                softwareVersion: String,
                hardwareVersion: String,
                deviceId: String,
                userId: String,
                measuredAt: Long,
                arrivedAt: Long,
                sentAt: Long?,
                value: ByteArray,
                characteristic: BluetoothGattCharacteristic) : this(uid,
            macId,
            firmwareVersion,
            softwareVersion,
            hardwareVersion,
            deviceId,
            userId,
            measuredAt,
            arrivedAt,
            sentAt,
            value) {

        this.characteristic = characteristic
        prepareParsedValues()
    }

    companion object {
        private val TAG = "HRData"
        private val TAGDATAPROC = "TAGDATAPROC"
    }

    @ColumnInfo(name = "hrValue")
    private var hr: Int = 0

    override fun prepareParsedValues() {
        if (::characteristic.isInitialized) {
            val flag = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
            val format: Int // -1
            val energy: Int //-1
            var offset: Int //1
            val rr_count: Int //0
            if (flag and 0x01 != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16
                Log.d(TAGDATAPROC, "class: $TAG --> Heart rate format UINT16.")
                offset = 3
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8
                Log.d(TAGDATAPROC, "class: $TAG --> Heart rate format UINT8.")
                offset = 2
            }
            val heartRate = characteristic.getIntValue(format, 1)
            Log.d(TAGDATAPROC, "class: $TAG --> Received heart rate: {}$heartRate")
            if (flag and 0x08 != 0) {
                // calories present
                energy = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset)
                offset += 2
                Log.d(TAGDATAPROC, "class: $TAG --> Received energy: {}$energy")
            }
            if (flag and 0x10 != 0) {
                // RR stuff.
                rr_count = (getValue().size - offset) / 2
                val mRr_values = arrayOfNulls<Int>(rr_count)
                for (i in 0 until rr_count) {
                    mRr_values[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, offset)
                    offset += 2
                    Log.d(TAGDATAPROC, "class: $TAG --> Received RR: {}" + mRr_values[i])
                }
            }

            Log.d(TAGDATAPROC, "class: $TAG --> HR : $heartRate")

            hr = heartRate
            Log.d(TAGDATAPROC, "class: $TAG --> ${getHr()}")
        }
    }

    fun getHr(): Int = hr

    fun setHr(hr: Int) {
        this.hr = hr
    }
}
