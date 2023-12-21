package hu.aut.android.dm01_v11.ui.activities.questionnaire

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hu.aut.android.dm01_v11.R
import hu.euronetrt.okoskp.euronethealth.GlobalRes
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects.*
import hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects.QuestionnaireGetModel
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.meteor.EuronetMeteorSingleton
import im.delight.android.ddp.ResultListener
import kotlinx.android.synthetic.main.activity_fullscreen.*
import java.text.SimpleDateFormat

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {

    companion object {
        private val TAG = "FullscreenActivity"
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000
    }

    private lateinit var questionnaireModel: QuestionnaireGetModel
    private lateinit var questionnaireResultListFillableElement: QuestionnaireResultListFillable
    private var handlerDeactiv = false
    private var now = System.currentTimeMillis()
    private lateinit var bleService: BluetoothLeService
    private var enabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bindService(
                Intent(this, BluetoothLeService::class.java),
                bleServiceConnection,
                Context.BIND_AUTO_CREATE
        )
        MyTimeThread().start()
    }

    private inner class MyTimeThread : Thread() {
        @SuppressLint("SimpleDateFormat")
        override fun run() {
            val handlerMain = Handler(Looper.getMainLooper())
            while (enabled) {
                handlerMain.post {
                    if(questionnaireResultListFillableElement.scheduledUntil!!.date!! > System.currentTimeMillis()){
                        if(!handlerDeactiv){
                            /*ne szakitsuk meg ha épp szerver kommunikáció van.*/
                            myFinishActivity()
                        }
                    }else{
                        val time = questionnaireResultListFillableElement.scheduledUntil!!.date!! - System.currentTimeMillis()
                        val formatter = SimpleDateFormat("mm:ss")
                        id_progress_timer_count.text = formatter.format(time)
                    }
                }
                sleep(2000)
            }
        }
    }

    private val bleServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e(TAG, "onServiceDisconnected Bluetooth")
        }

        override fun onServiceConnected(name: ComponentName?, serviceBinder: IBinder?) {
            val mBluetoothLeService = (serviceBinder) as BluetoothLeService.BluetoothLeServiceBinder
            bleService = mBluetoothLeService.service
            if (!bleService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth")
            } else {
                val pair = bleService.getFullScreanActData()
                questionnaireModel = pair.second
                questionnaireResultListFillableElement = pair.first
                Log.e(TAG, "onServiceConnected cal getFreshModel")
                getFreshModel()
            }
        }
    }

    private fun getFreshModel() {
        if (EuronetMeteorSingleton.getInstance().isConnected) {

            val gson = Gson()

            val params = arrayOf("")
            params[0] = questionnaireResultListFillableElement.id

            Log.e(TAG, "questionnaireResultListFillableElement.id : ${questionnaireResultListFillableElement.id}")

            EuronetMeteorSingleton.getInstance().call("questionnaire-result.get", params, object : ResultListener {
                override fun onSuccess(result: String?) {
                    Log.d(TAG, "-->questionnaire-result.get $result ")
                    val REVIEW_TYPE2 = object : TypeToken<QuestionnaireResultListFillable>() {}.type
                    questionnaireResultListFillableElement = gson.fromJson(result, REVIEW_TYPE2)
                    process()
                }

                override fun onError(error: String?, reason: String?, details: String?) {
                    Log.d(TAG, "-->error $error ")
                    Log.d(TAG, "-->error $reason ")
                    Log.d(TAG, "-->error $details ")
                    if (reason == "Empty ID!") {
                        /**
                         * nem volt még create biztosan*/
                        createFirst()
                    }
                }
            })
        }
    }

    private fun createFirst() {
        if (questionnaireResultListFillableElement.id == "null" || questionnaireResultListFillableElement.id.isNullOrEmpty()) {
            val params = Array<Any>(2) {}
            params[0] = questionnaireResultListFillableElement.questionnaireId
            params[1] = questionnaireResultListFillableElement.scheduleOrder

            Log.e(TAG, "--> questionnaireResultListFillableElement.questionnaireId ${questionnaireResultListFillableElement.questionnaireId}")
            Log.e(TAG, "--> questionnaireResultListFillableElement.scheduleOrder ${questionnaireResultListFillableElement.scheduleOrder}")

            EuronetMeteorSingleton.getInstance().call("questionnaire-result.create", params, object : ResultListener {
                override fun onSuccess(result: String?) {
                    val gson = Gson()
                    val REVIEW_TYPE = object : TypeToken<QuestionnaireResultListFillable>() {}.type

                    questionnaireResultListFillableElement = gson.fromJson(result, REVIEW_TYPE)

                    val questionnaireObejtTesztMiatt = gson.toJson(questionnaireResultListFillableElement, REVIEW_TYPE)
                    Log.e(TAG, "questionnaireObejtTesztMiatt - create response  ${questionnaireObejtTesztMiatt} ")
                    process()
                }

                override fun onError(error: String?, reason: String?, details: String?) {
                    Log.e(TAG, "-->error $error ")
                    Log.e(TAG, "-->error $reason ")
                    Log.e(TAG, "-->error $details ")
                }
            })
        }
    }

    private fun process() {

        id_text_question.text = questionnaireModel.template.sections[0].questions[0].text
        id_text_max.text = questionnaireModel.template.sections[0].questions[0].answers[questionnaireModel.template.sections[0].questions[0].answers.lastIndex]!!.text
        id_text_min.text = questionnaireModel.template.sections[0].questions[0].answers[0]!!.text

        ratingBar.max = questionnaireModel.template.sections[0].questions[0].answers.size
        if (ratingBar.max % 2 == 0) {
            ratingBar.stepSize = 0.5f
        } else {
            ratingBar.stepSize = 1f
        }

        ratingBar.numStars = questionnaireModel.template.sections[0].questions[0].answers.size
        ratingBar.setBackgroundColor(Color.WHITE)

        id_done.setOnClickListener {
            Toast.makeText(this, "${ratingBar.rating}", Toast.LENGTH_LONG).show()
            if (ratingBar.rating == 0.0f) {
                Snackbar.make(id_done, "Please select!", Snackbar.LENGTH_LONG).show()
            } else {
                handlerDeactiv = true
                sendAndFinish(ratingBar.rating)
            }
        }
    }

    private fun sendAndFinish(rating: Float) {

        val answers = ArrayList<QuestionnaireResultAnswer>()
        val qRAnswer = QuestionnaireResultAnswerIntValue(
                /*a kérdések számozása 1 től indul 1-el növekvő monoton , az index pedig 0-tól indul*/
                questionnaireModel.template.sections[0].questions[0].order,
                questionnaireResultListFillableElement.startedAt?.date
                        ?: now,
                System.currentTimeMillis(),
                rating.toInt()
        )
        answers.add(qRAnswer)

        val sectionArray = ArrayList<QuestionnaireResultSection>()
        val section = QuestionnaireResultSection(
                questionnaireModel.template.sections[0].order,
                questionnaireModel.template.sections[0].type,
                answers,
                now,
                System.currentTimeMillis()
        )
        sectionArray.add(section)

        val questionnaireResultForServer = QuestionnaireResultForServer(

                questionnaireResultListFillableElement.seq,
                questionnaireResultListFillableElement.creator,
                questionnaireResultListFillableElement.created_at,
                questionnaireResultListFillableElement.modifier,
                questionnaireResultListFillableElement.modified_at,
                questionnaireResultListFillableElement.active,
                questionnaireResultListFillableElement.id,
                questionnaireResultListFillableElement.questionnaireId,
                questionnaireResultListFillableElement.userId,
                questionnaireResultListFillableElement.scheduleOrder,
                questionnaireResultListFillableElement.scheduledAt?.date,
                questionnaireResultListFillableElement.scheduledUntil?.date,
                questionnaireResultListFillableElement.state,
                questionnaireResultListFillableElement.startedAt?.date,
                questionnaireResultListFillableElement.finishedAt?.date,
                sectionArray,
                questionnaireResultListFillableElement.suspends
        )

        val params = Array<Any>(3) {}
        params[0] = questionnaireResultForServer.id
        params[1] = questionnaireResultForServer.seq
        params[2] = questionnaireResultForServer

        val jsonString = questionnaireResultForServer.toString()

        Log.d(TAG, "questionnaireResult $jsonString,  ${params[0]}  ${params[1]}")

        val gson = Gson()
        val REVIEW_TYPE_ID = object : TypeToken<QuestionnaireResultForServer>() {}.type
        val questionnaireObejtTesztMiatt = gson.toJson(questionnaireResultForServer, REVIEW_TYPE_ID)
        Log.d(TAG, "questionnaireObejtTesztMiatt - Finish  ${questionnaireObejtTesztMiatt} ")

        EuronetMeteorSingleton.getInstance().call("questionnaire-result.finish", params, object : ResultListener {
            override fun onSuccess(result: String?) {
                Log.d(TAG, " questionnaire-result.Finish -->result $result ")

                /**sikeres finish esetén zárjuk az aktuális kérdőívet és frissítjük a listát*/
                val oldCounter = PreferenceManager.getDefaultSharedPreferences(applicationContext).getInt(BluetoothLeService.KEY_NEW_QUESTIONNAIRECOUNTER, -1)
                val KEY_NEWQUESTIONNAIRECOUNTER = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                KEY_NEWQUESTIONNAIRECOUNTER.edit()
                        .putInt(BluetoothLeService.KEY_NEW_QUESTIONNAIRECOUNTER, oldCounter - 1)
                        .apply()
                handlerDeactiv = false
            }

            override fun onError(error: String?, reason: String?, details: String?) {
                Log.d(TAG, "questionnaire-result.Finish -->error $error ")
                Log.d(TAG, "questionnaire-result.Finish -->error $reason ")
                Log.d(TAG, "questionnaire-result.Finish -->error $details ")
            }
        })
    }

    override fun onPause() {
        Log.d(TAG, "onPause ")
        super.onPause()
    }

    override fun onResume() {
        Log.d(TAG, "onResume  ")
        super.onResume()
    }

    private fun myFinishActivity() {
        try {
            finish()
        }catch (e: Exception){
            Log.d(TAG, "finish $e ")
        }
    }

    override fun onDestroy() {
        unbindService(bleServiceConnection)
        GlobalRes.FullScreenActivityActive = false
        super.onDestroy()
    }
}
