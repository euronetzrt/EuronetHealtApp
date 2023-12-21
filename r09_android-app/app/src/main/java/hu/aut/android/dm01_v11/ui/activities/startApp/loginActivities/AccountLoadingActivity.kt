package hu.aut.android.dm01_v11.ui.activities.startApp.loginActivities

import android.accounts.Account
import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import hu.aut.android.dm01_v11.R
import hu.aut.android.dm01_v11.ui.activities.startApp.loginActivities.LoginActivity.Companion.TAGLOGIN
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.asynkTaskClass.CreateAccountManagerAsynkTask
import java.util.*

class AccountLoadingActivity : AccountAuthenticatorActivity() {

    companion object {
        val TAG = "AccountLoadingActivity"
    }

    private var mAccountManager: AccountManager? = null
    private var mAuthTokenType: String = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS

    private var username = ""
    private var pass = ""
    private var type = ""
    private val dataArrayList = ArrayList<String>()
    val PARAM_USER_PASS = "USER_PASS"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        username = intent.getStringExtra("user")
        pass = intent.getStringExtra("pass")
        type = intent.getStringExtra(AccountGeneral.ACCOUNT_TYPE)

        mAccountManager = AccountManager.get(this)

        dataArrayList.add(0, username)
        dataArrayList.add(1, pass)
        dataArrayList.add(2, type)
    }

    override fun onStart() {
        super.onStart()
        callAsynk()
    }

    private fun callAsynk() {
        CreateAccountManagerAsynkTask(this) { responseIntent ->
            if (responseIntent.hasExtra(AccountGeneral.KEY_ERROR_MESSAGE)) {
                Log.d(TAGLOGIN, "$TAG callAsynk ------>  KEY_ERROR_MESSAGE")
                Toast.makeText(this, responseIntent.getStringExtra(AccountGeneral.KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show()
                setAccountAuthenticatorResult(responseIntent.extras)
                setResult(Activity.RESULT_OK, responseIntent)
                finish()
            } else {
                finishLogin(responseIntent)
            }
        }.execute(dataArrayList)
    }

    private fun finishLogin(responseIntent: Intent) {
        val accountName = responseIntent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        val accountPassword = responseIntent.getStringExtra(PARAM_USER_PASS)
        val account = Account(accountName, responseIntent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE))

        if (responseIntent.getBooleanExtra(AccountGeneral.ARG_IS_ADDING_NEW_ACCOUNT, false)) {

            val authtoken = responseIntent.getStringExtra(AccountManager.KEY_AUTHTOKEN)
            val authtokenType = mAuthTokenType
                mAccountManager!!.addAccountExplicitly(account,accountPassword , null)
            Log.d(TAGLOGIN, "$TAG REGISTER TOKEN: " + authtoken)
                mAccountManager!!.setPassword(account,accountPassword)
                mAccountManager!!.setAuthToken(account, authtokenType, authtoken)
        }
        Log.d(TAGLOGIN, "$TAG responseIntent.extras) ------>  ${responseIntent.extras}")
        setAccountAuthenticatorResult(responseIntent.extras)
        setResult(Activity.RESULT_OK, responseIntent)
        finish()
    }
}
