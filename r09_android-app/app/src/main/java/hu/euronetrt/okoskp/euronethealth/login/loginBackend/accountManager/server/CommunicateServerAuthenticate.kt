package hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountManager.server

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import hu.aut.android.dm01_v11.R
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_USERID
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountInterface.ServerAuthenticate
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral.mHost
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.meteor.EuronetMeteorSingleton
import im.delight.android.ddp.MeteorCallback
import im.delight.android.ddp.ResultListener
import im.delight.android.ddp.db.memory.InMemoryDatabase

class CommunicateServerAuthenticate : ServerAuthenticate, MeteorCallback {

    companion object {
        val TAG = "CommServerAutClass"
        val TAGRUNLOGIN = "RunLogin"
    }

    private val WAITING_INTERVAL: Long = 15000
    private lateinit var mutex: Object
    private var mEmail: String? = null
    private lateinit var mcontext: Context
    private var mPass: String? = null
    private lateinit var meteor: EuronetMeteorSingleton
    private lateinit var token: String

    /**
     * user Sign Up
     *
     * @param context
     * @param email
     * @param pass
     * @param authType
     * @return
     */
    override fun userSignUp(context: Context, email: String, pass: String, authType: String): String {
        Log.d(TAGRUNLOGIN, "$TAG userSignUp run ")

        mEmail = email
        mPass = pass
        mcontext = context

        try {
            EuronetMeteorSingleton.destroyInstance()
        } catch (e: IllegalStateException) {
        }

        meteor = EuronetMeteorSingleton.createInstance(context, "ws://$mHost:3000/websocket", InMemoryDatabase())
        meteor.addCallback(this)

        if (!meteor.isConnected) {
            Log.d(TAGRUNLOGIN, "$TAG   meteor.connect() ")
            meteor.connect()
        } else {
            Log.d(TAGRUNLOGIN, "$TAG meteor is connected ")
        }

        mutex = Object()
        synchronized(mutex) {
            try {
                mutex.wait(WAITING_INTERVAL)
            } catch (e: InterruptedException) {
                val error = e.message
                Log.d(TAGRUNLOGIN, "$TAG Wait $error ")
            }
        }
        if (!::token.isInitialized) {
            token = mcontext.getString(R.string.login_error)
        }
        meteor.removeCallback(this)
        return token
    }

    override fun onConnect(signedInAutomatically: Boolean) {
        Log.d(TAGRUNLOGIN, "$TAG onConnect run ")

        if (signedInAutomatically) {
            Log.d(TAGRUNLOGIN, "$TAG signedInAutomatically true")
            token = mcontext.getString(R.string.login_ok)
            try {
                if (::mutex.isInitialized) {
                    synchronized(mutex) {
                        mutex.notify()
                    }
                }
            } catch (e: Exception) {
                val error = e.message
                Log.d(TAGRUNLOGIN, "$TAG Wait $error ")
            }
        } else {
            if (!mEmail.isNullOrEmpty() && !mPass.isNullOrEmpty()) {
                Log.d(TAGRUNLOGIN, "$TAG mEmail $mEmail ")
                Log.d(TAGRUNLOGIN, "$TAG mEmail $mPass ")
                meteor.loginWithEmail(mEmail, mPass, object : ResultListener {

                    override fun onSuccess(result: String?) {
                        Log.d(TAGRUNLOGIN, "$TAG onSuccess $result ")
                        val convertedObject = Gson().fromJson(result, JsonObject::class.java)

                        val spUserID = PreferenceManager.getDefaultSharedPreferences(mcontext.applicationContext)
                        spUserID.edit()
                                .putString(KEY_USERID, convertedObject.getAsJsonPrimitive("id").asString)
                                .apply()

                        token = convertedObject.getAsJsonPrimitive("token").asString

                        try {
                            if (::mutex.isInitialized) {
                                synchronized(mutex) {
                                    mutex.notify()
                                }
                            }
                        } catch (e: Exception) {
                            val error = "onSuccess error" + e.message
                            Log.d(TAGRUNLOGIN, "$TAG $error")
                        }
                    }

                    override fun onError(error: String?, reason: String?, details: String?) {
                        Log.d(TAGRUNLOGIN, "$TAG $error")
                        Log.d(TAGRUNLOGIN, "$TAG $reason")
                        Log.d(TAGRUNLOGIN, "$TAG $details")
                        Log.d(TAGRUNLOGIN, "$TAG onError")
                        Log.d(TAGRUNLOGIN, "$TAG $mEmail")
                        Log.d(TAGRUNLOGIN, "$TAG $mPass")
                        meteor.logout()
                        token = /*mcontext.getString(R.string.login_error) +*/ AccountGeneral.INCORRECT_PASS
                        EuronetMeteorSingleton.destroyInstance()
                        try {
                            if (::mutex.isInitialized) {
                                synchronized(mutex) {
                                    mutex.notify()
                                }
                            }
                        } catch (e: Exception) {
                            val loginError = "onError error" + e.message
                            Log.d(TAGRUNLOGIN, "$TAG $loginError")
                        }
                    }
                })
            } else {
                token = mcontext.getString(R.string.login_error)
                val error = "   if(!mEmail.isNullOrEmpty() && !mPass.isNullOrEmpty()){"
                Log.d(TAGRUNLOGIN, "$TAG  $error")

                meteor.removeCallback(this)
                EuronetMeteorSingleton.destroyInstance()
                try {
                    if (::mutex.isInitialized) {
                        synchronized(mutex) {
                            mutex.notify()
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAGRUNLOGIN, "$TAG  ${e.message}")
                }
            }
        }
    }

    override fun onDataAdded(collectionName: String?, documentID: String?, newValuesJson: String?) {
        val mCollectionName = "onDataAdded $collectionName"
        val mDocumentID = "onDataAdded $documentID"
        val mNewValuesJson = "onDataAdded$newValuesJson"

        Log.d(TAGRUNLOGIN, "$TAG $mCollectionName")
        Log.d(TAGRUNLOGIN, "$TAG $mDocumentID")
        Log.d(TAGRUNLOGIN, "$TAG $mNewValuesJson")
    }

    override fun onDataRemoved(collectionName: String?, documentID: String?) {
        val mCollectionName = "onDataRemoved $collectionName"
        val mDocumentID = "onDataRemoved $documentID"

        Log.d(TAGRUNLOGIN, "$TAG $mCollectionName")
        Log.d(TAGRUNLOGIN, "$TAG $mDocumentID")
    }

    override fun onException(e: java.lang.Exception?) {
        val error = "onError error" + e!!.message
        Log.d(TAGRUNLOGIN, "$TAG $error")
        token = mcontext.getString(R.string.login_error)
        Log.d(TAGRUNLOGIN, "$TAG $error")
    }

    override fun onDisconnect() {
        val ondisconnect = "onDisconnect"
        Log.d(TAGRUNLOGIN, "$TAG $ondisconnect")
    }

    override fun onDataChanged(collectionName: String?, documentID: String?, updatedValuesJson: String?, removedValuesJson: String?) {
        val mCollectionName = "onDataChanged $collectionName"
        val mDocumentID = "onDataChanged $documentID"
        val mNewValuesJson = "onDataChanged $updatedValuesJson"
        val mRemovedValuesJson = "onDataChanged $removedValuesJson"

        Log.d(TAGRUNLOGIN, "$TAG $mCollectionName")
        Log.d(TAGRUNLOGIN, "$TAG $mDocumentID")
        Log.d(TAGRUNLOGIN, "$TAG  $mNewValuesJson")
        Log.d(TAGRUNLOGIN, "$TAG $mRemovedValuesJson")
    }
}