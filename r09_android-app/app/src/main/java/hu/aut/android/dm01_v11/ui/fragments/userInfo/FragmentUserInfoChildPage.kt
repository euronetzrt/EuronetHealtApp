package hu.aut.android.dm01_v11.ui.fragments.userInfo

import android.os.Bundle
import android.view.View
import androidx.preference.*
import hu.aut.android.dm01_v11.R
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService

class FragmentUserInfoChildPage : PreferenceFragmentCompat() {

    companion object {
        val TAG = "FrgUserInfoChildPage"
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        val genderPreference = ListPreference(context)
        genderPreference.key = "gender"
        genderPreference.title = "Gender"


        val weightPreference = ListPreference(context)
        weightPreference.key = "weight"
        weightPreference.title = "Weight"

        val heightPreference = ListPreference(context)
        heightPreference.key = "height"
        heightPreference.title = "Height"

        val birthdayPreference = Preference(context)
        birthdayPreference.key = "birthday"
        birthdayPreference.title = "Birthday"

        val profileCategory = PreferenceCategory(context)
        profileCategory.key = "profile_category"
        profileCategory.title = "Profile"
        screen.addPreference(profileCategory)
        profileCategory.addPreference(genderPreference)
        profileCategory.addPreference(weightPreference)
        profileCategory.addPreference(heightPreference)
        profileCategory.addPreference(birthdayPreference)

        preferenceScreen = screen

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.user_screen, p1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val birthday = preferenceManager.findPreference<Preference>(
                "birthday")
        birthday!!.summary = if (PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(BluetoothLeService.KEY_BIRTHDAY, null) == null) "1900-01-01" else PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(BluetoothLeService.KEY_BIRTHDAY, null)


        val genderPreference = preferenceManager.findPreference<ListPreference>(
                "gender")
        var value= 0
        if(PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(BluetoothLeService.KEY_USERSEX, null) == "option1") {
            value = 1
        }
        genderPreference?.setValueIndex(value)

        super.onViewCreated(view, savedInstanceState)
    }
}