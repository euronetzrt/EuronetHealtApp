package hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.dbThreads

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import hu.euronetrt.okoskp.euronethealth.GlobalRes.connectedDevice
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.data.AppDatabase
import hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.RabbitMQ

/**
 *  STEP Database Thread
 *
 * @property mContext
 */
class STEPDatabaseThread(private var mContext: Context) : Thread() {

    companion object{
         val TAG = "STEPDatabaseThread"
    }

    var running = true
    override fun run() {
        Log.d(TAG," + run+")
        val bleService = mContext as BluetoothLeService
        val rabbitMQ = RabbitMQ(mContext)  //--> bleService contextusa

        val gson = Gson()

        while (running) {
                do {
                    //     Log.d(TAG, "$TAGDBTSTEPEAD +do {:  run ")
                    var isWork = true
                    val deviceInformation = bleService.getDeviceInformation()
                    //  Log.d(TAG, " + deviceInformation  ${deviceInformation.size}")
                    if (connectedDevice && !deviceInformation["FIRMWARE"].isNullOrEmpty() && !deviceInformation["HARDWARE"].isNullOrEmpty()) {
                        //   Log.d(TAG, "$TAGDBTSTEPEAD +if (connectedDevice) {:  run ")
                        val idListMustUpdateInDB = arrayListOf<Long?>()

                        val stepListFromDB = AppDatabase.getInstance(mContext).stepDatabaseDao().getDataWaitingToBeSend()

                        Log.d(TAG, " + stepListFromDB  ${stepListFromDB.size}")
                        if (stepListFromDB.isNotEmpty()) {
                            val conn = rabbitMQ.connection()
                            var channel: Boolean
                            if (conn) {
                                channel = rabbitMQ.createChannel()
                                // Log.d(TAG, "$TAGDBTSTEPEAD + conn  OK!")
                            } else {
                                Log.d(TAG, " +channel error")
                                isWork = false //error rabbit
                                continue
                            }

                            if (channel) {
                                var i = 0
                                //Log.d(TAG, "$TAGDBTSTEPEAD + channel  OK! while run  while (i < stepListFromDB.size)")
                                while (i < stepListFromDB.size) {
                                    if (!running) {
                                        return
                                    }
                                    // Log.d(TAG, "$TAGDBTSTEPEAD + deviceInformation rabbit ${deviceInformation}")
                                    stepListFromDB[i].setType("STEP")

                                    val item = stepListFromDB[i]
                                    // Log.d(TAG, "params[0]size  ${params.size}")

                                    val id = item.getUid()

                                    idListMustUpdateInDB.add(id)

                                    stepListFromDB[i].setSentAt(System.currentTimeMillis())

                                    var stepJson = gson.toJson(item)

                                    Log.d(TAG, "stepJson  $stepJson")
                                    try {
                                        rabbitMQ.queue(stepJson)
                                    } catch (e: Exception) {
                                        Log.d(TAG, " +Server error!  ${e.message}")
                                        isWork = false
                                        continue
                                    }
                                    i++
                                }
                                try {
                                    rabbitMQ.closeChannel()
                                    rabbitMQ.disConnection()
                                } catch (e: Exception) {
                                    Log.d(TAG, " +SERVER close error: ${e.message}")
                                    isWork = false
                                    continue
                                }
                            } else {
                                try {
                                    rabbitMQ.disConnection()
                                } catch (e: Exception) {
                                    Log.d(TAG, " + SERVER close error: ${e.message}")
                                    isWork = false
                                    continue
                                }
                            }
                            if (!running) {
                                return
                            }
                            Log.d(TAG, " idListMustUpdateInDB + size: ${idListMustUpdateInDB.size}")

                            if (isWork) {
                                idListMustUpdateInDB.forEach {
                                    //                Log.d(TAG, " idListMustUpdateInDB + id item: ${it}")
                                    AppDatabase.getInstance(mContext).stepDatabaseDao().itemDataSentAtUpdate(System.currentTimeMillis(), it!!)
                                }
                            }
                        } else {
                            Log.d(TAG, " + No new data")
                            isWork = false
                            continue
                        }
                    } else {
                        isWork = false
                    }

                } while (isWork)

                run {
                    Log.d(TAG, " + -------!!------SLEEP ----- +")
                    try {
                        sleep(5000)
                    } catch (e: InterruptedException) {
                        Log.d(TAG, " + INTEUPTED EXCEPTION")
                        this.interrupt()
                    }
                }
        }
    }
    fun stopAndInterrupt(){
        running = false
        interrupt()
    }
}