package hu.euronetrt.okoskp.euronethealth.dfuManager.dfu

import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.gastudio.downloadloadding.library.GADownloadingView
import hu.aut.android.dm01_v11.R
import hu.euronetrt.okoskp.euronethealth.GlobalRes.DFU_START
import hu.euronetrt.okoskp.euronethealth.GlobalRes.DFU_UPDATE_ERROR
import hu.euronetrt.okoskp.euronethealth.GlobalRes.DFU_UPDATE_PROGRESS_REFRESH
import hu.euronetrt.okoskp.euronethealth.GlobalRes.DFU_UPDATE_SUCCESSFULLY
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_DEVICEIMAGE
import hu.euronetrt.okoskp.euronethealth.broadcastrecievers.DFUUpdateBroadcastReciever
import kotlinx.android.synthetic.main.activity_dfuupdate.*

class DFUUpdateActivity : AppCompatActivity() {

    companion object {
        private val TAG = "DFUUpdateActivity"
    }

    lateinit var mGADownloadingView : GADownloadingView
    private val deviceDFUProgressEvent = DFUUpdateBroadcastReciever()

    private val WAITINTERVAL: Long = 2500
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dfuupdate)

        if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(KEY_DEVICEIMAGE, null) != null) {
            val image = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(KEY_DEVICEIMAGE, null)!!
            val decodeString = image.decode()
            val replaceImageString = decodeString.replace("data:image/jpeg;base64,", "")
            val base64 = Base64.decode(replaceImageString, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(base64, 0, base64.size)
            appCompatImageView.setImageBitmap(bitmap)
        }

           val intentFilterDFU = IntentFilter()
          intentFilterDFU.addAction(DFU_START)
          intentFilterDFU.addAction(DFU_UPDATE_SUCCESSFULLY)
          intentFilterDFU.addAction(DFU_UPDATE_ERROR)
          intentFilterDFU.addAction(DFU_UPDATE_PROGRESS_REFRESH)
          registerReceiver(
                  deviceDFUProgressEvent,
                  intentFilterDFU
          )

        mGADownloadingView = findViewById(R.id.ga_downloading)
        mGADownloadingView.performAnimation()
    }

    fun dfuUpdate(newPercent: Int) {
        mGADownloadingView.updateProgress(newPercent)
    }

    fun dfuError() {
        mGADownloadingView.onFail()

        handler.postDelayed({
            finish()
        }, WAITINTERVAL)
    }

    fun dfuSuccess() {
        handler.postDelayed({
            finish()
        }, 500)
    }

    override fun onBackPressed() {
        //nothing, so no work back button
    }

    override fun onDestroy() {
        unregisterReceiver(deviceDFUProgressEvent)
        super.onDestroy()
    }

    /**
     * Image Base64 decode
     *
     * @return
     */
    private fun String.decode() : String{
        return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
    }
}
