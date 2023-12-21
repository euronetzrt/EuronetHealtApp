package hu.euronetrt.okoskp.euronethealth.bluetooth.bleInterfaces

import hu.euronetrt.okoskp.euronethealth.serverCommunicate.meteor.EuronetMeteorSingleton
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.availableDeviceModel.AvailableDevicesJson

interface BLEServerFunctionsInterfaces {
    fun getAvailableDevice(): AvailableDevicesJson
    fun getMeteor(): EuronetMeteorSingleton
}