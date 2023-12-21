package hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountManager

import android.accounts.*
import android.accounts.AccountManager.KEY_BOOLEAN_RESULT
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import hu.aut.android.dm01_v11.ui.activities.startApp.LoadingActivity.Companion.TAGLOGIN
import hu.aut.android.dm01_v11.ui.activities.startApp.loginActivities.AccountLoadingActivity
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral

class AbstractAccountAuth(context: Context) : AbstractAccountAuthenticator(context) {

    private var mcontext: Context = context

    companion object {
        val TAG = "AbstractAccountAuth"
    }

    @Throws(NetworkErrorException::class)
    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String?, authTokenType: String?, requiredFeatures: Array<out String>?, options: Bundle): Bundle {

        val intent = Intent(mcontext, AccountLoadingActivity::class.java)
        intent.putExtra(AccountGeneral.ACCOUNT_TYPE, accountType)
        intent.putExtra(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, authTokenType)
        intent.putExtra(AccountGeneral.ARG_IS_ADDING_NEW_ACCOUNT, true)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        intent.putExtra("user",options["username"]!!.toString())
        intent.putExtra("pass",options["pass"]!!.toString())

        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(response: AccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?): Bundle {
        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        val accountManager = AccountManager.get(mcontext)
        val authToken = accountManager.peekAuthToken(account,authTokenType)
        val resultBundle= Bundle()
        // Lets give another try to authenticate the user
        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            resultBundle.putString(AccountManager.KEY_ACCOUNT_NAME,account!!.name)
            resultBundle.putString(AccountManager.KEY_ACCOUNT_TYPE,account.type)
            resultBundle.putString(AccountManager.KEY_AUTHTOKEN,authToken)
        }
        return resultBundle
    }

    override fun editProperties(accountAuthenticatorResponse: AccountAuthenticatorResponse?, response: String?): Bundle {
        val mResponse = "editProperties $response"
        Log.d(TAGLOGIN, mResponse)
        return Bundle()
    }

    override fun confirmCredentials(accountAuthenticatorResponse: AccountAuthenticatorResponse?, account: Account?, bundle: Bundle?): Bundle {
        val mAccount = "confirmCredentials $account"
        Log.d(TAGLOGIN, mAccount)
        return Bundle()
    }

    override fun getAuthTokenLabel(response: String?): String {
        return "full"
    }

    override fun hasFeatures(accountAuthenticatorResponse: AccountAuthenticatorResponse?, account: Account?, array: Array<out String>?): Bundle {
        val result = Bundle()
        result.putBoolean(KEY_BOOLEAN_RESULT, false)
        return result
    }

    override fun updateCredentials(accountAuthenticatorResponse: AccountAuthenticatorResponse?, account: Account?, response: String?, bundle: Bundle?): Bundle {
        val mAccount = "updateCredentials $account"
        Log.d(TAGLOGIN, mAccount)
        return Bundle()
    }
}