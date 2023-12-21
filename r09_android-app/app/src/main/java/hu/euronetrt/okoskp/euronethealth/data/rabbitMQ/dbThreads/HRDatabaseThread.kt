package hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.dbThreads

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import hu.euronetrt.okoskp.euronethealth.GlobalRes.connectedDevice
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.data.AppDatabase
import hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.RabbitMQ

/**
 * HR Database Thread
 *
 * @property mContext
 */
class HRDatabaseThread(private var mContext: Context) : Thread() {

    companion object {
        val TAG = "HRDatabaseThread"
    }

    var running = true
    private var msg = ""
    override fun run() {
        val bleService = mContext as BluetoothLeService
        val rabbitMQ = RabbitMQ(mContext)  //--> bleService contextusa

        val gson = Gson()

        while (running) {
            do {
                    Log.d(TAG, "$TAG +do {:  run ")
                var isWork = true
                val deviceInformation = bleService.getDeviceInformation()
                //  Log.d(TAG, " + deviceInformation  ${deviceInformation.size}")
                if (connectedDevice && !deviceInformation["FIRMWARE"].isNullOrEmpty() && !deviceInformation["HARDWARE"].isNullOrEmpty()) {
                    //   Log.d(TAG, "$TAGDBTHREAD +if (connectedDevice) {:  run ")
                    val idListMustUpdateInDB = arrayListOf<Long?>()

                    val hrListFromDB = AppDatabase.getInstance(mContext).hrDatabaseDao().getDataWaitingToBeSend()

                    Log.d(TAG, " + hrListFromDB  ${hrListFromDB.size}")
                    if (hrListFromDB.isNotEmpty()) {
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
                            //Log.d(TAG, "$TAGDBTHREAD + channel  OK! while run  while (i < hrListFromDB.size)")
                            while (i < hrListFromDB.size) {
                                if (!running) {
                                    return
                                }
                                // Log.d(TAG, "$TAGDBTHREAD + deviceInformation rabbit ${deviceInformation}")
                                /*    hrListFromDB[i].setFirmwareVersion(deviceInformation["FIRMWARE"]!!)
                            hrListFromDB[i].setHardwareVersion(deviceInformation["HARDWARE"]!!)
                            hrListFromDB[i].setMacId(deviceInformation["MAC"]!!)
                            hrListFromDB[i].setDeviceId(deviceInformation["DEVICEID"]!!)
                            hrListFromDB[i].setUserId(deviceInformation["USERID"]!!)
                            hrListFromDB[i].setSoftwareVersion(BuildConfig.VERSION_NAME)*/

                                hrListFromDB[i].setType("HR")

                                val item = hrListFromDB[i]
                                // Log.d(TAG, "params[0]size  ${params.size}")

                                val id = item.getUid()

                                idListMustUpdateInDB.add(id)

                                hrListFromDB[i].setSentAt(System.currentTimeMillis())

                                /*obj to sring to jsonElement remove propetry to new string */
                                var hrJson = gson.toJson(item)

                                Log.d(TAG, "hrJson  $hrJson")
                                try {
                                    rabbitMQ.queue(hrJson)
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
                                Log.d(TAG, " idListMustUpdateInDB + id item: ${it}")
                                AppDatabase.getInstance(mContext).hrDatabaseDao().itemDataSentAtUpdate(System.currentTimeMillis(), it!!)
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
                }catch (e: InterruptedException) {
                    msg = "INTEUPTED"
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