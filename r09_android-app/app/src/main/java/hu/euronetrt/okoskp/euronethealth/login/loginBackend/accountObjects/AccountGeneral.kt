package hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects

import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountInterface.ServerAuthenticate
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountManager.server.CommunicateServerAuthenticate

object AccountGeneral {
    val INCORRECT_PASS = "INCORRECT_PASS"
    /**
     * Account type id
     */
    val ACCOUNT_TYPE = "hu.aut.android.dm01_v11"

    /**
     * Account name
     */
    val ACCOUNT_NAME = "Euronet"

    /**
     * Auth token types
     */
    val AUTHTOKEN_TYPE_READ_ONLY = "Read only"
    val AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an Udinic account"
    val AUTHTOKEN_TYPE_FULL_ACCESS = "Full access"
    val AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an Euronet account"
    val ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT"
    val sServerAuthenticate: ServerAuthenticate = CommunicateServerAuthenticate()

    val KEY_ERROR_MESSAGE = "ERR_MSG"
    val KEY_ERROR_SERVER = "KEY_ERROR_SERVER"
    val PARAM_USER_PASS = "USER_PASS"

    val mHost = "192.168.0.228"
}