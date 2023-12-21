package hu.euronetrt.okoskp.euronethealth.bluetooth.bleInterfaces

import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.MultipleDeviceInfo

interface ConnectDeviceWithGatt {
    fun connectGatt(deviceAddress : String)
    fun connectGatt(iChooseThisDevice: Array<MultipleDeviceInfo>)
}
