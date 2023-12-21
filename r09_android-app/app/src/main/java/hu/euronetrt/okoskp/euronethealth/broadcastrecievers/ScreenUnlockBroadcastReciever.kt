package hu.euronetrt.okoskp.euronethealth.broadcastrecievers

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import hu.euronetrt.okoskp.euronethealth.questionnaire.timer.QuestionnaireOneThread

class ScreenUnlockBroadcastReciever : BroadcastReceiver() {

    companion object {
        val TAG = "ScreenUnlockBroadcastReciever"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.action
            val myKM : KeyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if(action.equals(Intent.ACTION_USER_PRESENT)
                        || action.equals(Intent.ACTION_SCREEN_OFF)
                        || action.equals(Intent.ACTION_SCREEN_ON)  )
                    if( myKM.isKeyguardLocked)
                    {
                        Log.d(TAG,"Screen off " + "LOCKED")
                    } else
                    {
                        Log.d(TAG,"Screen off " + "UNLOCKED")
                        QuestionnaireOneThread.getInstance(context).screenUnlock()
                    }
            /*GlobalRes.ACTION_SCREEN_ON -> {
                Log.d(TAG, "TAGQUESTION -> ACTION_SCREEN_ON")
                if (context is BluetoothLeService) {
                    bleService = context
                    bleService.qetQuestionnaireCheck()
                } else {
                    Log.d(TAG, "Not implement context fragment")
                }
            }*/
    }
}
