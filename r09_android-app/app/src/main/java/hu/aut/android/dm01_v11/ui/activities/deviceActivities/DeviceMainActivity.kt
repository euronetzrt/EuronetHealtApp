package hu.aut.android.dm01_v11.ui.activities.deviceActivities

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.*
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hu.aut.android.dm01_v11.R
import hu.aut.android.dm01_v11.crashLog.CustomizedExceptionHandler
import hu.aut.android.dm01_v11.ui.activities.startApp.loginActivities.LoginActivity
import hu.aut.android.dm01_v11.ui.fragments.FragmentAppSettingsScreenPage
import hu.aut.android.dm01_v11.ui.fragments.FragmentDeviceNordicPage
import hu.aut.android.dm01_v11.ui.fragments.FragmentDeviceSettingsPage
import hu.aut.android.dm01_v11.ui.fragments.deviceInfo.FragmentDeviceInfoPage
import hu.aut.android.dm01_v11.ui.fragments.deviceInfo.FragmentDeviceScreenPage
import hu.aut.android.dm01_v11.ui.fragments.homePage.FragmentDeviceHomePage
import hu.aut.android.dm01_v11.ui.fragments.questionnaire.FragmentDeviceQuestionnairePage
import hu.aut.android.dm01_v11.ui.fragments.questionnaire.SectionFragment.Companion.TAGNOTIFY
import hu.aut.android.dm01_v11.ui.fragments.userInfo.FragmentUserInfoPage
import hu.euronetrt.okoskp.euronethealth.GlobalRes.ACTION_ACL_CONNECTED
import hu.euronetrt.okoskp.euronethealth.GlobalRes.ACTION_ACL_DISCONNECTED
import hu.euronetrt.okoskp.euronethealth.GlobalRes.ACTION_BATTERY_DATA_AVAILABLE
import hu.euronetrt.okoskp.euronethealth.GlobalRes.ACTION_SERVICE_DISCOVER_DONE
import hu.euronetrt.okoskp.euronethealth.GlobalRes.ACTION_TRIANGLESIGNAL_DATA_AVAILABLE
import hu.euronetrt.okoskp.euronethealth.GlobalRes.SERVER_CONN
import hu.euronetrt.okoskp.euronethealth.GlobalRes.SERVER_NOT_CONN
import hu.euronetrt.okoskp.euronethealth.GlobalRes.applicationVersionModeIsLight
import hu.euronetrt.okoskp.euronethealth.GlobalRes.connectedDevice
import hu.euronetrt.okoskp.euronethealth.GlobalRes.dfuRefreshTryAgain
import hu.euronetrt.okoskp.euronethealth.GlobalRes.searchingDeviceList
import hu.euronetrt.okoskp.euronethealth.GlobalRes.writeFileCrashRiportActive
import hu.euronetrt.okoskp.euronethealth.bluetooth.BluetoothServiceNotificationType
import hu.euronetrt.okoskp.euronethealth.bluetooth.bleInterfaces.ConnectDeviceWithGatt
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.BleScanCallback
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.BleScanCallback.adapter
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.BleScanCallback.arrayOfFoundBTDevices
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.BleScanCallback.contextIsDeviceMainAct
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.LightBleScanCallback
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.LightBleScanCallback.lightAdapter
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.LightBleScanCallback.lightArrayOfFoundBTDevices
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.callbacks.LightBleScanCallback.lightContextMain
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.adapter.DeviceAdapter
import hu.euronetrt.okoskp.euronethealth.bluetooth.scan.deviceToUi.adapter.LightDeviceAdapter
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_BIRTHDAY
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_DEVICEIMAGE
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_DEVICENAME
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_DEVICE_ID
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_FW_VERSION
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_HARDWARE_VERSION
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_HWMAC_ID
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_MANUFACTURER_NAME_STRING
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_MODEL_NUMBER_STRING
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_NEW_QUESTIONNAIRECOUNTER
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_SERIAL_NUMBER_STRING
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_USEREMAIL
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_USERID
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_USERIMAGE
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_USERNAME
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.KEY_USERSEX
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService.Companion.TAGSCAN
import hu.euronetrt.okoskp.euronethealth.broadcastrecievers.*
import hu.euronetrt.okoskp.euronethealth.dfuManager.DFUManager
import hu.euronetrt.okoskp.euronethealth.dfuManager.dfu.DFUUpdateActivity
import hu.euronetrt.okoskp.euronethealth.dfuManager.dfu.FirmwareUpdateActvity
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral.mHost
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.meteor.EuronetMeteorSingleton
import hu.euronetrt.okoskp.euronethealth.serverCommunicate.model.listByMultipleModel.MultipleDeviceInfo
import im.delight.android.ddp.MeteorCallback
import im.delight.android.ddp.ResultListener
import im.delight.android.ddp.db.memory.InMemoryDatabase
import kotlinx.android.synthetic.main.activity_device_main.*
import kotlinx.android.synthetic.main.connect_waiting_progress.*
import kotlinx.android.synthetic.main.fragment_device_scan_page.*
import org.json.JSONArray
import java.io.File
import kotlin.system.exitProcess

class DeviceMainActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, ConnectDeviceWithGatt {

    companion object {
        private val TAG = "DEVICE_MAIN_ACTIVITY"
        private val TAGPAIR = "TAGPAIR"
        private val TAGUNPAIR = "TAGUNPAIR"
        private val TAGMETEOR = "TAGMETEOR"
        private val TAGLOGOUT = "TAGLOGOUT"
        private const val RESULT_CODE = 101
        private const val REQUEST_ENABLE_BT = 201
        private const val MIN_BATTERY_VALUE = 20
    }

    private lateinit var userMenuItem: MenuItem
    private lateinit var badge: BadgeDrawable
    private lateinit var badgeTriangle: BadgeDrawable
    private var sendTimeFirst = true
    private var serverOKUserInfo = false
    private val homeFragment = FragmentDeviceHomePage()
    private val nordicFragment = FragmentDeviceNordicPage()
    private val appSettingsFragment = FragmentAppSettingsScreenPage()
    private val userFragment = FragmentUserInfoPage()
    private val deviceInfoFragment = FragmentDeviceInfoPage()
    private val settingsPageFragment = FragmentDeviceSettingsPage()
    private val questionnairePageFragment = FragmentDeviceQuestionnairePage()
    private lateinit var navView: BottomNavigationView
    private lateinit var loadedFragment: Fragment
    private lateinit var bleService: BluetoothLeService
    private var deviceBatteryValue = 0
    private var DFUFile = "/PPGo_DFU.zip"
    private lateinit var accountManager: AccountManager
    private val serverNotConnBroadcast = ServerNotConnectedBroadcastReciever()
    private val errorLicense = ErrorLicenseBroadcastReciever()
    private val serverConnBroadcast = ServerConnectedBroadcastReciever()
    private val bluetoothStateChange = BluetoothOFFBroadcastReciever()
    private val deviceBatteryChange = BleBatteryBroadcastReceiver()
    private val triangleSignalBroadcastReciever = TriangleSignalBroadcastReciever()
    private lateinit var textCartItemCount: TextView
    private lateinit var meteor: EuronetMeteorSingleton
    var serverOK = false
    private lateinit var oncreteOptionsMenuVariable: Menu

    private lateinit var dfuThread: Thread
    /*BroadcastReciever*/
    private val deviceConnectionBroadcastReciever = BleConnectBroadcastReceiver()

    /**
     * onCreate Device main activity
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (writeFileCrashRiportActive) {
            // Sets the default uncaught exception handler. This handler is invoked
            // in case any Thread dies due to an unhandled exception.
            Thread.setDefaultUncaughtExceptionHandler(CustomizedExceptionHandler(
                    "Crash.txt"))
        }

        setContentView(R.layout.activity_device_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(applicationContext, BluetoothLeService::class.java))
        } else {
            startService(Intent(applicationContext, BluetoothLeService::class.java))
        }

        if (!applicationVersionModeIsLight) {
            accountManager = AccountManager.get(this)
            val accounts = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)
            if (accounts.size == 1 && !accounts[0].name.isNullOrEmpty()) {
                accountManager.getAuthToken(accounts[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, this,
                        { future ->
                            val bnd = future.result
                            val authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN)
                            if (authtoken.isNullOrEmpty()) {
                                // missig token!
                                finish()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                            }
                        }, null)
            }
        }

        navView = findViewById(R.id.nav_bottom_navigate_view)
        val toolbar: Toolbar = findViewById(R.id.my_toolbar)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        badge = navView.getOrCreateBadge(R.id.navigation_questionnaire)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (EuronetMeteorSingleton.hasInstance()) {
            Log.d(TAGMETEOR, "Connect meteor")
            meteor = EuronetMeteorSingleton.getInstance()
        } else {
            Log.d(TAGMETEOR, "create meteor devmain")
            meteor = EuronetMeteorSingleton.createInstance(this, "ws://$mHost:3000/websocket", InMemoryDatabase())
        }

        if (!meteor.isConnected) {
            Log.d(TAGMETEOR, "connect hívás meteorra devMAin")
            meteor.connect()
        }

        if (savedInstanceState == null) {
            loadedFragment = homeFragment
            val ft = supportFragmentManager.beginTransaction()
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            ft.replace(R.id.deviceFrameLayout, homeFragment, FragmentDeviceHomePage.TAG)
            ft.commit()
        }

        id_syncDeviceStartStop.setOnClickListener {
            if (id_scanLayoutProgressBar.visibility == View.VISIBLE) {
                scanAndPaidProgressController(ScanAndPairProgressCommandType.SCANSTOP.commandType, "")
            } else {
                startScan()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val intetnfilter = IntentFilter()
        intetnfilter.addAction(ACTION_ACL_CONNECTED)
        intetnfilter.addAction(ACTION_ACL_DISCONNECTED)
        intetnfilter.addAction(ACTION_SERVICE_DISCOVER_DONE)
        registerReceiver(
                deviceConnectionBroadcastReciever,
                IntentFilter(intetnfilter)
        )

        val intentFilterBattery = IntentFilter()
        intentFilterBattery.addAction(ACTION_BATTERY_DATA_AVAILABLE)
        registerReceiver(
                deviceBatteryChange,
                intentFilterBattery
        )

        if (!applicationVersionModeIsLight) {

            val intentFilter = IntentFilter()
            intentFilter.addAction(SERVER_NOT_CONN)
            registerReceiver(
                    serverNotConnBroadcast,
                    intentFilter
            )

            val intentFilterServerConn = IntentFilter()
            intentFilterServerConn.addAction(SERVER_CONN)
            registerReceiver(
                    serverConnBroadcast,
                    intentFilterServerConn
            )

            val intentFilterError = IntentFilter()
            intentFilterError.addAction("ERROR_LICENSE")
            registerReceiver(
                    errorLicense,
                    intentFilterError
            )

            val intentFilterTriangle = IntentFilter()
            intentFilterTriangle.addAction(ACTION_TRIANGLESIGNAL_DATA_AVAILABLE)
            registerReceiver(
                    triangleSignalBroadcastReciever,
                    intentFilterTriangle
            )
        }

        val intentFilterBluetoothStateChange = IntentFilter()
        intentFilterBluetoothStateChange.addAction("android.bluetooth.adapter.action.STATE_CHANGED")
        registerReceiver(
                bluetoothStateChange,
                intentFilterBluetoothStateChange
        )


        if (getHasOwnDevice() == null) {
            Log.d(TAG, "onStart call mainProg")
            mainProgressController(ProgressCommandType.HIDE.commandType, false)
        } else {
            if (!connectedDevice) {
                Log.d(TAG, "onstart !connectedDevice call mainProg")
                mainProgressController(ProgressCommandType.RECONNECT.commandType, false)
            } else {
                Log.d(TAG, "onstart connectedDevice call mainProg")
                deviceConnected()
            }
        }

        // Mardjon ezzel lehet leválasztani eszközt ha netán beragadna. a végsőbe nem kell bent legyen
        /*  if (!applicationVersionModeIsLight) {
           //   if (getHasOwnDevice() != null) {

                   toolbar_title.setOnClickListener {

                       Log.d(TAGUNPAIR, "$TAG --> toolbar_title click ok ")
                       if (!applicationVersionModeIsLight) {

                           if (!meteor.isConnected) {
                               Log.d(TAGUNPAIR, "$TAG --> !meteor.isConnected run (false connect meteor) ")
                               meteor.connect()
                               mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                           } else {
                               Log.d(TAGUNPAIR, "$TAG --> meteor connected ")

                               /*ServerCommunicate*/
                               Log.d(TAGUNPAIR, "$TAG  -->meteor.run ")

                               val methodGet = "devices.get"
                               val paramsGet = Array(1) { "" }

                               val devid = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(KEY_DEVICE_ID, null)
                               paramsGet[0] = devid!! //"5de11a5fe1f32e79558259a4" --> új kijelzős eszköz /*"5ddf9f3d615c4875e65e560a"   -> fekete F4:D0..*/  //devid!!

                               meteor.call(methodGet, paramsGet, object : ResultListener {
                                   override fun onSuccess(result: String?) {
                                       try {
                                           Log.d(TAGUNPAIR, "$TAG  --> onSuccess result $result ")
                                           val notValidResult = "[]"
                                           if (!result.isNullOrEmpty() && result != notValidResult) {

                                               val REVIEW_TYPE = object : TypeToken<MultipleDeviceInfo>() {}.type
                                               val gson = Gson()
                                               val dataMultipleList: MultipleDeviceInfo = gson.fromJson(result, REVIEW_TYPE)

                                               serverPushUnpairMethod(dataMultipleList)
                                           }
                                       } catch (e: Exception) {
                                           Log.d(TAGUNPAIR, "$TAG  --> ${e.message}")
                                           mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                                       }
                                   }

                                   override fun onError(error: String?, reason: String?, details: String?) {
                                       Log.d(TAGUNPAIR, "$TAG  --> error $error ")
                                       Log.d(TAGUNPAIR, "$TAG  --> error $reason ")
                                       Log.d(TAGUNPAIR, "$TAG  --> error $details ")
                                       mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                                   }
                               })
                           }
                       }
          //        }
               }
           }*/

        bindService(
                Intent(this, BluetoothLeService::class.java),
                bleServiceConnection,
                Context.BIND_AUTO_CREATE
        )
    }

    override fun onResume() {
        super.onResume()

        if (connectedDevice) {
            if (::bleService.isInitialized) {
                if (!bleService.getArriveingData()) {
                    Log.d(TAG, "onresume !bleService.getArriveingData() call mainProg")
                    mainProgressController(ProgressCommandType.HIDE.commandType, false)
                } else {
                    Log.d(TAG, "onresume bleService.getArriveingData() call mainProg")
                    //true - > érkezik nordic adat legalább is feliratkoztunk rá.
                    mainProgressController(ProgressCommandType.NORDICACTIVE.commandType, false)
                }
            }
        }

        getQuestionnaireCount()

        getUserInfo()
    }

    private fun getUserInfo() {
        var tryReConnectCounter = 0
        val WAIT_INTERVAL: Long = 5000

        if (networkCheck(this)) {
            // if (EuronetMeteorSingleton.hasInstance()) {
            //     meteor = EuronetMeteorSingleton.getInstance()
            // } else {
            //     meteor = EuronetMeteorSingleton.createInstance(this, "ws://${AccountGeneral.mHost}:3000/websocket", InMemoryDatabase())
            // }

            if (!meteor.isConnected) {
                meteor.connect()
            } else {
                Log.d(TAG, "meteor isconnected true")
            }

            Handler().postDelayed({
                if (!serverOKUserInfo) {
                    if (tryReConnectCounter == 10) {
                        tryReConnectCounter = 0
                    } else {
                        tryReConnectCounter++
                        getUserInfo()
                    }
                } else {
                    //alaphelyzetbe állítjuk
                    serverOKUserInfo = false
                }
            }, WAIT_INTERVAL)
            getUserInfoStepTwo()
        }
    }

    private fun getUserInfoStepTwo() {
        val collectionsArray = meteor.database.collectionNames
        Log.d(TAG, "collectionsArray $collectionsArray")
        if (!collectionsArray.isNullOrEmpty()) {
            val userId = android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(KEY_USERID, null)
            val collection = meteor.database.getCollection(collectionsArray[0])
            val document = collection.getDocument(userId)
            Log.d(TAG, "document $document")

            if (document != null) {
                var emails = document.getField("emails")
                var firstEmail = ((emails as List<*>)[0] as Map<*, *>).get("address")
                Log.d("LACINAK", firstEmail.toString())
//                val userInfoIbject = gson.fromJson<UserInfoModel>(document.toString(), REVIEW_TYPE)

                val KEYUSEREMAIL = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                KEYUSEREMAIL.edit()
                        .putString(KEY_USEREMAIL, firstEmail as String?)
                        .apply()

                val KEYUSERNAME = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                KEYUSERNAME.edit()
                        .putString(KEY_USERNAME, (document.getField("profile") as Map<*, *>).get("displayName") as String?)
                        .apply()

                val KEYUSERSEX = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                KEYUSERSEX.edit()
                        .putString(KEY_USERSEX, (document.getField("profile") as Map<*, *>).get("sex") as String?)
                        .apply()

                val KEYBIRTHDAY = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                KEYBIRTHDAY.edit()
                        .putString(KEY_BIRTHDAY, (document.getField("profile") as Map<*, *>).get("birthday") as String?)
                        .apply()

                val KEYUSERIMAGE = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                KEYUSERIMAGE.edit()
                        .putString(KEY_USERIMAGE, (document.getField("profile") as Map<*, *>).get("picture") as String?)
                        .apply()
            }
        }

        serverOKUserInfo = true
    }

    private fun getQuestionnaireCount() {
        var tryReConnectCounter = 0
        val WAIT_INTERVAL: Long = 5000

        if (networkCheck(this)) {
            /* if (EuronetMeteorSingleton.hasInstance()) {
                 meteor = EuronetMeteorSingleton.getInstance()
             } else {
                 meteor = EuronetMeteorSingleton.createInstance(this, "ws://${AccountGeneral.mHost}:3000/websocket", InMemoryDatabase())
             }*/

            if (!meteor.isConnected) {
                meteor.connect()
            }

            Handler().postDelayed({
                if (!serverOK) {
                    if (tryReConnectCounter == 2) {
                        tryReConnectCounter = 0
                    } else {
                        tryReConnectCounter++
                        getQuestionnaireCount()
                    }
                } else {
                    //alaphelyzetbe állítjuk
                    serverOK = false
                }
            }, WAIT_INTERVAL)
            getQuestionsList()
        }
    }

    private fun getQuestionsList() {
        Log.d(TAG, "getQuestionsList call")
        if (!meteor.isConnected) {
            Log.d(TAG, "$TAG --> !meteor.isConnected run (false connect meteor) ")
            meteor.connect()
        } else {
            meteor.call("questionnaire-result.listFillable", object : ResultListener {
                override fun onSuccess(result: String?) {
                    //  Log.d(TAG, "result questionnaire-result.listFillable: $result")
                    serverOK = true

                    val resultQuestionnaireListFillableArray = JSONArray(result)
                    // Log.d(TAGNOTIFY, "elérhető kérdőív:  ${resultQuestionnaireListFillableArray.length()}")

                    val actualCounter = PreferenceManager.getDefaultSharedPreferences(applicationContext).getInt(KEY_NEW_QUESTIONNAIRECOUNTER, -1)
                    // Log.d(TAGNOTIFY, "actualCounter:  ${actualCounter}")

                    if (actualCounter == -1) {
                        val KEY_NEWQUESTIONNAIRECOUNTER = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        KEY_NEWQUESTIONNAIRECOUNTER.edit()
                                .putInt(KEY_NEW_QUESTIONNAIRECOUNTER, resultQuestionnaireListFillableArray.length())
                                .apply()

                        Log.d(TAGNOTIFY, "new actualCounter:  ${resultQuestionnaireListFillableArray.length()} mert először állítjuk be")
                        badge.setVisible(true)
                        badge.number = resultQuestionnaireListFillableArray.length()
                    }

                    if (!applicationVersionModeIsLight) {
                        val newQuestionnaire = resultQuestionnaireListFillableArray.length() - PreferenceManager.getDefaultSharedPreferences(applicationContext).getInt(KEY_NEW_QUESTIONNAIRECOUNTER, -1)
                        Log.d(TAGNOTIFY, "ennyi a különbség : $newQuestionnaire")
                        badge.setVisible(true)
                        val KEY_NEWQUESTIONNAIRECOUNTER = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        KEY_NEWQUESTIONNAIRECOUNTER.edit()
                                .putInt(KEY_NEW_QUESTIONNAIRECOUNTER, resultQuestionnaireListFillableArray.length())
                                .apply()
                        Log.d(TAGNOTIFY, "uj érték a plusszok miatt: ${resultQuestionnaireListFillableArray.length()}")
                        if (newQuestionnaire != 0) badge.number = newQuestionnaire
                    }
                }

                override fun onError(error: String?, reason: String?, details: String?) {
                    Log.d(TAG, "-->error $error ")
                    Log.d(TAG, "--> error $reason ")
                    Log.d(TAG, "-->error $details ")
                }
            })
        }
    }

    /**
     * network Check
     *
     * @param context
     * @return boolean
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

    override fun onStop() {
        super.onStop()
        if (!applicationVersionModeIsLight) {
            unregisterReceiver(serverNotConnBroadcast)
            unregisterReceiver(serverConnBroadcast)
            unregisterReceiver(errorLicense)
            unregisterReceiver(triangleSignalBroadcastReciever)
        }
        unregisterReceiver(deviceBatteryChange)
        unregisterReceiver(bluetoothStateChange)
        unregisterReceiver(deviceConnectionBroadcastReciever)
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                showFragment(homeFragment, FragmentDeviceHomePage.TAG, false)
                refreshingEnable()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_nordic -> {

                showFragment(nordicFragment, FragmentDeviceNordicPage.TAG, false)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_groups -> {
                showFragment(settingsPageFragment, FragmentDeviceSettingsPage.TAG, false)
                refreshingEnable()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_questionnaire -> {
                if (!applicationVersionModeIsLight) {
                    if (badge.isVisible) {  // check whether the item showing badge
                        badge.clearNumber()  //  remove badge notification
                    }
                }
                showFragment(questionnairePageFragment, FragmentDeviceQuestionnairePage.TAG, false)
                refreshingEnable()
                return@OnNavigationItemSelectedListener true
            }
        }
        return@OnNavigationItemSelectedListener false
    }

    /**
     * onCreateOptionsMenu
     *
     * @param menu
     * @return Boolean
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        oncreteOptionsMenuVariable = menu!!
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        userMenuItem = menu.findItem(R.id.id_action_user)
        val imageString = if (androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(BluetoothLeService.KEY_USERIMAGE, null) == null) "" else androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(BluetoothLeService.KEY_USERIMAGE, "")
        if (!imageString.isNullOrEmpty() && imageString != "") {
            val decodeString = imageString.decode()
            val replaceImageString = decodeString.replace("data:image/jpeg;base64,", "")
            val base64 = Base64.decode(replaceImageString, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(base64, 0, base64.size)
            val drawable = BitmapDrawable(getResources(), bitmap)
            userMenuItem.setIcon(drawable)

        }
        return true
    }

    /**
     * onOptionsItemSelected
     *
     * @param item
     */
    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                R.id.id_refresh -> {
                    if (id_loading_LAYOUT.visibility == View.GONE) {

                        if (getHasOwnDevice() == null) {
                            Toast.makeText(this, "Please paired device first!", Toast.LENGTH_LONG).show()
                        } else {
                            mainProgressController(ProgressCommandType.RECONNECT.commandType, false)
                            tryConnectDevice()
                            Toast.makeText(this, "Refresh connection status..", Toast.LENGTH_LONG).show()
                        }
                        true
                    } else {
                        false
                    }
                }
                R.id.id_action_settings -> {
                    //   buttonIsChecked(false)
                    showFragment(appSettingsFragment, FragmentAppSettingsScreenPage.TAG, true)
                    true
                }
                R.id.id_action_user -> {
                    // buttonIsChecked(false)
                    showFragment(userFragment, FragmentUserInfoPage.TAG, false)
                    true
                }
                R.id.id_device_info -> {
                    if (getHasOwnDevice() != null) {
                        //   buttonIsChecked(false)
                        showFragment(deviceInfoFragment, FragmentDeviceScreenPage.TAG, false)
                        true
                    } else {
                        startScan()
                        true
                    }
                }
                R.id.id_nav_disconnect -> {
                    if (::bleService.isInitialized) {
                        Log.d("RECONNE", "id_nav_disconnect ")
                        bleService.serviceNotificationsSet(BluetoothServiceNotificationType.BATTERY_SERVICE.type, false)
                        bleService.serviceNotificationsSet(BluetoothServiceNotificationType.HEARTRATE_SERVICE.type, false)
                        bleService.serviceNotificationsSet(BluetoothServiceNotificationType.NORDIC_SERVICE.type, false)
                        bleService.serviceNotificationsSet(BluetoothServiceNotificationType.DEVICEINFO_SERVICE.type, false)
                        bleService.unSubscribeTriangleSignal()
                        Log.d(TAG, "Ondestroy call, connectionGatt.close")
                        bleService.getGatt().disconnect()
                        bleService.getGatt().close()
                        connectedDevice = false
                    }
                    true
                }

                /* R.id.id_nav_DFU_Update -> {
                     if (connectedDevice) {
                         val path = Environment.getExternalStorageDirectory().toString() + "/Euronet" + DFUFile

                         if (File(path).exists()) {
                             checkIsHasFirmweareUpdate()
                         } else {
                             SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                     .setTitleText("Update device's firmware")
                                     .setContentText("Your device's firmware is up to date!")
                                     .show()
                         }
                     }
                     true
                 }

                 R.id.id_nav_device_off -> {
                     /*   val nordicService = bleService.getGatt().getService(NORDIC_UART_SERVICE_UUID)
                        val rx = nordicService.getCharacteristic(RX_CHAR_UUID)
                        bleService.writeCharacteristic(rx,"OFF")
                        Toast.makeText(this, "Device off!", Toast.LENGTH_LONG).show()*/
                     true
                 }*/
                R.id.id_nav_logout -> {
                    if (!applicationVersionModeIsLight) {
                        logout()
                        true
                    } else {
                        true
                    }
                }
                else -> {
                    supportFragmentManager.popBackStack()
                    true
                }
            }

    private fun logout() {
        id_loading_LAYOUT.visibility = View.VISIBLE
        if (getConnectivityStatusString(this)) {

            val userId = android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(KEY_USERID, null)
            if (meteor.isConnected()) {

                meteor.logout(object : ResultListener {
                    override fun onSuccess(result: String?) {
                        Log.d(TAGLOGOUT, "onSuccess logout meteor - >$result   userid-> $userId")
                        invalidateAuthToken(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS)
                    }

                    override fun onError(error: String?, reason: String?, details: String?) {
                        Log.d(TAGLOGOUT, "onError logout meteor - >$error, $reason, $details")
                        Toast.makeText(this@DeviceMainActivity, "Login not succes please try again later!", Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                meteor.connect()
                meteor.addCallback(object : MeteorCallback {
                    override fun onConnect(signedInAutomatically: Boolean) {
                        meteor.logout(object : ResultListener {
                            override fun onSuccess(result: String?) {
                                Log.d(TAGLOGOUT, "onSuccess2 logout meteor - >$result userid: $userId")
                                invalidateAuthToken(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS)
                            }

                            override fun onError(error: String?, reason: String?, details: String?) {
                                invalidateAuthToken(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS)
                                Log.d(TAGLOGOUT, "onError logout meteor - >$error, $reason, $details")
                                Toast.makeText(this@DeviceMainActivity, "Login not succes please try again later!", Toast.LENGTH_LONG).show()
                            }
                        })
                    }

                    override fun onDataAdded(collectionName: String?, documentID: String?, newValuesJson: String?) {
                        val mCollectionName = "onDataAdded $collectionName"
                        val mDocumentID = "onDataAdded $documentID"
                        val mNewValuesJson = "onDataAdded$newValuesJson"

                        Log.d(TAGLOGOUT, mCollectionName)
                        Log.d(TAGLOGOUT, mDocumentID)
                        Log.d(TAGLOGOUT, mNewValuesJson)
                    }

                    override fun onDataRemoved(collectionName: String?, documentID: String?) {
                        val mCollectionName = "onDataRemoved $collectionName"
                        val mDocumentID = "onDataRemoved $documentID"

                        Log.d(TAGLOGOUT, mCollectionName)
                        Log.d(TAGLOGOUT, mDocumentID)
                    }

                    override fun onException(e: Exception) {
                        val onexception = "onException ${e.message}"
                        invalidateAuthToken(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS)
                        Log.d(TAGLOGOUT, onexception)
                    }

                    override fun onDisconnect() {
                        val ondisconnect = "onDisconnect"
                        Log.d(TAGLOGOUT, ondisconnect)
                    }

                    override fun onDataChanged(collectionName: String?, documentID: String?, updatedValuesJson: String?, removedValuesJson: String?) {
                        val mCollectionName = "onDataChanged $collectionName"
                        val mDocumentID = "onDataChanged $documentID"
                        val mNewValuesJson = "onDataChanged $updatedValuesJson"
                        val mRemovedValuesJson = "onDataChanged $removedValuesJson"

                        Log.d(TAGLOGOUT, mCollectionName)
                        Log.d(TAGLOGOUT, mDocumentID)
                        Log.d(TAGLOGOUT, mNewValuesJson)
                        Log.d(TAGLOGOUT, mRemovedValuesJson)
                    }
                })
            }
        }
    }

    /**
     * invalidateAuthToken
     *
     * @param authtokenTypeFullAccess
     */
    private fun invalidateAuthToken(authtokenTypeFullAccess: String) {
        if (!applicationVersionModeIsLight) {
            val accounts = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)
            accountManager.getAuthToken(accounts[0], authtokenTypeFullAccess, null, this,
                    { future ->
                        val bnd = future.result
                        val authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN)
                        accountManager.invalidateAuthToken(AccountGeneral.ACCOUNT_TYPE, authtoken)
                        //checkTokenClear(authtokenTypeFullAccess)
                        // moveTaskToBack(true)

                        val deleteUserID = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        deleteUserID.edit()
                                .putString(KEY_USERID, null)
                                .apply()

                        val deleteUserEmail = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        deleteUserEmail.edit()
                                .putString(KEY_USEREMAIL, null)
                                .apply()

                        val deleteUserName = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        deleteUserName.edit()
                                .putString(KEY_USERNAME, null)
                                .apply()

                        val deleteUserSex = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        deleteUserSex.edit()
                                .putString(KEY_USERSEX, null)
                                .apply()

                        val deleteUserBirth = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        deleteUserBirth.edit()
                                .putString(KEY_BIRTHDAY, null)
                                .apply()

                        val deleteUserImage = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        deleteUserImage.edit()
                                .putString(KEY_USERIMAGE, null)
                                .apply()

                        val deleteDeviceImage = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        deleteDeviceImage.edit()
                                .putString(KEY_DEVICEIMAGE, null)
                                .apply()

                        val deleteDeviceName = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        deleteDeviceName.edit()
                                .putString(KEY_DEVICENAME, null)
                                .apply()

                        val deleteDeviceId = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        deleteDeviceId.edit()
                                .putString(KEY_DEVICE_ID, null)
                                .apply()

                        val deleteDeviceMac = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        deleteDeviceMac.edit()
                                .putString(KEY_HWMAC_ID, null)
                                .apply()


                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, null)
        }
    }

    /**
     * check Token Clear
     *
     * @param authtokenTypeFullAccess
     */
    private fun checkTokenClear(authtokenTypeFullAccess: String) {
        if (!applicationVersionModeIsLight) {
            val accounts = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)
            accountManager.getAuthToken(accounts[0], authtokenTypeFullAccess, null, this,
                    {
                        moveTaskToBack(true)
                        finishAffinity()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }, null)
        }
    }

    /**
     * Connectivity Status String
     *
     * @param mcontext
     * @return Boolean
     */
    private fun getConnectivityStatusString(mcontext: Context): Boolean {
        if (!applicationVersionModeIsLight) {
            val connManager: ConnectivityManager = mcontext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val actNetwork = connManager.activeNetworkInfo

            if (actNetwork != null) {
                Log.d(TAG, "network :${actNetwork.isConnected}")
                return actNetwork.isConnected
            } else {
                return false
            }
        } else {
            return false
        }
    }

    /**
     * show Fragment
     *
     * @param showThisFragment
     * @param tag
     * @param addBackstack
     */
    private fun showFragment(showThisFragment: Fragment, tag: String, addBackstack: Boolean) {
        val ft = supportFragmentManager.beginTransaction()

        if (!::loadedFragment.isInitialized) {
            loadedFragment = homeFragment
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            ft.replace(R.id.deviceFrameLayout, homeFragment, FragmentDeviceHomePage.TAG)
            ft.commit()
        }

        if (loadedFragment != showThisFragment) {
            ft.remove(loadedFragment)
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            ft.replace(R.id.deviceFrameLayout, showThisFragment, tag)
            if (addBackstack) {
                ft.addToBackStack(tag)
            }
            try {
                ft.commit()
                loadedFragment = showThisFragment
            } catch (e: IllegalStateException) {

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
                //     finish()
            } else {
                if (!connectedDevice) {
                    tryConnectDevice()
                } else {
                    if (dfuRefreshTryAgain) {
                        dfuRefreshTryAgain = false
                        firmwareCheck()
                    }
                }
            }
        }
    }

    /**
     * try Connect Device
     *
     */
    private fun tryConnectDevice() {
        Log.d(TAG, "getForUser --- >tryConnectDevice ")
        if (::bleService.isInitialized) {
            val bluetoothAdapter = bleService.getBluetoothAdapter()
            if (!bluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                if (getHasOwnDevice() == null) {
                    if (meteor.isConnected) {
                        meteor.call("devices.getForUser", object : ResultListener {
                            override fun onSuccess(resultDev: String?) {
                                Log.d(TAG, "getForUser --- > $resultDev")
                                val REVIEW_TYPE = object : TypeToken<Array<MultipleDeviceInfo>>() {}.type
                                val gson = Gson()
                                var dataMultipleList: Array<MultipleDeviceInfo> = gson.fromJson(resultDev, REVIEW_TYPE)


                                if (resultDev.isNullOrEmpty() || resultDev == "[]") {
                                    /*Nincs saját eszközünk*/
                                    if (!applicationVersionModeIsLight) {
                                        BleScanCallback.setContextFromDeviceMainAct(this@DeviceMainActivity, recycleDeviceList)
                                    } else {
                                        LightBleScanCallback.setContext(this@DeviceMainActivity, recycleDeviceList)
                                    }

                                    val dialog = SweetAlertDialog(this@DeviceMainActivity, SweetAlertDialog.NORMAL_TYPE)
                                            .setTitleText("Would liket to add device?")
                                            .setConfirmButton("Yes") {
                                                it.dismissWithAnimation()
                                                scanAndPaidProgressController(ScanAndPairProgressCommandType.SCANSTART.commandType, "")
                                            }
                                            .setCancelButton("Later") {
                                                it.dismissWithAnimation()
                                            }
                                    dialog.setCancelable(false)
                                    dialog.show()
                                } else {
                                    /*van eszközünk*/
                                    val spId = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                                    spId.edit()
                                            .putString(KEY_DEVICE_ID, dataMultipleList[0].id)
                                            .apply()

                                    val spHwMacId = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                                    spHwMacId.edit()
                                            .putString(KEY_HWMAC_ID, dataMultipleList[0].hw_mac_id)
                                            .apply()

                                    bleService.connectWithThisDevice(dataMultipleList[0].hw_mac_id)
                                }
                            }

                            override fun onError(error: String?, reason: String?, details: String?) {
                                Log.d(TAGUNPAIR, "$TAG  --> error $error ")
                                Log.d(TAGUNPAIR, "$TAG  --> error $reason ")
                                Log.d(TAGUNPAIR, "$TAG  --> error $details ")
                            }
                        })
                    } else {
                        Log.d(TAG, "meteor not connect getForUser.")
                    }
                } else {
                    bleService.continousTryConnectDevice()
                }
            }
        }
    }

    /**
     * onActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (REQUEST_ENABLE_BT == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                if (getHasOwnDevice() == null) {
                    Log.d(TAGPAIR, "ittt a bibi")
                    startScan()
                }
            }
        }
    }

    /**
     * onPreferenceStartFragment
     *
     * @param caller
     * @param pref
     * @return
     */
    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                pref.fragment
        ).apply {
            arguments = args
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
                .replace(R.id.deviceFrameLayout, fragment)
                .addToBackStack(null)
                .commit()
        return true
    }

    private fun checkIsHasFirmweareUpdate() {
        val path = Environment.getExternalStorageDirectory().toString() + "/Euronet" + DFUFile

        if (File(path).exists()) {
            val dialogDFU = SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("Update device's firmware")
                    .setContentText("Available new firmeare version. Would you like to update now?")
                    .setConfirmText("Yes")
                    .setConfirmClickListener {
                        it.dismissWithAnimation()
                        val intent = Intent(this, FirmwareUpdateActvity::class.java)
                        startActivityForResult(intent, RESULT_CODE)
                    }
                    .setCancelText("No")
                    .setCancelClickListener {
                        it.dismissWithAnimation()
                    }

            dialogDFU.setCancelable(false)
            dialogDFU.show()
        }
    }


    private fun exitApp() {

        val dialogExit = SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Exit")
                .setContentText("Would you like to disconnecting and exit?")
                .setConfirmText("Exit")
                .setConfirmClickListener {
                    //   mBluetoothLeService.choose = false
                    it.dismissWithAnimation()
                    exitApplication()
                }
                .setCancelText("No")
                .setCancelClickListener {
                    it.dismissWithAnimation()
                }
        dialogExit.setCancelable(false)
        dialogExit.show()
    }

    private fun exitApplication() {
        val fm = supportFragmentManager
        Log.d(TAG, "szám: ${fm.backStackEntryCount}")
        while (fm.backStackEntryCount != 0) {
            fm.popBackStack()
        }
        finishActivity(0)
        finishAffinity()
        super.onDestroy()
        exitProcess(0)
    }

    /**
     * changeDeviceBatteryValue
     *
     * @param newValue
     */
    fun changeDeviceBatteryValue(newValue: Int) {
        deviceBatteryValue = newValue

        when (deviceBatteryValue) {
            in 0..20 -> {
                batteryIcon__tool.setImageResource(R.drawable.ic_battery_20)
            }
            in 21..39 -> {
                batteryIcon__tool.setImageResource(R.drawable.ic_battery_30)
            }
            in 40..59 -> {
                batteryIcon__tool.setImageResource(R.drawable.ic_battery_50)
            }
            in 60..80 -> {
                batteryIcon__tool.setImageResource(R.drawable.ic_battery_60)
            }
            in 81..90 -> {
                batteryIcon__tool.setImageResource(R.drawable.ic_battery_80)
            }
            in 91..99 -> {
                batteryIcon__tool.setImageResource(R.drawable.ic_battery_90)
            }
            100 -> {
                batteryIcon__tool.setImageResource(R.drawable.ic_battery_full)
            }
            else -> {
                batteryIcon__tool.setImageResource(R.drawable.ic_battery_alert)
            }
        }
        id_battery_value_text__tool.text = deviceBatteryValue.toString() + " %"

        if (!applicationVersionModeIsLight) {
            if (::bleService.isInitialized) {
                if (dfuRefreshTryAgain) {
                    dfuRefreshTryAgain = false
                    firmwareCheck()
                }
            }
        }
    }

    fun startScan() {
        if (::bleService.isInitialized) {
            val bluetoothAdapter = bleService.getBluetoothAdapter()
            if (!bluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                scanAndPaidProgressController(ScanAndPairProgressCommandType.SCANSTART.commandType, "")
                if (!applicationVersionModeIsLight) {
                    BleScanCallback.setContextFromDeviceMainAct(this@DeviceMainActivity, recycleDeviceList)
                } else {
                    LightBleScanCallback.setContext(this@DeviceMainActivity, recycleDeviceList)
                }

                deleteAdapter()
                bleService.continousTryConnectDeviceForUI()
            }
        }
    }

    private fun deleteAdapter() {
        searchingDeviceList.clear()

        if (!applicationVersionModeIsLight) {
            arrayOfFoundBTDevices.clear()
            arrayOfFoundBTDevices.removeAll { true }
            adapter = DeviceAdapter(contextIsDeviceMainAct, arrayOfFoundBTDevices)
            recycleDeviceList.adapter = adapter
            adapter.notifyDataSetChanged()
        } else {
            lightArrayOfFoundBTDevices.clear()
            lightArrayOfFoundBTDevices.removeAll { true }
            lightAdapter = LightDeviceAdapter(lightContextMain, lightArrayOfFoundBTDevices)
            recycleDeviceList.adapter = lightAdapter
            lightAdapter.notifyDataSetChanged()
        }

        if (recycleDeviceList.adapter != null) {
            recycleDeviceList.adapter!!.notifyDataSetChanged()
        }
    }

    /* google exit
     private fun singOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                revokeAccess()
            }
    }

    private fun revokeAccess() {
        mGoogleSignInClient.revokeAccess()
            .addOnCompleteListener(this) {
                name.text  = ""
                email.text = ""
            }
    }*/

    fun deviceNull() {
        if (connectedDevice) {
            bleService.serviceNotificationsSet(BluetoothServiceNotificationType.BATTERY_SERVICE.type, false)
            bleService.serviceNotificationsSet(BluetoothServiceNotificationType.HEARTRATE_SERVICE.type, false)
            bleService.serviceNotificationsSet(BluetoothServiceNotificationType.NORDIC_SERVICE.type, false)
            bleService.serviceNotificationsSet(BluetoothServiceNotificationType.DEVICEINFO_SERVICE.type, false)
        }
        mainProgressController(ProgressCommandType.HIDE.commandType, false)

        if (!applicationVersionModeIsLight) {
            id_loading_LAYOUT.visibility = View.VISIBLE

            /*Külön áltozóban kell maradjon hogy a callback- ide kötődjön ne íródjon felül*/
            val meteorUnpaired = if (EuronetMeteorSingleton.hasInstance()) {
                EuronetMeteorSingleton.getInstance()
            } else {
                EuronetMeteorSingleton.createInstance(this, "ws://${AccountGeneral.mHost}:3000/websocket", InMemoryDatabase())
            }

            if (!meteorUnpaired.isConnected) {
                Log.d(TAGUNPAIR + "   " + TAG, "not Connected")
                meteorUnpaired.connect()
                Log.d(TAGUNPAIR + "   " + TAG, "call connect")
            } else {
                Log.d(TAGUNPAIR + "   " + TAG, "isConnected")
                pushServerUnPaired(meteorUnpaired)
            }

            meteorUnpaired.addCallback(object : MeteorCallback {
                override fun onConnect(signedInAutomatically: Boolean) {
                    Log.d(TAGUNPAIR + "   " + TAG, "onConnect")
                    pushServerUnPaired(meteorUnpaired)
                }

                override fun onDataAdded(collectionName: String?, documentID: String?, newValuesJson: String?) {
                    val mCollectionName = "onDataAdded $collectionName"
                    val mDocumentID = "onDataAdded $documentID"
                    val mNewValuesJson = "onDataAdded$newValuesJson"

                    Log.d(TAGUNPAIR + "   " + TAG, mCollectionName)
                    Log.d(TAGUNPAIR + "   " + TAG, mDocumentID)
                    Log.d(TAGUNPAIR + "   " + TAG, mNewValuesJson)
                }

                override fun onDataRemoved(collectionName: String?, documentID: String?) {
                    val mCollectionName = "onDataRemoved $collectionName"
                    val mDocumentID = "onDataRemoved $documentID"

                    Log.d(TAGUNPAIR + "   " + TAG, mCollectionName)
                    Log.d(TAGUNPAIR + "   " + TAG, mDocumentID)
                }

                override fun onException(e: Exception?) {
                    val error = "onError error" + e!!.message
                    Log.d(TAGUNPAIR + "   " + TAG, error)
                }

                override fun onDisconnect() {
                    val ondisconnect = "onDisconnect"
                    Log.d(TAGUNPAIR + "   " + TAG, ondisconnect)
                }

                override fun onDataChanged(collectionName: String?, documentID: String?, updatedValuesJson: String?, removedValuesJson: String?) {
                    val mCollectionName = "mCollectionName--> $collectionName"
                    val mDocumentID = "mDocumentID--> $documentID"
                    val mNewValuesJson = "mNewValuesJson--> $updatedValuesJson"
                    val mRemovedValuesJson = "mRemovedValuesJson--> $removedValuesJson"
                }
            })

        } else {

            val deleteMyDeviceId = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            deleteMyDeviceId.edit()
                    .putString(KEY_DEVICE_ID, null)
                    .apply()

            val deleteMyDeviceMacID = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            deleteMyDeviceMacID.edit()
                    .putString(KEY_HWMAC_ID, null)
                    .apply()

            /*
 */
            val deleteFW = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            deleteFW.edit()
                    .putString(KEY_FW_VERSION, null)
                    .apply()

            val deleteMan = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            deleteMan.edit()
                    .putString(KEY_MANUFACTURER_NAME_STRING, null)
                    .apply()

            val deleteDevName = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            deleteDevName.edit()
                    .putString(KEY_DEVICENAME, null)
                    .apply()

            val deleteModel = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            deleteModel.edit()
                    .putString(KEY_MODEL_NUMBER_STRING, null)
                    .apply()

            val deleteHW = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            deleteHW.edit()
                    .putString(KEY_HARDWARE_VERSION, null)
                    .apply()

            val deleteSerrial = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            deleteSerrial.edit()
                    .putString(KEY_SERIAL_NUMBER_STRING, null)
                    .apply()

            val deleteDeviceImage = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            deleteDeviceImage.edit()
                    .putString(KEY_DEVICEIMAGE, null)
                    .apply()

            if (connectedDevice) {
                connectedDevice = false
                if (::bleService.isInitialized) {
                    bleService.serviceNotificationsSet(BluetoothServiceNotificationType.BATTERY_SERVICE.type, false)
                    bleService.serviceNotificationsSet(BluetoothServiceNotificationType.HEARTRATE_SERVICE.type, false)
                    bleService.serviceNotificationsSet(BluetoothServiceNotificationType.NORDIC_SERVICE.type, false)
                    bleService.serviceNotificationsSet(BluetoothServiceNotificationType.DEVICEINFO_SERVICE.type, false)
                    bleService.unSubscribeTriangleSignal()
                    bleService.updateNotification("connecting: false measurement active: false")
                    bleService.getGatt().close()
                }
            }
            recycleDeviceList.adapter = null
        }
        showFragment(deviceInfoFragment, FragmentDeviceScreenPage.TAG, false)
    }

    /**
     * push Server UnPaired
     *
     * @param meteor
     */
    @Synchronized
    private fun pushServerUnPaired(meteorUnpaired: EuronetMeteorSingleton) {
        id_loading_LAYOUT.visibility = View.VISIBLE
        Toast.makeText(this, "Please wait unpaired..", Toast.LENGTH_LONG).show()

        if (!applicationVersionModeIsLight) {
            /*ServerCommunicate*/
            Log.d(TAGUNPAIR, "$TAG  -->meteor.run ")

            val methodGet = "devices.get"
            val paramsGet = Array(1) { "" }

            val myDeviceId = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(
                    KEY_DEVICE_ID, null)

            if (myDeviceId != null) {
                paramsGet[0] = myDeviceId

                meteorUnpaired.call(methodGet, paramsGet, object : ResultListener {
                    override fun onSuccess(result: String?) {
                        try {
                            Log.d(TAGUNPAIR, "$TAG  --> onSuccess result $result ")
                            val notValidResult = "[]"
                            if (!result.isNullOrEmpty() && result != notValidResult) {

                                val REVIEW_TYPE = object : TypeToken<MultipleDeviceInfo>() {}.type
                                val gson = Gson()
                                val dataMultipleList: MultipleDeviceInfo = gson.fromJson(result, REVIEW_TYPE)

                                serverPushUnpairMethod(meteorUnpaired, dataMultipleList)
                            }
                        } catch (e: Exception) {
                            Log.d(TAGUNPAIR, "$TAG  --> ${e.message}")
                            mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                        }
                    }

                    override fun onError(error: String?, reason: String?, details: String?) {
                        Log.d(TAGUNPAIR, "$TAG  --> error $error ")
                        Log.d(TAGUNPAIR, "$TAG  --> error $reason ")
                        Log.d(TAGUNPAIR, "$TAG  --> error $details ")
                        mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                        id_loading_LAYOUT.visibility = View.GONE
                    }
                })
            }
        }
    }

    /**
     * server Push Unpair Method
     *
     * @param meteor
     * @param dataMultipleList
     */
    private fun serverPushUnpairMethod(meteorUnpaired: EuronetMeteorSingleton, dataMultipleList: MultipleDeviceInfo) {
        if (!applicationVersionModeIsLight) {
            val method = "devices.release"
            val params = Array(4) { "" }
            params[0] = dataMultipleList.id
            params[1] = dataMultipleList.seq.toString()
            params[2] = dataMultipleList.serialnumber
            params[3] = dataMultipleList.hw_mac_id

            Log.d(TAGUNPAIR, "$TAG  --> device.release function params: ${params[0]}  ${params[1]}  ${params[2]}  ${params[3]}")

            meteorUnpaired.call(method, params, object : ResultListener {
                override fun onSuccess(result: String?) {
                    try {
                        Log.d(TAGUNPAIR, "$TAG  --> onSuccess result $result ")
                        //sikeres volt a leválasztás a szerveren akkor locálisan is eldobjuk
                        val deleteMyDeviceId = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        deleteMyDeviceId.edit()
                                .putString(KEY_DEVICE_ID, null)
                                .apply()

                        val deleteMyDeviceMacID = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        deleteMyDeviceMacID.edit()
                                .putString(KEY_HWMAC_ID, null)
                                .apply()

                        /* val deleteUserID = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                         deleteUserID.edit()
                                 .putString(KEY_USERID, null)
                                 .apply()*/

                        searchingDeviceList.clear()
                        continuousUnPairWork()
                    } catch (e: Exception) {
                        Log.d(TAGUNPAIR, "$TAG  --> ${e.message}")
                        id_loading_LAYOUT.visibility = View.GONE
                        mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                    }
                }

                override fun onError(error: String?, reason: String?, details: String?) {
                    Log.d(TAGUNPAIR, "$TAG  --> error $error ")
                    Log.d(TAGUNPAIR, "$TAG  --> error $reason ")
                    Log.d(TAGUNPAIR, "$TAG  --> error $details ")
                    id_loading_LAYOUT.visibility = View.GONE
                    mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                }
            })
        }
    }

    private fun continuousUnPairWork() {
        if (!applicationVersionModeIsLight) {
            Log.d(TAGUNPAIR, "$TAG  --> has own device null?: ${getHasOwnDevice()}")
            Toast.makeText(this, "Device is unpaired!", Toast.LENGTH_LONG).show()
            scanAndPaidProgressController(ScanAndPairProgressCommandType.HIDE.commandType, "")
            showFragment(homeFragment, FragmentDeviceHomePage.TAG, false)
            if (::bleService.isInitialized) {
                // bleService.getGatt().disconnect()
                Log.d(TAGUNPAIR, "$TAG  -->  bleService.getGatt().close()")
                bleService.serviceNotificationsSet(BluetoothServiceNotificationType.BATTERY_SERVICE.type, false)
                bleService.serviceNotificationsSet(BluetoothServiceNotificationType.HEARTRATE_SERVICE.type, false)
                bleService.serviceNotificationsSet(BluetoothServiceNotificationType.NORDIC_SERVICE.type, false)
                bleService.serviceNotificationsSet(BluetoothServiceNotificationType.DEVICEINFO_SERVICE.type, false)
                bleService.unSubscribeTriangleSignal()
                bleService.updateNotification("connecting: false measurement active: false")
                bleService.getGatt().close()
            }
            connectedDevice = false
            recycleDeviceList.adapter = null
            id_loading_LAYOUT.visibility = View.GONE
        }
    }

    /**
     * get Has Own Device
     *
     * @return device mac id or null
     */
    fun getHasOwnDevice(): String? {
        var haveDevice = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(
                KEY_DEVICE_ID, null)
        if (haveDevice != null && haveDevice.isEmpty()) {
            //check is not ""
            haveDevice = null
        }
        Log.d(TAG, "haveDevice $haveDevice")
        return haveDevice
    }

    fun serverNotConnected() {
        if (!applicationVersionModeIsLight) {
            Log.d(TAG, "serverNotConnected()   run")
            scanAndPaidProgressController(ScanAndPairProgressCommandType.SERVERNOTAVAILABLE.commandType, getString(ScanAndPairProgressCommandType.SERVERNOTAVAILABLE.message))
        }
    }

    fun serverConnected() {
        if (!applicationVersionModeIsLight) {
            Log.d(TAG, "server conn start")
            // scanAndPaidProgressController(ScanAndPairProgressCommandType.SERVERCONNECT.commandType, "")
            // if (recycleDeviceList.size != 0) {
            scanAndPaidProgressController(ScanAndPairProgressCommandType.SCANSTART.commandType, "")

            BleScanCallback.setContextFromDeviceMainAct(this@DeviceMainActivity, recycleDeviceList)
            if (::bleService.isInitialized) {
                bleService.continousTryConnectDeviceForUI()
            }
            //}
        }
    }

    fun refreshingFalse() {
        //swiperefresh.isRefreshing = false
        //swiperefresh.isEnabled = false
    }

    fun refreshingEnable() {
        //swiperefresh.isEnabled = true
    }

    @SuppressLint("RestrictedApi")
    fun bluetoothChange() {
        if (::bleService.isInitialized) {
            if (bleService.getBluetoothAdapter()!!.state == BluetoothAdapter.STATE_OFF || bleService.getBluetoothAdapter()!!.state == BluetoothAdapter.STATE_TURNING_OFF) {
                // The user bluetooth is already disabled.
                scanAndPaidProgressController(ScanAndPairProgressCommandType.BLUETOOTHDISABLED.commandType, "")
            }
        } else {
            scanAndPaidProgressController(ScanAndPairProgressCommandType.BLUETOOTHDISABLED.commandType, "")
        }
    }

    /**
     * connect Gatt
     *
     * @param iChooseThisDevice
     */
    override fun connectGatt(iChooseThisDevice: Array<MultipleDeviceInfo>) {
        if (::bleService.isInitialized) {
            scanAndPaidProgressController(ScanAndPairProgressCommandType.SCANSTOP.commandType, "")
        }

        scanAndPaidProgressController(ScanAndPairProgressCommandType.GATTCONNECTING.commandType, "")

        pairWithThisDevice(iChooseThisDevice)
    }

    /**
     * connect Gatt
     *
     * @param deviceAddress
     */
    override fun connectGatt(deviceAddress: String) {
        scanAndPaidProgressController(ScanAndPairProgressCommandType.GATTCONNECTING.commandType, "")
        if (::bleService.isInitialized) {
            try {
                bleService.getConnectorThread().bleConnectorThreadStop()
                Log.d(TAG, "connectGatt---->     bleService.getConnectorThread().bleConnectorThreadStop()")
            } catch (e: KotlinNullPointerException) {
                Log.d(TAG, "connectGatt---->   $e")
            }

            bleService.stopScan()
            bleService.connectWithThisDevice(deviceAddress)
        }
    }

    /**
     * pair With This Device
     *
     * @param iChooseThisDevice
     */
    private fun pairWithThisDevice(iChooseThisDevice: Array<MultipleDeviceInfo>) {
        if (!applicationVersionModeIsLight) {

            val device_id = iChooseThisDevice[0].id
            val seq = iChooseThisDevice[0].seq
            val serialnumber = iChooseThisDevice[0].serialnumber
            val hw_mac_id = iChooseThisDevice[0].hw_mac_id

            val params = Array(4) { "" }
            params[0] = device_id
            params[1] = seq.toString()
            params[2] = serialnumber
            params[3] = hw_mac_id

            Log.d(TAGPAIR, "$TAG  --> pairWithThisDevice run ${params[0]}, ${params[1]}, ${params[2]} ,${params[3]}")

            val methodPair = "devices.pair"

            if (meteor.isConnected) {
                //be vagyunk jelentkezve
                meteor.call(methodPair, params, object : ResultListener {
                    override fun onSuccess(result: String?) {
                        try {
                            Log.d(TAGPAIR, "$TAG  --> onSuccess result $result  $hw_mac_id")
                            when {
                                result!!.toBoolean() -> {
                                    Log.d(TAGPAIR, "$TAG  --> result true  $hw_mac_id")
                                    //Szerveren sikeres volt a párosítás
                                    continuousPairWork(iChooseThisDevice)
                                }
                                else -> {
                                    Log.d(TAGPAIR, "$TAG  --> onError pair server result $result  $hw_mac_id")
                                    mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                                    scanAndPaidProgressController(ScanAndPairProgressCommandType.SERVERNOTAVAILABLE.commandType, "Unknow error")
                                }
                            }
                        } catch (e: Exception) {
                            Log.d(TAGPAIR, "$TAG  -->  ${e.message} $hw_mac_id")
                            mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                            scanAndPaidProgressController(ScanAndPairProgressCommandType.SERVERNOTAVAILABLE.commandType, "Unknow error!")
                        }
                    }

                    override fun onError(error: String?, reason: String?, details: String?) {
                        Log.d(TAGPAIR, "$TAG  --> error $error  $hw_mac_id")
                        Log.d(TAGPAIR, "$TAG  --> error $reason  $hw_mac_id")
                        Log.d(TAGPAIR, "$TAG  --> error $details  $hw_mac_id")
                        mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                        scanAndPaidProgressController(ScanAndPairProgressCommandType.SERVERNOTAVAILABLE.commandType, "$reason")
                    }
                })
                /* }
             } catch (e: Exception) {
                 Log.d(TAGPAIR, "$TAG  -->  ${e.message} $hw_mac_id")
             }
         }

         override fun onError(error: String?, reason: String?, details: String?) {
             Log.d(TAGPAIR, "$TAG  --> error $error  $hw_mac_id")
             Log.d(TAGPAIR, "$TAG  --> error $reason  $hw_mac_id")
             Log.d(TAGPAIR, "$TAG  --> error $details  $hw_mac_id")
         }
     })*/
            } else {

                meteor.connect()
                meteor.addCallback(object : MeteorCallback {
                    override fun onConnect(signedInAutomatically: Boolean) {
                        meteor.call(methodPair, params, object : ResultListener {
                            override fun onSuccess(result: String?) {
                                try {
                                    scanAndPaidProgressController(ScanAndPairProgressCommandType.GATTCONNECTING.commandType, "Please wait..")
                                    Log.d(TAGPAIR, "$TAG  --> onSuccess result $result  $hw_mac_id")
                                    when {
                                        result!!.toBoolean() -> {
                                            Log.d(TAGPAIR, "$TAG  --> result true  $hw_mac_id")
                                            //Szerveren sikeres volt a párosítás
                                            continuousPairWork(iChooseThisDevice)
                                        }
                                        else -> {
                                            Log.d(TAGPAIR, "$TAG  --> onError pair server result $result  $hw_mac_id")
                                            mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                                            scanAndPaidProgressController(ScanAndPairProgressCommandType.SERVERNOTAVAILABLE.commandType, "Unknow error")
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.d(TAGPAIR, "$TAG  -->  ${e.message} $hw_mac_id")
                                    mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                                    scanAndPaidProgressController(ScanAndPairProgressCommandType.SERVERNOTAVAILABLE.commandType, "Unknow error!")
                                }
                            }

                            override fun onError(error: String?, reason: String?, details: String?) {
                                Log.d(TAGPAIR, "$TAG  --> error $error  $hw_mac_id")
                                Log.d(TAGPAIR, "$TAG  --> error $reason  $hw_mac_id")
                                Log.d(TAGPAIR, "$TAG  --> error $details  $hw_mac_id")
                                mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                                scanAndPaidProgressController(ScanAndPairProgressCommandType.SERVERNOTAVAILABLE.commandType, "$reason")
                            }
                        })
                    }

                    override fun onDataAdded(collectionName: String?, documentID: String?, newValuesJson: String?) {
                        val mCollectionName = "onDataAdded $collectionName"
                        val mDocumentID = "onDataAdded $documentID"
                        val mNewValuesJson = "onDataAdded$newValuesJson"

                        Log.d(TAGPAIR, mCollectionName)
                        Log.d(TAGPAIR, mDocumentID)
                        Log.d(TAGPAIR, mNewValuesJson)
                    }

                    override fun onDataRemoved(collectionName: String?, documentID: String?) {
                        val mCollectionName = "onDataRemoved $collectionName"
                        val mDocumentID = "onDataRemoved $documentID"

                        Log.d(TAGPAIR, mCollectionName)
                        Log.d(TAGPAIR, mDocumentID)
                    }

                    override fun onException(e: Exception) {
                        val onexception = "onException ${e.message}"
                        Log.d(TAGPAIR, onexception)
                    }

                    override fun onDisconnect() {
                        val ondisconnect = "onDisconnect"
                        Log.d(TAGPAIR, ondisconnect)
                    }

                    override fun onDataChanged(collectionName: String?, documentID: String?, updatedValuesJson: String?, removedValuesJson: String?) {
                        val mCollectionName = "onDataChanged $collectionName"
                        val mDocumentID = "onDataChanged $documentID"
                        val mNewValuesJson = "onDataChanged $updatedValuesJson"
                        val mRemovedValuesJson = "onDataChanged $removedValuesJson"

                        Log.d(TAGPAIR, mCollectionName)
                        Log.d(TAGPAIR, mDocumentID)
                        Log.d(TAGPAIR, mNewValuesJson)
                        Log.d(TAGPAIR, mRemovedValuesJson)
                    }
                })

                Log.d(TAGPAIR, "$TAG  --> error meteor is not connected $hw_mac_id")
                mainProgressController(ProgressCommandType.SERVERERROR.commandType, true)
                if (id_scanLayout.visibility == View.VISIBLE) {
                    scanAndPaidProgressController(ScanAndPairProgressCommandType.SERVERNOTAVAILABLE.commandType, "Please wait..")
                }
            }
        } else {
            continuousPairWork(iChooseThisDevice)
        }
    }

    /**
     * continuous Pair Work
     *
     * @param iChooseThisDevice
     */
    private fun continuousPairWork(iChooseThisDevice: Array<MultipleDeviceInfo>) {
        bleService.connectWithThisDevice(null, iChooseThisDevice)
    }

    fun errorLicense() {
        mainProgressController(ProgressCommandType.HIDE.commandType, false)
        val dialogExit = SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Error")
                .setContentText("Unknown error!")
                .setConfirmText("OK")
                .setConfirmClickListener {
                    it.dismissWithAnimation()

                    exitApplication()
                }
        dialogExit.setCancelable(false)
        dialogExit.show()
    }

    /**
     * device Connected
     *
     */
    fun deviceConnected() {
        scanAndPaidProgressController(ScanAndPairProgressCommandType.HIDE.commandType, "")

        if (::bleService.isInitialized && bleService.getArriveingData()) {
            Log.d(TAG, " reconnect esemény device reconn - > elindítjuk a szerviceket")
            mainProgressController(ProgressCommandType.NORDICACTIVE.commandType, false)

            //unbindService(bleServiceConnection)
            bleService.serviceNotificationsSet(BluetoothServiceNotificationType.BATTERY_SERVICE.type, true)
            bleService.serviceNotificationsSet(BluetoothServiceNotificationType.HEARTRATE_SERVICE.type, true)
            bleService.serviceNotificationsSet(BluetoothServiceNotificationType.NORDIC_SERVICE.type, true)
            bleService.serviceNotificationsSet(BluetoothServiceNotificationType.DEVICEINFO_SERVICE.type, true)

            homeFragment.startViewElements()
        } else {
            mainProgressController(ProgressCommandType.CONNECT.commandType, true)
        }
    }


    private fun firmwareCheck() {
        Log.d("DFUManager", " firmwareCheck call")
        val connManager: ConnectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val allNetworks = connManager.allNetworks
        if (!allNetworks.isNullOrEmpty()) {
            allNetworks.forEach {
                if (it != null && connManager.getNetworkInfo(it).isConnected) {
                    Log.d("DFUManager", " network is ok! ${allNetworks.size}")
                    phoneBatteryCheck()
                    return
                }
            }
        }
    }

    /**
     * phone Battery Check, device battery check
     *
     */
    private fun phoneBatteryCheck() {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        if (batteryManager.isCharging || batteryLevel >= MIN_BATTERY_VALUE) {
            Log.d("DFUManager", " phone battery is ok! device battery: $deviceBatteryValue")
            if (deviceBatteryValue > MIN_BATTERY_VALUE) {
                Log.d("DFUManager", " device battery ok!")
                mainProgressController(ProgressCommandType.HIDE.commandType, false)

                Handler().postDelayed({
                    dfuThread = DFUManager(this, bleService)
                    dfuThread.start()
                }, 2000)
            }
        }
    }

    fun deviceDisconnected() {
        mainProgressController(ProgressCommandType.DISCONNECT.commandType, false)
        homeFragment.pauseViewElements()
    }

    fun getBleServiceReference(): BluetoothLeService? {
        if (::bleService.isInitialized) {
            return bleService
        } else {
            return null
        }
    }

    /**
     * main Progress Controller
     *
     * @param commandType
     * @param handler
     */
    fun mainProgressController(commandType: Int, handler: Boolean) {
        Log.d(TAG, "mainProgressController run   handler: $handler ")
        when (commandType) {
            ProgressCommandType.HIDE.commandType -> {
                id_connect_status_PROGRESSBAR.visibility = View.GONE
                id_connect_status_TEXT.visibility = View.GONE
            }
            ProgressCommandType.CONNECT.commandType -> {
                //  id_connect_LAYOUT.visibility = View.VISIBLE
                id_connect_status_PROGRESSBAR.visibility = View.GONE
                id_connect_status_TEXT.setTextColor(ContextCompat.getColor(this@DeviceMainActivity, ProgressCommandType.CONNECT.color))
                id_connect_status_TEXT.text = getString(ProgressCommandType.CONNECT.message)
            }
            ProgressCommandType.DISCONNECT.commandType -> {
                //     id_connect_LAYOUT.visibility = View.VISIBLE
                id_connect_status_TEXT.setTextColor(ContextCompat.getColor(this@DeviceMainActivity, ProgressCommandType.DISCONNECT.color))
                id_connect_status_TEXT.text = getString(ProgressCommandType.DISCONNECT.message)
                id_connect_status_PROGRESSBAR.indeterminateTintList = ColorStateList.valueOf(ContextCompat.getColor(this, ProgressCommandType.DISCONNECT.color))
            }
            ProgressCommandType.SERVERERROR.commandType -> {
                // if (id_scanLayout.visibility == View.GONE) {
                //      id_connect_LAYOUT.visibility = View.VISIBLE
                id_connect_status_PROGRESSBAR.visibility = View.GONE
                id_connect_status_TEXT.visibility = View.GONE
                id_connect_status_TEXT.text = getString(ProgressCommandType.SERVERERROR.message)
                id_connect_status_TEXT.setTextColor(ContextCompat.getColor(this@DeviceMainActivity, ProgressCommandType.SERVERERROR.color))
                //  }
            }
            ProgressCommandType.RECONNECT.commandType -> {
                //    id_connect_LAYOUT.visibility = View.VISIBLE
                id_connect_status_PROGRESSBAR.visibility = View.VISIBLE
                id_connect_status_TEXT.visibility = View.VISIBLE
                id_connect_status_PROGRESSBAR.indeterminateTintList = ColorStateList.valueOf(ContextCompat.getColor(this, ProgressCommandType.RECONNECT.color))
                id_connect_status_TEXT.text = getString(ProgressCommandType.RECONNECT.message)
                id_connect_status_TEXT.setTextColor(ContextCompat.getColor(this@DeviceMainActivity, ProgressCommandType.RECONNECT.color))

                if (::bleService.isInitialized) bleService.continousTryConnectDevice()
            }
            ProgressCommandType.NORDICACTIVE.commandType -> {
                //      id_connect_LAYOUT.visibility = View.VISIBLE
                id_connect_status_TEXT.text = getString(ProgressCommandType.NORDICACTIVE.message)
                id_connect_status_TEXT.setTextColor(ContextCompat.getColor(this@DeviceMainActivity, ProgressCommandType.NORDICACTIVE.color))
                id_connect_status_PROGRESSBAR.visibility = View.VISIBLE
                id_connect_status_TEXT.visibility = View.VISIBLE
                id_connect_status_PROGRESSBAR.indeterminateTintList = ColorStateList.valueOf(ContextCompat.getColor(this@DeviceMainActivity, ProgressCommandType.NORDICACTIVE.color))
            }
        }

        if (handler) {
            val mHandler = Handler()
            mHandler.postDelayed({
                Log.d(TAG, "mainProgressController----> handler run")
                //swiperefresh.isRefreshing = false
                //    id_connect_LAYOUT.visibility = View.GONE
                id_connect_status_PROGRESSBAR.visibility = View.GONE
                id_connect_status_TEXT.visibility = View.GONE
            }, 1500)
        }
    }

    fun scanAndPaidProgressController(scanAndPairCommandType: Int, message: String) {
        when (scanAndPairCommandType) {
            ScanAndPairProgressCommandType.HIDE.commandType -> {
                Log.d(TAG, "HIDE---->   run")
                id_scanLayout.visibility = View.GONE
                id_syncDeviceStartStop.isClickable = true
            }
            ScanAndPairProgressCommandType.SCANSTART.commandType -> {
                if (getHasOwnDevice() == null) {
                    Log.d(TAG, "SCANSTART---->   run")
                    id_scanLayout.visibility = View.VISIBLE
                    id_progress_tx.visibility = View.VISIBLE
                    id_scanLayoutProgressBar.visibility = View.VISIBLE

                    id_progress_tx.text = getString(R.string.search_device)
                    id_syncDeviceStartStop.setImageResource(R.drawable.ic_action_playback_stop)
                    bleService.continousTryConnectDeviceForUI()
                }
            }
            ScanAndPairProgressCommandType.SCANSTOP.commandType -> {
                Log.d(TAG, "SCANSTOP---->   run")
                //id_scanLayout.visibility = View.VISIBLE
                try {
                    bleService.getConnectorThread().bleConnectorThreadStop()
                    Log.d(TAG, "SCANSTOP---->     bleService.getConnectorThread().bleConnectorThreadStop()")
                } catch (e: KotlinNullPointerException) {
                    Log.d(TAG, "SCANSTOP---->   $e")
                }

                bleService.stopScan()
                id_scanLayoutProgressBar.visibility = View.GONE
                id_progress_tx.visibility = View.GONE
                id_progress_tx.text = "Scan stop"
                id_syncDeviceStartStop.setImageResource(R.drawable.ic_sync)
            }
            ScanAndPairProgressCommandType.GATTCONNECTING.commandType -> {
                Log.d(TAG, "GATTCONNECTING---->  run")
                //   id_scanLayout.visibility = View.VISIBLE
                id_progress_tx.visibility = View.VISIBLE
                id_scanLayoutProgressBar.visibility = View.VISIBLE

                id_progress_tx.text = getString(ScanAndPairProgressCommandType.GATTCONNECTING.message)
                id_syncDeviceStartStop.isClickable = false
                id_syncDeviceStartStop.setImageResource(R.drawable.ic_sync)
            }
            ScanAndPairProgressCommandType.SERVERCONNECT.commandType -> {
                Log.d(TAG, "SERVERCONNECT---->  run")
                //     id_scanLayout.visibility = View.VISIBLE
                id_progress_tx.visibility = View.VISIBLE
                id_scanLayoutProgressBar.visibility = View.VISIBLE
                id_syncDeviceStartStop.isClickable = false
                id_progress_tx.text = getString(ScanAndPairProgressCommandType.SERVERCONNECT.message)
                id_syncDeviceStartStop.setImageResource(R.drawable.ic_sync)
            }
            ScanAndPairProgressCommandType.SERVERNOTAVAILABLE.commandType -> {
                Log.d(TAG, "SERVERNOTAVAILABLE----> run")
                try {
                    bleService.getConnectorThread().bleConnectorThreadStop()
                    Log.d(TAG, "SCANSTOP---->     bleService.getConnectorThread().bleConnectorThreadStop()")
                } catch (e: KotlinNullPointerException) {
                    Log.d(TAG, "SCANSTOP---->   $e")
                }

                bleService.stopScan()
                // id_scanLayout.visibility = View.VISIBLE
                id_syncDeviceStartStop.isClickable = true
                id_scanLayoutProgressBar.visibility = View.GONE
                id_progress_tx.text = message
                id_progress_tx.visibility = View.VISIBLE
                id_syncDeviceStartStop.setImageResource(R.drawable.ic_sync)
            }
            ScanAndPairProgressCommandType.BLUETOOTHDISABLED.commandType -> {
                Log.d(TAG, "BLUETOOTHDISABLED---->run")

                Log.d(TAGSCAN, " The user bluetooth is already disabled.")
                id_progress_tx.visibility = View.GONE
                id_syncDeviceStartStop.setImageResource(R.drawable.ic_sync)

                if (getHasOwnDevice() == null) {
                    id_scanLayout.visibility = View.VISIBLE
                    id_scanLayoutProgressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Ondestroy call, deviceMain")
        val prefNOStop = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("runAllways", false)

        if (!prefNOStop) {
            Log.d(TAG, "Ondestroy call, bleondestroy")
            unbindService(bleServiceConnection)
            if (::bleService.isInitialized) {
                if (connectedDevice) {
                    bleService.serviceNotificationsSet(BluetoothServiceNotificationType.BATTERY_SERVICE.type, false)
                    bleService.serviceNotificationsSet(BluetoothServiceNotificationType.HEARTRATE_SERVICE.type, false)
                    bleService.serviceNotificationsSet(BluetoothServiceNotificationType.NORDIC_SERVICE.type, false)
                    bleService.serviceNotificationsSet(BluetoothServiceNotificationType.DEVICEINFO_SERVICE.type, false)

                    bleService.ibiThreadRun(false)
                    bleService.imuThreadRun(false)
                    bleService.hrThreadRun(false)
                    bleService.stepThreadRun(false)
                    bleService.ppgThreadRun(false)
                    bleService.getGatt().close()
                    bleService.unSubscribeTriangleSignal()
                }
                bleService.onDestroy()
            }
        }

        super.onDestroy()
    }

    fun dfuStart(dfuWorkManager: DFUManager) {
        val WAITING_INTERVAL_USER: Long = 10000
        var resultOK: Boolean? = null

        Log.d("DFUManager", "dfuStartcall ")

        val dialogDFU = SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Update device's firmware")
                .setContentText("Available new firmeare version. Would you like to update now?")
                .setConfirmText("Yes")
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    resultOK = true
                    val intent = Intent(this, DFUUpdateActivity::class.java)
                    startActivity(intent)
                    dfuWorkManager.userResponse()
                }
                .setCancelText("No")
                .setCancelClickListener {
                    it.dismissWithAnimation()
                    resultOK = false
                    dfuThread.interrupt()
                }

        dialogDFU.setCancelable(false)
        dialogDFU.show()

        val handler = Handler()
        handler.postDelayed({
            if (resultOK == null) {
                Log.d(TAG, "időkeret lejárt a user dfu frissítés kérdésére")
                dfuThread.interrupt()
                dialogDFU.dismissWithAnimation()
            }
        }, WAITING_INTERVAL_USER)
    }

    fun dfuUpdateSuccessfully() {
        mainProgressController(ProgressCommandType.RECONNECT.commandType, true)
        tryConnectDevice()
    }

    fun dfuThreadStop() {
        Log.d(TAG, "dfu thread interupt")
        dfuThread.interrupt()
    }

    fun getQuestionnaireReferenc(): FragmentDeviceQuestionnairePage {
        return questionnairePageFragment
    }

    override fun onBackPressed() {

        val ft = supportFragmentManager
        val fragment = ft.findFragmentById(R.id.questionnaireFrameLayout)
        if (fragment != null) {
            questionnairePageFragment.backToQuestionnaireList()
        } else {
            if (id_scanLayout.visibility == View.VISIBLE) {
                scanAndPaidProgressController(ScanAndPairProgressCommandType.SCANSTOP.commandType, "")
                scanAndPaidProgressController(ScanAndPairProgressCommandType.HIDE.commandType, "")
            } else {
                val backstackCount = supportFragmentManager.backStackEntryCount
                Log.d(TAG, "$backstackCount")
                if (backstackCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    exitApp()
                }
            }
        }
    }

    fun getDeviceBatteryValue(): Int {
        return deviceBatteryValue
    }

    /**
     * Image Base64 decode
     *
     * @return
     */
    private fun String.decode(): String {
        return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
    }

    fun setTriangleValue(weariness: Int) {
        if (weariness <= 20) {
            id_weariness_top_text.visibility = View.VISIBLE
            id_weariness_top_text.text = "Weariness $weariness%"
            //vibrate
            val vibrate: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrateArray = LongArray(3)
                vibrateArray[0]= 100
                vibrateArray[1]= 500
                vibrateArray[2]= 100
                vibrate.vibrate(VibrationEffect.createWaveform(vibrateArray,-1))
            } else {
                //deprecated in API 26
                vibrate.vibrate(500);
            }
        } else {
            id_weariness_top_text.visibility = View.GONE
        }
    }
}

