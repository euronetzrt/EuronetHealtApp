package hu.aut.android.dm01_v11.ui.fragments.homePage.homePageSubClasses

import android.content.Context
import android.view.View
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.data.AbstractData
import hu.euronetrt.okoskp.euronethealth.data.CollectableType
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataBroadcaster
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataListener
import hu.euronetrt.okoskp.euronethealth.data.broadcast.HRListener
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.HRData
import kotlinx.android.synthetic.main.fragment_device_home_page.view.*


@Suppress("UNCHECKED_CAST")
class DeviceHomePageHR(private var mContext: Context, mView: View) : HRListener{
    override fun onDataArrived(data: HRData) {
        if (GlobalRes.registredNordicBroadcast) {
            view.post {
                view.id_bpmFR.text = "${data.getHr()}" + " BPM"
                view.gifViewPlayerHR.setPaused(false)
            }
        }
    }

    private var view: View = mView

    companion object {
        val TAG = "FragmentChildHR"
    }

    fun start() {

      DataBroadcaster.getInstance(mContext).addListener(CollectableType.HEARTRATE, this as DataListener<AbstractData>)

        view.gifViewPlayerHR.setMovieAssets("Heart.gif")
        view.gifViewPlayerHR.setPaused(true)
    }

    fun stop(){
        view.gifViewPlayerHR.setPaused(true)
        DataBroadcaster.getInstance(mContext).removeListener(CollectableType.HEARTRATE,this as DataListener<AbstractData>)
    }
}