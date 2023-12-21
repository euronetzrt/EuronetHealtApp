@file:Suppress("UNCHECKED_CAST")

package hu.euronetrt.okoskp.euronethealth.bluetooth.services

import android.app.IntentService
import android.content.Intent
import android.util.Log
import hu.aut.android.dm01_v11.BuildConfig
import hu.euronetrt.okoskp.euronethealth.data.CollectableType
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataBroadcaster
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.*

class DataFirstProcessIntentService : IntentService("DataFirstProcessIntentService") {
    companion object {
        val KEY_DATA = "KEY_DATA"
        private val TAG = "DataFirstProcessIntentService"
        private val TAGDATAPROC = "TAGDATAPROC"
        val DEVICEINFORMATION = "DEVICEINFORMATION"
    }

    private val emptyArray = ArrayList<Long?>()
    private var payloadSize = 0
    private var resolutionArray: MutableMap<Int, Int> = mutableMapOf()

    init {
        emptyArray.add(null)

        resolutionArray[0] = 1
        resolutionArray[1] = 2
        resolutionArray[2] = 2
        resolutionArray[3] = 2
        resolutionArray[4] = 2
        resolutionArray[5] = 3
        resolutionArray[6] = 3
        resolutionArray[7] = 4
    }

    fun ByteArray.toHexString() = joinToString(" ") { "%02x".format(it) }

    override fun onHandleIntent(intent: Intent) {
        val dataByteArray = intent.getByteArrayExtra(KEY_DATA)
        val deviceInformation: HashMap<String, String> = intent.getSerializableExtra(DEVICEINFORMATION) as (HashMap<String, String>)

        /*val dataByteArray = byteArrayOf(0xB7.toByte(), 0x09.toByte(), 0x88.toByte(), 0x00.toByte(),
                0x00.toByte(), 0xE8.toByte(), 0x03.toByte(), 0x01.toByte(), 0x00.toByte(),
                0x18.toByte(), 0xFC.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xD0.toByte(),
                0x07.toByte(), 0xFE.toByte(), 0xFF.toByte(), 0x30.toByte(), 0xF8.toByte(), 0x42.toByte(),

                0xB7.toByte(), 0x09.toByte(), 0x88.toByte(), 0x00.toByte(),
                0x00.toByte(), 0xE8.toByte(), 0x03.toByte(), 0x01.toByte(), 0x00.toByte(),
                0x18.toByte(), 0xFC.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xD0.toByte(),
                0x07.toByte(), 0xFE.toByte(), 0xFF.toByte(), 0x30.toByte(), 0xF8.toByte(), 0x42.toByte(),

                0xB7.toByte(), 0x09.toByte(), 0x88.toByte(), 0x00.toByte(),
                0x00.toByte(), 0xE8.toByte(), 0x03.toByte(), 0x01.toByte(), 0x00.toByte(),
                0x18.toByte(), 0xFC.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xD0.toByte(),
                0x07.toByte(), 0xFE.toByte(), 0xFF.toByte(), 0x30.toByte(), 0xF8.toByte(), 0x42.toByte())*/

        var i = 0
        var packageEndIdx = 0
        var whileRunNumber = 1

        while (i < dataByteArray.size) {

            //   Log.d(TAG, "dataByteArray size: ${dataByteArray.size}")

            val header = dataByteArray.copyOfRange(i, i + 3)

            val headerToIntArray = header.map { it.toInt() }
            val headerPosIntArray = headerToIntArray.map { if (it < 0) it + 256 else it }

            val length = headerPosIntArray[2] and 0x1F
            val resolution = headerPosIntArray[2] shr 5 and 0xFF
            val dataByte = resolutionArray[resolution]!!


            payloadSize = dataByte * length
            packageEndIdx += payloadSize + 4 //header =3 crc = 1


            //   Log.d(TAG, "packageEndIdx  $packageEndIdx")
            //    val sendArray = dataByteArray.copyOfRange(i,onePackageSize)

            if (packageEndIdx > dataByteArray.size) {
                //amenniben a header szerint a hossz nagyobb mint a teljes adathozz a csomag félbemaradt,
                // továbbítjuk a hibás csomagot is hogy az adatkiirásban jelentkezzen a hibás csomag.
                packageEndIdx = dataByteArray.size
                Log.d(TAG, "félbevágott adatcsomagot kaptunk. header szerint a hossz nagyobb mint ami jött.")
            }
            val sendArray = dataByteArray.copyOfRange(i, packageEndIdx)

            val type = headerPosIntArray[1] and 7

            //    Log.d(TAG, "adatcsomag $whileRunNumber " +
            //          " type: $type  length: $length")

            when (type) {
                CollectableType.PPG.type -> {
              //      Log.d(TAGDATAPROC, "class: $TAG  érkezett --> PPGData példányosítás ")
                    val ppgData = PPGData(null,deviceInformation["MAC"]!!, deviceInformation["FIRMWARE"]!!,BuildConfig.VERSION_NAME ,deviceInformation["HARDWARE"]!! ,deviceInformation["DEVICEID"]!!,deviceInformation["USERID"]!! ,System.currentTimeMillis(), System.currentTimeMillis(), null, sendArray, this)
                    DataBroadcaster.getInstance(this).send(ppgData)
                }
                CollectableType.IBI.type -> {
                  //  Log.d(TAGDATAPROC, "class: $TAG  érkezett --> IBIData  példányosítás")
                    val ibiData = IBIData(null,deviceInformation["MAC"]!!, deviceInformation["FIRMWARE"]!!,BuildConfig.VERSION_NAME ,deviceInformation["HARDWARE"]!! ,deviceInformation["DEVICEID"]!!,deviceInformation["USERID"]!! ,System.currentTimeMillis(), System.currentTimeMillis(), null, sendArray, this)
                    DataBroadcaster.getInstance(this).send(ibiData)
                }
                CollectableType.OTHER.type -> {
                  //  Log.d(TAGDATAPROC, "class: $TAG  érkezett --> OTHER  példányosítás")
                    OTHERData(null,deviceInformation["MAC"]!!, deviceInformation["FIRMWARE"]!!,BuildConfig.VERSION_NAME ,deviceInformation["HARDWARE"]!! ,deviceInformation["DEVICEID"]!!,deviceInformation["USERID"]!! , System.currentTimeMillis(), System.currentTimeMillis(), null, sendArray, this)
                }
                CollectableType.IMU.type -> {
                    //Log.d(TAGDATAPROC, "class: $TAG érkezett --> IMUData példányosítás")
                    val imuData = IMUData(null,deviceInformation["MAC"]!!, deviceInformation["FIRMWARE"]!!,BuildConfig.VERSION_NAME ,deviceInformation["HARDWARE"]!! ,deviceInformation["DEVICEID"]!!,deviceInformation["USERID"]!! , System.currentTimeMillis(), System.currentTimeMillis(), null, sendArray, this)
                    DataBroadcaster.getInstance(this).send(imuData)
                }
                CollectableType.STEP.type -> {
                   // Log.d(TAGDATAPROC, "class: $TAG érkezett --> StepData példányosítás")
                    val stepData = StepData(null,deviceInformation["MAC"]!!, deviceInformation["FIRMWARE"]!!,BuildConfig.VERSION_NAME ,deviceInformation["HARDWARE"]!! ,deviceInformation["DEVICEID"]!!,deviceInformation["USERID"]!! , System.currentTimeMillis(), System.currentTimeMillis(), null, sendArray, this)
                    DataBroadcaster.getInstance(this).send(stepData)
                }
                else -> {
                 //   Log.d(TAGDATAPROC, "class: $TAG érkezett --> Egyéb típussal jött adat type: $type")
                    return
                }
            }

            i = packageEndIdx
            whileRunNumber++
        }
    }
}