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
import hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.rabbitMQClasses.subIMUData.AccelerometerDataFromDBToRabbitMQ
import hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.rabbitMQClasses.subIMUData.GyroscopeDataFromDBToRabbitMQ

/**
 *  IMU Database Thread
 *
 * @property mContext
 */
class IMUDatabaseThread(private var mContext: Context) : Thread() {

    companion object {
         val TAG = "IMUDatabaseThread"
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
                //    Log.d(TAG, " + deviceInformation  ${deviceInformation.size}")
                if (connectedDevice && !deviceInformation["FIRMWARE"].isNullOrEmpty() && !deviceInformation["HARDWARE"].isNullOrEmpty()) {
                    //   Log.d(TAG, "$TAGDBTHREAD +if (connectedDevice) {:  run ")
                    val idListMustUpdateInDB = arrayListOf<Long?>()

                    val imuListFromDB = AppDatabase.getInstance(mContext).imuValueDatabaseDao().getDataWaitingToBeSend()

                    //        Log.d(TAG, " + imuListFromDB  ${imuListFromDB.size}")
                    if (imuListFromDB.isNotEmpty()) {
                        val conn = rabbitMQ.connection()
                        var channel: Boolean
                        if (conn) {
                            channel = rabbitMQ.createChannel()
                            // Log.d(TAG, "$TAGDBTHREAD + conn  OK!")
                        } else {
                            Log.d(TAG, " +connection error")
                            isWork = false //error rabbit
                            continue
                        }

                        if (channel) {
                            /**
                             * 25 adott típushoz tartozó adat, pl 25. imu amihez x darab value tartozik, jelenleg 5 db 1 imuhoz.
                             * */
                            var i = 0
                            //Log.d(TAG, "$TAGDBTHREAD + channel  OK! while run  while (i < imuListFromDB.size)")
                            while (i < imuListFromDB.size) {
                                if (!running) {
                                    return
                                }
                                val oneLine = imuListFromDB[i]
                                oneLine.setType("IMU")

                                val item = imuListFromDB[i]
                                // Log.d(TAG, "params[0]size  ${params.size}")

                                val id = item.getParentDataUid()
                                val findElement = idListMustUpdateInDB.find { s -> s == id }
                                if (findElement == null) { //az elem nincs még bent a listában
                                    idListMustUpdateInDB.add(id)
                                }

                                imuListFromDB[i].setSentAt(System.currentTimeMillis())

                                val accelerometerData = HashMap<String, Long>()
                                accelerometerData["x"] = oneLine.getAcceleroX()
                                accelerometerData["y"] = oneLine.getAcceleroY()
                                accelerometerData["z"] = oneLine.getAcceleroZ()

                                val gyroscopeData = HashMap<String, Long>()
                                gyroscopeData["x"] = oneLine.getGyroscopeX()
                                gyroscopeData["y"] = oneLine.getGyroscopeY()
                                gyroscopeData["z"] = oneLine.getGyroscopeZ()

                                val magnetometerData = HashMap<String, Long>()
                                magnetometerData["x"] = oneLine.getMagnetometerX()
                                magnetometerData["y"] = oneLine.getMagnetometerY()
                                magnetometerData["z"] = oneLine.getMagnetometerX()

                                val IMUAccelerometer = AccelerometerDataFromDBToRabbitMQ(
                                        oneLine.getMacId(),
                                        oneLine.getFirmwareVersion(),
                                        oneLine.getSoftwareVersion(),
                                        oneLine.getHardwareVersion(),
                                        oneLine.getDeviceId(),
                                        oneLine.getUserId(),
                                        "ACCELEROMETER",
                                        oneLine.getUid(),
                                        oneLine.getMeasuredAt(),
                                        oneLine.getArrivedAt(),
                                        oneLine.getSentAt(),
                                        oneLine.getParentDataUid(),
                                        accelerometerData
                                )

                                val IMUGyroscope = GyroscopeDataFromDBToRabbitMQ(
                                        oneLine.getMacId(),
                                        oneLine.getFirmwareVersion(),
                                        oneLine.getSoftwareVersion(),
                                        oneLine.getHardwareVersion(),
                                        oneLine.getDeviceId(),
                                        oneLine.getUserId(),
                                        "GYROSCOPE",
                                        oneLine.getUid(),
                                        oneLine.getMeasuredAt(),
                                        oneLine.getArrivedAt(),
                                        oneLine.getSentAt(),
                                        oneLine.getParentDataUid(),
                                        gyroscopeData
                                )

                                /*   val IMUMagnetometer = MagnetometerDataFromDBToRabbitMQ(
                                    oneLine.getMacId(),
                                    oneLine.getFirmwareVersion(),
                                    oneLine.getSoftwareVersion(),
                                    oneLine.getHardwareVersion(),
                                    oneLine.getDeviceId(),
                                    oneLine.getUserId(),
                                    "MAGNETOMETER",
                                    oneLine.getUid(),
                                    oneLine.getMeasuredAt(),
                                    oneLine.getArrivedAt(),
                                    oneLine.getSentAt(),
                                    oneLine.getParentDataUid(),
                                    magnetometerData
                            )*/

                                var accelerometerJson = gson.toJson(IMUAccelerometer)
                                val jsonACC: JsonObject = JsonParser().parse(accelerometerJson) as JsonObject
                                jsonACC.remove("parentDataUid")
                                accelerometerJson = gson.toJson(jsonACC)

                                // Log.d(TAG, " acceleroscopeJson  ${acceleroscopeJson}")

                                var gyroscopeJson = gson.toJson(IMUGyroscope)
                                val jsonGYRO: JsonObject = JsonParser().parse(gyroscopeJson) as JsonObject
                                jsonGYRO.remove("parentDataUid")
                                gyroscopeJson = gson.toJson(jsonGYRO)

                                try {
                                    rabbitMQ.queue(accelerometerJson)
                                    rabbitMQ.queue(gyroscopeJson)
                                    //rabbitMQ.queue(magnetometerJson)
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
                                //Log.d(TAG, " idListMustUpdateInDB + id item: ${it}")
                                AppDatabase.getInstance(mContext).imuDatabaseDao().itemDataSentAtUpdate(System.currentTimeMillis(), it!!)
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