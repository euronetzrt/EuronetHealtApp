package hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountInterface

import android.content.Context

interface ServerAuthenticate {
    @Throws(Exception::class)
    fun userSignUp(context: Context, email: String, pass: String, authType: String) : String
}