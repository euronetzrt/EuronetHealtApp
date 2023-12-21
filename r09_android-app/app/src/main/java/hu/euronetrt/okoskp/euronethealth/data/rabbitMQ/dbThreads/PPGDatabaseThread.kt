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
 *  PPG Database Thread
 *
 * @property mContext
 */
class PPGDatabaseThread(private var mContext: Context) : Thread() {

    companion object {
         val TAG = "PPGDatabaseThread"
    }


    var running = true

    override fun run() {
        val bleService = mContext as BluetoothLeService
        val rabbitMQ = RabbitMQ(mContext)  //--> bleService contextusa

        val gson = Gson()

        while (running) {
                do {
                    Log.d(TAG, "$TAG +do {:  run ")
                    var isWork = true
                    val deviceInformation = bleService.getDeviceInformation()
                      Log.d(TAG, " + deviceInformation  ${deviceInformation.size}")
                    if (connectedDevice && !deviceInformation["FIRMWARE"].isNullOrEmpty() && !deviceInformation["HARDWARE"].isNullOrEmpty()) {
                        //   Log.d(TAG, "$TAGDBTHREAD +if (connectedDevice) {:  run ")
                        val idListMustUpdateInDB = arrayListOf<Long?>()

                        val ppgListFromDB = AppDatabase.getInstance(mContext).ppgValueDatabaseDao().getDataWaitingToBeSend()

                        Log.d(TAG, "+ ppgListFromDB  ${ppgListFromDB.size}")
                        if (ppgListFromDB.isNotEmpty()) {
                            val conn = rabbitMQ.connection()
                            var channel: Boolean
                            if (conn) {
                                channel = rabbitMQ.createChannel()
                                 Log.d(TAG, "$TAG + conn  OK!")
                            } else {
                                Log.d(TAG, " +channel error")
                                isWork = false //error rabbit
                                continue
                            }

                            if (channel) {
                                Log.d(TAG, "channel : $channel")
                                /**
                                 * 25 adott típushoz tartozó adat, pl 25. ppg amihez x darab value tartozik. (ppg esetén lehet itt akár 625 sor is.)
                                 **/
                                var i = 0

                                while (i < ppgListFromDB.size) {
                                    Log.d(TAG, "while (i < ppgListFromDB.size) {")
                                    if(!running){
                                        return
                                    }
                                    // Log.d(TAG, "$TAGDBTHREAD + deviceInformation rabbit ${deviceInformation}")
                                    ppgListFromDB[i].setType("PPG")

                                    Log.d(TAG, "whileon belül")
                                    val item = ppgListFromDB[i]

                                    val id = item.getParentDataUid()
                                    val findElement = idListMustUpdateInDB.find { s -> s == id }
                                    if (findElement == null) { //az elem nincs még bent a listában
                                        idListMustUpdateInDB.add(id)
                                    }

                                    ppgListFromDB[i].setSentAt(System.currentTimeMillis())

                                    /*obj to sring to jsonElement remove propetry to new string */

                                    var ppgJson = gson.toJson(item)
                                    val jsonObj = JsonParser().parse(ppgJson) as JsonObject
                                    jsonObj.remove("parentDataUid")
                                    ppgJson = gson.toJson(jsonObj)

                                    try {
                                        Log.d(TAG, "küldi : $ppgJson")
                                        rabbitMQ.queue(ppgJson)
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

                            if(!running){
                                return
                            }

                            Log.d(TAG, " idListMustUpdateInDB + size: ${idListMustUpdateInDB.size}")
                            Log.d(TAG, " iinterupted: ${currentThread().isInterrupted}")
                            if (isWork) {
                                Log.d(TAG, "Start update time :${System.currentTimeMillis()}")
                                //       idListMustUpdateInDB.forEach {
                                //        Log.d(TAG, "$PPGDatabaseThread idListMustUpdateInDB + id item: ${it}")
                                //       AppDatabase.getInstance(mContext).ppgDatabaseDao().itemDataSentAtUpdate(System.currentTimeMillis(), it!!)
                                AppDatabase.getInstance(mContext).ppgDatabaseDao().updateItemPlaces(System.currentTimeMillis(), idListMustUpdateInDB)
                                // }
                                Log.d(TAG, "END update time :${System.currentTimeMillis()}")
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