package hu.euronetrt.okoskp.euronethealth.bluetooth.bleInterfaces

interface GetCharacteristicListFromServiceAct {
    fun getList() :  MutableMap<String, ArrayList<String>>
}