package hu.euronetrt.okoskp.euronethealth.questionnaire.timer

import android.content.Context
import android.util.Log
import hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects.QuestionnaireResultListFillable
import hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects.QuestionnaireGetModel
import java.util.*

class QuestionnaireTimerTask(var questionnairePair: Pair<QuestionnaireResultListFillable,QuestionnaireGetModel>, val context: Context) : TimerTask() {

    companion object{
        private val TAG = "QuestionnaireTimerTask"
    }

    override fun run() {
        Log.d(TAG, "Run timer task guestionnaireid: ${questionnairePair.first.id}")
        val context = QuestionnaireOneThread.getInstance(context)
        context.startActivityFullScreen(questionnairePair)
    }
}
