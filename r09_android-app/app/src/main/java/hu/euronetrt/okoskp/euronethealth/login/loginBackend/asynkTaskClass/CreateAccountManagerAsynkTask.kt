package hu.euronetrt.okoskp.euronethealth.login.loginBackend.asynkTaskClass

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import hu.aut.android.dm01_v11.R
import hu.aut.android.dm01_v11.ui.activities.startApp.loginActivities.LoginActivity
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral.sServerAuthenticate

class CreateAccountManagerAsynkTask(val context: Context, private val callback: (Intent) -> Unit) : AsyncTask<ArrayList<String>, Void, Intent>() {

    companion object {
        val TAG = "CreateAcManAsynkTask"
        val KEY_ERROR_MESSAGE = "ERR_MSG"
        val PARAM_USER_PASS = "USER_PASS"
    }

    private lateinit var authtoken: String

    override fun doInBackground(vararg params: ArrayList<String>): Intent {

        val userEmail = params[0][0]
        val userPass = params[0][1]
        val accountType = params[0][2]

        val data = Bundle()
        try {
            Log.d(LoginActivity.TAGLOGIN, "$TAG call sServerAuthenticate.userSignUp")
            authtoken = sServerAuthenticate.userSignUp(context, userEmail, userPass, accountType)

            if (context.getString(R.string.login_ok) == authtoken || context.getString(R.string.login_error) == authtoken || AccountGeneral.INCORRECT_PASS == authtoken) {
                data.putBoolean(AccountGeneral.ARG_IS_ADDING_NEW_ACCOUNT, false)

                if (context.getString(R.string.login_error) == authtoken) {
                    data.putBoolean(AccountGeneral.KEY_ERROR_SERVER, true)
                    data.putBoolean(AccountGeneral.INCORRECT_PASS, false)
                } else if(AccountGeneral.INCORRECT_PASS == authtoken) {
                    data.putBoolean(AccountGeneral.INCORRECT_PASS, true)
                    Log.d(LoginActivity.TAGLOGIN, "$TAG INCORRECT_PASS true")
                }else{
                    data.putBoolean(AccountGeneral.KEY_ERROR_SERVER, false)
                }
            } else {
               data.putString(AccountManager.KEY_AUTHTOKEN, authtoken)
               data.putBoolean(AccountGeneral.ARG_IS_ADDING_NEW_ACCOUNT, true)
               data.putBoolean(AccountGeneral.KEY_ERROR_SERVER, false)
            }

            data.putString(AccountManager.KEY_ACCOUNT_NAME, userEmail)
            data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
            data.putString(PARAM_USER_PASS, userPass)

        } catch (e: Exception) {
            data.putString(KEY_ERROR_MESSAGE, "Error Server +${e.message}")
            Log.d(LoginActivity.TAGLOGIN, "$TAG ${e.message} ------>  catch ág")
        }

        val res = Intent()
        res.putExtras(data)
        return res
    }

    override fun onPostExecute(result: Intent) {
        super.onPostExecute(result)
        callback.invoke(result)
    }
}