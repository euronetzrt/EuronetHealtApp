package hu.euronetrt.okoskp.euronethealth.data.dataClasses

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.TypeConverters
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.bluetooth.objects.NordicObject
import hu.euronetrt.okoskp.euronethealth.bluetooth.objects.NordicObject.datasToCSVSTEP
import hu.euronetrt.okoskp.euronethealth.data.AbstractData
import hu.euronetrt.okoskp.euronethealth.data.CollectableType
import hu.euronetrt.okoskp.euronethealth.data.CollectableTypeConverter
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataBroadcaster
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Step Data class
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
@Entity(tableName = "STEPData")
@TypeConverters(CollectableTypeConverter::class)
class StepData(
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
        value: ByteArray) : AbstractData(uid,
        macId,
        firmwareVersion,
        softwareVersion,
        hardwareVersion,
        deviceId,
        userId,
        CollectableType.STEP,
        measuredAt,
        arrivedAt,
        sentAt,
        value) {

    @Ignore
    private lateinit var mcontext: Context

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
                mContext: Context) : this(uid,
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
        this.mcontext = mContext
        this.prepareParsedValues()
    }

    companion object {
        private val TAGDATAFILEWRITE = "TAGDATAFILEWRITE"
        private val TAG = "STEPData"
        private val TAGDATAPROC = "TAGDATAPROC"
        private val STEP_DATABYTE = 4     /* Step timestamp = fix 16 bit resolution */
        private val STEP_TIMESTAMP_DATABYTE = 7     /* Step timestamp = fix 32 bit resolution */
    }

    @ColumnInfo(name = "stepValue")
    private var stepValue: Long = 0

    @ColumnInfo(name = "stepTimestamp")
    private var stepTimestamp: Long = 0

    override fun prepareParsedValues() {

        if (!DataBroadcaster.getInstance(mcontext).getStartedApp()) {

            //false --> már futhatat az app
            //    //Log.d(TAGDATAPROC, "class: $TAG --> prepareParsedValues run")
            if (getUid() == null) {

                val emptyArray = ArrayList<Long?>()
                val resolutionBytesPerValue: MutableMap<Int, Int> = mutableMapOf()
                val resolutionBitsPerValue: MutableMap<Int, Int> = mutableMapOf()
                val mask: MutableMap<Int, Long> = mutableMapOf()
                val formatted: String
                var payloadSize: Int
                val resultArray = ArrayList<Long?>()

                emptyArray.add(null)
                resolutionBytesPerValue[0] = 1
                resolutionBytesPerValue[1] = 2
                resolutionBytesPerValue[2] = 2
                resolutionBytesPerValue[3] = 2
                resolutionBytesPerValue[4] = 2
                resolutionBytesPerValue[5] = 3
                resolutionBytesPerValue[6] = 3
                resolutionBytesPerValue[7] = 4

                resolutionBitsPerValue[0] = 8
                resolutionBitsPerValue[1] = 10
                resolutionBitsPerValue[2] = 12
                resolutionBitsPerValue[3] = 14
                resolutionBitsPerValue[4] = 16
                resolutionBitsPerValue[5] = 18
                resolutionBitsPerValue[6] = 24
                resolutionBitsPerValue[7] = 32

                mask[8] = 0xFF
                mask[10] = 0x3FF
                mask[12] = 0xFFF
                mask[14] = 0x3FFF
                mask[16] = 0xFFFF
                mask[18] = 0x3FFFF
                mask[24] = 0xFFFFFF
                mask[32] = 0xFFFFFFFF

                val oneDataPackByteArray = getValue()

                //    //Log.d(TAGDATAPROC, "class: $TAG --> dataByteArray $oneDataPackByteArray")

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val current = LocalDateTime.now()

                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                    formatted = current.format(formatter)
                } else {
                    val current = System.currentTimeMillis()

                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                    formatted = formatter.format(current)
                }

                val charValuesToInt = oneDataPackByteArray.map { it.toInt() }
                val charValuesToPositive = charValuesToInt.map { if (it < 0) it + 256 else it }

                val header = oneDataPackByteArray.copyOfRange(0, 3)

                val headerToIntArray = header.map { it.toInt() }
                val headerPosIntArray = headerToIntArray.map { if (it < 0) it + 256 else it }

                val subType = oneDataPackByteArray.copyOfRange(3, 4)
                val subTypeToIntArray = subType.map { it.toInt() }
                val subTypePosIntArray = subTypeToIntArray.map { if (it < 0) it + 256 else it }
                Log.d(TAG, "STEP subTypePosIntArray $subTypePosIntArray")

                val counterFirstByte = headerPosIntArray[0] shl 4
                val counterSecoundByte = headerPosIntArray[1] shr 4
                // //Log.d(TAG, "Binary " + Integer.toBinaryString(headerPosIntArray[1]))
                val counterSum = counterFirstByte + counterSecoundByte
                Log.d(TAG, "STEP counterSum $counterSum")

                val length = headerPosIntArray[2] and 0x1F
                Log.d(TAG, "STEP length  $length")

                val resolution = headerPosIntArray[2] shr 5 and 0xFF
                Log.d(TAG, "STEP resolution $resolution")
                val dataByte = resolutionBytesPerValue[resolution]!!
                Log.d(TAG, "STEP dataByte $dataByte")

                val dataBit = resolutionBitsPerValue[resolution]!!
                Log.d(TAG, "STEP dataBit $dataBit")

                payloadSize = dataByte * length
                Log.d(TAG, "STEP payloadSize $payloadSize")

                val type = headerPosIntArray[1] and 7
                Log.d(TAG, "class: $TAG --> típus: $type")

                /** A típusok a következők:

                0 - Az eszköz (in)aktív állapotát jelzi, itt az adat 1 bájt hosszú.
                1 - Az orientációt tartalmazó információ, az adat 3 bájtos
                2 - A lépésszámláló, az adat 6 bájt*/
                if (subTypePosIntArray[0] == 2) {

                    /** Csomagméret ellenőrzés*/
                    if (charValuesToPositive.size >= payloadSize + 3) {
                        /** CRC Adat ellenőrzés hogy nem e sérült a csomag.*/
                        val crcIdx = 3 + payloadSize
                        val crcValue = charValuesToPositive[crcIdx] and 0xFF
                        var dataSum = 0

                        for (i in 0 until crcIdx) {
                            dataSum += charValuesToPositive[i]
                        }

                        /** CRC értéket nem vesszük bele a számításba*/
                        dataSum = dataSum and 0xFF
                        //Log.d(TAG, "dataSum $dataSum")
                        //Log.d(TAG, "dataSum3 $crcValue")

                        if (dataSum == crcValue) {
                            /** Az ellenőrző méretek azonosak*/

                            /**Log.d(TAG, "counterSum:  $counterSum   $length  ${NordicObject.counterSumCheckSTEP}")
                             Működési elv: A counterSumCheckSTEP vizsgálata után szabad csak módosítani,
                             szükséges az iftől függetlenül. azonban else esetén el kell return-ölni a csomaggal.
                             A legelső esetben ez az if az else ágba fut viszont beállítódik jól a számláló,
                             gyakorlatilag az első csomagot mindig eldobjuk, beáldozzuk a számláló beállítása miatt így nem kell külön
                             változóban flag-es megoldással tárolni hogy ha az első adat jön nem baj ha nem egyezik meg az érték.
                             tehát első csomag eldobásra kerül a kounter beállítódik ezek után már csak akkor kell a return ha tényleg rossz adat jön.
                             a counter nem állítható az if előtt viszont utána nem futna rá mert az eslő csomag mindig az else ágba kerül.
                            */

                            if (NordicObject.counterSumCheckSTEP == counterSum) {
                                counterSumChechVariableModify(counterSum)
                            } else {
                                //Log.d(TAG, "Elleorzes counterSum error:  $counterSum")
                                // Hibás csomag megjelölése a csv-be

                                datasToCSVSTEP.add(Pair(formatted, emptyArray))
                                datasToCSVSizeCheck(datasToCSVSTEP)
                                writeFileError(formatted, "Step", "A countersum ellenőrzése során hiba lépett fel. oldCountersum: ${NordicObject.counterSumCheckSTEP} newCounterSum: $counterSum length: $length")
                                //Log.d(TAGDATAPROC, "class: $TAG -->ERROR- STEP Elleorzes counterSum error:  $counterSum")
                                counterSumChechVariableModify(counterSum)
                                return
                            }
                        } else {
                            /*Hiba lépett fel a datasum - crc ellenőrzés során
                              Log.d(TAG, "datasum!=crc")*/
                            //Log.d(TAGDATAPROC, "class: $TAG -->ERROR- STEP Hiba lépett fel a datasum - crc ellenőrzés során")
                            // beírjuk az üres sort hogy tudjuk maradt ki itt adat
                            writeToArrayForCSVIfWasError(emptyArray, formatted)
                            writeFileError(formatted, "Step", "A datasum - crc ellenőrzése során hiba lépett fel. datasum:$dataSum  crc: $crcValue  oldCountersum: ${NordicObject.counterSumCheckSTEP} newCounterSum: $counterSum length: $length")
                            return
                        }
                        /** Mivel a real dataMultipleList size csak a méret ezért az helyes indexméret
                            * miatt hozzá kell adni a kezdőt indexet hogy onnantól kell az x db byte -1 mert a típust jelző adat is bent a payloadba... és az nem kell a valós adatokhoz
                         * */

                        // 4. indextől kell ami az 5. byte tól a payload size-ig ez megmondja hányas indexig kell
                        // azonban ez nem a kezdőtől számított x db hanem a 0- tól számított hányadik ezért kell lényegéven a vég indexet ugy kapjuk meg
                        // hogy a 3 header + payloadsize adja a végső indexet ennek amjd az elejéről leveszi a headert. viszont a payload pont egyel nagyobb mint kéne mert bent a típus adat is.

                        payloadSize--   // subtype byte lekerült

                        val realData = oneDataPackByteArray.copyOfRange(4, 4 + payloadSize)
                        val realDataToIntArray = realData.map { it.toInt() }
                        val realDataPositivIntArray = realDataToIntArray.map { if (it < 0) it + 256 else it }
                        // //Log.d(TAG, "realDataPosIntArray  $realDataPosIntArray")

                        var idx = 0
                        var subidx = 0
                        var s: String

                        while (idx != payloadSize/*lenght * dataByte*/) {

                            // number of steps 16bit
                            var value: Long = readIntValue(realDataPositivIntArray, idx, resolutionBytesPerValue[STEP_DATABYTE]!!, resolutionBitsPerValue[STEP_DATABYTE]!!, mask)
                            resultArray.add(value)
                            stepValue = value
                            setStepValue(stepValue)
                            idx += resolutionBytesPerValue[STEP_DATABYTE]!!

                            // steps timestamp 32bit
                            value = readIntValue(realDataPositivIntArray, idx, resolutionBytesPerValue[STEP_TIMESTAMP_DATABYTE]!!, resolutionBitsPerValue[STEP_TIMESTAMP_DATABYTE]!!, mask)
                            resultArray.add(value)
                            stepTimestamp = value
                            setStepTimestamp(stepTimestamp)
                            idx += resolutionBytesPerValue[STEP_TIMESTAMP_DATABYTE]!!

                            s = formatted + "%02d".format(subidx)
                            val copyArray = ArrayList<Long?>(resultArray)
                            copyArray.add(0, counterSum.toLong())
                            datasToCSVSTEP.add(Pair(s, copyArray))
                            subidx++
                            resultArray.clear()
                        }

                        datasToCSVSizeCheck(datasToCSVSTEP)
                    } else {
                        //Log.d(TAGDATAPROC, "class: $TAG --> ERROR-STEP csomag ellenőrzés során hiba lépett fel. csomaghossz < payloadSize + 4")
                        writeToArrayForCSVIfWasError(emptyArray, formatted)
                        writeFileError(formatted, "Step", "A csomag ellenőrzés során hiba lépett fel. csomaghossz < payloadSize  csomaghossz: ${charValuesToPositive.size} sizeAll+4: ${payloadSize + 4}")
                        return
                        //  throw EuronetErrorDataPackageException()
                    }
                }
            }
        }
    }

    private fun readIntValue(realDataPositivIntArray: List<Int>, idx: Int, dataByte: Int, dataBit: Int, mask: MutableMap<Int, Long>): Long {
        var value: Long = 0

        val positiv: Boolean = (realDataPositivIntArray[idx + (dataByte - 1)] and (1 shl (((dataBit - 1) % 8))) == 0)
        var i = 0

        while (i != dataByte) {
            // //Log.d(TAG, "realDataPosIntArray[pos+i] " + realDataPosIntArray[pos + i])
            value += realDataPositivIntArray[idx + i] shl (i * 8)
            i++
        }

        // Negítiv esetén maszkolni kell
        if (!positiv) {
            value = -1 * (mask[dataBit]!! - value + 1)
        }

        return value
    }

    private fun counterSumChechVariableModify(counterSum: Int) {
        NordicObject.counterSumCheckSTEP = (counterSum + 1) % 4096
    }

    private fun writeToArrayForCSVIfWasError(emptyArray: ArrayList<Long?>, formatted: String) {
        datasToCSVSTEP.add(Pair(formatted, emptyArray))
        datasToCSVSizeCheck(datasToCSVSTEP)
    }


    private fun datasToCSVSizeCheck(datasToCSV: MutableList<Pair<String, ArrayList<Long?>>>) {
        if (GlobalRes.writeFileActive) {
            writeFile(datasToCSV)
        }
        datasToCSVSTEP.clear()

    }

    private fun writeFile(datasToCSV: MutableList<Pair<String, ArrayList<Long?>>>): Boolean {
        //   //Log.d(TAGDATAPROC, "class: $TAG alrész: $TAGDATAFILEWRITE-->  Elérte a tömb  az 50 elemet, kiírjuk. Típus: $type")
        val formatted: String
        val CSV_HEADER = "Time;Counter;Step_Timestamp"

        val datas = datasToCSV

        if (datas.isNotEmpty() /*&& datas.size > MinDataNumber*/) {
            formatted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val current = LocalDateTime.now()

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")
                current.format(formatter)
            } else {
                val current = System.currentTimeMillis()

                val formatter = SimpleDateFormat("yyyy-MM-dd HH")
                formatter.format(current)
            }

            val hasSDCard = Environment.getExternalStorageState()
            val fileName = "STEP_$formatted.csv"

            when (hasSDCard) {
                Environment.MEDIA_MOUNTED -> {

                    // Írható olvasható
                    val fileWriter: FileWriter?

                    val folder = File(Environment.getExternalStorageDirectory().toString() + "/Euronet/")

                    if (!folder.exists()) {
                        folder.mkdirs()
                        // fix
                        folder.setExecutable(true)
                        folder.setReadable(true)
                        folder.setWritable(true)
                    }

                    // initiate media scan and put the new things into the path array to
                    // make the scanner aware of the location and the files you want to see

                    val file = File(folder, fileName)

                    if (!file.exists()) {
                        file.setWritable(true)
                        file.setReadable(true)
                        file.setExecutable(true)

                        fileWriter = FileWriter(file)
                        fileWriter.append(CSV_HEADER)
                        fileWriter.append('\n')
                        //Log.d(TAGDATAPROC, "filewrite->fileName : $fileName")
                    } else {
                        fileWriter = FileWriter(file, true)
                    }
                    try {

                        //     f = open(file, 'a').write(what)
                        for (data in datas) {
                            //Log.d(TAGDATAFILEWRITE, "key: ${data.first}")
                            fileWriter.append(data.first)
                            data.second.forEach {
                                fileWriter.append(';')
                                fileWriter.append(it.toString())
                            }
                            fileWriter.append('\n')
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return false
                    } finally {
                        try {
                            fileWriter.flush()
                            fileWriter.close()
                        } catch (e: IOException) {
                            //Log.d(TAGDATAFILEWRITE, "Error ${e.message}")
                            e.printStackTrace()
                            return false
                        }
                    }
                }
                Environment.MEDIA_MOUNTED_READ_ONLY -> {
                    // csak olvasható
                    //Log.e(TAGDATAFILEWRITE, "Error csak olvasható a tár!!!")
                    return false
                }
                else -> {
                    // Se írható se olvasható
                    //Log.e(TAGDATAFILEWRITE, "Nem írható se nem olvasható!")
                    return false
                }
            }
        }
        return true
    }

    private fun writeFileError(time: String, type: String, message: String): Boolean {
        val formatted: String
        val CSV_HEADER = "Time;Type;Message"

        formatted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")
            current.format(formatter)
        } else {
            val current = System.currentTimeMillis()

            val formatter = SimpleDateFormat("yyyy-MM-dd HH")
            formatter.format(current)
        }

        val hasSDCard = Environment.getExternalStorageState()
        val fileName = "Error_$formatted.csv"

        when (hasSDCard) {
            Environment.MEDIA_MOUNTED -> {

                // Írható olvasható
                val fileWriter: FileWriter?

                val folder = File(Environment.getExternalStorageDirectory().toString() + "/Euronet/")

                if (!folder.exists())
                    folder.mkdirs()

                // fix
                folder.setExecutable(true)
                folder.setReadable(true)
                folder.setWritable(true)

                // initiate media scan and put the new things into the path array to
                // make the scanner aware of the location and the files you want to see


                val file = File(folder, fileName)
                file.setWritable(true)
                file.setReadable(true)
                file.setExecutable(true)

                if (!file.exists()) {
                    fileWriter = FileWriter(file)
                    fileWriter.append(CSV_HEADER)
                    fileWriter.append('\n')
                    //Log.d(TAGDATAPROC, "filewrite->fileName : $fileName")
                } else {
                    fileWriter = FileWriter(file, true)
                }
                try {

                    fileWriter.append(time)
                    fileWriter.append(type)
                    fileWriter.append(message)
                    fileWriter.append('\n')
                } catch (e: Exception) {
                    e.printStackTrace()
                    return false
                } finally {
                    try {
                        fileWriter.flush()
                        fileWriter.close()
                    } catch (e: IOException) {
                        //Log.d(TAGDATAFILEWRITE, "Error ${e.message}")
                        e.printStackTrace()
                        return false

                    }
                }
            }
            Environment.MEDIA_MOUNTED_READ_ONLY -> {
                // csak olvasható
                //Log.e(TAGDATAFILEWRITE, "Error csak olvasható a tár!!!")
                return false
            }
            else -> {
                // Se írható se olvasható
                //Log.e(TAGDATAFILEWRITE, "Nem írható se nem olvasható!")
                return false
            }
        }
        return true
    }

    fun getStepValue(): Long {
        return stepValue
    }

    fun setStepValue(value: Long) {
        this.stepValue = value
    }

    fun getStepTimestamp(): Long {
        return stepTimestamp
    }

    fun setStepTimestamp(value: Long) {
        this.stepTimestamp = value
    }
}


