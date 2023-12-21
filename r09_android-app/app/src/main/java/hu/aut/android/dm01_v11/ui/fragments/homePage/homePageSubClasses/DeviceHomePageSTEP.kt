package hu.aut.android.dm01_v11.ui.fragments.homePage.homePageSubClasses

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import hu.aut.android.dm01_v11.R
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.data.AbstractData
import hu.euronetrt.okoskp.euronethealth.data.CollectableType
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataBroadcaster
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataListener
import hu.euronetrt.okoskp.euronethealth.data.broadcast.StepListener
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.StepData
import kotlinx.android.synthetic.main.fragment_device_home_page.view.*
import me.itangqi.waveloadingview.WaveLoadingView


@Suppress("UNCHECKED_CAST")
class DeviceHomePageSTEP(private var mContext: Context, mView: View) : StepListener {

    private var now = System.currentTimeMillis() + 500

    override fun onDataArrived(data: StepData) {
        if (GlobalRes.registredNordicBroadcast) {
            if(System.currentTimeMillis() > now){
                now = System.currentTimeMillis() + 500
                view.post {
                    view.waveLoadingView.centerTitle = data.getStepValue().toString()
                    view.waveLoadingView.progressValue = (data.getStepValue() / 100).toInt()
                }
            }
        }
    }

    companion object {
        val TAG = "DeviceHomePageSTEP"
    }

    private var view: View = mView

    fun start() {

        view.waveLoadingView.setShapeType(WaveLoadingView.ShapeType.CIRCLE)
        view.waveLoadingView.topTitle = "GOAL"
        view.waveLoadingView.centerTitle = "Actual step"
        view.waveLoadingView.bottomTitle = ""
        view.waveLoadingView.startAnimation()
        view.waveLoadingView.centerTitleColor = Color.BLACK
        view.waveLoadingView.setCenterTitleStrokeWidth(0f)

        view.waveLoadingView.progressValue = 0
        view.waveLoadingView.borderWidth = 2f
        view.waveLoadingView.setAmplitudeRatio(60)
        view.waveLoadingView.waveColor = ContextCompat.getColor(mContext, R.color.colorPrimary)
        view.waveLoadingView.borderColor = ContextCompat.getColor(mContext, R.color.colorAccent)
        view.waveLoadingView.setAnimDuration(3000)

        DataBroadcaster.getInstance(mContext).addListener(CollectableType.STEP,this as DataListener<AbstractData>)
    }

    fun stop() {
        Log.d(TAG, "stop")
        DataBroadcaster.getInstance(mContext).removeListener(CollectableType.STEP, this as DataListener<AbstractData>)
    }
}