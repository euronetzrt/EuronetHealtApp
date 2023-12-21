package hu.aut.android.dm01_v11.ui.fragments.questionnaire

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hu.aut.android.dm01_v11.R
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_USERID
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral
import hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects.*
import hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects.QuestionnaireGetModel
import hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects.TemplateSectionModel
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.meteor.EuronetMeteorSingleton
import im.delight.android.ddp.MeteorCallback
import im.delight.android.ddp.ResultListener
import im.delight.android.ddp.db.memory.InMemoryDatabase
import kotlinx.android.synthetic.main.section.*


class SectionFragment(var questionnaireResultListFillableElement: QuestionnaireResultListFillable, var questionnairegetObject: QuestionnaireGetModel) : Fragment(), MeteorCallback {

    companion object {
        val TAG = "SectionFragment"
        val TAGRESREQ = "TAGRESREQ"
        val TAGNOTIFY = "TAGNOTIFY"
    }

    private var currentSectionPage = 0
    private lateinit var meteor: EuronetMeteorSingleton
    var sectionsFinished = mutableMapOf<Int, OwnModel?>()

    private lateinit var questionnaireResultForServer: QuestionnaireResultForServer

    lateinit var viewPager: ViewPager
    var checkError = false
    lateinit var adapter: CustomPagerAdapter
    lateinit var finish: Button
    private var updateWork = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.section, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewpager.setOnClickListener {
            val imm: InputMethodManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        pager_title_strip.setOnClickListener {
            val imm: InputMethodManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        id_progressWheel.visibility = View.VISIBLE

        viewPager = viewpager
        adapter = CustomPagerAdapter(childFragmentManager)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            @Synchronized
            override fun onPageSelected(position: Int) {
                update(position, view, false)
            }
        })

        getFreshModel(viewPager, adapter, false, false)

        id_finishButton.setOnClickListener { itView ->
            finishFunction(itView)
        }
    }

    @Synchronized
    private fun updateSection(answers: ArrayList<QuestionnaireResultAnswer>, actualSectionBlock: TemplateSectionModel, startDate: Long, sectionArray: ArrayList<QuestionnaireResultSection>, suspendArray: ArrayList<QuestionnaireResultSuspend>, nextFinish: Boolean) {
        if (!updateWork) {
            updateWork = true
            if (answers.size != 0) {
                val qRSection = QuestionnaireResultSection(
                        actualSectionBlock.order,
                        actualSectionBlock.type,
                        answers,
                        questionnaireResultListFillableElement.startedAt?.date ?: startDate,
                        System.currentTimeMillis()
                )

                var mustAdd = true

                for (i in 0 until sectionArray.size) {
                    if (sectionArray[i].order == actualSectionBlock.order) {
                        sectionArray[i] = qRSection
                        mustAdd = false
                    }
                }

                if (mustAdd) {
                    sectionArray.add(qRSection)
                }

                val userId = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getString(KEY_USERID, null)

                if (!userId.isNullOrEmpty()) {
                    questionnaireResultListFillableElement.userId = userId

                    questionnaireResultForServer = QuestionnaireResultForServer(
                            questionnaireResultListFillableElement.seq,
                            questionnaireResultListFillableElement.creator,
                            questionnaireResultListFillableElement.createdAt.date,
                            questionnaireResultListFillableElement.modifier,
                            questionnaireResultListFillableElement.modifiedAt?.date,
                            questionnaireResultListFillableElement.active,
                            questionnaireResultListFillableElement.id,
                            questionnaireResultListFillableElement.questionnaireId,
                            questionnaireResultListFillableElement.userId,
                            questionnaireResultListFillableElement.scheduleOrder,
                            questionnaireResultListFillableElement.scheduledAt?.date,//questionnaireResultListFillableElement.scheduledAt!!.date,
                            questionnaireResultListFillableElement.scheduledUntil?.date,
                            questionnaireResultListFillableElement.state,
                            questionnaireResultListFillableElement.startedAt!!.date,
                            questionnaireResultListFillableElement.finishedAt?.date,
                            sectionArray,
                            suspendArray
                    )

                    val params = Array<Any>(3) {}
                    params[0] = questionnaireResultListFillableElement.id
                    params[1] = questionnaireResultListFillableElement.seq
                    params[2] = questionnaireResultForServer

                    Log.d(TAGRESREQ, "questionnaire-result.update params:  ${params[0]}  ${params[1]}  ${params[2]} ")

                    val gson = Gson()
                    val REVIEW_TYPE = object : TypeToken<QuestionnaireResultForServer>() {}.type
                    val questionnaireUPDATE = gson.toJson(questionnaireResultForServer, REVIEW_TYPE)
                    Log.d(TAGRESREQ, "questionnaireUPDATE  ${questionnaireUPDATE} ")

                    meteor.call("questionnaire-result.update", params, object : ResultListener {
                        override fun onSuccess(result: String?) {
                            Log.e(TAGRESREQ, "questionnaireUPDATE  succes!!!!!!!! ")
                            Toast.makeText(view!!.context, "Saved!", Toast.LENGTH_SHORT).show()
                            updateWork = false
                            getFreshModel(null, null, true, nextFinish)
                        }

                        override fun onError(error: String?, reason: String?, details: String?) {
                            Log.d(TAGRESREQ, "questionnaire-result.update -->error $error ")
                            Log.d(TAGRESREQ, "questionnaire-result.update -->error $reason ")
                            Log.d(TAGRESREQ, "questionnaire-result.update -->error $details ")
                        }
                    })
                } else {
                    Log.e(TAGRESREQ, "USERID is null !!!!!!!!!!!!!!! $userId ")
                }
            }
        }
    }

    override fun onConnect(signedInAutomatically: Boolean) {
        Log.d(TAG, "onConnect questionnaire $signedInAutomatically")
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

    @Synchronized
    private fun getFreshModel(viewPager: ViewPager?, adapter: CustomPagerAdapter?, justModelRefresh: Boolean, nextFinish: Boolean) {

        meteor = if (EuronetMeteorSingleton.hasInstance()) {
            EuronetMeteorSingleton.getInstance()
        } else {
            EuronetMeteorSingleton.createInstance(view!!.context, "ws://${AccountGeneral.mHost}:3000/websocket", InMemoryDatabase())
        }

        if (meteor.isConnected) {

            val gson = Gson()

            val params = arrayOf("")
            params[0] = questionnaireResultListFillableElement.id

            meteor.call("questionnaire-result.get", params, object : ResultListener {
                override fun onSuccess(result: String?) {
                    Log.d(TAG, "-->questionnaire-result.get $result ")
                    val REVIEW_TYPE2 = object : TypeToken<QuestionnaireResultListFillable>() {}.type

                    questionnaireResultListFillableElement = gson.fromJson(result, REVIEW_TYPE2)
                    //  val questionnaireObejtTesztMiatt2 = gson.toJson(questionnaireResultListFillableElement, REVIEW_TYPE2)
                    Log.d(TAG, "getFreshModel ${questionnaireResultListFillableElement.seq} \n\n\n")
                    var firstResumableSectionOrder = -1
                    if (!justModelRefresh) {
                        questionnairegetObject.template.sections.forEach {
                            /*  Log.d(TAG, "-->questionnaireResultListFillableElement $questionnaireObejtTesztMiatt2 ")
                              Log.d(TAG, "-->questionnairegetObject $questionnairegetObject ")
                              Log.d(TAG, "-->error it $it ")*/
                            val REVIEW_TYPE3 = object : TypeToken<TemplateSectionModel>() {}.type
                            val actualSectionBlock = gson.toJson(it, REVIEW_TYPE3)
                            Log.d(TAG, "actualSectionBlock:  $actualSectionBlock")


                            var title = ""
                            if (it.required) {
                                title = title + " *req."
                            }
                            if (it.resumable) {
                                if (title != "") {
                                    title = title + "/res."
                                } else {
                                    title = title + " *res."
                                }
                            }

                            adapter!!.addFrag(Questionsfragement(sectionsFinished, questionnaireResultListFillableElement, questionnairegetObject, it, this@SectionFragment), it.name + title)
                            if (firstResumableSectionOrder == -1
                                    && (it.resumable
                                            || (!it.resumable && questionnaireResultListFillableElement.sections.size < it.order))) {
                                firstResumableSectionOrder = it.order - 1
                            }
                        }
                        viewPager!!.adapter = adapter

                        if (firstResumableSectionOrder != -1) {
                            viewPager.currentItem = firstResumableSectionOrder
                        } else {
                            Log.d(TAGRESREQ, " Nincs több folytatható ezért bekell zárni!")
                        }
                    }

                    if (nextFinish) {
                        finishFunction(view!!)
                    }
                }

                override fun onError(error: String?, reason: String?, details: String?) {
                    Log.d(TAG, "-->error $error ")
                    Log.d(TAG, "-->error $reason ")
                    Log.d(TAG, "-->error $details ")
                    if (!justModelRefresh) {
                        if (reason == "Empty ID!") {
                            /**
                             * nem volt még create biztosan*/
                            createFirst()
                        }
                    }
                }
            })
        }
    }

    private fun finishFunction(itView: View) {
        Log.d(TAGRESREQ, "finish start")
        meteor = if (EuronetMeteorSingleton.hasInstance()) {
            EuronetMeteorSingleton.getInstance()
        } else {
            EuronetMeteorSingleton.createInstance(view!!.context, "ws://${AccountGeneral.mHost}:3000/websocket", InMemoryDatabase())
        }

        Log.d(TAGRESREQ, "meteor ellenőrzés")
        if (meteor.isConnected) {
            Log.d(TAGRESREQ, "meteor connect")
            var error = true
            if (questionnairegetObject.template.sections.size != questionnaireResultListFillableElement.sections.size) {
                /*Ebben az esetben kevesebb elmentett szekciónk van mint amennyinek kéne lennie**/
                questionnairegetObject.template.sections.forEach { partOneSection ->
                    //  Log.d(TAGRESREQ, "partOneSection.order ${partOneSection}")
                    questionnaireResultListFillableElement.sections.forEach lit@{ partTwoSection ->
                        /*  Log.d(TAGRESREQ, "partOneSection.order ${partOneSection.order}")
                          Log.d(TAGRESREQ, "partTwoSection.order ${partTwoSection.order}")*/
                        if (partOneSection.order == partTwoSection.order) {
                            /*Ez a szekció el lett már mentve készen van**/
                            Log.d(TAGRESREQ, "szekció kész: ${partOneSection.order}")
                            error = false
                            return@lit
                        }
                    }

                    if (error) {
                        if (partOneSection.order == questionnairegetObject.template.sections[questionnairegetObject.template.sections.lastIndex].order) {
                            /**ez elméletben azt jelenti hogy az utolsó szekció az ami nincs még mentve ilyenkor előbb egy update-et küldünk rá majd a finish-t hogy ne kelljen elnavigálni az utolsó oldalról(ezzel eszközölni egy update eseményt) ahhoz hogy a finish működjön.*/
                            //  Toast.makeText(view!!.context , "----------------------------------------utolsó elem ide kel az update", Toast.LENGTH_LONG).show()
                            Log.d(TAGRESREQ, "utolsó szekció nincs mentve ezért itt kéne egy update")
                            /*pl 3 szekció van akkor itt a partOneSection.order értke a 3-as de a viewPageben a 2-es mert ott 0-val indul a szekció szám*/
                            update(partOneSection.order - 1, view!!, true)
                        } else {
                            /*Találtunk egy szekciót ami nincs kitöltve**/
                            Log.d(TAGRESREQ, "Találtunk egy szekciót ami nincs kitöltve ${partOneSection.order}")
                            Snackbar.make(itView, "${partOneSection.order} No. questionnaire is not ready, or not saved!", Snackbar.LENGTH_LONG).show()
                        }
                        return
                    } else {
                        error = true
                    }
                }
            } else {
                error = false
            }
            Log.d(TAGRESREQ, "error vizsgálat")
            if (!error) {
                Log.d(TAGRESREQ, "nincs error")
                /*Ha nincs hiba minden kitöltött, mehet a finish*/
                var questionnaireResultForServer: QuestionnaireResultForServer? = null

                val sectionArray = ArrayList<QuestionnaireResultSection>()

                if (questionnaireResultForServer == null) {

                    questionnaireResultForServer = QuestionnaireResultForServer(
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
                            System.currentTimeMillis(),
                            sectionArray,
                            questionnaireResultListFillableElement.suspends
                    )
                } else {
                    questionnaireResultForServer.seq = questionnaireResultListFillableElement.seq
                    questionnaireResultForServer.creator = questionnaireResultListFillableElement.creator
                    questionnaireResultForServer.created_at = questionnaireResultListFillableElement.created_at
                    questionnaireResultForServer.modifier = questionnaireResultListFillableElement.modifier
                    questionnaireResultForServer.modified_at = questionnaireResultListFillableElement.modified_at
                    questionnaireResultForServer.active = questionnaireResultListFillableElement.active
                    questionnaireResultForServer.id = questionnaireResultListFillableElement.id
                    questionnaireResultForServer.questionnaireId = questionnaireResultListFillableElement.questionnaireId
                    questionnaireResultForServer.userId = questionnaireResultListFillableElement.userId
                    questionnaireResultForServer.scheduleOrder = questionnaireResultListFillableElement.scheduleOrder
                    questionnaireResultForServer.scheduledAt = questionnaireResultListFillableElement.scheduledAt?.date
                    questionnaireResultForServer.scheduledUntil = questionnaireResultListFillableElement.scheduledUntil?.date
                    questionnaireResultForServer.state = questionnaireResultListFillableElement.state
                    questionnaireResultForServer.startedAt = questionnaireResultListFillableElement.startedAt?.date
                    questionnaireResultForServer.finishedAt = questionnaireResultListFillableElement.finishedAt?.date
                    questionnaireResultForServer.suspends = questionnaireResultListFillableElement.suspends
                }

                questionnaireResultListFillableElement.sections.forEach { qResultGetSectionItem ->
                    /**A szekció order sorszáma nem egyenlő a szekciós tömb indexével ezért kell korrigálni 1-el */
                    //    Log.d(TAGRESREQ, "Szekció: ${qResultGetSectionItem.order}")
                    //    Log.d(TAGRESREQ, "seq: ${questionnaireResultListFillableElement.seq}")

                    val uploadAnswers = ArrayList<QuestionnaireResultAnswer>()
                    /**Betöltjük ami eddig a modelben van*/
                    questionnairegetObject.template.sections.forEach {
                        if (it.order == qResultGetSectionItem.order) {
                            it.questions.forEach { qGetTempSecQuestionItem ->
                                if (qGetTempSecQuestionItem.order - 1 < qResultGetSectionItem.answers.size) {
                                    //              Log.d(TAGRESREQ, "kérdés sorszáma: ${qGetTempSecQuestionItem.order} típusa")
                                    when (qGetTempSecQuestionItem.type) {
                                        QuestionnaireQuestionType.Text.type -> {
                                            //                    Log.d(TAGRESREQ, "Text value: ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value} ")
                                            /**a kérdés sorszámával megegyező válasz indexe kell*/
                                            val answer = QuestionnaireResultAnswerStringValue(
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value.toString()
                                            )
                                            uploadAnswers.add(answer)
                                        }
                                        QuestionnaireQuestionType.SingleChoice.type -> {
                                            //                  Log.d(TAGRESREQ, "SingleChoice ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value}")
                                            val value = qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value
                                            /**a kérdés sorszámával megegyező válasz indexe kell*/
                                            val answer = QuestionnaireResultAnswerIntValue(
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                                    (value as Double).toInt()
                                            )
                                            uploadAnswers.add(answer)
                                        }
                                        QuestionnaireQuestionType.MultipleChoice.type -> {
                                            //                Log.d(TAGRESREQ, "MultipleChoice ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value}")

                                            val value = qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value

                                            @Suppress("UNCHECKED_CAST")
                                            val arrayDouble = value as ArrayList<Double>

                                            val intArrayValue = arrayDouble.map { it.toInt() }

                                            /**a kérdés sorszámával megegyező válasz indexe kell*/
                                            val answer = QuestionnaireResultAnswerArrayValue(
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                                    ArrayList<Int>(intArrayValue)
                                            )
                                            uploadAnswers.add(answer)
                                        }
                                        QuestionnaireQuestionType.LikertScale.type -> {
                                            //              Log.d(TAGRESREQ, "LikertScale ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value}")
                                            /**a kérdés sorszámával megegyező válasz indexe kell*/
                                            val value = qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value
                                            val answer = QuestionnaireResultAnswerIntValue(
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                                    (value as Double).toInt()
                                            )
                                            uploadAnswers.add(answer)
                                        }
                                        QuestionnaireQuestionType.LikertItem.type -> {
                                            //            Log.d(TAGRESREQ, "LikertItem ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value}")
                                            val value = qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value
                                            /**a kérdés sorszámával megegyező válasz indexe kell*/
                                            val answer = QuestionnaireResultAnswerIntValue(
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                                    qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                                    (value as Double).toInt()
                                            )
                                            uploadAnswers.add(answer)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val section = QuestionnaireResultSection(
                            qResultGetSectionItem.order,
                            qResultGetSectionItem.type,
                            uploadAnswers,
                            qResultGetSectionItem.startedAt?.date,
                            qResultGetSectionItem.finishedAt?.date
                    )

                    sectionArray.add(section)
                }

                questionnaireResultForServer.sections = sectionArray

                /**
                 * A kérdőív kitöltésének megszakításának és folytatásának időpontjai.
                 *
                 * Alapeset: Egy felfüggesztés-, majd egy folytatás-jelzés érkezik.
                 *           Ekkor a felfüggesztés jelre létrejön egy új elem a tömbben
                 *           kitöltve a 'suspendedAt' értékét, majd a folytatás jelzésre
                 *           az utolsó tömb-elem 'resumedAt' értéke kerül kitöltésre.
                 * Két felfüggesztés egymás után:
                 *           !!! Kivétel - látható legyen, hogy már fel van függesztve
                 * Két folytatás esemény jön egymás után:
                 *           !!! Kivétel - látható legyen, hogy már újra van indítva
                 */

                if (questionnaireResultForServer.scheduledUntil != null && (questionnaireResultForServer.scheduledUntil!! < questionnaireResultForServer.finishedAt!!)) {

                    val params = Array<Any>(2) {}
                    params[0] = questionnaireResultForServer.id
                    params[1] = questionnaireResultForServer.seq

                    meteor.call("questionnaire-result.cancel", params, object : ResultListener {
                        override fun onSuccess(result: String?) {
                            Log.d(TAGRESREQ, "questionnaire-result.cancel $result")
                        }

                        override fun onError(error: String?, reason: String?, details: String?) {
                            Log.d(TAGRESREQ, "-->error $error ")
                            Log.d(TAGRESREQ, "-->error $reason ")
                            Log.d(TAGRESREQ, "-->error $details ")
                        }
                    })
                } else {
                    val params = Array<Any>(3) {}
                    params[0] = questionnaireResultForServer.id
                    params[1] = questionnaireResultForServer.seq
                    params[2] = questionnaireResultForServer

                    val jsonString = questionnaireResultForServer.toString()

                    Log.d(TAG, "questionnaireResult $jsonString,  ${params[0]}  ${params[1]}")

                    val gson = Gson()
                    val REVIEW_TYPE_ID = object : TypeToken<QuestionnaireResultForServer>() {}.type
                    val questionnaireObejtTesztMiatt = gson.toJson(questionnaireResultForServer, REVIEW_TYPE_ID)
                    Log.d(TAGRESREQ, "questionnaireObejtTesztMiatt - Finish  ${questionnaireObejtTesztMiatt} ")

                    meteor.call("questionnaire-result.finish", params, object : ResultListener {
                        override fun onSuccess(result: String?) {
                            Log.d(TAGRESREQ, " questionnaire-result.Finish -->result $result ")

                            /**sikeres finish esetén zárjuk az aktuális kérdőívet és frissítjük a listát*/
                            val oldCounter = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getInt(BluetoothLeService.KEY_NEW_QUESTIONNAIRECOUNTER, -1)
                            val KEY_NEWQUESTIONNAIRECOUNTER = androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
                            KEY_NEWQUESTIONNAIRECOUNTER.edit()
                                    .putInt(BluetoothLeService.KEY_NEW_QUESTIONNAIRECOUNTER, oldCounter - 1)
                                    .apply()
                            Log.d(TAGNOTIFY, "uj érték a finish miatt: ${oldCounter - 1})")
                            (activity as DeviceMainActivity).onBackPressed()
                        }

                        override fun onError(error: String?, reason: String?, details: String?) {
                            Log.d(TAGRESREQ, "questionnaire-result.Finish -->error $error ")
                            Log.d(TAGRESREQ, "questionnaire-result.Finish -->error $reason ")
                            Log.d(TAGRESREQ, "questionnaire-result.Finish -->error $details ")
                        }
                    })
                }
            } else {
                Log.d(TAGRESREQ, "error van nem csinálunk semmit")
            }

        } else {
            Log.d(TAGRESREQ, "meteor nem okés")
            Toast.makeText(view!!.context, "Server is not available please try again later!", Toast.LENGTH_LONG).show()
        }
    }

    private fun createFirst() {
        if (questionnaireResultListFillableElement.id == "null" || questionnaireResultListFillableElement.id.isNullOrEmpty() ) {
            val params = Array<Any>(2) {}
            params[0] = questionnaireResultListFillableElement.questionnaireId
            params[1] = questionnaireResultListFillableElement.scheduleOrder

            Log.e(TAG, "--> questionnaireResultListFillableElement.questionnaireId ${questionnaireResultListFillableElement.questionnaireId}")
            Log.e(TAG, "--> questionnaireResultListFillableElement.scheduleOrder ${questionnaireResultListFillableElement.scheduleOrder}")

            meteor.call("questionnaire-result.create", params, object : ResultListener {
                override fun onSuccess(result: String?) {
                    val gson = Gson()
                    val REVIEW_TYPE = object : TypeToken<QuestionnaireResultListFillable>() {}.type

                    questionnaireResultListFillableElement = gson.fromJson(result, REVIEW_TYPE)

                    val questionnaireObejtTesztMiatt = gson.toJson(questionnaireResultListFillableElement, REVIEW_TYPE)
                    Log.e(TAG, "questionnaireObejtTesztMiatt - create response  ${questionnaireObejtTesztMiatt} ")


                    questionnairegetObject.template.sections.forEach {
                        var title = ""
                        if (it.required) {
                            title = title + " *req."
                        }
                        if (it.resumable) {
                            if (title != "") {
                                title = title + "/res."
                            } else {
                                title = title + " *res."
                            }
                        }

                        adapter.addFrag(Questionsfragement(sectionsFinished, questionnaireResultListFillableElement, questionnairegetObject, it, this@SectionFragment), it.name + title)
                    }

                    viewPager.adapter = adapter
                }

                override fun onError(error: String?, reason: String?, details: String?) {
                    Log.e(TAG, "-->error $error ")
                    Log.e(TAG, "-->error $reason ")
                    Log.e(TAG, "-->error $details ")
                }
            })
        }
    }

    @Synchronized
    private fun update(position: Int, view: View, nextFinish: Boolean) {
        val sectionArray = ArrayList<QuestionnaireResultSection>()
        Log.d(TAGRESREQ, "seq: ${questionnaireResultListFillableElement.seq}")
        questionnaireResultListFillableElement.sections.forEach { qResultGetSectionItem ->
            /**A szekció order sorszáma nem egyenlő a szekciós tömb indexével ezért kell korrigálni 1-el */
            Log.d(TAGRESREQ, "Szekció: ${qResultGetSectionItem.order}")

            val uploadAnswers = ArrayList<QuestionnaireResultAnswer>()
            /**Betöltjük ami eddig a modelben van*/
            questionnairegetObject.template.sections.forEach {
                if (it.order == qResultGetSectionItem.order) {
                    it.questions.forEach { qGetTempSecQuestionItem ->
                        if (qGetTempSecQuestionItem.order - 1 < qResultGetSectionItem.answers.size) {
                            Log.d(TAGRESREQ, "kérdés sorszáma: ${qGetTempSecQuestionItem.order} típusa")
                            Log.d(TAGRESREQ, "kérdés answers: ${qGetTempSecQuestionItem.answers} típusa")
                            when (qGetTempSecQuestionItem.type) {
                                QuestionnaireQuestionType.Text.type -> {
                                    Log.d(TAGRESREQ, "Text value: ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value} ")
                                    /**a kérdés sorszámával megegyező válasz indexe kell*/
                                    val answer = QuestionnaireResultAnswerStringValue(
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value.toString()
                                    )
                                    uploadAnswers.add(answer)
                                }
                                QuestionnaireQuestionType.SingleChoice.type -> {
                                    Log.d(TAGRESREQ, "SingleChoice ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value}")
                                    val value = qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value
                                    /**a kérdés sorszámával megegyező válasz indexe kell*/
                                    val answer = QuestionnaireResultAnswerIntValue(
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                            (value as Double).toInt()
                                    )
                                    uploadAnswers.add(answer)
                                }
                                QuestionnaireQuestionType.MultipleChoice.type -> {
                                    //                Log.d(TAGRESREQ, "MultipleChoice ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value}")

                                    val value = qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value

                                    @Suppress("UNCHECKED_CAST")
                                    val arrayDouble = value as ArrayList<Double>

                                    val intArrayValue = arrayDouble.map { it.toInt() }

                                    /**a kérdés sorszámával megegyező válasz indexe kell*/
                                    val answer = QuestionnaireResultAnswerArrayValue(
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                            ArrayList<Int>(intArrayValue)
                                    )
                                    uploadAnswers.add(answer)
                                }
                                QuestionnaireQuestionType.LikertScale.type -> {
                                    //              Log.d(TAGRESREQ, "LikertScale ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value}")
                                    /**a kérdés sorszámával megegyező válasz indexe kell*/
                                    val value = qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value
                                    val answer = QuestionnaireResultAnswerIntValue(
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                            (value as Double).toInt()
                                    )
                                    uploadAnswers.add(answer)
                                }
                                QuestionnaireQuestionType.LikertItem.type -> {
                                    //            Log.d(TAGRESREQ, "LikertItem ${qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value}")
                                    val value = qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].value
                                    /**a kérdés sorszámával megegyező válasz indexe kell*/
                                    val answer = QuestionnaireResultAnswerIntValue(
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].questionOrder,
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].startedAt?.date,
                                            qResultGetSectionItem.answers[qGetTempSecQuestionItem.order - 1].finishedAt?.date,
                                            (value as Double).toInt()
                                    )
                                    uploadAnswers.add(answer)
                                }
                            }
                        }
                    }
                }
            }

            val section = QuestionnaireResultSection(
                    qResultGetSectionItem.order,
                    qResultGetSectionItem.type,
                    uploadAnswers,
                    qResultGetSectionItem.startedAt?.date,
                    qResultGetSectionItem.finishedAt?.date
            )
            sectionArray.add(section)
        }

        id_progressWheel.visibility = View.VISIBLE
        val childFragment = adapter.getItem(currentSectionPage) as Questionsfragement
        val mView = childFragment.view!!

        val answerHelper = childFragment.getAnswerHelper()
        val answerLikertHelper = childFragment.getAnswerLIKERTHelper()
        val actualSectionBlock = childFragment.actualSectionBlock

        /**
         * A kérdőív kitöltésének megszakításának és folytatásának időpontjai.
         *
         * Alapeset: Egy felfüggesztés-, majd egy folytatás-jelzés érkezik.
         *           Ekkor a felfüggesztés jelre létrejön egy új elem a tömbben
         *           kitöltve a 'suspendedAt' értékét, majd a folytatás jelzésre
         *           az utolsó tömb-elem 'resumedAt' értéke kerül kitöltésre.
         * Két felfüggesztés egymás után:
         *           !!! Kivétel - látható legyen, hogy már fel van függesztve
         * Két folytatás esemény jön egymás után:
         *           !!! Kivétel - látható legyen, hogy már újra van indítva
         */
        val suspendArray = ArrayList<QuestionnaireResultSuspend>()
        val subSus = QuestionnaireResultSuspend(null, null)
        suspendArray.add(subSus)

        val answers = ArrayList<QuestionnaireResultAnswer>()

        answerHelper.forEach { answerListHelperItem ->

            if (!checkError) {
                val qNumber = answerListHelperItem.key
                var valueType: Int = -1
                var value: Any? = null

                /*az it értéke a mutable list melynek az első eleme az adott kérdéstípusa */
                when (answerListHelperItem.value[0].first) {
                    QuestionnaireQuestionType.Text.type -> {

                        Log.e(TAGRESREQ, "Adatellenőrzések: Text")
                        /*az it értéke a mutable list melynek a második eleme az adott kérdéshez tartozó viewID  */
                        val viewT = mView.findViewById<EditText>(answerListHelperItem.value[0].second)
                        value = viewT.text
                        valueType = AnswerType.STRING.type

                        //Log.e(TAGRESREQ, "Adatellenőrzések: Text value :$value")

                        if (actualSectionBlock.required && (value.isNullOrEmpty() || value == "" || value == " " || value == "  " || value == "  ")) {
                            checkError = true
                            Log.e(TAGRESREQ, "hiba van")
                        }
                    }
                    QuestionnaireQuestionType.SingleChoice.type -> {
                        Log.e(TAGRESREQ, "Adatellenőrzések: SingleChoice radiogroup :${answerListHelperItem.value[0].second}")

                        /*az it értéke a mutable list melynek a második eleme az adott kérdéshez tartozó viewID  */
                        val viewRadio = mView.findViewById<RadioGroup>(answerListHelperItem.value[0].second)
                        var chooseNumber = false
                        var i = 1
                        viewRadio.forEach {
                            if (!chooseNumber) {
                                try {
                                    value = 0 //nem választott semmit
                                    valueType = AnswerType.INT.type
                                    val radiobutton = mView.findViewById<RadioButton>(it.id)
                                    if (radiobutton != null) {
                                        if (radiobutton.isChecked) {
                                            value = i
                                            Log.d(TAGRESREQ, "kiválasztotta értéke:  $value ")
                                            chooseNumber = true
                                        }
                                    } else {
                                        Log.d(TAGRESREQ, "nem választott semmit ezért értéke:  $value ")
                                    }
                                    i++
                                } catch (e: Exception) {
                                    Log.d(TAGRESREQ, "Semmi gond csak egy imageview a radioGroupban , nem érdekes.")
                                }
                            }
                        }

                        if (actualSectionBlock.required && (value == null || value == 0)) {
                            checkError = true
                        }
                    }
                    QuestionnaireQuestionType.MultipleChoice.type -> {
                        //     Log.e(TAGRESREQ, "Adatellenőrzések: MultipleChoice")
                        val multiValue = ArrayList<Int>()

                        for (i in 0 until answerListHelperItem.value.size) {
                            //   Log.e(TAGRESREQ, "answerListHelperItem.value.size ${answerListHelperItem.value.size}")
                            /*csak a secoundal dolgozunk az a view id!*/
                            val viewCh = mView.findViewById<CheckBox>(answerListHelperItem.value[i].second)
                            //     Log.e(TAGRESREQ, "answerListHelperItem.value[i].second ${answerListHelperItem.value[i].second}")
                            //         Log.e(TAGRESREQ, "viewCh.isChecked ${viewCh.isChecked}")
                            //       Log.e(TAGRESREQ, "viewCh text ${viewCh.text}")
                            if (viewCh.isChecked) {
                                multiValue.add(actualSectionBlock.questions[qNumber - 1].answers[i]!!.order)
                            }
                        }
                        value = multiValue
                        valueType = AnswerType.ARRAY.type

                        if (actualSectionBlock.required && (value == null || multiValue.size == 0)) {
                            checkError = true
                        }
                    }
                    QuestionnaireQuestionType.LikertScale.type -> {
                        Log.e(TAGRESREQ, "Adatellenőrzések: LikertScale")
                        /*az it értéke a mutable list melynek a második eleme az adott kérdéshez tartozó viewID  */
                        value = if (answerLikertHelper[answerListHelperItem.value[0].second] == null) 0 else answerLikertHelper[answerListHelperItem.value[0].second]
                        valueType = AnswerType.INT.type
                        Log.e(TAGRESREQ, "value: LikertScale savere :$value")

                        if (actualSectionBlock.required && (value == null || value == 0)) {
                            checkError = true
                        }
                    }
                    QuestionnaireQuestionType.LikertItem.type -> {
                        //         Log.e(TAGRESREQ, "Adatellenőrzések: LikertItem")
                        /*az it értéke a mutable list melynek a második eleme az adott kérdéshez tartozó viewID  */
                        value = if (answerLikertHelper[answerListHelperItem.value[0].second] == null) 0 else answerLikertHelper[answerListHelperItem.value[0].second]
                        valueType = AnswerType.INT.type
                        //       Log.e(TAGRESREQ, "value: LikertItem savere :$value")

                        if (actualSectionBlock.required && (value == null || value == 0)) {
                            checkError = true
                        }
                    }
                }

                if (value != null) {

                    when (valueType) {
                        AnswerType.INT.type -> {
                            val qRAnswer = QuestionnaireResultAnswerIntValue(
                                    /*a kérdések számozása 1 től indul 1-el növekvő monoton , az index pedig 0-tól indul*/
                                    actualSectionBlock.questions[answerListHelperItem.key - 1].order,
                                    questionnaireResultListFillableElement.startedAt?.date
                                            ?: childFragment.startDate,
                                    System.currentTimeMillis(),
                                    value!! as Int
                            )
                            answers.add(qRAnswer)
                        }
                        AnswerType.STRING.type -> {
                            val qRAnswer = QuestionnaireResultAnswerStringValue(
                                    /*a kérdések számozása 1 től indul 1-el növekvő monoton , az index pedig 0-tól indul*/
                                    actualSectionBlock.questions[answerListHelperItem.key - 1].order,
                                    questionnaireResultListFillableElement.startedAt?.date
                                            ?: childFragment.startDate,
                                    System.currentTimeMillis(),// QuestionnaireResultAnswerFinishedAt(null),
                                    value!!.toString()
                            )
                            answers.add(qRAnswer)
                        }
                        AnswerType.ARRAY.type -> {
                            @Suppress("UNCHECKED_CAST") val qRAnswer = QuestionnaireResultAnswerArrayValue(
                                    /*a kérdések számozása 1 től indul 1-el növekvő monoton , az index pedig 0-tól indul*/
                                    actualSectionBlock.questions[answerListHelperItem.key - 1].order,
                                    questionnaireResultListFillableElement.startedAt?.date
                                            ?: childFragment.startDate,
                                    System.currentTimeMillis(),// QuestionnaireResultAnswerFinishedAt(null),

                                    value!! as ArrayList<Int>
                            )
                            answers.add(qRAnswer)
                        }
                    }
                }
            }
        }

        if (checkError) {
            Snackbar.make(view, "All fields are required!", Snackbar.LENGTH_LONG).show()
            viewPager.currentItem = currentSectionPage
            checkError = false
        } else {

            //  Log.d(TAGRESREQ, " Kötelezőség pipa megnézzük a folytathatóságot")
            //*Pozició adat 0 tól indul de a sectionsFinished a szekciók orderjét tárolja ami 1-től*/
            /**Megnézzük az a szekció már kilett-e töltve**/
            if ((questionnaireResultListFillableElement.sections.size > position) && questionnaireResultListFillableElement.sections[position].finishedAt != null) {
                /**ebben az esetben egy update már volt rá tehát nem először nyitjuk meg így folytathatónak kell lennie.*/
                var hasItem = false
                for (i in questionnaireResultListFillableElement.sections) {
                    if (i.order == questionnairegetObject.template.sections[position].order) {
                        hasItem = true
                    }
                }

                if (questionnairegetObject.template.sections[position].resumable || !hasItem) {
                    /**Folytatható ezért mgnyithatjuk.*/
                    Log.d(TAGRESREQ, "nincs gond mehet tovább ide: $position ")
                    currentSectionPage = position
                    updateSection(answers, actualSectionBlock, childFragment.startDate, sectionArray, suspendArray, nextFinish)
                } else {
                    //Snackbar.make(view, "Sorry not resumable this the section, this is the next section to continue!", Snackbar.LENGTH_LONG).show()
                    /**ha van olyan szekció amit folytathat akkor azt kell betölteni ha nincs akkor marad a jelenlegin.*/

                    if (position < currentSectionPage) {
                        /**lefele haladna*/
                        for (i in (currentSectionPage - 1) downTo 0 step 1) {
                            if (questionnairegetObject.template.sections[i].resumable || (!questionnairegetObject.template.sections[i].resumable && (questionnaireResultListFillableElement.sections.size <= i || questionnaireResultListFillableElement.sections[i].finishedAt == null))) {
                                currentSectionPage = questionnairegetObject.template.sections[i].order - 1
                                viewPager.currentItem = currentSectionPage
                                Log.d(TAGRESREQ, "következő folytatható: $position ")
                                updateSection(answers, actualSectionBlock, childFragment.startDate, sectionArray, suspendArray, nextFinish)
                                return
                            }
                        }

                        //ha nem volt return jöhet ide ha volt return ide nem jutunk el.
                        /*   /**Nem találtunk lefele több folytathatót ezért megnézzük felfele is**/
                           for (i in (currentSectionPage + 1) until questionnairegetObject.template.sections.size step 1) {
                               if (questionnairegetObject.template.sections[i].resumable|| (!questionnairegetObject.template.sections[i].resumable && (questionnaireResultListFillableElement.sections.size <= i || questionnaireResultListFillableElement.sections[i].finishedAt == null))) {
                                   currentSectionPage = questionnairegetObject.template.sections[i].order - 1
                                   viewPager.currentItem = currentSectionPage
                                   Log.d(TAGRESREQ, "következő folytatható: $position ")
                                   updateSection(answers, actualSectionBlock, childFragment.startDate, sectionArray, suspendArray,nextFinish)
                                   return
                               }
                           }*/
                        //  Log.d(TAGRESREQ, "Nem található több folytatható section ezért ezt jelezzük és mentjük az állapotát")
                        /**Nem található több folytatható section ezért ezt jelezzük és mentjük az állapotát**/
                        Snackbar.make(view, "Sorry you have not more resumable section (down)!", Snackbar.LENGTH_LONG).show()
                        viewPager.currentItem = currentSectionPage
                        updateSection(answers, actualSectionBlock, childFragment.startDate, sectionArray, suspendArray, nextFinish)

                    } else {
                        /**felfele haladna*/
                        for (i in (currentSectionPage + 1) until questionnairegetObject.template.sections.size step 1) {
                            if (questionnairegetObject.template.sections[i].resumable || (!questionnairegetObject.template.sections[i].resumable && (questionnaireResultListFillableElement.sections.size <= i || questionnaireResultListFillableElement.sections[i].finishedAt == null))) {
                                currentSectionPage = questionnairegetObject.template.sections[i].order - 1
                                viewPager.currentItem = currentSectionPage
                                updateSection(answers, actualSectionBlock, childFragment.startDate, sectionArray, suspendArray, nextFinish)
                                return
                            }
                        }
                        /**Nem találtunk felfele több folytathatót ezért megnézzük lefele is**/
                        /*  for (i in (currentSectionPage - 1) downTo 0 step 1) {
                              if (questionnairegetObject.template.sections[i].resumable|| (!questionnairegetObject.template.sections[i].resumable && (questionnaireResultListFillableElement.sections.size <= i || questionnaireResultListFillableElement.sections[i].finishedAt == null))) {
                                  currentSectionPage = questionnairegetObject.template.sections[i].order - 1
                                  viewPager.currentItem = currentSectionPage
                                  Log.d(TAGRESREQ, "következő folytatható: $position ")
                                  updateSection(answers, actualSectionBlock, childFragment.startDate, sectionArray, suspendArray,nextFinish)
                                  return
                              }
                          }*/
                        Log.d(TAGRESREQ, "Nem található több folytatható section ezért ezt jelezzük és mentjük az állapotát")
                        /**Nem található több folytatható section ezért ezt jelezzük és mentjük az állapotát**/
                        Snackbar.make(view, "Sorry you have not more resumable section!(up)", Snackbar.LENGTH_LONG).show()
                        viewPager.currentItem = currentSectionPage
                        updateSection(answers, actualSectionBlock, childFragment.startDate, sectionArray, suspendArray, nextFinish)
                    }
                }
            } else {
                Log.d(TAGRESREQ, "nincs gond mehet tovább ide: $position ")
                currentSectionPage = position
                updateSection(answers, actualSectionBlock, childFragment.startDate, sectionArray, suspendArray, nextFinish)
            }
        }
    }
}