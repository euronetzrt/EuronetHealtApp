package hu.aut.android.dm01_v11.ui.activities.startApp

import android.Manifest
import android.Manifest.permission.GET_ACCOUNTS
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import hu.aut.android.dm01_v11.R
import kotlinx.android.synthetic.main.activity_policy.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class PolicyActivity : AppCompatActivity() {

    companion object{
        val TAG = "PolicyActivity"
        val KEY_ALLOW = "KEY_ALLOW"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_policy)

        allowPolicy.setOnClickListener {

            val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            sp.edit()
                    .putBoolean(KEY_ALLOW, true)
                    .apply()
            startApp()
        }

        deniedPolicy.setOnClickListener {
            val data = Intent()
            setResult(Activity.RESULT_CANCELED, data)
            //---close the activity---
            finish()
        }
        startLocationMonitoringWithPermissionCheck()
    }

    private fun startApp() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    @SuppressLint("SetJavaScriptEnabled")
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BODY_SENSORS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,GET_ACCOUNTS)
    fun startLocationMonitoring() {

        val policyOk = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(
                KEY_ALLOW, false)

        if(policyOk){
            startApp()
        }else{
            allowPolicy.visibility= View.VISIBLE
            deniedPolicy.visibility= View.VISIBLE
            webView.loadUrl("file:///android_asset/privacy_policy.html")
            webView.settings.javaScriptEnabled = true
        }
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BODY_SENSORS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,GET_ACCOUNTS)
    fun showDeniedForFineLocation() {
        Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BODY_SENSORS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,GET_ACCOUNTS)
    fun showNeverAskForFineLocation() {
        Toast.makeText(this, "Never ask", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }
}
