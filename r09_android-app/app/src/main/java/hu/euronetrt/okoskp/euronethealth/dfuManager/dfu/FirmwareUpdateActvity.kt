package hu.euronetrt.okoskp.euronethealth.dfuManager.dfu

import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dmax.dialog.SpotsDialog
import hu.aut.android.dm01_v11.R
import hu.euronetrt.okoskp.euronethealth.bluetooth.BluetoothServiceNotificationType
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import no.nordicsemi.android.dfu.*
import java.io.File



/**
 *
 *
 * A kód egy korábbi működéséhez szükséges.
 * A menüből való dfu frissítés innen megy, a light version app verzióban ezzel megy még a dfu frissítés
 *
 *
 *
 * */
class FirmwareUpdateActvity : AppCompatActivity() {

    private lateinit var device: BluetoothDevice
    private lateinit var mBluetoothLeService: BluetoothLeService
    lateinit var controller: DfuController
    lateinit var dialog: AlertDialog
    private var DFUFile = "/PPGo_DFU.zip"


    companion object {
        val TAG = "FIRMWAREUPDATEACTVITY"
        val DFUTAG = "DFURUN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firmware_update_actvity)

        bindService(Intent(this, BluetoothLeService::class.java),
                bleServiceConnectionDFU,
                Context.BIND_AUTO_CREATE)

        dialog = SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Update device")
                .setCancelable(false)
                .build()
                .apply {
                    show()
                }
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener)
    }

    private val bleServiceConnectionDFU = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, serviceBinder: IBinder?) {
            mBluetoothLeService = (serviceBinder as BluetoothLeService.BluetoothLeServiceBinder).service
            runDFU()
        }
    }

    private fun runDFU() {

        mBluetoothLeService.serviceNotificationsSet(BluetoothServiceNotificationType.BATTERY_SERVICE.type, false)
        mBluetoothLeService.serviceNotificationsSet(BluetoothServiceNotificationType.HEARTRATE_SERVICE.type, false)
        mBluetoothLeService.serviceNotificationsSet(BluetoothServiceNotificationType.NORDIC_SERVICE.type, false)
        mBluetoothLeService.serviceNotificationsSet(BluetoothServiceNotificationType.DEVICEINFO_SERVICE.type, false)

        device = mBluetoothLeService.getGatt().device
        val starter = DfuServiceInitiator(device.address).setDeviceName(device.name)

        // If you want to have experimental buttonless DFU feature supported call additionally:
        starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)

        val file = getFile()

        val uri = Uri.fromFile(file)
        starter.setZip(uri, file.absolutePath)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DfuServiceInitiator.createDfuNotificationChannel(this)
        }

        controller = starter.start(this, OwnDfuService::class.java)
        // You may use the controller to pause, resume or abort the DFU process.
    }

    private fun getFile(): File {
        // In case of a ZIP file, the init packet (a DAT file) must be included inside the ZIP file.
        val path = Environment.getExternalStorageDirectory().toString() + "/Euronet" + DFUFile

        return File(path)
    }

    private val mDfuProgressListener: DfuProgressListener = object : DfuProgressListenerAdapter() {

        override fun onDeviceConnected(deviceAddress: String) {
            super.onDeviceConnected(deviceAddress)
            Log.d(DFUTAG, "DFU connecting")
        }

        override fun onDeviceConnecting(deviceAddress: String) {
            super.onDeviceConnecting(deviceAddress)
            Log.d(DFUTAG, "DFU connecting")
        }

        override fun onEnablingDfuMode(deviceAddress: String) {
            super.onEnablingDfuMode(deviceAddress)
            Log.d(DFUTAG, "DFU mode enable")
        }

        override fun onDfuCompleted(deviceAddress: String) {
            super.onDfuCompleted(deviceAddress)
            Log.d(DFUTAG, "DFU completed")
            //   fileDelete()
            dialog.dismiss()
            finish()
        }

        override fun onDeviceDisconnected(deviceAddress: String) {
            super.onDeviceDisconnected(deviceAddress)
            Log.d(DFUTAG, "DFU device disconnected")
        }

        override fun onError(deviceAddress: String, error: Int, errorType: Int, message: String?) {
            super.onError(deviceAddress, error, errorType, message)
            Log.d(DFUTAG, "DFU device dfuerror $message")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener)
        unbindService(bleServiceConnectionDFU)
    }
}

