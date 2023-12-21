package hu.aut.android.dm01_v11.ui.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import hu.aut.android.dm01_v11.R
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.bluetooth.BindServiceClass
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_HRTHREADRUN
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_IBITHREADRUN
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_IMUTHREADRUN
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_PPGTHREADRUN
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_STEPTHREADRUN
import hu.euronetrt.okoskp.euronethealth.data.rabbitMQ.dbThreads.PPGDatabaseThread
import kotlinx.android.synthetic.main.fragment_device_settings_page.*
import java.util.*

class FragmentDeviceSettingsPage : Fragment() {

    private lateinit var bleService: BluetoothLeService

    private val methodSet = byteArrayOf(0x10, 0x00) //0x1000
    private val time = byteArrayOf(0x00, 0x01) //0x0001
    private val timeZoneOffset = byteArrayOf(0x00, 0x02)//0x0002
    private val status = byteArrayOf(0x00, 0xFE.toByte()) //0x00FE
    private val deviceOff = byteArrayOf(0x00, 0xFF.toByte()) //0x00FF
    private val vibra = byteArrayOf(0x10, 0x01) //0x1001
    private val stepCounterSetZero = byteArrayOf(0x10, 0x03)//0x1002
    private val weariness = byteArrayOf(0x00, 0x10)//0x0010
    private val setSec = byteArrayOf(0x10, 0x02)//0x1002

    companion object {
        val TAG = "FRAGMENT_DEV_GROUP_PAGE"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_device_settings_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bleService = BindServiceClass.getInstance(activity!!).getBleServiceBind()

        id_switch_hr.isChecked = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getBoolean(KEY_HRTHREADRUN, true)
        id_switch_ibi.isChecked = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getBoolean(KEY_IBITHREADRUN, true)
        id_switch_imu.isChecked = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getBoolean(KEY_IMUTHREADRUN, true)
        id_switch_ppg.isChecked = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getBoolean(KEY_PPGTHREADRUN, true)
        id_switch_step.isChecked = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getBoolean(KEY_STEPTHREADRUN, true)


        /**Send data server setting**/
        id_switch_hr.setOnClickListener {

            bleService.hrThreadRun(id_switch_hr.isChecked)
            val KEYHRTHREADRUN = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
            KEYHRTHREADRUN.edit()
                    .putBoolean(KEY_HRTHREADRUN, id_switch_hr.isChecked)
                    .apply()
        }

        id_switch_ibi.setOnClickListener {
            bleService.ibiThreadRun(id_switch_ibi.isChecked)
            val KEYIBITHREADRUN = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
            KEYIBITHREADRUN.edit()
                    .putBoolean(KEY_IBITHREADRUN, id_switch_ibi.isChecked)
                    .apply()
        }

        id_switch_imu.setOnClickListener {

            bleService.imuThreadRun(id_switch_imu.isChecked)
            val KEYIMUTHREADRUN = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
            KEYIMUTHREADRUN.edit()
                    .putBoolean(KEY_IMUTHREADRUN, id_switch_imu.isChecked)
                    .apply()
        }

        id_switch_ppg.setOnClickListener {
            Log.d(PPGDatabaseThread.TAG,"PPGDatabaseThread ${id_switch_ppg.isChecked}")

            val KEYPPGTHREADRUN = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
            KEYPPGTHREADRUN.edit()
                    .putBoolean(KEY_PPGTHREADRUN, id_switch_ppg.isChecked)
                    .apply()
            bleService.ppgThreadRun(id_switch_ppg.isChecked)
        }

        id_switch_step.setOnClickListener {
            bleService.stepThreadRun(id_switch_step.isChecked)
            val KEYSTEPTHREADRUN = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
            KEYSTEPTHREADRUN.edit()
                    .putBoolean(KEY_STEPTHREADRUN, id_switch_step.isChecked)
                    .apply()
        }

        if (GlobalRes.connectedDevice) {

            /**
             * TIME
             * */
            id_time.setOnClickListener {

                val timeNow = Calendar.getInstance().time
                val timeNowLong = timeNow.time
                val timeArray = byteArrayOf(
                        timeNowLong.toByte(),
                        (timeNowLong shr 8).toByte(),
                        (timeNowLong shr 16).toByte(),
                        (timeNowLong shr 24).toByte(),
                        (timeNowLong shr 32).toByte(),
                        (timeNowLong shr 40).toByte(),
                        (timeNowLong shr 48).toByte(),
                        (timeNowLong shr 56).toByte())


                // or you already have long value of date, use this instead of milliseconds variable.
                Log.d(TAG, "WriteChar long to bytearray id_time  ${timeArray.toHexString()}" +
                        " eredeti long: $timeNow" +
                        "   long to date : ${Date(timeNowLong)}.")

                /*ellenőrző fejtés*/
                val l = (timeArray[7].toLong() shl 56
                        or (timeArray[6].toLong() and 0xff shl 48)
                        or (timeArray[5].toLong() and 0xff shl 40)
                        or (timeArray[4].toLong() and 0xff shl 32)
                        or (timeArray[3].toLong() and 0xff shl 24)
                        or (timeArray[2].toLong() and 0xff shl 16)
                        or (timeArray[1].toLong() and 0xff shl 8)
                        or (timeArray[0].toLong() and 0xff))

                Log.d(TAG, "WriteChar bytearray to long id_time }" +
                        " visszafejtett long: $l" +
                        "   visszafejtett date : ${Date(l)}.")

                val sendData = byteArrayOf(
                        time[0], time[1],
                        timeArray[0],
                        timeArray[1],
                        timeArray[2],
                        timeArray[3],
                        timeArray[4],
                        timeArray[5],
                        timeArray[6],
                        timeArray[7],
                        /* CRC helye */   0x00
                )

                writeCharacteristic(sendData,Date(l).toString())
            }

            /**
             * TIME OFFSET
             * */
            id_time_offset.setOnClickListener {

                val tz = TimeZone.getDefault()
                val now = Date()
                val offsetFromUtc = tz.getOffset(now.time) / 1000 / 60

                Log.d(TAG, "local id_time_offset $offsetFromUtc")

                val offsetArray = byteArrayOf(
                        offsetFromUtc.toByte(),
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0
                     )

                /*ellenőrző fejtés*/
                val l = (offsetArray[7].toLong() shl 56
                        or (offsetArray[6].toLong() and 0xff shl 48)
                        or (offsetArray[5].toLong() and 0xff shl 40)
                        or (offsetArray[4].toLong() and 0xff shl 32)
                        or (offsetArray[3].toLong() and 0xff shl 24)
                        or (offsetArray[2].toLong() and 0xff shl 16)
                        or (offsetArray[1].toLong() and 0xff shl 8)
                        or (offsetArray[0].toLong() and 0xff))

                Log.d(TAG, "WriteChar bytearray to long id_time_offset }" +
                        " visszafejtett long: $l" +
                        "   visszafejtett offset : ${Date(l)}.")

                val sendData = byteArrayOf(
                        timeZoneOffset[0], timeZoneOffset[1],
                        offsetArray[0],
                        offsetArray[1],
                        offsetArray[2],
                        offsetArray[3],
                        offsetArray[4],
                        offsetArray[5],
                        offsetArray[6],
                        offsetArray[7],
                        /* CRC helye */   0x00
                )

                writeCharacteristic(sendData,l.toString())
            }

            /**
             * STATUS
             * */
            id_status.setOnClickListener {
                Toast.makeText(activity!!, "Get status", Toast.LENGTH_LONG).show()
                val sendData = byteArrayOf(
                        status[0], status[1],
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        /* CRC helye */   0x00
                )
                writeCharacteristic(sendData,"Get status")
            }

            /**
             * METHOD SET , MOVING AVG
             * */
            id_movingAVG.setOnClickListener {

                var timeBase = 0x00
                var wasErr = false

                when (id_radioButtonGroup.checkedRadioButtonId) {
                    R.id.id_radioButtonONE -> {
                        timeBase = 0x31 //1
                    }
                    R.id.id_radioButtonFIVE -> {
                        timeBase = 0x35 //5
                    }
                    else -> {
                        Toast.makeText(activity!!, "Please select time base!", Toast.LENGTH_LONG).show()
                        wasErr = true
                    }
                }

                if (!wasErr) {
                    val sendData = byteArrayOf(
                            methodSet[0], methodSet[1],
                            0x4D,
                            timeBase.toByte(),
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            /* CRC helye */   0x00
                    )
                    Toast.makeText(activity!!, "METHOD SET , MOVING AVG", Toast.LENGTH_LONG).show()
                    writeCharacteristic(sendData, "METHOD SET --> MOVING AVG -->$timeBase")
                }
            }

            /**
             * METHOD SET , AUTOREGRESSION
             * */
            id_autoregression.setOnClickListener {
                var timeBase = 0x00
                var wasErr = false

                when (id_radioButtonGroup.checkedRadioButtonId) {
                    R.id.id_radioButtonONE -> {
                        timeBase = 0x31 //1
                    }
                    R.id.id_radioButtonFIVE -> {
                        timeBase = 0x35 //5
                    }
                    else -> {
                        Toast.makeText(activity!!, "Please select time base!", Toast.LENGTH_LONG).show()
                        wasErr = true
                    }
                }

                if (!wasErr) {
                    val sendData = byteArrayOf(
                            methodSet[0], methodSet[1],
                            0x41,
                            timeBase.toByte(),
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            0x00,
                            /* CRC helye */   0x00
                    )
                    Toast.makeText(activity!!, "METHOD SET , AUTOREGRESSION", Toast.LENGTH_LONG).show()
                    writeCharacteristic(sendData,"METHOD SET --> AUTOREGRESSION --> $timeBase")
                }
            }

            /**
             * VIBRA
             * */
            id_vibra.setOnClickListener {
                Toast.makeText(activity!!, "Set vibra", Toast.LENGTH_LONG).show()
                val sendData = byteArrayOf(
                        vibra[0], vibra[1],
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        /* CRC helye */   0x00
                )
                writeCharacteristic(sendData,"Set vibra")
            }

            /**
             * STEP COUNTER SET ZERO
             * */
            id_stepCounterZero.setOnClickListener {
                Toast.makeText(activity!!, "Set zero", Toast.LENGTH_LONG).show()
                val sendData = byteArrayOf(
                        stepCounterSetZero[0], stepCounterSetZero[1],
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        /* CRC helye */   0x00
                )
                writeCharacteristic(sendData,"STEP COUNTER SET ZERO")
            }

            /**
             * OFF
             * */
            id_off.setOnClickListener {
                Toast.makeText(activity!!, "Set off", Toast.LENGTH_LONG).show()
                val sendData = byteArrayOf(
                        deviceOff[0], deviceOff[1],
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        0x00,
                        /* CRC helye */   0x00
                )
                writeCharacteristic(sendData,"OFF")
            }

            /**
             * Weariness
             * */
            id_weariness.setOnClickListener {

                val value = id_wearinessNumber

                if (value.text.toString().isEmpty() || value.text.toString().toInt() < 0 || value.text.toString().toInt() > 100) {
                    Toast.makeText(activity!!, "Weariness must be 0-100!", Toast.LENGTH_LONG).show()
                } else {
                    val wearinessLong = value.text.toString().toLong()
                    val wearinessArray = byteArrayOf(
                            wearinessLong.toByte(),
                            (wearinessLong shr 8).toByte(),
                            (wearinessLong shr 16).toByte(),
                            (wearinessLong shr 24).toByte(),
                            (wearinessLong shr 32).toByte(),
                            (wearinessLong shr 40).toByte(),
                            (wearinessLong shr 48).toByte(),
                            (wearinessLong shr 56).toByte())

                    Toast.makeText(activity!!, "Weariness sent ${id_wearinessNumber.text}%", Toast.LENGTH_LONG).show()

                    /*ellenőrző fejtés*/
                    val l = (wearinessArray[7].toLong() shl 56
                            or (wearinessArray[6].toLong() and 0xff shl 48)
                            or (wearinessArray[5].toLong() and 0xff shl 40)
                            or (wearinessArray[4].toLong() and 0xff shl 32)
                            or (wearinessArray[3].toLong() and 0xff shl 24)
                            or (wearinessArray[2].toLong() and 0xff shl 16)
                            or (wearinessArray[1].toLong() and 0xff shl 8)
                            or (wearinessArray[0].toLong() and 0xff))

                    Log.d(TAG, "WriteChar bytearray to long $wearinessLong }" +
                            " visszafejtett long: $l")

                    val sendData = byteArrayOf(
                            weariness[0], weariness[1],
                            wearinessArray[0],
                            wearinessArray[1],
                            wearinessArray[2],
                            wearinessArray[3],
                            wearinessArray[4],
                            wearinessArray[5],
                            wearinessArray[6],
                            wearinessArray[7],
                            /* CRC helye */   0x00
                    )
                    writeCharacteristic(sendData,"Weariness sent $l%")
                }
            }


            /**
             * id_set_per_sec
             * */
            id_set_per_sec.setOnClickListener {
                var secLong: Long = 0
                var secLongArray = byteArrayOf()
                var wasErr = false

                when (id_radioButtonGroupPPG.checkedRadioButtonId) {
                    R.id.id_radioButtonONEHundred -> {
                        secLong = 1
                        secLongArray = byteArrayOf(
                                secLong.toByte(),
                                (secLong shr 8).toByte(),
                                (secLong shr 16).toByte(),
                                (secLong shr 24).toByte(),
                                (secLong shr 32).toByte(),
                                (secLong shr 40).toByte(),
                                (secLong shr 48).toByte(),
                                (secLong shr 56).toByte())
                    }
                    R.id.id_radioButtonTwoHundred -> {

                        secLong = 2
                        secLongArray = byteArrayOf(
                                secLong.toByte(),
                                (secLong shr 8).toByte(),
                                (secLong shr 16).toByte(),
                                (secLong shr 24).toByte(),
                                (secLong shr 32).toByte(),
                                (secLong shr 40).toByte(),
                                (secLong shr 48).toByte(),
                                (secLong shr 56).toByte())
                    }
                    else -> {
                        Toast.makeText(activity!!, "Please select sec!", Toast.LENGTH_LONG).show()
                        wasErr = true
                    }
                }

                if (!wasErr) {
                    val sendData = byteArrayOf(
                            setSec[0], setSec[1],
                            secLongArray[0],
                            secLongArray[1],
                            secLongArray[2],
                            secLongArray[3],
                            secLongArray[4],
                            secLongArray[5],
                            secLongArray[6],
                            secLongArray[7],
                            /* CRC helye */   0x00
                    )
                    Toast.makeText(activity!!, "Set sec", Toast.LENGTH_LONG).show()
                    writeCharacteristic(sendData,"Set sec $secLong hundred")
                }
            }
        }
    }

    private fun writeCharacteristic(sendData: ByteArray, value : String) {
        // CRC számítás ELEJE -------------
        var dataSum = 0
        for (i in 0 until 10) {
            dataSum += sendData[i]
        }

        /*
         * CRC értéket nem vesszük bele a számításba
         */
        val crcValue = dataSum and 0xFF
        // CRC számítás VÉGE -------------

        // CRC az utolsó pozícióra
        sendData[sendData.lastIndex] = crcValue.toByte()

        Log.d(TAG, "sendData:  ${sendData.toHexString()}")

        val nordicService = bleService.getGatt().getService(GlobalRes.NORDIC_UART_SERVICE_UUID)
        val rx = nordicService.getCharacteristic(GlobalRes.RX_CHAR_UUID)
        bleService.writeCharacteristic(rx, sendData)

        bleService.writeFileNORDIC(sendData,value)
    }

    fun ByteArray.toHexString() = joinToString(" ") { "%02x".format(it) }
}