package hu.aut.android.dm01_v11.ui.fragments.questionnaire

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hu.aut.android.dm01_v11.R
import hu.aut.android.dm01_v11.ui.fragments.questionnaire.SectionFragment.Companion.TAGNOTIFY
import hu.euronetrt.okoskp.euronethealth.GlobalRes.applicationVersionModeIsLight
import hu.euronetrt.okoskp.euronethealth.GlobalRes.writeFileQuestionnaireActive
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral
import hu.euronetrt.okoskp.euronethealth.questionnaire.QuestionnaireAdapter
import hu.euronetrt.okoskp.euronethealth.questionnaire.QuestionnaireDataClass
import hu.euronetrt.okoskp.euronethealth.questionnaire.QuestionnaireInterface
import hu.euronetrt.okoskp.euronethealth.questionnaire.QuestionnaireType
import hu.euronetrt.okoskp.euronethealth.questionnaire.answersResultObjects.QuestionnaireResultListFillable
import hu.euronetrt.okoskp.euronethealth.questionnaire.questionnaireObjects.QuestionnaireGetModel
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.meteor.EuronetMeteorSingleton
import im.delight.android.ddp.MeteorCallback
import im.delight.android.ddp.ResultListener
import im.delight.android.ddp.db.memory.InMemoryDatabase
import kotlinx.android.synthetic.main.fragment_device_question_page.*
import org.json.JSONArray
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat

class FragmentDeviceQuestionnairePage : Fragment(), QuestionnaireInterface, MeteorCallback {

    companion object {
        val TAG = "FRAGMENT_DEV_QUESTION_PAGE"
        private val WAIT_INTERVAL: Long = 5000
        private val TAGMETEORQUEST = "TAGMETEORQUEST"
    }

    private lateinit var sectionFragment: SectionFragment
    private lateinit var meteor: EuronetMeteorSingleton

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuestionnaireAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var tryReConnectCounter = 0
    private var serverOK = false
    private var i = 0

    override fun questionnaireInterface(questionnaireResultListFillableElement: QuestionnaireResultListFillable, questionnairegetObject: QuestionnaireGetModel) {
        sectionFragment = SectionFragment(questionnaireResultListFillableElement, questionnairegetObject)
        val ft = fragmentManager!!.beginTransaction()
        ft.add(R.id.questionnaireFrameLayout, sectionFragment, SectionFragment.TAG)
        ft.addToBackStack(SectionFragment.TAG)
        ft.commit()
        swipeRefresQUE.visibility = View.GONE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_device_question_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        id_noQuestionnaire.visibility = View.GONE

        if (!applicationVersionModeIsLight) {
            viewManager = LinearLayoutManager(view.context)
            recyclerView = id_questionnaire_recycleView.apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                // use a linear layout manager
                layoutManager = viewManager

                // specify an viewAdapter (see also next example)
            }
        }

        if (!applicationVersionModeIsLight) {

            Log.d(TAG, "start")
            if (swipeRefresQUE != null) {
                swipeRefresQUE.isRefreshing = true
            }

            tryConnect(view.context)

            swipeRefresQUE.setOnRefreshListener {
                id_noQuestionnaire.visibility = View.GONE
                tryConnect(view.context)
            }
        } else {
            swipeRefresQUE.isRefreshing = false
            swipeRefresQUE.isEnabled = false
        }

        fr_dev_questionnaire.setOnClickListener {
            val imm: InputMethodManager = activity!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun tryConnect(context: Context) {
        if (networkCheck(context)) {
            if (EuronetMeteorSingleton.hasInstance()) {
                Log.d(TAGMETEORQUEST, "meteor connect questionnairePage")
                meteor = EuronetMeteorSingleton.getInstance()
            } else {
                meteor = EuronetMeteorSingleton.createInstance(context, "ws://${AccountGeneral.mHost}:3000/websocket", InMemoryDatabase())
                meteor.addCallback(this)
            }

            if (!meteor.isConnected) {
                Log.d(TAGMETEORQUEST, "connect hívás meteorra questionnairePage")
                meteor.connect()
            }

            Handler().postDelayed({
                if (!serverOK) {
                    if (tryReConnectCounter == 2) {
                        tryReConnectCounter = 0
                        if (swipeRefresQUE != null) {
                            swipeRefresQUE.isRefreshing = false
                            Toast.makeText(context, "Server connection failed!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        tryReConnectCounter++
                        if (view != null) {
                            tryConnect(context)
                        }
                    }
                } else {
                    //alaphelyzetbe állítjuk
                    serverOK = false
                }
            }, WAIT_INTERVAL)
            getQuestionsList()
        }
    }

    /**
     * networkCheck
     *
     * @param context
     * @return Boolean
     */
    private fun networkCheck(context: Context): Boolean {
        val connManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val allNetworks = connManager.allNetworks
        var result = false
        if (!allNetworks.isNullOrEmpty()) {
            allNetworks.forEach {
                if (it != null && connManager.getNetworkInfo(it).isConnected) {
                    result = true
                }
            }
            return result
        } else {
            Toast.makeText(context, "Please check your the internet connetion!", Toast.LENGTH_LONG).show()
            return result
        }
    }

    private fun getQuestionsList() {
        Log.d(TAG, "getQuestionsList call")
        if (!meteor.isConnected) {
            Log.d(TAG, "$TAG --> !meteor.isConnected run (false connect meteor) ")
            Toast.makeText(view!!.context, "Loading...", Toast.LENGTH_LONG).show()
            meteor.connect()
            swipeRefresQUE.isRefreshing = true
        } else {
            meteor.call("questionnaire-result.listFillable", object : ResultListener {
                override fun onSuccess(result: String?) {
                    if (id_noQuestionnaire != null) {
                        id_noQuestionnaire.visibility = View.GONE
                    }

                    if (writeFileQuestionnaireActive) {
                        writeFile(result, QuestionnaireType.QUESTIONNAIRE_RESULT_LISTFILLABLE, 0)
                    }
                    Log.d(TAG, "result questionnaire-result.listFillable: $result")
                    serverOK = true

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

    private fun getQuestions(resultQuestionnaireListFillableArray: JSONArray) {
        Log.d(TAG, "--> getQuestions call ")
        val resultData = ArrayList<QuestionnaireDataClass>()
        if (resultQuestionnaireListFillableArray.length() == 0) {
            if (swipeRefresQUE != null) {
                swipeRefresQUE.isRefreshing = false
                id_noQuestionnaire.visibility = View.VISIBLE
            }
            if (::adapter.isInitialized) {
                adapter.removeAll()
            }
        } else {

            if (::adapter.isInitialized) {
                adapter.removeAll()
            }

            getQuestionSecound(resultQuestionnaireListFillableArray, resultData)
        }
    }

    private fun getQuestionSecound(resultQuestionnaireListFillableArray: JSONArray, resultData: ArrayList<QuestionnaireDataClass>) {
        //    for (i in 0 until resultQuestionnaireListFillableArray.length()) {

        if (swipeRefresQUE != null) {
            swipeRefresQUE.isRefreshing = true
        }
        val gson = Gson()
        val RT = object : TypeToken<QuestionnaireResultListFillable>() {}.type
        val questionnaireResultListFillable: QuestionnaireResultListFillable = gson.fromJson(resultQuestionnaireListFillableArray[i].toString(), RT)


        Log.d(TAG, "--> gquestionnaireResultListFillable.id : ${questionnaireResultListFillable.id} ")
        Log.d(TAG, "--> questionnaireResultListFillable.seq ${questionnaireResultListFillable.seq} ")
        Log.d(TAG, "--> i:  $i ")

        if (questionnaireResultListFillable.scheduledUntil != null && questionnaireResultListFillable.scheduledUntil!!.date != null && (questionnaireResultListFillable.scheduledUntil!!.date!! < System.currentTimeMillis())) {

            val params = Array<Any>(2) {}
            params[0] = questionnaireResultListFillable.id
            params[1] = questionnaireResultListFillable.seq

            meteor.call("questionnaire-result.cancel", params, object : ResultListener {
                override fun onSuccess(result: String?) {
                    Log.d(TAG, "questionnaire-result.cancel $result")
                    val oldCounter = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext).getInt(BluetoothLeService.KEY_NEW_QUESTIONNAIRECOUNTER, -1)
                    val KEY_NEWQUESTIONNAIRECOUNTER = androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
                    KEY_NEWQUESTIONNAIRECOUNTER.edit()
                            .putInt(BluetoothLeService.KEY_NEW_QUESTIONNAIRECOUNTER, oldCounter - 1)
                            .apply()

                    Log.d(TAGNOTIFY, "uj érték a cancel miatt: ${oldCounter - 1})")

                    if ((i + 1) != resultQuestionnaireListFillableArray.length()) {
                        getQuestionSecound(resultQuestionnaireListFillableArray, resultData)
                        i++
                    }else {
                        i = 0
                    }
                }

                override fun onError(error: String?, reason: String?, details: String?) {
                    Log.d(TAG, "-->error $error ")
                    Log.d(TAG, "-->error $reason ")
                    Log.d(TAG, "-->error $details ")
                    if ((i + 1) != resultQuestionnaireListFillableArray.length()) {
                        getQuestionSecound(resultQuestionnaireListFillableArray, resultData)
                        i++
                    }else {
                        i = 0
                    }
                }
            })
        } else {
            /**Még kitölthető*/
            val params = arrayOf("")
            params[0] = questionnaireResultListFillable.questionnaireId

            meteor.call("questionnaire.get", params, object : ResultListener {
                override fun onSuccess(result: String?) {

                    Log.d(TAG, "questionnaire.get $result")

                    if (!result.isNullOrEmpty() && result != "[]" && result != "{}" && result != "{[]}" && result != "[{}]" && result != "null" && result != " null") {
                        if (writeFileQuestionnaireActive) {
                            writeFile(result, QuestionnaireType.QUESTIONNAIRE_GET, i)
                        }
                        if (id_noQuestionnaire != null) {
                            id_noQuestionnaire.visibility = View.GONE
                        }

                        val REVIEW_TYPE_QUESTIONNAIREGET = object : TypeToken<QuestionnaireGetModel>() {}.type
                        val questionnaireGetModel: QuestionnaireGetModel = gson.fromJson(result, REVIEW_TYPE_QUESTIONNAIREGET)

                        resultData.add(QuestionnaireDataClass(questionnaireResultListFillable, questionnaireGetModel))

                        if (view != null) {

                            adapter = QuestionnaireAdapter(view!!.context, resultData)
                            if (id_questionnaire_recycleView != null) {
                                id_questionnaire_recycleView.adapter = adapter
                            }
                            adapter.notifyDataSetChanged()
                            if (swipeRefresQUE != null) {
                                swipeRefresQUE.isRefreshing = false
                            }
                        }
                    } else {
                        if (swipeRefresQUE != null) {
                            swipeRefresQUE.isRefreshing = false
                            id_noQuestionnaire.visibility = View.VISIBLE
                        }
                    }
                    if ((i + 1) != resultQuestionnaireListFillableArray.length()) {
                        getQuestionSecound(resultQuestionnaireListFillableArray, resultData)
                        i++
                    }else {
                        i = 0
                    }
                }

                override fun onError(error: String?, reason: String?, details: String?) {
                    Log.d(TAG, "-->error $error ")
                    Log.d(TAG, "-->error $reason ")
                    Log.d(TAG, "-->error $details ")
                    if ((i + 1) != resultQuestionnaireListFillableArray.length()) {
                        getQuestionSecound(resultQuestionnaireListFillableArray, resultData)
                        i++
                    }else {
                        if (swipeRefresQUE != null) {
                            swipeRefresQUE.isRefreshing = false
                            id_noQuestionnaire.visibility = View.VISIBLE
                        }
                        i = 0
                    }
                }
            })
        }
    }

    private fun getTemplate() {
        val params = arrayOf("")
        meteor.call("questionnaire-template.get", params, object : ResultListener {
            override fun onSuccess(result: String?) {
                Log.d(TAG, "result template: $result")
                if (writeFileQuestionnaireActive) {
                    writeFile(result, QuestionnaireType.QUESTIONNAIRE_TEMPLATE_GET, 0)
                }
            }

            override fun onError(error: String?, reason: String?, details: String?) {
                Log.d(TAG, "-->error $error ")
                Log.d(TAG, "--> error $reason ")
                Log.d(TAG, "-->error $details ")
            }
        })
    }

    fun backToQuestionnaireList() {
        if (!applicationVersionModeIsLight) {
            if (swipeRefresQUE != null) {
                swipeRefresQUE.visibility = View.VISIBLE
            }

            if (fragmentManager != null) {
                val ft = fragmentManager!!.beginTransaction()
                ft.remove(sectionFragment)
                ft.commit()
                tryConnect(view!!.context)
            }
        }
    }

    /**
     * questionnaire datas write csv
     *
     * @param result
     * @param type
     * @param i
     */
    private fun writeFile(result: String?, type: QuestionnaireType, i: Int) {
        val formatter = SimpleDateFormat("HH_mm_ss")
        val time = formatter.format(System.currentTimeMillis())
        var fileName = ""
        val hasSDCard = Environment.getExternalStorageState()
        when (type.type) {

            QuestionnaireType.QUESTIONNAIRE_GET.type -> {
                fileName = "QUESTIONNAIRE_GET_$i" + "_${time}.txt"
            }
            QuestionnaireType.QUESTIONNAIRE_RESULT_LISTFILLABLE.type -> {
                fileName = "QUESTIONNAIRE_RESULT_LISTFILLABLE_${time}.txt"
            }
            QuestionnaireType.QUESTIONNAIRE_TEMPLATE_GET.type -> {
                fileName = "QUESTIONNAIRE_TEMPLATE_GET_$i" + "_${time}.txt"
            }
        }

        when (hasSDCard) {
            Environment.MEDIA_MOUNTED -> {

                // Írható olvasható
                var fileWriter: FileWriter?

                val folder = File(Environment.getExternalStorageDirectory().toString() + "/Euronet/QUESTIONNAIRE")

                if (!folder.exists()) {
                    folder.mkdirs()  // fix
                    folder.setExecutable(true)
                    folder.setReadable(true)
                    folder.setWritable(true)
                }

                val file = File(folder, fileName)

                if (!file.exists()) {
                    file.setWritable(true)
                    file.setReadable(true)
                    file.setExecutable(true)

                    fileWriter = FileWriter(file)
                    fileWriter.append(result)
                    fileWriter.flush()
                    fileWriter.close()
                }
            }
        }
    }

    override fun onConnect(signedInAutomatically: Boolean) {
        Log.d(TAGMETEORQUEST, "onConnect questionnaire $signedInAutomatically")
    }

    override fun onDataAdded(collectionName: String?, documentID: String?, newValuesJson: String?) {
        val mCollectionName = "onDataAdded $collectionName"
        val mDocumentID = "onDataAdded $documentID"
        val mNewValuesJson = "onDataAdded$newValuesJson"

        Log.d(TAGMETEORQUEST, mCollectionName)
        Log.d(TAGMETEORQUEST, mDocumentID)
        Log.d(TAGMETEORQUEST, mNewValuesJson)
    }

    override fun onDataRemoved(collectionName: String?, documentID: String?) {
        val mCollectionName = "onDataRemoved $collectionName"
        val mDocumentID = "onDataRemoved $documentID"

        Log.d(TAGMETEORQUEST, mCollectionName)
        Log.d(TAGMETEORQUEST, mDocumentID)
    }

    override fun onException(e: Exception) {
        val onexception = "onException ${e.message}"
        Log.d(TAGMETEORQUEST, onexception)
    }

    override fun onDisconnect() {
        val ondisconnect = "onDisconnect"
        Log.d(TAGMETEORQUEST, ondisconnect)
    }

    override fun onDataChanged(collectionName: String?, documentID: String?, updatedValuesJson: String?, removedValuesJson: String?) {
        val mCollectionName = "onDataChanged $collectionName"
        val mDocumentID = "onDataChanged $documentID"
        val mNewValuesJson = "onDataChanged $updatedValuesJson"
        val mRemovedValuesJson = "onDataChanged $removedValuesJson"

        Log.d(TAGMETEORQUEST, mCollectionName)
        Log.d(TAGMETEORQUEST, mDocumentID)
        Log.d(TAGMETEORQUEST, mNewValuesJson)
        Log.d(TAGMETEORQUEST, mRemovedValuesJson)
    }
}