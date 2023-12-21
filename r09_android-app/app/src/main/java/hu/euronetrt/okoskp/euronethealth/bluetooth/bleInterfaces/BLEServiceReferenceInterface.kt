package hu.euronetrt.okoskp.euronethealth.bluetooth.bleInterfaces

import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.BleConnector
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService

interface BLEServiceReferenceInterface {
    fun getBLEServiceReference(): BluetoothLeService
    fun getConnectorThread() : BleConnector?
}