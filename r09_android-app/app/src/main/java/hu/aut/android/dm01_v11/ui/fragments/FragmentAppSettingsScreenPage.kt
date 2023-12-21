package hu.aut.android.dm01_v11.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import hu.aut.android.dm01_v11.BuildConfig
import hu.aut.android.dm01_v11.R

class FragmentAppSettingsScreenPage : PreferenceFragmentCompat() {
    companion object {
        val TAG = "FRAGMENT_DEVICE_SETTINGS_PAGE"
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

     /* val autoDataSavePreference = SwitchPreferenceCompat(context)
        autoDataSavePreference.key = "autoDataSavePreference"
        autoDataSavePreference.title = "Auto Save"

        val applicationUpdatesPreference = SwitchPreferenceCompat(context)
        applicationUpdatesPreference.key = "applicationUpdates"
        applicationUpdatesPreference.title = "Application Updates"

        val batteryNotificationPreference = SwitchPreferenceCompat(context)
        batteryNotificationPreference.key = "batteryNotification"
        batteryNotificationPreference.title = "Battery notification"*/

        val autoStartPreference = SwitchPreferenceCompat(context)
        autoStartPreference.key = "autoStart"
        autoStartPreference.title = "Auto Start"

      /*val autoSyncPreference = SwitchPreferenceCompat(context)
        autoSyncPreference.key = "autoSync"
        autoSyncPreference.title = "Auto Sync"*/

        val runAllwaysPreference = SwitchPreferenceCompat(context)
        runAllwaysPreference.key = "runAllways"
        runAllwaysPreference.title = "Run Allways"

        val settingsCategory = PreferenceCategory(context)
        settingsCategory.key = "settings_category"
        settingsCategory.title = "Settings"
        screen.addPreference(settingsCategory)

     /* settingsCategory.addPreference(autoDataSavePreference)
        settingsCategory.addPreference(applicationUpdatesPreference)
        settingsCategory.addPreference(batteryNotificationPreference)
        settingsCategory.addPreference(autoSyncPreference)*/

        settingsCategory.addPreference(autoStartPreference)
        settingsCategory.addPreference(runAllwaysPreference)

        val appVersionPreference = Preference(context)
        appVersionPreference.key = "appVersion"
        appVersionPreference.title = "Application version"
        appVersionPreference.summary = "Version number "

       /* val feedbackPreference = Preference(context)
        feedbackPreference.key = "feedback"
        feedbackPreference.title = "Feedback"*/

        val aboutAppPreferenceCategory = PreferenceCategory(context)
        aboutAppPreferenceCategory.key = "aboutApp_category"
        aboutAppPreferenceCategory.title = "About the app"
        screen.addPreference(aboutAppPreferenceCategory)

        aboutAppPreferenceCategory.addPreference(appVersionPreference)
       // aboutAppPreferenceCategory.addPreference(feedbackPreference)

        preferenceScreen = screen
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.settings_screen, p1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val appVer = preferenceManager.findPreference<Preference>(
                "appVersion")
        appVer!!.summary ="Version number ${BuildConfig.VERSION_NAME}"
        super.onViewCreated(view, savedInstanceState)
    }
}