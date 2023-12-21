package hu.aut.android.dm01_v11.ui.fragments.homePage.homePageSubClasses

import android.content.Context
import android.util.Log
import android.view.View
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.data.AbstractData
import hu.euronetrt.okoskp.euronethealth.data.CollectableType
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataBroadcaster
import hu.euronetrt.okoskp.euronethealth.data.broadcast.DataListener
import hu.euronetrt.okoskp.euronethealth.data.broadcast.IMUListener
import hu.euronetrt.okoskp.euronethealth.data.dataClasses.IMUData
import kotlinx.android.synthetic.main.fragment_device_home_page.view.*

@Suppress("UNCHECKED_CAST")
class DeviceHomePageIMU(private var mContext: Context, mView: View) : IMUListener {

    companion object{

        val TAG = "DeviceHomePageIMU"

        private lateinit var INSTANCE : DeviceHomePageIMU

        fun getInstance(mContext: Context,mView: View) : DeviceHomePageIMU {
            if(!::INSTANCE.isInitialized){
                INSTANCE = DeviceHomePageIMU(mContext,mView)
            }
            return INSTANCE
        }
    }

    private var now = System.currentTimeMillis() + 700

    override fun onDataArrived(data: IMUData) {
        if (GlobalRes.registredNordicBroadcast) {
            if(System.currentTimeMillis() > now) {
                now = System.currentTimeMillis() + 700
                view.post {
                    data.getImuValue().forEach {
                        view.id_acc_x.text = it.getAccelerometer_x().toString()
                        view.id_acc_y.text = it.getAccelerometer_y().toString()
                        view.id_acc_z.text = it.getAccelerometer_z().toString()
                        view.id_gyro_x.text = it.getGyroscope_x().toString()
                        view.id_gyro_y.text = it.getGyroscope_y().toString()
                        view.id_gyro_z.text = it.getGyroscope_z().toString()
                        view.id_mag_x.text = it.getMagnetometer_x().toString()
                        view.id_mag_y.text = it.getMagnetometer_y().toString()
                        view.id_mag_z.text = it.getMagnetometer_z().toString()
                    }
                }
            }
        }
    }

    private var view: View = mView

    fun start() {

        view.id_acc_x.text = "--"
        view.id_acc_y.text = "--"
        view.id_acc_z.text = "--"

        view.id_gyro_x.text = "--"
        view.id_gyro_y.text = "--"
        view.id_gyro_z.text = "--"

        view.id_mag_x.text = "--"
        view.id_mag_y.text = "--"
        view.id_mag_z.text = "--"

        DataBroadcaster.getInstance(mContext).addListener(CollectableType.IMU, this as DataListener<AbstractData>)
    }

    fun stop() {
        Log.d(TAG, "stop")
        DataBroadcaster.getInstance(mContext).removeListener(CollectableType.IMU, this as DataListener<AbstractData>)
    }
}
