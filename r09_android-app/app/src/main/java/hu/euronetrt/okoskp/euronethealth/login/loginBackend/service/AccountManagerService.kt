package hu.euronetrt.okoskp.euronethealth.login.loginBackend.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountManager.AbstractAccountAuth

class AccountManagerService : Service(){

    override fun onBind(intent: Intent): IBinder? {
        val authenticator = AbstractAccountAuth(this)
        return authenticator.iBinder
    }
}