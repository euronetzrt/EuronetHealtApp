package hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.dbThreads

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import hu.euronetrt.okoskp.euronethealth.GlobalRes.connectedDevice
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.data.AppDatabase
import hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.RabbitMQ


/**
 * IBI Database Thread
 *
 * @property mContext
 */
class IBIDatabaseThread(private var mContext: Context) : Thread() {

    companion object {
         val TAG = "IBIDatabaseThread"
    }

    var running = true

    override fun run() {
        val bleService = mContext as BluetoothLeService
        val rabbitMQ = RabbitMQ(mContext)  //--> bleService contextusa


        val gson = Gson()

        while (running) {

            do {
                //     Log.d(TAG, "$TAGDBTHREAD +do {:  run ")
                var isWork = true
                val deviceInformation = bleService.getDeviceInformation()

                if (connectedDevice && !deviceInformation["FIRMWARE"].isNullOrEmpty() && !deviceInformation["HARDWARE"].isNullOrEmpty()) {

                    val idListMustUpdateInDB = arrayListOf<Long?>()
                    val ibiListFromDB = AppDatabase.getInstance(mContext).ibiValueDatabaseDao().getDataWaitingToBeSend()

                    //    Log.d(TAG, " + ibiListFromDB  ${ibiListFromDB.size}")
                    if (ibiListFromDB.isNotEmpty()) {
                        val conn = rabbitMQ.connection()
                        var channel: Boolean
                        if (conn) {
                            channel = rabbitMQ.createChannel()
                            // Log.d(TAG, "$TAGDBTHREAD + conn  OK!")
                        } else {
                            Log.d(TAG, " +channel error")
                            isWork = false //error rabbit
                            continue
                        }

                        if (channel) {
                            var i = 0
                            //Log.d(TAG, "$TAGDBTHREAD + channel  OK! while run  while (i < ibiListFromDB.size)")
                            while (i < ibiListFromDB.size) {
                                if (!running) {
                                    return
                                }
                                // Log.d(TAG, "$TAGDBTHREAD + deviceInformation rabbit ${deviceInformation}")

                                //              ibiListFromDB[i].setType("IBI")

                                val item = ibiListFromDB[i]
                                // Log.d(TAG, "params[0]size  ${params.size}")

                                val id = item.getParentDataUid()
                                val findElement = idListMustUpdateInDB.find { s -> s == id }
                                if (findElement == null) { //az elem nincs még bent a listában
                                    idListMustUpdateInDB.add(id)
                                }

                                ibiListFromDB[i].setSentAt(System.currentTimeMillis())

                                var ibiJson = gson.toJson(item)
                                val jsonObj = JsonParser().parse(ibiJson) as JsonObject
                                jsonObj.remove("parentDataUid")
                                ibiJson =  gson.toJson(jsonObj)

                                 Log.d(TAG, "myJson  $ibiJson")
                                try {
                                    rabbitMQ.queue(ibiJson)
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
                                //         Log.d(TAG, " idListMustUpdateInDB + id item: ${it}")
                                AppDatabase.getInstance(mContext).ibiDatabaseDao().itemDataSentAtUpdate(System.currentTimeMillis(), it!!)
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