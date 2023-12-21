package hu.aut.android.dm01_v11.ui.fragments.userInfo

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import hu.aut.android.dm01_v11.R
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import kotlinx.android.synthetic.main.fragment_device_user_page.*

class FragmentUserInfoPage : Fragment() {

    private val fragmentUserInfoChildPage = FragmentUserInfoChildPage()
    private lateinit var ft: FragmentTransaction

    companion object {
        val TAG = "FRAGMENT_DEVICE_ACCOUNT_PAGE"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_device_user_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ft = childFragmentManager.beginTransaction()
        ft.replace(R.id.childUserFrameLayout, fragmentUserInfoChildPage)
        ft.commit()

        id_user_page_name.text =  if (androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(BluetoothLeService.KEY_USERNAME, null) == null) "User name" else androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(BluetoothLeService.KEY_USERNAME, null)
        id_user_page_email.text = if (androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(BluetoothLeService.KEY_USEREMAIL, null) == null) "email" else androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(BluetoothLeService.KEY_USEREMAIL, null)
        val imageString = if (androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(BluetoothLeService.KEY_USERIMAGE, null) == null) "" else androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(BluetoothLeService.KEY_USERIMAGE, "")
        val decodeString = imageString.decode()
        val replaceImageString = decodeString.replace("data:image/jpeg;base64,", "")
        val base64 = Base64.decode(replaceImageString, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(base64, 0, base64.size)
        imageView.setImageBitmap(bitmap)
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