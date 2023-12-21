package hu.aut.android.dm01_v11.ui.fragments.deviceInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import hu.aut.android.dm01_v11.R
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_FW_VERSION
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_HARDWARE_VERSION
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_HWMAC_ID
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_MANUFACTURER_NAME_STRING
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_SERIAL_NUMBER_STRING

class FragmentDeviceScreenPage : PreferenceFragmentCompat() {

    companion object {
        val TAG = "FRAGMENT_DEVICE_SETTINGS_PAGE"
        val FVER = "Firmware version "
    }
    private lateinit var bleService : BluetoothLeService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bleService = (activity as DeviceMainActivity).getBleServiceReference()!!
        bleService.deviceINFONotification(KEY_FW_VERSION)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       /*
        átkerült máshova viszont az edit pref kellhet.
        val prefDevName = preferenceManager.findPreference<EditTextPreference>(
                "device_name") as EditTextPreference

        prefDevName.summary =
                if(PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_DEVICENAME, null) == null) "name" else PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_DEVICENAME, null)
*/
      /* Nincs implementálva az eszközben a név módosítás
            prefDevName.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            prefDevName.summary = newValue.toString()
            true
        }*/

        val manufacturer = preferenceManager.findPreference<Preference>(
                "manufacturer")
        manufacturer!!.summary =  if(PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_MANUFACTURER_NAME_STRING, null) == null) "manufacturer" else PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_MANUFACTURER_NAME_STRING, null)


        val mac = preferenceManager.findPreference<Preference>(
                "mac")
        mac!!.summary =  if(PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_HWMAC_ID, null) == null) "mac id" else PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_HWMAC_ID, null)


        val pref = preferenceManager.findPreference<Preference>(
                "battery")
        pref!!.summary = (activity as DeviceMainActivity).getDeviceBatteryValue().toString() + "%"

        val devstatus = preferenceManager.findPreference<Preference>(
                "device_status")
        if (GlobalRes.connectedDevice) {
            devstatus!!.summary = "Connected"
        } else {
            devstatus!!.summary = "Disconnected"
        }

        val firmVer = preferenceManager.findPreference<Preference>(
                "firmware")
        firmVer!!.summary = FVER + if(PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_FW_VERSION, null) == null) "firmware" else PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_FW_VERSION, null)


        val hardware = preferenceManager.findPreference<Preference>(
                "hardware")
        hardware!!.summary = "Hardware " +  if(PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_HARDWARE_VERSION, null) == null) "hardware" else PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_HARDWARE_VERSION, null)


        val serial = preferenceManager.findPreference<Preference>(
                "serial")
        serial!!.summary = "Serial " +if(PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_SERIAL_NUMBER_STRING, null) == null) "serial" else PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_SERIAL_NUMBER_STRING, null)

        val devPreference = findPreference<Preference>("disconnect_device")

        devPreference!!.setOnPreferenceClickListener {
            devstatus.summary = "Disconnected"
            (activity as DeviceMainActivity).deviceNull()
            true
        }
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        //----------------------------//
 /*       val deviceNamePreference = EditTextPreference(context)
        deviceNamePreference.key = "device_name"
        deviceNamePreference.title = "Device name"
    //    deviceNamePreference.isIconSpaceReserved = false

        val modelPreference = Preference(context)
        modelPreference.key = "model"
        modelPreference.title = "Model"
 //       modelPreference.isIconSpaceReserved = false*/

        val manufacturerPreference = Preference(context)
        manufacturerPreference.key = "manufacturer"
        manufacturerPreference.title = "Manufacturer"
         manufacturerPreference.isIconSpaceReserved = false

        val macAddressPreference = Preference(context)
        macAddressPreference.key = "mac"
        macAddressPreference.title = "Mac address"
        macAddressPreference.isIconSpaceReserved = false

        val batteryPreference = Preference(context)
        batteryPreference.key = "battery"
        batteryPreference.title = "Battery"
         batteryPreference.isIconSpaceReserved = false


        val deviceInfoCategory = PreferenceCategory(context)
        deviceInfoCategory.key = "device_informationCategory"
        deviceInfoCategory.title = "Device information"
        screen.addPreference(deviceInfoCategory)
       /* deviceInfoCategory.addPreference(deviceNamePreference)
        deviceInfoCategory.addPreference(modelPreference)*/
        deviceInfoCategory.addPreference(manufacturerPreference)
        deviceInfoCategory.addPreference(macAddressPreference)
        deviceInfoCategory.addPreference(batteryPreference)
        deviceInfoCategory.isIconSpaceReserved = false

        //----------------------------//

        val deviceStatusPreference = Preference(context)
        deviceStatusPreference.key = "device_status"
        deviceStatusPreference.title = "Device status"

        deviceStatusPreference.isIconSpaceReserved = false

        val firmwarePreference = Preference(context)
        firmwarePreference.key = "firmware"
        firmwarePreference.title = "Firmware status"
        firmwarePreference.isIconSpaceReserved = false

        val hardmwarePreference = Preference(context)
        hardmwarePreference.key = "hardware"
        hardmwarePreference.title = "Hardware status"
        hardmwarePreference.isIconSpaceReserved = false

        val serialPreference = Preference(context)
        serialPreference.key = "serial"
        serialPreference.title = "Hardware status"
        serialPreference.isIconSpaceReserved = false

        val disconnectDevicePreference = Preference(context)
        disconnectDevicePreference.key = "disconnect_device"
        disconnectDevicePreference.title = "Disconnect device"
        disconnectDevicePreference.isIconSpaceReserved = false

        val aboutDeviceCategory = PreferenceCategory(context)
        aboutDeviceCategory.key = "about_deviceCategory"
        aboutDeviceCategory.title = "About"
        screen.addPreference(aboutDeviceCategory)
        aboutDeviceCategory.addPreference(deviceStatusPreference)
        aboutDeviceCategory.addPreference(firmwarePreference)
        aboutDeviceCategory.addPreference(hardmwarePreference)
        aboutDeviceCategory.addPreference(serialPreference)
        aboutDeviceCategory.isIconSpaceReserved = false

        //----------------------------//
        preferenceScreen = screen
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.device_screen, p1)
    }
}