package hu.aut.android.dm01_v11.ui.fragments.homePage

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import hu.aut.android.dm01_v11.R
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.data.AbstractData
import hu.euronetrt.okoskp.euronethealth.data.CollectableType
import hu.euronetrt.okoskp.euronethealth.data.broadcast.*
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.*
import kotlinx.android.synthetic.main.fragment_device_home_page.*
import me.itangqi.waveloadingview.WaveLoadingView

@Suppress("UNCHECKED_CAST")
class FragmentDeviceHomePage : Fragment() {

    private var nowIMU = System.currentTimeMillis() + 700
    private var nowIBI = System.currentTimeMillis() + 600
    private var nowSTEP = System.currentTimeMillis() + 500

    private var running = false

    private var hrListener: HRListener = object : HRListener {
        override fun onDataArrived(data: HRData) {
            if (GlobalRes.registredNordicBroadcast) {
                if (view != null) {
                    view!!.post {
                        if (id_bpmFR != null) {
                            id_bpmFR.text = "${data.getHr()}" + " BPM"
                        }
                        if (gifViewPlayerHR != null) {
                            gifViewPlayerHR.setPaused(false)
                        }
                    }
                }
            }
        }
    }

    /* private var otherListener : OTHERListener = object : OTHERListener{
         override fun onDataArrived(data: OTHERData) {
             this@LocalStorage.onDataArrived(data)
         }
     }*/

    private var stepListener: StepListener = object : StepListener {
        override fun onDataArrived(data: StepData) {
            if (GlobalRes.registredNordicBroadcast) {
                if (view != null) {
                    if (System.currentTimeMillis() > nowSTEP) {
                        nowSTEP = System.currentTimeMillis() + 500
                        view!!.post {
                            if(data.getStepValue() != 0.toLong()){
                            waveLoadingView.centerTitle = data.getStepValue().toString()
                            waveLoadingView.progressValue = (data.getStepValue() / 100).toInt()
                        }
                        }
                    }
                }
            }
        }
    }

    private var ppgListener: PPGListener = object : PPGListener {
        override fun onDataArrived(data: PPGData) {
            if (!running) {
                if (GlobalRes.registredNordicBroadcast) {
                    if (view != null) {
                        view!!.post {
                            if (id_progressBarPPG != null) {
                                id_progressBarPPG.visibility = View.VISIBLE
                            }
                            if(child_ppg_text != null){
                                child_ppg_text.text = "PPG measurement run..."
                            }
                        }
                        running = true
                    }
                }
            }
        }
    }

    private var ibiListener: IBIListener = object : IBIListener {
        override fun onDataArrived(data: IBIData) {
            if (GlobalRes.registredNordicBroadcast) {
                if (view != null) {
                    if (System.currentTimeMillis() > nowIBI) {
                        nowIBI = System.currentTimeMillis() + 600
                        view!!.post {
                            data.getIbiValue().forEach {
                                if (id_IBIValue != null) {
                                    id_IBIValue.text = it.getValue().toString()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private val imuListener: IMUListener = object : IMUListener {
        override fun onDataArrived(data: IMUData) {
            if (GlobalRes.registredNordicBroadcast) {
                if (view != null) {
                    if (System.currentTimeMillis() > nowIMU) {
                        nowIMU = System.currentTimeMillis() + 700
                        view!!.post {
                            data.getImuValue().forEach {
                                if (id_acc_x != null) {
                                    id_acc_x.text = it.getAccelerometer_x().toString()
                                }

                                if (id_acc_y != null) {

                                    id_acc_y.text = it.getAccelerometer_y().toString()
                                }

                                if (id_acc_z != null) {
                                    id_acc_z.text = it.getAccelerometer_z().toString()
                                }

                                if (id_gyro_x != null) {
                                    id_gyro_x.text = it.getGyroscope_x().toString()
                                }

                                if (id_gyro_y != null) {
                                    id_gyro_y.text = it.getGyroscope_y().toString()
                                }
                                if (id_gyro_z != null) {
                                    id_gyro_z.text = it.getGyroscope_z().toString()
                                }

                                if (id_mag_x != null) {
                                    id_mag_x.text = it.getMagnetometer_x().toString()
                                }


                                if (id_mag_y != null) {
                                    id_mag_y.text = it.getMagnetometer_y().toString()
                                }

                                if (id_mag_z != null) {
                                    id_mag_z.text = it.getMagnetometer_z().toString()
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        val TAG = "FRGMNT_DEVICE_HOME_PAGE"
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "hali onAttach")
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "hali onCreateView")
        return inflater.inflate(R.layout.fragment_device_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "hali onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        id_acc_x.text = "--"
        id_acc_y.text = "--"
        id_acc_z.text = "--"
        id_gyro_x.text = "--"
        id_gyro_y.text = "--"
        id_gyro_z.text = "--"
        id_mag_x.text = "--"
        id_mag_y.text = "--"
        id_mag_z.text = "--"
        id_IBIValue.text = "--"


        waveLoadingView.setShapeType(WaveLoadingView.ShapeType.CIRCLE)
        waveLoadingView.topTitle = "GOAL"
        waveLoadingView.centerTitle = "Actual step"
        waveLoadingView.bottomTitle = ""
        waveLoadingView.startAnimation()
        waveLoadingView.centerTitleColor = Color.BLACK
        waveLoadingView.setCenterTitleStrokeWidth(0f)

        waveLoadingView.progressValue = 0
        waveLoadingView.borderWidth = 2f
        waveLoadingView.setAmplitudeRatio(60)
        waveLoadingView.waveColor = ContextCompat.getColor(view.context, R.color.colorPrimary)
        waveLoadingView.borderColor = ContextCompat.getColor(view.context, R.color.colorAccent)
        waveLoadingView.setAnimDuration(3000)

        gifViewPlayerHR.setMovieAssets("Heart.gif")
        gifViewPlayerHR.setPaused(true)
        id_progressBarPPG.visibility = View.GONE
    }

    override fun onResume() {
        Log.d(TAG, "hali onResume")
        // if (GlobalRes.registredNordicBroadcast) {
        DataBroadcaster.getInstance(view!!.context).addListener(CollectableType.IMU, imuListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(view!!.context).addListener(CollectableType.IBI, ibiListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(view!!.context).addListener(CollectableType.HEARTRATE, hrListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(view!!.context).addListener(CollectableType.STEP, stepListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(view!!.context).addListener(CollectableType.PPG, ppgListener as DataListener<AbstractData>)
        //   }
        super.onResume()
    }

    override fun onPause() {
        //   if (!GlobalRes.registredNordicBroadcast) {
        DataBroadcaster.getInstance(view!!.context).removeListener(CollectableType.IMU, imuListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(view!!.context).removeListener(CollectableType.IBI, ibiListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(view!!.context).removeListener(CollectableType.HEARTRATE, hrListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(view!!.context).removeListener(CollectableType.STEP, stepListener as DataListener<AbstractData>)
        DataBroadcaster.getInstance(view!!.context).removeListener(CollectableType.PPG, ppgListener as DataListener<AbstractData>)
        running = false
        // }
        Log.d(TAG, "hali onPause")
        super.onPause()
    }

    override fun onDestroyView() {
        Log.d(TAG, "hali onDestroyView")
        super.onDestroyView()
    }

    override fun onDetach() {
        Log.d(TAG, "hali onDetach")
        pauseViewElements()
        super.onDetach()
    }

    override fun onDestroy() {
        Log.d(TAG, "hali onDestroy")

        super.onDestroy()
    }

    fun startViewElements() {

        //    gifViewPlayer.setMovieAssets("signal.gif")
        //  gifViewPlayer.setPaused(true)


        /*    DeviceHomePageHR(activity!!, mview).start()
          DeviceHomePagePPG(activity!!, mview).start()
          DeviceHomePageIBI(activity!!, mview).start()
           DeviceHomePageIMU(activity!!, mview).start()
           DeviceHomePageSTEP(activity!!, mview).start()*/
    }

    fun pauseViewElements() {
        /* DeviceHomePageHR(activity!!, mview).stop()
         DeviceHomePagePPG(activity!!, mview).stop()
         DeviceHomePageIBI(activity!!, mview).stop()
         DeviceHomePageIMU(activity!!, mview).stop()
         DeviceHomePageSTEP(activity!!, mview).stop()*/
        // gifViewPlayerHR.setPaused(true)
        // gifViewPlayer.setPaused(true)


    }
}