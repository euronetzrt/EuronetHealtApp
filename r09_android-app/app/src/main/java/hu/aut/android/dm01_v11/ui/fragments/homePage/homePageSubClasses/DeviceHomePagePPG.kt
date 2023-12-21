package hu.aut.android.dm01_v11.ui.fragments.homePage.homePageSubClasses

import android.content.Context
import android.util.Log
import android.view.View
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.data.AbstractData
import hu.euronetrt.okoskp.euronethealth.data.CollectableType
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataBroadcaster
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataListener
import hu.euronetrt.okoskp.euronethealth.data.broadcast.PPGListener
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.PPGData
import kotlinx.android.synthetic.main.fragment_device_home_page.view.*

@Suppress("UNCHECKED_CAST")
class DeviceHomePagePPG(private var mContext: Context, mView: View) : PPGListener {
    override fun onDataArrived(data: PPGData) {
        if (!running) {
            if (GlobalRes.registredNordicBroadcast) {
                view.post {
                    //id_progressBarPPG.visibility  = View.VISIBLE
                   // view.gifViewPlayer.setPaused(false)
              //      child_ppg_text.text = "PPG measurement run..."
                }
                running = true
            }
        }
    }

    companion object {
        val TAG = "DeviceHomePagePPG"
    }

    private var view: View = mView
    private var running = false

    fun start() {
       // view.id_progressBarPPG.visibility  = View.GONE
      //  view.gifViewPlayer.setMovieAssets("signal.gif")
        //view.gifViewPlayer.setPaused(true)


        DataBroadcaster.getInstance(mContext).addListener(CollectableType.PPG, this as DataListener<AbstractData>)
    }


    fun stop() {
        Log.d(TAG, "stop")
        running = false
        view.child_ppg_text.text = "PPG measurement not run..."
      //  vie w.gifViewPlayer.setPaused(true)
     //   view.id_progressBarPPG.visibility  = View.GONE
        DataBroadcaster.getInstance(mContext).removeListener(CollectableType.PPG, this as DataListener<AbstractData>)
    }
}