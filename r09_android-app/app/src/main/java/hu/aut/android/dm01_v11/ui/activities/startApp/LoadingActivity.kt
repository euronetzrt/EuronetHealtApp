package hu.aut.android.dm01_v11.ui.activities.startApp

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import hu.aut.android.dm01_v11.R
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity
import hu.aut.android.dm01_v11.ui.activities.startApp.loginActivities.LoginActivity
import hu.euronetrt.okoskp.euronethealth.GlobalRes.applicationVersionModeIsLight
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.broadcastrecievers.BleServiceStartBroadcastReceiver
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral
import kotlinx.android.synthetic.main.loading_layout.*

class LoadingActivity : AppCompatActivity() {

    companion object {
        val TAG = "LoadingActivity"
        val LOADING_REQUEST_CODE = 109
        val TAGLOGIN = "RunLogin"
        val VERSIONMODE = "VERSIONMODE"
        val KEY_ALLOW = "KEY_ALLOW"
    }

    private lateinit var mAccountManager: AccountManager
    private var mAuthTokenType: String? = null
    private val STARTDEVACTIVITY = "startDevActivity"
    private val START_SERVICE = "hu.aut.android.dm01_v11.action.START_SERVICE"
    private var startServiceReciever = BleServiceStartBroadcastReceiver()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val imageString = if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(BluetoothLeService.KEY_USERIMAGE, null) == null) "" else PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(BluetoothLeService.KEY_USERIMAGE, "")
        if(!imageString.isNullOrEmpty() && imageString != ""){
            val decodeString = imageString.decode()
            val replaceImageString = decodeString.replace("data:image/jpeg;base64,", "")
            val base64 = Base64.decode(replaceImageString, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(base64, 0, base64.size)
            appCompatImageView.setImageBitmap(bitmap)
        }


        val policyOk = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(
                KEY_ALLOW, false)

        if (!policyOk) {
            /*most indul először vagy az engedélyeket nem adták még meg.
            * elmentjük a jelenlegi verzió változatot a globalResből.*/
            val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            sp.edit()
                    .putBoolean(VERSIONMODE, applicationVersionModeIsLight)
                    .apply()
        } else {
            /*nem most indult először tehát van versionmód share pref.*/
            val versionMod = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(
                    VERSIONMODE, true)

            if (versionMod != applicationVersionModeIsLight) {
                PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().clear().apply()

                val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                sp.edit()
                        .putBoolean(VERSIONMODE, applicationVersionModeIsLight)
                        .apply()
            }
        }

        if (!applicationVersionModeIsLight) {
            mAccountManager = AccountManager.get(this)
            mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS

            val accounts = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)
            when (accounts.size) {
                0 -> {
                    Log.d(TAG, accounts.size.toString() + "nincs account")
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                1 -> {
                    Log.d(TAGLOGIN, accounts.size.toString() + "   db account van")
                    Log.d(TAGLOGIN, "accounts[0] ${accounts[0]}")
                    mAccountManager.getAuthToken(
                            accounts[0],                     // Account retrieved using getAccountsByType()
                            AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,            // Auth scope
                            null,                  // Authenticator-specific options
                            this, { future ->
                        /*handler this is*/
                        try {
                            val bnd = future.result
                            Log.d(TAGLOGIN, "getauth token $bnd")
                            if (!bnd.getString(AccountManager.KEY_AUTHTOKEN).isNullOrEmpty()) {
                                Log.d(TAGLOGIN, "getauth token 2")
                                // van tokenünk az accountban
                                if (getConnectivityStatusString(this)) {
                                    Log.d(TAGLOGIN, "getauth token 3")
                                    // ha van netkapcsolat mehet a szerverre a token kérés első körben
                                    addNewAccount(AccountGeneral.ACCOUNT_TYPE, mAuthTokenType, accounts[0].name, mAccountManager.getPassword(accounts[0]))
                                } else {
                                    Log.d(TAGLOGIN, "getauth token 4")
                                    //Van account de nincs net , account tokent a managerben miatt mehet az appba
                                    val myintent = Intent(this, PolicyActivity::class.java)
                                    startActivityForResult(myintent, LOADING_REQUEST_CODE)
                                }
                            } else {
                                Log.d(TAGLOGIN, "getauth token 5")
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                        } catch (e: Exception) {
                            Log.d(TAGLOGIN, "Erros is ${e.message}")
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }, null)       // Callback called if an error occurs

                    // validate the token, invalidate and generate a new one if required
                }
                else -> {
                    Log.d(TAGLOGIN, accounts.size.toString() + "   Több mint 1 account")
                    showAccountPicker(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, false)
                }
            }
        } else {
            val myintent = Intent(this, PolicyActivity::class.java)
            startActivityForResult(myintent, LOADING_REQUEST_CODE)
        }
    }


    private fun getConnectivityStatusString(mcontext: Context): Boolean {
        if (!applicationVersionModeIsLight) {

            val connManager: ConnectivityManager = mcontext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val actNetwork = connManager.activeNetworkInfo

            if (actNetwork != null) {
                Log.d(TAGLOGIN, "${actNetwork.isConnected}")
                return actNetwork.isConnected
            } else {
                return false
            }
        } else {
            return false
        }
    }

    /**
     * Create a new account
     *
     * @param accountType
     * @param mAuthTokenType
     * @param username
     * @param pass
     */
    private fun addNewAccount(accountType: String, mAuthTokenType: String?, username: String, pass: String) {
        if (!applicationVersionModeIsLight) {
            val accountInfoBundle = Bundle()
            accountInfoBundle.putString("username", username)
            accountInfoBundle.putString("pass", pass)
            mAccountManager.addAccount(accountType, mAuthTokenType, null, accountInfoBundle, this, {
                /*handler this is*/
                try {
                    val myintent = Intent(this, PolicyActivity::class.java)
                    startActivityForResult(myintent, LOADING_REQUEST_CODE)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d(TAGLOGIN, "Erros is $TAG + error addaccount ${e.message}")
                }
            }, null)
        }
    }

    /**
     * Show all the accounts registered on the account manager. Request an auth token upon user select.
     * @param authTokenType
     */
    private fun showAccountPicker(authtokenTypeFullAccess: String, invalidate: Boolean) {
        if (!applicationVersionModeIsLight) {
            val availableAccounts = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)

            if (availableAccounts.isEmpty()) {
                Toast.makeText(this, "No accounts", Toast.LENGTH_SHORT).show()
            } else {
                val name = arrayOfNulls<String>(availableAccounts.size)
                for (i in availableAccounts.indices) {
                    name[i] = availableAccounts[i].name
                }

                // Account picker
                val mAlertDialog = AlertDialog.Builder(this).setTitle("Pick Account").setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, name), object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        if (invalidate)
                            invalidateAuthToken(availableAccounts[which], authtokenTypeFullAccess)
                        else
                            getExistingAccountAuthToken(availableAccounts[which], authtokenTypeFullAccess)
                    }
                }).create()
                mAlertDialog.show()
            }
        }
    }

    /**
     * Get the auth token for an existing account on the AccountManager
     * @param account
     * @param authTokenType
     */
    private fun getExistingAccountAuthToken(account: Account, authTokenType: String) {
        if (!applicationVersionModeIsLight) {
            val future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null)

            Thread(Runnable {
                try {
                    val bnd = future.result
                    val authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN)
                    showMessage(if (authtoken != null) "SUCCESS!\n" else "FAIL")
                } catch (e: Exception) {
                    e.printStackTrace()
                    showMessage(e.message!!)
                    Log.d(TAG, e.message!!)
                }
            }).start()
        }
    }

    /**
     * Invalidates the auth token for the account
     * @param account
     * @param authTokenType
     */
    private fun invalidateAuthToken(account: Account, authTokenType: String) {
        if (!applicationVersionModeIsLight) {
            val future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null)

            Thread(Runnable {
                try {
                    val bnd = future.result
                    val authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN)
                    mAccountManager.invalidateAuthToken(account.type, authtoken)
                    showMessage(account.name + " invalidated")
                } catch (e: Exception) {
                    e.printStackTrace()
                    showMessage(e.message!!)
                }
            }).start()
        }
    }

    private fun showMessage(msg: String) {
        if (!applicationVersionModeIsLight) {
            if (TextUtils.isEmpty(msg))
                return
            runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOADING_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                if (!applicationVersionModeIsLight) {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Permission denien!", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            if (resultCode == Activity.RESULT_OK) {
                Log.d("STARTSERVICE", "Activity.RESULT_OK  sendbroadcast startService")

                val intent = Intent(this, DeviceMainActivity::class.java)
                startActivity(intent)

                /* val intent = Intent()
                 intent.action = START_SERVICE
                 intent.putExtra(STARTDEVACTIVITY,true)
                 sendBroadcast(intent)*/

                finish()
            }
        }
    }

    override fun onStart() {
        Log.d(TAG, "onStart()call")
        super.onStart()
        val intetnfilter = IntentFilter()
        intetnfilter.addAction(START_SERVICE)
        registerReceiver(
                startServiceReciever,
                IntentFilter(intetnfilter)
        )
    }

    override fun onStop() {

        Log.d(TAG, "onStop()call")
        super.onStop()
        unregisterReceiver(startServiceReciever)
    }

    override fun onDestroy() {
        /*if (!applicationVersionModeIsLight) {
            if (EuronetMeteorSingleton.hasInstance()) {
                if (EuronetMeteorSingleton.getInstance().isConnected) {
                    EuronetMeteorSingleton.destroyInstance()
                }
            }
        }*/

        Log.d(TAG, "onDestroy()call")
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