package hu.aut.android.dm01_v11.ui.fragments.homePage.homePageSubClasses

import android.content.Context
import android.util.Log
import android.view.View
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.data.AbstractData
import hu.euronetrt.okoskp.euronethealth.data.CollectableType
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataBroadcaster
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataListener
import hu.euronetrt.okoskp.euronethealth.data.broadcast.IBIListener
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.IBIData
import kotlinx.android.synthetic.main.fragment_device_home_page.view.*


@Suppress("UNCHECKED_CAST")
class DeviceHomePageIBI(private var mContext: Context, mView: View) : IBIListener{

    private var now = System.currentTimeMillis() + 500

    override fun onDataArrived(data: IBIData) {
        if (GlobalRes.registredNordicBroadcast) {
            if(System.currentTimeMillis() > now) {
                now = System.currentTimeMillis() + 500
                view.post {
                    data.getIbiValue().forEach {
                        view.id_IBIValue.text = it.getValue().toString()
                    }
                }
            }
        }
    }

    companion object {
        val TAG = "DeviceHomePageIBI"
    }

    private var view: View = mView

    fun start() {
        DataBroadcaster.getInstance(mContext).addListener(CollectableType.IBI, this as DataListener<AbstractData>)
    }

    fun stop() {
        Log.d(TAG, "stop")
        DataBroadcaster.getInstance(mContext).removeListener(CollectableType.IBI, this as DataListener<AbstractData>)
    }
}