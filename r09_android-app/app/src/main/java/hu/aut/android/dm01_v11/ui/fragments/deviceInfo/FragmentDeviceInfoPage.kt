package hu.aut.android.dm01_v11.ui.fragments.deviceInfo

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import hu.aut.android.dm01_v11.R
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_DEVICEIMAGE
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_MODEL_NUMBER_STRING
import kotlinx.android.synthetic.main.fragment_device_info_page.*

class FragmentDeviceInfoPage : Fragment() {

    private val fragmentDeviceInfoChildPage = FragmentDeviceScreenPage()
    private lateinit var ft: FragmentTransaction

    companion object {
        val TAG = "FRAGMENT_DEVICE_INFO_PAGE"
    }

    /**
     * onCreateView in FragmentDeviceInfoPage class
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_device_info_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        id_device_page_name.text = if (PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(BluetoothLeService.KEY_DEVICENAME, null) == null) "name" else PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(BluetoothLeService.KEY_DEVICENAME, null)
        id_device_model.text = if (PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_MODEL_NUMBER_STRING, null) == null) "model" else PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_MODEL_NUMBER_STRING, null)

        ft = childFragmentManager.beginTransaction()
        ft.replace(R.id.childDeviceFrameLayout, fragmentDeviceInfoChildPage)
        ft.commit()
    }

    override fun onResume() {
        super.onResume()
        val image: String

        if (PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_DEVICEIMAGE, null) != null) {
            image = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_DEVICEIMAGE, null)!!
            val decodeString = image.decode()
            val replaceImageString = decodeString.replace("data:image/jpeg;base64,", "")
            val base64 = Base64.decode(replaceImageString, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(base64, 0, base64.size)
            dev_imageView.setImageBitmap(bitmap)
        }
    }

    /**
     * Image Base64 decode
     *
     * @return
     */
    private fun String.decode() : String{
        return Base64.decode(this,Base64.DEFAULT).toString(charset("UTF-8"))
    }
}