package hu.euronetrt.okoskp.euronethealth.questionnaire.timer

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.facebook.FacebookSdk.getApplicationContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hu.aut.android.dm01_v11.ui.activities.questionnaire.FullscreenActivity
import hu.euronetrt.okoskp.euronethealth.GlobalRes.FullScreenActivityActive
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral
import hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects.QuestionnaireResultListFillable
import hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects.QuestionnaireGetModel
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.meteor.EuronetMeteorSingleton
import im.delight.android.ddp.MeteorCallback
import im.delight.android.ddp.ResultListener
import im.delight.android.ddp.db.memory.InMemoryDatabase
import org.json.JSONArray
import java.util.*


class QuestionnaireOneThread(val context: Context) : Thread(), MeteorCallback {

    private var enabled = true
    private lateinit var meteor: EuronetMeteorSingleton
    private var notationQuestionnaireList: MutableMap<String, Pair<QuestionnaireResultListFillable, QuestionnaireGetModel>> = mutableMapOf()
    private lateinit var questionnaireResultListFillable: QuestionnaireResultListFillable
    private lateinit var fullScreanActData: JSONArray
    private var i = 0
    private var showIfScreenOnIfActiveYet: Pair<QuestionnaireResultListFillable, QuestionnaireGetModel>? = null

    companion object {
        private val TAG = "QUESTIONNAIREONETHREAD"
        private val SLEEPINTERVAL: Long = 600000/*10min*/

        private var INSTANCE: QuestionnaireOneThread? = null

        @Synchronized
        fun getInstance(mContext: Context): QuestionnaireOneThread {
            if (INSTANCE == null) {
                INSTANCE = QuestionnaireOneThread(mContext)
            }
            return INSTANCE!!
        }
    }

    override fun run() {
        while (enabled) {
            Log.d(TAG, "qetQuestionnaireCheck run")
            qetQuestionnaireCheck()

            run {
                Log.d(TAG, " + restart again 10 min+")
                try {
                    sleep(SLEEPINTERVAL)
                } catch (e: InterruptedException) {
                    Log.d(TAG, " + INTEUPTED EXCEPTION")
                    this.interrupt()
                }
            }
        }
    }

    fun qetQuestionnaireCheck() {
        Log.d(TAG, "qetQuestionnaireCheck")
        if (!::meteor.isInitialized) {
            Log.d(TAG, "!::meteor.isInitialized")
            meteor = if (EuronetMeteorSingleton.hasInstance()) {
                EuronetMeteorSingleton.getInstance()
            } else {
                EuronetMeteorSingleton.createInstance(context, "ws://${AccountGeneral.mHost}:3000/websocket", InMemoryDatabase())
            }
        }
        meteor.addCallback(this)

        if (!meteor.isConnected) {
            meteor.connect()
            Log.d(TAG, " meteor.connect()")
        } else {
            meteor.call("questionnaire-result.listFillable", object : ResultListener {
                override fun onSuccess(result: String?) {
            //        Log.d(TAG, "result questionnaire-result.listFillable   $result")

                    val resultQuestionnaireListFillableArray = JSONArray(result)
                    getQuestions(resultQuestionnaireListFillableArray)
                }

                override fun onError(error: String?, reason: String?, details: String?) {
                    Log.d(TAG, "-->error $error ")
                    Log.d(TAG, "-->error $reason ")
                    Log.d(TAG, "-->error $details ")
                }
            })
        }
    }

    fun getQuestions(resultQuestionnaireListFillableArray: JSONArray) {

        val gson = Gson()
        val RT = object : TypeToken<QuestionnaireResultListFillable>() {}.type
        questionnaireResultListFillable = gson.fromJson(resultQuestionnaireListFillableArray[i].toString(), RT)
        //   Log.d(TAG, "-->i $i  ${resultQuestionnaireListFillableArray.length()}   questionnaireResultListFillable   ${questionnaireResultListFillable.questionnaireId}")

        /**Még kitölthető*/
        val params = arrayOf("")
        params[0] = questionnaireResultListFillable.questionnaireId

        meteor.call("questionnaire.get", params, object : ResultListener {
            override fun onSuccess(result: String?) {

                Log.d(TAG, "onSuccess ${questionnaireResultListFillable.questionnaireId}")

                if (!result.isNullOrEmpty() && result != "[]" && result != "{}" && result != "{[]}" && result != "[{}]" && result != "null" && result != " null") {
                    val REVIEW_TYPE_QUESTIONNAIREGET = object : TypeToken<QuestionnaireGetModel>() {}.type
                    val questionnaireGetModel: QuestionnaireGetModel = gson.fromJson(result, REVIEW_TYPE_QUESTIONNAIREGET)

                    /*itt kell megvizsgálnihogy milyen típus és mikor töltendő ki. majd ha ezek megvannak akkor el kell menteni őket plusz
                    * megoldani hogy a kitöltési idejükben feldobni őket. ha nincs új akkor automata lekérdezés mehet 1 óránként. kb*/

                    //  Log.d(TAG, "questionnaireGetModel.template.type --> ${questionnaireGetModel.template.type}")
                    if (questionnaireGetModel.template.type == 0) {
                            Log.d(TAG, "questionnaireResultListFillable.id:  ${questionnaireResultListFillable.id}")
                            Log.d(TAG, "scheduletAt ettől: ${questionnaireResultListFillable.scheduledAt!!.date} ")

                        //    if (notationQuestionnaireList.contains(questionnaireResultListFillable.questionnaireId)
                        //  ) {
                        /*ha bent van akkor megnézzük hogy a dátumok különböznek-e*/
                        notationQuestionnaireList.forEach {
                            if (it.key == questionnaireResultListFillable.questionnaireId) {
                                if (it.value.first.scheduledAt != questionnaireResultListFillable.scheduledAt
                                        ||
                                        it.value.first.scheduledUntil != questionnaireResultListFillable.scheduledUntil
                                ) {
                                    if (questionnaireResultListFillable.scheduledAt!!.date!! > System.currentTimeMillis()
                                            &&
                                            questionnaireGetModel.active) {

                                        /*az id bent van de az idők mások tehát erre is kell timer mivel az időkeretbe esnek, mehet  a listába*/
                                        Log.d(TAG, "save questionnaire  first show: ${questionnaireResultListFillable.scheduledAt!!.date!!}")
                                        /*Mentendő elem*/
                                        val pair = Pair(questionnaireResultListFillable, questionnaireGetModel)
                                        notationQuestionnaireList[questionnaireResultListFillable.questionnaireId] = pair
                                    }
                                }
                            } else {
                                /*az elem nincs a listában de az időkertbe kell essen*/
                                if (questionnaireResultListFillable.scheduledAt!!.date!! > System.currentTimeMillis()
                                        &&
                                        questionnaireGetModel.active) {
                                    Log.d(TAG, "save questionnaire  first show: ${questionnaireResultListFillable.scheduledAt!!.date!!}")
                                    /*Mentendő elem*/
                                    val pair = Pair(questionnaireResultListFillable, questionnaireGetModel)
                                    notationQuestionnaireList[questionnaireResultListFillable.questionnaireId] = pair
                                }
                            }
                        }
                    }

                    // Log.d(TAG, "notationQuestionnaireList size ${notationQuestionnaireList.size}")

                    if ((i + 1) == resultQuestionnaireListFillableArray.length()) {
                        //   Log.d(TAG, "i: $i  length- ${resultQuestionnaireListFillableArray.length()}")
                        runProcess()
                    } else {
                        i++
                        // Log.d(TAG, "else i: $i  length- ${resultQuestionnaireListFillableArray.length()}")
                        getQuestions(resultQuestionnaireListFillableArray)
                    }
                }
            }

            override fun onError(error: String?, reason: String?, details: String?) {
                Log.d(TAG, "-->error $error ")
                Log.d(TAG, "-->error $reason ")
                Log.d(TAG, "-->error $details ")
            }
        })
    }

    private fun runProcess() {
        Log.d(TAG, "runProcess ")
        if (notationQuestionnaireList.isNotEmpty()) {
            Log.d(TAG, "--> notationQuestionnaireList.size > 0 ")

            notationQuestionnaireList.forEach {
                //Now create the time and schedule it
                val timer = Timer()
                //the Date and time at which you want to execute
                val date = it.value.first.scheduledAt!!.date!!
                //Use this if you want to execute it once
                timer.schedule(QuestionnaireTimerTask(it.value, context), date)

                /**
                 * own test fun
                 * */
                // teszt(it.value)
            }
        }
    }

    fun startActivityFullScreen(questionnairePair: Pair<QuestionnaireResultListFillable, QuestionnaireGetModel>) {
        Log.d(TAG, "startActivityFullScreen ")
        if (!FullScreenActivityActive) {
            if (questionnairePair.second.activeTo.date < System.currentTimeMillis() && questionnairePair.second.activeFrom.date > System.currentTimeMillis()) {
                val vibrate: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                try {
                    val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val r = RingtoneManager.getRingtone(getApplicationContext(), notification)
                    r.play()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val vibrateArray = LongArray(3)
                    vibrateArray[0] = 100
                    vibrateArray[1] = 500
                    vibrateArray[2] = 100
                    vibrate.vibrate(VibrationEffect.createWaveform(vibrateArray, -1))
                } else {
                    //deprecated in API 26
                    vibrate.vibrate(500)
                }

                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
                val isScreenOn = pm!!.isInteractive
                if (isScreenOn) {
                    (context as BluetoothLeService).setFullScreanActData(questionnairePair)
                    context.startActivity(Intent(context, FullscreenActivity::class.java))
                    /*elindult a timer kivesszük a listából hogy ha ujra lekérve az adatokat megint bent van akkor beállítsuk*/
                    notationQuestionnaireList.remove(questionnairePair.first.questionnaireId)
                    FullScreenActivityActive = true
                } else {
                    showIfScreenOnIfActiveYet = questionnairePair
                }
            }
        }
    }

    /*  fun teszt(notationQuestionnaireList: Pair<QuestionnaireResultListFillable, QuestionnaireGetModel>) {
          if(!FullScreenActivityActive){
              try {
                  val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                  val r = RingtoneManager.getRingtone(getApplicationContext(), notification)
                  r.play()
              } catch (e: java.lang.Exception) {
                  e.printStackTrace()
              }
              (context as BluetoothLeService).setFullScreanActData(notationQuestionnaireList)
              context.startActivity(Intent(context, FullscreenActivity::class.java))
              FullScreenActivityActive = true
          }
      }*/

    fun stopAndInterrupt() {
        enabled = false
        interrupt()
    }

    fun deleteItem(key: String) {
        notationQuestionnaireList.remove(key)
    }

    fun screenUnlock() {
        if (showIfScreenOnIfActiveYet != null) {
            if (showIfScreenOnIfActiveYet!!.first.scheduledUntil!!.date!! > System.currentTimeMillis()) {
                (context as BluetoothLeService).setFullScreanActData(showIfScreenOnIfActiveYet!!)
                context.startActivity(Intent(context, FullscreenActivity::class.java))
                notationQuestionnaireList.remove(showIfScreenOnIfActiveYet!!.first.questionnaireId)
                FullScreenActivityActive = true
            }
        }
    }

    override fun onConnect(signedInAutomatically: Boolean) {
        Log.d(TAG, " onConnect $signedInAutomatically")
        if (signedInAutomatically) {
            meteor.call("questionnaire-result.listFillable", object : ResultListener {
                override fun onSuccess(result: String?) {
                    Log.d(TAG, "result questionnaire-result.listFillable")

                    val resultQuestionnaireListFillableArray = JSONArray(result)
                    getQuestions(resultQuestionnaireListFillableArray)
                }

                override fun onError(error: String?, reason: String?, details: String?) {
                    Log.d(TAG, "-->error $error ")
                    Log.d(TAG, "--> error $reason ")
                    Log.d(TAG, "-->error $details ")
                }
            })
        }
    }

    override fun onDataAdded(collectionName: String?, documentID: String?, newValuesJson: String?) {
        val mCollectionName = "onDataAdded $collectionName"
        val mDocumentID = "onDataAdded $documentID"
        val mNewValuesJson = "onDataAdded$newValuesJson"

        Log.d(TAG, mCollectionName)
        Log.d(TAG, mDocumentID)
        Log.d(TAG, mNewValuesJson)
    }

    override fun onDataRemoved(collectionName: String?, documentID: String?) {
        val mCollectionName = "onDataRemoved $collectionName"
        val mDocumentID = "onDataRemoved $documentID"

        Log.d(TAG, mCollectionName)
        Log.d(TAG, mDocumentID)
    }

    override fun onException(e: Exception) {
        val onexception = "onException ${e.message}"
        Log.d(TAG, onexception)
    }

    override fun onDisconnect() {
        val ondisconnect = "onDisconnect"
        Log.d(TAG, ondisconnect)
    }

    override fun onDataChanged(collectionName: String?, documentID: String?, updatedValuesJson: String?, removedValuesJson: String?) {
        val mCollectionName = "onDataChanged $collectionName"
        val mDocumentID = "onDataChanged $documentID"
        val mNewValuesJson = "onDataChanged $updatedValuesJson"
        val mRemovedValuesJson = "onDataChanged $removedValuesJson"

        Log.d(TAG, mCollectionName)
        Log.d(TAG, mDocumentID)
        Log.d(TAG, mNewValuesJson)
        Log.d(TAG, mRemovedValuesJson)
    }
}