package hu.euronetrt.okoskp.euronethealth.data.dataClasses

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.TypeConverters
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.bluetooth.objects.NordicObject
import hu.euronetrt.okoskp.euronethealth.bluetooth.objects.NordicObject.datasToCSVOTHER
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
 * OTHER Data class
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
@Entity(tableName = "OTHERData")
@TypeConverters(CollectableTypeConverter::class)
class OTHERData(
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
        CollectableType.OTHER,
        measuredAt,
        arrivedAt,
        sentAt,
        value) {

    companion object {
        private val TAGDATAFILEWRITE = "TAGDATAFILEWRITE"
        private val TAG = "OTHERData"
        private val TAGDATAPROC = "TAGDATAPROC"
        private val FIRS_SECOUND_DATABYTE = 4     /* adatcsomag első két bytja = fix 16 bit resolution  (1 byte 8 bit)*/
    }

    @Ignore
    private lateinit var mcontext: Context

    constructor( uid: Long?,
                 macId: String,
                 firmwareVersion: String,
                 softwareVersion: String,
                 hardwareVersion: String,
                 deviceId: String,
                 userId: String,
                 measuredAt: Long,
                 arrivedAt: Long,
                 sentAt: Long?,
                 value: ByteArray, mContext: Context) : this(uid,
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

    @Ignore
    private lateinit var IBIAlgorithmArrayResult: ArrayList<Long?>


    @SuppressLint("SimpleDateFormat")
    override fun prepareParsedValues() {
        IBIAlgorithmArrayResult = ArrayList()
        if (!DataBroadcaster.getInstance(mcontext).getStartedApp()) {
            //false --> már futhatat az app

            if (getUid() == null) {

                val emptyArray = ArrayList<Long?>()
                val resolutionBytesPerValue: MutableMap<Int, Int> = mutableMapOf()
                val resolutionBitsPerValue: MutableMap<Int, Int> = mutableMapOf()
                val mask: MutableMap<Int, Long> = mutableMapOf()
                val formatted: String
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

                mask[4] = 0xF
                mask[8] = 0xFF
                mask[10] = 0x3FF
                mask[12] = 0xFFF
                mask[14] = 0x3FFF
                mask[16] = 0xFFFF
                mask[18] = 0x3FFFF
                mask[24] = 0xFFFFFF
                mask[32] = 0xFFFFFFFF


                val oneDataPackByteArray = getValue()
                //       //Log.d(TAGDATAPROC, "class: $TAG --> dataByteArray $dataByteArray")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

                val counterFirstByte = headerPosIntArray[0] shl 4
                val counterSecoundByte = headerPosIntArray[1] shr 4
                //Log.d(TAG, "Binary " + Integer.toBinaryString(headerPosIntArray[1]))
                val counterSum = counterFirstByte + counterSecoundByte
                //     //Log.d(TAGDATAPROC, "class: $TAG --> counterSum $counterSum")

                val length = headerPosIntArray[2] and 0x1F
                //Log.d(TAG, "length  $length")

                val resolution = headerPosIntArray[2] shr 5 and 0xFF

                val dataByte = resolutionBytesPerValue[resolution]!!
                val dataBit = resolutionBitsPerValue[resolution]!!

                payloadSize = dataByte * length

                //val type = headerPosIntArray[1] and 7
                if (charValuesToPositive.size >= payloadSize + 4) {

                     //Log.d(TAG, "charValuesToPositive  $charValuesToPositive")
                    /*
                     * CRC Adat ellenőrzés hogy nem e sérült a csomag.
                     */
                    val crcIdx = 3 + payloadSize
                    val crcValue = charValuesToPositive[crcIdx] and 0xFF
                    //Log.d(TAG, "charValuesToPositive : $crcValue")

                    var dataSum = 0

                    for (i in 0 until crcIdx) {
                        dataSum += charValuesToPositive[i]
                    }

                    /*
                     * CRC értéket nem vesszük bele a számításba
                     */
                    dataSum = dataSum and 0xFF
                    //Log.d(TAG, "dataSum $dataSum")
                    //Log.d(TAG, "dataSum3 $crcValue")

                    if (dataSum == crcValue) {
                        /**
                         * Az ellenőrző méretek azonosak
                         */

                         //Log.d(TAG, "counterSum:  $counterSum   $length  ${NordicObject.counterSumCheckOTHER}")
                        // Működési elv: A counterSumCheckOTHER vizsgálata után szabad csak módosítani,
                        // szükséges az iftől függetlenül. azonban else esetén el kell return-ölni a csomaggal.
                        // A legelső esetben ez az if az else ágba fut viszont beállítódik jól a számláló,
                        // gyakorlatilag az első csomagot mindig eldobjuk, beáldozzuk a számláló beállítása miatt így nem kell külön
                        // változóban flag-es megoldással tárolni hogy ha az első adat jön nem baj ha nem egyezik meg az érték.
                        // tehát első csomag eldobásra kerül a kounter beállítódik ezek után már csak akkor kell a return ha tényleg rossz adat jön.
                        // a counter nem állítható az if előtt viszont utána nem futna rá mert az eslő csomag mindig az else ágba kerül.

                        if (NordicObject.counterSumCheckOTHER == counterSum) {
                            counterSumChechVariableModify(counterSum)
                        } else {
                            datasToCSVOTHER.add(Pair(formatted,emptyArray))
                            datasToCSVSizeCheck(datasToCSVOTHER)
                            //Log.d(TAG, "Elleorzes counterSum error:  $counterSum")
                           // counterSumChechVariableModify(counterSum)
                            // Hibás csomag megjelölése a csv-be
                            datasToCSVOTHER.add(Pair(formatted,emptyArray))
                            writeFileError(formatted, "IMU", "--> A countersum ellenőrzése során hiba lépett fel. oldCountersum: ${NordicObject.counterSumCheckOTHER} newCounterSum: $counterSum length: $length")
                          //  datasToCSVOTHER[formatted] = emptyArray
                            counterSumChechVariableModify(counterSum)
                            return
                        }
                    }else {
                        /*Hiba lépett fel a datasum - crc ellenőrzés során
                         //Log.d(TAG, "datasum!=crc")*/
                        //Log.d(TAGDATAPROC, "class: $TAG -->ERROR-OTHER Hiba lépett fel a datasum - crc ellenőrzés során")
                        // beírjuk az üres sort hogy tudjuk maradt ki itt adat
                        writeToArrayForCSVIfWasError(emptyArray, formatted)
                        writeFileError(formatted,"OTHER","A datasum - crc ellenőrzése során hiba lépett fel. datasum:$dataSum  crc: $crcValue  oldCountersum: ${NordicObject.counterSumCheckOTHER} newCounterSum: $counterSum length: $length")
                        return
                    }
                    /**
                     * Mivel a real dataMultipleList size csak a méret ezért az helyes indexméret
                     * miatt hozzá kell adni a kezdőt indexet hogy onnantól kell az x db byte
                     * */

                    //Log.d(TAG, "adatcsomag counter $counterSum")
                    val realData = oneDataPackByteArray.copyOfRange(3, 3 + payloadSize)
                    val realDataToIntArray = realData.map { it.toInt() }
                    val realDataPositivIntArray = realDataToIntArray.map { if (it < 0) it + 256 else it }
                    //Log.d(TAG, "realDataPosIntArray  $realDataPosIntArray")

                    var idx = 0
                    val subidx = 0
                    val s: String

                    //első két byte egyként kezelve
                    var value = (realDataPositivIntArray[idx] shl (8)).toLong()
                    value += realDataPositivIntArray[idx+1]


                    resultArray.add(value)
                    idx += dataByte * 2  // mert 2 adatot vettem ezért szor 2

                    when (value) {
                        0x1000.toLong() /*Státusz lekérdezés*/ -> {
                            val methodValue = readIntValue(realDataPositivIntArray, idx, dataByte, dataBit, mask)
                            resultArray.add(methodValue)

                            //Log.d(TAG, "számolt methodValue --> value:  $methodValue")


                            idx += dataByte
                            val methodMillisecValue = readIntValue(realDataPositivIntArray, idx, dataByte, dataBit, mask)
                            resultArray.add(methodMillisecValue)
                            //Log.d(TAG, "szímolt  methodMillisecValue --> value:  $methodMillisecValue")

                            when (methodValue) {
                                'A'.toLong() -> {
                                    //Log.d(TAG, "class: $TAG --> value: $methodValue  --> Autokorreláció")
                                }
                                'M'.toLong() -> {
                                    //Log.d(TAG, "class: $TAG --> value: $methodValue  --> Mozgóátlag")
                                }
                            }
                            when(methodMillisecValue){
                                '1'.toLong() -> {
                                    //Log.d(TAG, "class: $TAG --> value: $methodMillisecValue   --> 1 ezredmásodperc")
                                }
                                '5'.toLong() -> {
                                    //Log.d(TAG, "class: $TAG --> value: $methodMillisecValue   --> 5 ezredmásodperc")
                                }
                            }
                        }
                        0x1002.toLong() /*Gomb nyomás*/ -> {

                            var upperFourBitValue: Long = 0
                            var lowerFourBitValue: Long = 0
                            val lastByteValue: Long

                            val positiv: Boolean = (realDataPositivIntArray[idx + (dataByte - 1)] and (1 shl (((dataBit - 1) % 8))) == 0)
                            //Log.d(TAG, "megertes:positive- > $positiv idx -> $idx, dataByte -> $dataByte  dataBit-> $dataBit")

                            upperFourBitValue += realDataPositivIntArray[idx] shr 4

                            // Negítiv esetén maszkolni kell
                            if (!positiv) {
                                upperFourBitValue = -1 * (mask[dataBit]!! - upperFourBitValue + 1)
                            }
                            //Log.d(TAG, "class: $TAG --> felső 4 bit value: $upperFourBitValue   --> ")
                            resultArray.add(upperFourBitValue)
//-----------------------------------------------------------UPPER 4 BIT -----------------------------------------------
//-----------------------------------------------------------LOWER 4 BIT -----------------------------------------------
                            var iLower = 0

                            while (iLower != dataByte) {
                                //Log.d(TAG, "realDataPositivIntArray- > $realDataPositivIntArray")
                                lowerFourBitValue = realDataPositivIntArray[idx + iLower].toLong() and mask[4]!!
                                iLower++
                            }

                            // Negítiv esetén maszkolni kell
                            if (!positiv) {
                                lowerFourBitValue = -1 * (mask[dataBit]!! - lowerFourBitValue + 1)
                            }
                            //Log.d(TAG, "class: $TAG --> alsó 4 bit value: $lowerFourBitValue   --> ")
                            resultArray.add(lowerFourBitValue)
//-----------------------------------------------------------LOWER 4 BIT -----------------------------------------------
                            idx += dataByte  //utolsó 8 bites adat

                            lastByteValue = readIntValue(realDataPositivIntArray, idx, dataByte, dataBit, mask)
                            resultArray.add(lastByteValue)

                            //Log.d(TAG, "class: $TAG --> utolsó 8 bit value: $lastByteValue   --> ")
                        }
                    }

                    s = formatted + "%02d".format(subidx)
                    val copyArray = ArrayList<Long?>(resultArray)
                    copyArray.add(0,counterSum.toLong())
                    datasToCSVOTHER.add(Pair(s,copyArray))
                    resultArray.clear()

                    datasToCSVSizeCheck(datasToCSVOTHER)
                } else {
                    // beírjuk az üres sort hogy tudjuk maradt ki itt adat
                    //Log.d(TAGDATAPROC, "class: $TAG --> ERROR-OTHER A csomag ellenőrzés során hiba lépett fel. csomaghossz < payloadSize + 4")
                    // beírjuk az üres sort hogy tudjuk maradt ki itt adat
                    ByteArray(0)
                    writeToArrayForCSVIfWasError(emptyArray, formatted)
                    writeFileError(formatted,"IBI","A csomag ellenőrzés során hiba lépett fel. csomaghossz < payloadSize  csomaghossz: ${charValuesToPositive.size} sizeAll+4: ${payloadSize+4}")
                    return
                }
            }
        }
    }

    /**
     * read Int Value calculate
     *
     * @param realDataPositivIntArray
     * @param idx
     * @param dataByte
     * @param dataBit
     * @param mask
     * @return
     */
    private fun readIntValue(realDataPositivIntArray: List<Int>, idx: Int, dataByte: Int, dataBit: Int, mask: MutableMap<Int, Long>): Long {
        var value: Long = 0

        val positiv: Boolean = (realDataPositivIntArray[idx + (dataByte - 1)] and (1 shl (((dataBit - 1) % 8))) == 0)
        var i = 0

        while (i != dataByte) {
            //Log.d(TAG, "realDataPosIntArray[pos+i] " + realDataPosIntArray[pos + i])
            value += realDataPositivIntArray[idx + i] shl (i * 8)
            i++
        }

        // Negítiv esetén maszkolni kell
        if (!positiv) {
            value = -1 * (mask[dataBit]!! - value + 1)
        }

        return value
    }

    /**
     * counter Sum Chech Variable Modify
     *
     * @param counterSum
     */
    private fun counterSumChechVariableModify(counterSum: Int) {
        NordicObject.counterSumCheckOTHER = (counterSum + 1/*length*/) % 4096
    }

    /**
     * write To Array For CSV If Was Error
     *
     * @param emptyArray
     * @param formatted
     */
    private fun writeToArrayForCSVIfWasError(emptyArray: ArrayList<Long?>, formatted: String) {
        datasToCSVOTHER.add(Pair(formatted,emptyArray))
        datasToCSVSizeCheck(datasToCSVOTHER)
    }

    /**
     * datas To CSV Size Check
     *
     * @param datasToCSV
     */
    private fun datasToCSVSizeCheck(datasToCSV: MutableList<Pair<String, ArrayList<Long?>>>) {
        if(GlobalRes.writeFileActive) {
            writeFile(datasToCSV)
        }
        datasToCSVOTHER.clear()
    }

    /**
     * ByteArray to Hex String
     */
    fun ByteArray.toHexString() = joinToString(" ") { "%02x".format(it) }

    /**
     * write File
     *
     * @param datasToCSV
     */
    @SuppressLint("SimpleDateFormat")
    private fun writeFile(datasToCSV: MutableList<Pair<String, ArrayList<Long?>>>) {
        //Log.d(TAG, "filewrite->writeFile run")
        val formatted: String
        val CSV_HEADER = "Time;Counter;Data"

        val datas = datasToCSV


        if (datasToCSV.isNotEmpty() /*&& datas.size > MinDataNumber*/) {
            formatted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val current = LocalDateTime.now()

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")
                current.format(formatter)
            } else {
                val current = System.currentTimeMillis()

                val formatter = SimpleDateFormat("yyyy-MM-dd HH")
                formatter.format(current)
            }

            val hasSDCard = Environment.getExternalStorageState()
            val fileName = "Other_$formatted.csv"

            when (hasSDCard) {
                Environment.MEDIA_MOUNTED -> {

                    // Írható olvasható
                    val fileWriter: FileWriter?

                    val folder = File(Environment.getExternalStorageDirectory().toString() + "/Euronet/")

                    if (!folder.exists()){
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
                        //Log.d(TAG, "filewrite->fileName : $fileName")
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
                                when (it) {
                                    0x1002.toLong() -> {
                                        fileWriter.append("Gombnyomás")
                                    }
                                    0x1000.toLong() -> {
                                        fileWriter.append("Státusz lekérdezés")
                                    }
                                    'A'.toLong() -> {
                                        fileWriter.append("Autokorreláció")
                                    }
                                    'M'.toLong() -> {
                                        fileWriter.append("Mozgóátlag")
                                    }
                                    '1'.toLong() -> {
                                        fileWriter.append("1 ezredmásodperc")
                                    }
                                    '5'.toLong() -> {
                                        fileWriter.append("5 ezredmásodperc")
                                    }
                                    else -> {
                                        fileWriter.append(it.toString())
                                    }
                                }
                            }
                            fileWriter.append('\n')
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try {
                            fileWriter.flush()
                            fileWriter.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    /**
     * write File Error
     *
     * @param time
     * @param type
     * @param message
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    private fun writeFileError(time: String, type: String, message: String): Boolean {
        val formatted: String
        val csvHeader = "Time;Type;Message"

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
                    fileWriter.append(csvHeader)
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
                    //Context kell majd ide
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
}
