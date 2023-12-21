package hu.euronetrt.okoskp.euronethealth.asyncTasks

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import hu.euronetrt.okoskp.euronethealth.data.CollectableType
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Data File Write async task class
 *
 * @property myContext
 * @property type
 * @property callback
 */
class DataFileWrite(private val myContext: Context, private val type: Int, private val callback: (String) -> Unit) : AsyncTask<MutableList<Pair<String, ArrayList<Long?>>>, Void, String>() {

    private var formatted = ""
    private var CSV_HEADER = ""

    companion object {
        val TAG = "DATAFILEWRITE"
    }

    @SuppressLint("SimpleDateFormat", "SetWorldReadable", "SetWorldWritable")
    override fun doInBackground(vararg params: MutableList<Pair<String, ArrayList<Long?>>>): String {

        val datas = params[0]
        val formatted: String

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
            var fileName = ""
            when (type) {
                CollectableType.PPG.type -> {
                    fileName = "PPG_$formatted.csv"
                    CSV_HEADER = "Time;Counter;Data"
                }
                CollectableType.IBI.type -> {
                    fileName = "IBI_$formatted.csv"
                    CSV_HEADER = "Time;Counter;Data"
                }
                CollectableType.IMU.type -> {
                    fileName = "IMU_$formatted.csv"
                    CSV_HEADER = "Time;Counter;Data 6x16bit"
                }
                CollectableType.STEP.type -> {
                    fileName = "Step_$formatted.csv"
                    CSV_HEADER = "Time;Counter;Step_Timestamp"
                }
                CollectableType.OTHER.type -> {
                    fileName = "Other_$formatted.csv"
                    CSV_HEADER = "Time;Counter;Data"
                }
            }

            Log.d(TAG, "filename: $fileName")

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

                    val file = File(folder, fileName)

                    if (!file.exists()) {
                        file.setWritable(true)
                        file.setReadable(true)
                        file.setExecutable(true)

                        fileWriter = FileWriter(file)
                        fileWriter.append(CSV_HEADER)
                        fileWriter.append('\n')
                        Log.d(TAG, "filewrite->fileName : $fileName")
                    } else {
                        fileWriter = FileWriter(file, true)
                    }
                    try {
                        when (type) {
                            CollectableType.PPG.type -> {
                                for (data in datas) {
                                    Log.d(TAG, "key: ${data.first}")
                                    fileWriter.append(data.first)
                                    data.second.forEach {
                                        fileWriter.append(';')
                                        fileWriter.append(it.toString())
                                    }
                                    fileWriter.append('\n')
                                }
                            }
                            CollectableType.IBI.type -> {
                                for (data in datas) {
                                    Log.d(TAG, "key: ${data.first}")
                                    fileWriter.append(data.first)
                                    data.second.forEach {
                                        fileWriter.append(';')
                                        fileWriter.append(it.toString())
                                    }
                                    fileWriter.append('\n')
                                }
                            }
                            CollectableType.IMU.type -> {
                                for (data in datas) {
                                    Log.d(TAG, "key: ${data.first}")
                                    fileWriter.append(data.first)
                                    data.second.forEach {
                                        fileWriter.append(';')
                                        fileWriter.append(it.toString())
                                    }
                                    fileWriter.append('\n')
                                }
                            }
                            CollectableType.STEP.type -> {
                                for (data in datas) {
                                    Log.d(TAG, "key: ${data.first}")
                                    fileWriter.append(data.first)
                                    data.second.forEach {
                                        fileWriter.append(';')
                                        fileWriter.append(it.toString())
                                    }
                                    fileWriter.append('\n')
                                }
                            }
                            CollectableType.OTHER.type -> {
                                for (data in datas) {
                                    Log.d(TAG, "key: ${data.first}")
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
                            }
                        }

                        for (data in datas) {
                            Log.d(TAG, "key: ${data.first}")
                            fileWriter.append(data.first)
                            data.second.forEach{
                                fileWriter.append(';')
                                fileWriter.append(it.toString())
                            }
                            fileWriter.append('\n')
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return "RESULT_FAIL"
                    } finally {
                        try {
                            fileWriter.flush()
                            fileWriter.close()
                        } catch (e: IOException) {
                            Log.d(TAG, "Error ${e.message}")
                            e.printStackTrace()
                            return "RESULT_FAIL"
                        }
                    }
                }
                Environment.MEDIA_MOUNTED_READ_ONLY -> {
                    // csak olvasható
                    Log.e(TAG, "Error csak olvasható a tár!!!")
                    return "RESULT_FAIL"
                }
                else -> {
                    // Se írható se olvasható
                    Log.e(TAG, "Nem írható se nem olvasható!")
                    return "RESULT_FAIL"
                }
            }
        }
        return "RESULT_OK"
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        callback.invoke(result!!)
    }
}
