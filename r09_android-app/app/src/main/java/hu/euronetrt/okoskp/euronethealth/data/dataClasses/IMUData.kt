package hu.euronetrt.okoskp.euronethealth.data.dataClasses

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.TypeConverters
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.bluetooth.objects.NordicObject
import hu.euronetrt.okoskp.euronethealth.bluetooth.objects.NordicObject.datasToCSVIMU
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
 * IMUData class
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
@Entity(tableName = "IMUData")
@TypeConverters(CollectableTypeConverter::class)
class IMUData(
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
        CollectableType.IMU,
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
                mContext: Context) : this(
            uid,
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
        private val TAG = "IMUData"
        private val TAGDATAPROC = "TAGDATAPROC"
        private val IMU_TENGELYEK_SZAMA = 6
    }

    @Ignore//@ColumnInfo(name="accelero")
    private var accelero: LongArray = LongArray(3)
    @Ignore//@ColumnInfo(name="magneto")
    private var magneto: LongArray = LongArray(3)
    @Ignore//@ColumnInfo(name="gyroscope")
    private var gyroscope: LongArray = LongArray(3)

    @Ignore
    private lateinit var imuValues: ArrayList<IMUDataValue>

    /**
     * prepare Parsed Values
     */
    override fun prepareParsedValues() {
        imuValues = ArrayList()
        if (!DataBroadcaster.getInstance(mcontext).getStartedApp()) {

            //false --> már futhatat az app
            //    Log.d(TAGDATAPROC, "class: $TAG --> prepareParsedValues run")
            if (getUid() == null) {

                val emptyArray = ArrayList<Long?>()
                val resolutionBytesPerValue: MutableMap<Int, Int> = mutableMapOf()
                val resolutionBitsPerValue: MutableMap<Int, Int> = mutableMapOf()
                val mask: MutableMap<Int, Long> = mutableMapOf()
                var formatted: String
                val payloadSize: Int
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

                //     Log.d(TAGDATAPROC, "class: $TAG --> prepareParsedValues run")

                val oneDataPackByteArray = getValue()

                val time = System.currentTimeMillis()
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                formatted = formatter.format(time)

                val charValuesToInt = oneDataPackByteArray.map { it.toInt() }
                val charValuesToPositive = charValuesToInt.map { if (it < 0) it + 256 else it }

                val header = oneDataPackByteArray.copyOfRange(0, 3)
                val headerToIntArray = header.map { it.toInt() }
                val headerPosIntArray = headerToIntArray.map { if (it < 0) it + 256 else it }

                val counterFirstByte = headerPosIntArray[0] shl 4
                val counterSecoundByte = headerPosIntArray[1] shr 4
                // Log.d(TAG, "Binary " + Integer.toBinaryString(headerPosIntArray[1]))
                val counterSum = counterFirstByte + counterSecoundByte
                //   Log.d(TAGDATAPROC, "class: $TAG --> counterSum $counterSum")

                val length = headerPosIntArray[2] and 0x1F
                //     Log.d(TAG, "length  $length")

                val resolution = headerPosIntArray[2] shr 5 and 0xFF
                //   Log.d(TAG, "resolution  $resolution")  //  4 es érték!!
                val dataByte = resolutionBytesPerValue[resolution]!!
                val dataBit = resolutionBitsPerValue[resolution]!!

                payloadSize = dataByte * length
                // Log.d(TAG, "payloadSize  $payloadSize")
                //   val type = headerPosIntArray[1] and 7
                //   Log.d(TAGDATAPROC, "class: $TAG --> típus: $type")

                /**
                 * Csomagméret ellenőrzés
                 */

                if (charValuesToPositive.size >= payloadSize + 4) {

                    // Log.d(TAG, "charValuesToPositive  $charValuesToPositive")
                    /**
                     * CRC Adat ellenőrzés hogy nem e sérült a csomag.
                     */
                    val crcIdx = 3 + payloadSize
                    val crcValue = charValuesToPositive[crcIdx] and 0xFF
                    // Log.d(TAG, "charValuesToPositive : $crcValue")

                    var dataSum = 0

                    for (i in 0 until crcIdx) {
                        dataSum += charValuesToPositive[i]
                    }


                    /**
                     * CRC értéket nem vesszük bele a számításba
                     */
                    dataSum = dataSum and 0xFF
                    // Log.d(TAG, "dataSum $dataSum")
                    // Log.d(TAG, "dataSum3 $crcValue")

                    if (dataSum == crcValue) {
                        /**
                         * Az ellenőrző méretek azonosak
                         */


                        // Log.d(TAG, "counterSum:  $counterSum   $length  ${NordicObject.counterSumCheckIMU}")
                        // Működési elv: A counterSumCheckIMU vizsgálata után szabad csak módosítani,
                        // szükséges az iftől függetlenül. azonban else esetén el kell return-ölni a csomaggal.
                        // A legelső esetben ez az if az else ágba fut viszont beállítódik jól a számláló,
                        // gyakorlatilag az első csomagot mindig eldobjuk, beáldozzuk a számláló beállítása miatt így nem kell külön
                        // változóban flag-es megoldással tárolni hogy ha az első adat jön nem baj ha nem egyezik meg az érték.
                        // tehát első csomag eldobásra kerül a kounter beállítódik ezek után már csak akkor kell a return ha tényleg rossz adat jön.
                        // a counter nem állítható az if előtt viszont utána nem futna rá mert az eslő csomag mindig az else ágba kerül.

                        if (NordicObject.counterSumCheckIMU == counterSum) {
                            counterSumCheckVariableModify(counterSum)
                        } else {
                            //       Log.d(TAG, "imu counter check: várt counter: ${NordicObject.counterSumCheckIMU}  csomag counter: $counterSum  ---------------------!!!!!!!!!!!!!!!!!!hiba!!!!!!!!!!!!!!")
                            // Hibás csomag megjelölése a csv-be
                            datasToCSVIMU.add(Pair(formatted, emptyArray))
                            //datasToCSVIMU[formatted] = emptyArray
                            datasToCSVSizeCheck(datasToCSVIMU)
                            writeFileError(formatted, "IMU", "--> A countersum ellenőrzése során hiba lépett fel. oldCountersum: ${NordicObject.counterSumCheckIMU} newCounterSum: $counterSum length: $length")
                            //     Log.d(TAGDATAPROC, "class: $TAG -->ERROR-IMU Elleorzes counterSum error:  $counterSum")
                            counterSumCheckVariableModify(counterSum)
                            return
                            //throw EuronetErrorDataPackageException()
                        }
                    } else {
                        /*Hiba lépett fel a datasum - crc ellenőrzés során
                         Log.d(TAG, "datasum!=crc")*/
                        //         Log.d(TAGDATAPROC, "class: $TAG -->ERROR-IMU Hiba lépett fel a datasum - crc ellenőrzés során")
                        // beírjuk az üres sort hogy tudjuk maradt ki itt adat
                        writeToArrayForCSVIfWasError(emptyArray, formatted)
                        writeFileError(formatted, "IMU", "A datasum - crc ellenőrzése során hiba lépett fel. datasum:$dataSum  crc: $crcValue  oldCountersum: ${NordicObject.counterSumCheckIMU} newCounterSum: $counterSum length: $length")
                        return
                        //throw EuronetErrorDataPackageException()
                    }
                    /**
                     * Mivel a real dataMultipleList size csak a méret ezért az helyes indexméret
                     * miatt hozzá kell adni a kezdőt indexet hogy onnantól kell az x db byte
                     * */
                    val realData = oneDataPackByteArray.copyOfRange(3, 3 + payloadSize)
                    val realDataToIntArray = realData.map { it.toInt() }
                    val realDataPositivIntArray = realDataToIntArray.map { if (it < 0) it + 256 else it }
                    // Log.d(TAG, "realDataPosIntArray  $realDataPosIntArray")

                    var idx = 0
                    var tengelyIdx = 0
                    var subidx = 0.0

                    while (idx != payloadSize/*lenght * dataByte*/) {

                        if (tengelyIdx == 0) {
                            resultArray.add(counterSum.toLong())
                        }

                        var value: Long = 0

                        val positiv: Boolean = (realDataPositivIntArray[idx + (dataByte - 1)] and (1 shl (((dataBit - 1) % 8))) == 0)
                        var i = 0

                        while (i != dataByte) {
                            // Log.d(TAG, "realDataPosIntArray[pos+i] " + realDataPosIntArray[pos + i])
                            value += realDataPositivIntArray[idx + i] shl (i * 8)
                            i++
                        }

                        // Negítiv esetén maszkolni kell
                        if (!positiv) {
                            value = -1 * (mask[dataBit]!! - value + 1)
                        }

                        resultArray.add(value)
                        Log.d(TAG, "counterSum:   $counterSum")
                        //  resultArray.add(counterSum.toLong())
                        idx += dataByte
                        tengelyIdx++


                        if ((tengelyIdx % IMU_TENGELYEK_SZAMA) == 0) {
                            var t: Long
                            var s: String

                            if (subidx == 0.toDouble()) {
                                t = time
                                s = formatter.format(t)
                            } else {
                                t = time + (subidx * (1.0 / 52.0) * 1000.0).toInt()
                                s = formatter.format(t)
                            }

                            val copyArrayList = ArrayList<Long?>(resultArray)

                            val pair = Pair(s, copyArrayList)
                            datasToCSVIMU.add(pair)

                            imuValues.add(IMUDataValue(null, null, resultArray[1]!!, resultArray[2]!!, resultArray[3]!!, resultArray[4]!!, resultArray[5]!!, resultArray[6]!!, 0, 0, 0))

                            resultArray.clear()

                            tengelyIdx = 0
                            subidx += 1.0
                        }

                        //minden sornál létrehozzuk a imudata value-t később beállítjuk az imuData ID-t létrehozásnál pedid a sima
                        //id-t tölti ki az autogenerate
                        //imuAcceleroValues.add(IMUDataValue(null, null, value))

                        //  Log.d(TAG, "Accelero : x-> $it ")
                        //  imuValues.add(IMUDataValue(null, null, resultArray[0]!!, resultArray[1]!!, resultArray[2]!!, resultArray[3]!!, resultArray[4]!!, resultArray[5]!!, 0, 0, 0))
                    }

                    setImuValues(imuValues)
                    /* val acceleroArray = LongArray(3)
                     val gyroArray = LongArray(3)
                     val magnetoArray = LongArray(3)

                     acceleroArray[0] = resultArray[0]!!
                     acceleroArray[1] = resultArray[1]!!
                     acceleroArray[2] = resultArray[2]!!
                     setAccelero(acceleroArray)

                     gyroArray[0] = resultArray[3]!!
                     gyroArray[1] = resultArray[4]!!
                     gyroArray[2] = resultArray[5]!!
                     setGyroscope(gyroArray)

                     magnetoArray[0] = 0
                     magnetoArray[1] = 0
                     magnetoArray[2] = 0
                     setMagneto(magnetoArray)*/

                    // Log.d(TAGDATAPROC, "class: $TAG --> IMUresultArray: $resultArray")

                    //Log.d(TAGDATAPROC, "class: $TAG --> IMUType most értünk a végére a feldolgozásnak nézzük hány eleme van: ${datasToCSVIMU.size}  result mérete: ${resultArray.size}")
                    Log.d(TAG, " datasToCSVIMU:    $datasToCSVIMU")
                    datasToCSVSizeCheck(datasToCSVIMU)

                } else {
                    Log.d(TAGDATAPROC, "class: $TAG --> ERROR-IMUERROR A csomag ellenőrzés során hiba lépett fel. csomaghossz < payloadSize + 4")
                    writeToArrayForCSVIfWasError(emptyArray, formatted)
                    writeFileError(formatted, "IMU", "A csomag ellenőrzés során hiba lépett fel. csomaghossz < payloadSize  csomaghossz: ${charValuesToPositive.size} sizeAll+4: ${payloadSize + 4}")
                    return
                }
            }
        }
    }

    /**
     * counter Sum Check Variable Modify
     *
     * @param counterSum
     */
    private fun counterSumCheckVariableModify(counterSum: Int) {
        NordicObject.counterSumCheckIMU = (counterSum + 1) % 4096
    }

    /**
     * write To Array For CSV If Was Error
     *
     * @param emptyArray
     * @param formatted
     */
    private fun writeToArrayForCSVIfWasError(emptyArray: ArrayList<Long?>, formatted: String) {
        //  Log.d(TAGDATAPROC, "class: $TAG --> most értünk a végére a feladatnak nézzük hán eleme van HIBAVOLT")
        datasToCSVIMU.add(Pair(formatted, emptyArray))
        datasToCSVSizeCheck(datasToCSVIMU)
    }

    /**
     * datas To CSV Size Check
     *
     * @param datasToCSV
     */
    private fun datasToCSVSizeCheck(datasToCSV: MutableList<Pair<String, ArrayList<Long?>>>) {
        if (GlobalRes.writeFileActive) {
            writeFile(datasToCSV)
        }

        datasToCSVIMU.clear()
    }

    /**
     * write File
     *
     * @param datasToCSV
     * @return
     */
    private fun writeFile(datasToCSV: MutableList<Pair<String, ArrayList<Long?>>>): Boolean {

        val formatted: String
        val CSV_HEADER = "Time;Counter;Data 6x16bit"

        val datas = datasToCSV

        //   Log.d(TAG, "data mérete: ${datas.size}")

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
            val fileName = "IMU_$formatted.csv"

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
                        //                 Log.d(TAGDATAPROC, "filewrite->fileName : $fileName")
                    } else {
                        fileWriter = FileWriter(file, true)
                    }
                    try {

                        //     f = open(file, 'a').write(what)
                        for (data in datas) {
                            //                   Log.d(TAGDATAFILEWRITE, "key: ${data.first}")

                            fileWriter.append(data.first)
                            data.second.forEach {
                                //                     Log.d(TAGDATAFILEWRITE, "value: $it")
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
                            //               Log.d(TAGDATAFILEWRITE, "Error ${e.message}")
                            e.printStackTrace()
                            return false

                        }
                    }
                }
                Environment.MEDIA_MOUNTED_READ_ONLY -> {
                    // csak olvasható
                    //     Log.d(TAGDATAFILEWRITE, "Error csak olvasható a tár!!!")
                    return false
                }
                else -> {
                    // Se írható se olvasható
                    //   Log.d(TAGDATAFILEWRITE, "Nem írható se nem olvasható!")
                    return false
                }
            }
        }
        return true
    }

    /**
     * write File Error
     *
     * @param time
     * @param type
     * @param message
     * @return
     */
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
                    Log.d(TAGDATAPROC, "filewrite->fileName : $fileName")
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
                        Log.d(TAGDATAFILEWRITE, "Error ${e.message}")
                        e.printStackTrace()
                        return false

                    }
                }
            }
            Environment.MEDIA_MOUNTED_READ_ONLY -> {
                // csak olvasható
                Log.d(TAGDATAFILEWRITE, "Error csak olvasható a tár!!!")
                return false
            }
            else -> {
                // Se írható se olvasható
                Log.d(TAGDATAFILEWRITE, "Nem írható se nem olvasható!")
                return false
            }
        }
        return true
    }

    /**
     * get Accelero
     *
     * @return
     */
    fun getAccelero(): LongArray {
        return accelero
    }

    /**
     * get Magneto
     *
     * @return
     */
    fun getMagneto(): LongArray {
        return magneto
    }

    /**
     * get Gyroscope
     *
     * @return
     */
    fun getGyroscope(): LongArray {
        return gyroscope
    }

    /**
     * set Accelero
     *
     * @param accelero
     */
    fun setAccelero(accelero: LongArray) {
        this.accelero = accelero
    }

    /**
     * set Magneto
     *
     * @param magneto
     */
    fun setMagneto(magneto: LongArray) {
        this.magneto = magneto
    }

    /**
     * set Gyroscope
     *
     * @param gyroscope
     */
    fun setGyroscope(gyroscope: LongArray) {
        this.gyroscope = gyroscope
    }

    /**
     * get Imu Value
     *
     * @return
     */
    fun getImuValue(): ArrayList<IMUDataValue> {
        return imuValues
    }

    /**
     * set Imu Values
     *
     * @param value
     */
    fun setImuValues(value: ArrayList<IMUDataValue>) {
        this.imuValues = value
    }
}


