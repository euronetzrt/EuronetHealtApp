package hu.euronetrt.okoskp.euronethealth.data.broadcast

import hu.euronetrt.okoskp.euronethealth.data.AbstractData

interface DataListener <T : AbstractData> {
    fun onDataArrived(data : T)
}