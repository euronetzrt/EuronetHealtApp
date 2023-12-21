package hu.aut.android.dm01_v11.ui.activities.startApp.loginActivities

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import hu.aut.android.dm01_v11.R
import hu.aut.android.dm01_v11.ui.activities.deviceActivities.DeviceMainActivity
import hu.aut.android.dm01_v11.ui.activities.startApp.PolicyActivity
import hu.euronetrt.okoskp.euronethealth.bluetooth.services.BluetoothLeService
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountManager.AbstractAccountAuth
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral.KEY_ERROR_MESSAGE
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral.KEY_ERROR_SERVER
import hu.euronetrt.okoskp.euronethealth.login.loginBackend.accountObjects.AccountGeneral.mHost
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.loading_layout.*
import org.json.JSONException
import java.util.*
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    companion object {
        val TAG = "LoginActivity"
        val TAGLOGIN = "RunLogin"
        val ACTIVITY_REQUESTCODE = 105
        val ACTIVITY_REQUESTCODEPOLICY = 106
        val RC_SIGN_IN = 101
    }

    private lateinit var callbackManager: CallbackManager
    private lateinit var accessTokenTracker: AccessTokenTracker
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var mAccountManager: AccountManager? = null
    private var mAuthTokenType: String? = null
    private lateinit var username : TextInputEditText
    private lateinit var password : TextInputEditText

    private lateinit var abstractAuthClass: AbstractAccountAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Google and Facebook Button set disable
        login_button.isEnabled = false
        sign_in_button.isEnabled = false


        //ne töltse ki az adataim
        imageView.setOnClickListener {
           username.setText("dmiszori@euronetrt.hu")
           password.setText("Almafa1")
        }

        val imageString = if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(BluetoothLeService.KEY_USERIMAGE, null) == null) "" else PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(BluetoothLeService.KEY_USERIMAGE, "")
        if(!imageString.isNullOrEmpty() && imageString != ""){
            val decodeString = imageString.decode()
            val replaceImageString = decodeString.replace("data:image/jpeg;base64,", "")
            val base64 = Base64.decode(replaceImageString, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(base64, 0, base64.size)
            imageView.setImageBitmap(bitmap)
        }

        registred.setOnClickListener {
            val intentWebReg = Intent(Intent.ACTION_WEB_SEARCH)
            intentWebReg.putExtra(SearchManager.QUERY, "$mHost:4200")
            startActivity(intentWebReg)
        }

        abstractAuthClass = AbstractAccountAuth(this)
        mAccountManager = AccountManager.get(this)

        mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS

        username = id_username as TextInputEditText
        password = id_password as TextInputEditText

        val accountManager = AccountManager.get(this)
        val accounts = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)
        if (accounts.isNotEmpty()) {
            if (accounts[0].name != "") {
                username.setText(accounts[0].name)
            }
        }

        login.setOnClickListener {
            // username , pass check

            if (isFormValid()) {
                //   if(!((username as TextView).text).isEmpty() && !((password as TextView).text).isEmpty()){
                if (isValidEmailId(username.text.toString().trim())) {
                    progressBar.isIndeterminate = true
                    progressBar.indeterminateDrawable.setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.MULTIPLY)

                    if (accounts.size <= 1) {
                        createAccount()
                    } else {
                        Log.d(TAG, accounts.size.toString() + "   Több mint 1 account")
                    }
                } else {
                    username.error = "This field not valid!"
                }
            }
        }
        /*--------Facebook login--------*/

        login_button.setPermissions(Arrays.asList("email", "public_profile", "user_likes"))

        // Creating CallbackManager
        callbackManager = CallbackManager.Factory.create()
        // Registering CallbackManager with the LoginButton
        login_button!!.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "onSuccess")
            }

            override fun onCancel() {
                Log.d(TAG, "onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "onCancel ${error.message}")
                messageError()
            }
        })

        // Defining the AccessTokenTracker
        accessTokenTracker = object : AccessTokenTracker() {
            // This method is invoked everytime access token changes
            override fun onCurrentAccessTokenChanged(oldAccessToken: AccessToken?, currentAccessToken: AccessToken?) {
                // currentAccessToken is null if the user is logged out
                if (currentAccessToken != null) {
                    // AccessToken is not null implies user is logged in and hence we sen the GraphRequest
                    //     if (oldAccessToken != currentAccessToken) {
                    useLoginInformation(currentAccessToken)
                }
            }
        }
        /*--------Facebook login--------*/

        /*--------Google login--------*/
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set the dimensions of the sign-in button.
        val signInButton = sign_in_button
        signInButton.setSize(SignInButton.SIZE_STANDARD)

        sign_in_button.setOnClickListener {
            signIn()
        }
        /*--------Google login--------*/

    /*    val ofcListener: View.OnFocusChangeListener = MyFocusChangeListener(this)
        username.onFocusChangeListener = ofcListener
        password.onFocusChangeListener = ofcListener
        loginConstranintLayoutID.onFocusChangeListener = ofcListener
*/
        loginConstranintLayoutID.setOnClickListener {
            val imm : InputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun isFormValid(): Boolean {
        return when {
            username.text.isNullOrEmpty() -> {
                username.error = "This field can not be empty"
                false
            }
            password.text.isNullOrEmpty() -> {
                password.error = "This field can not be empty"
                false
            }
            else -> true
        }
    }

    private fun isValidEmailId(trim: String): Boolean {
        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(trim).matches()
    }


    /*--------Google login signIn--------*/
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun messageError() {
        Toast.makeText(this, "Please check your internet connections!", Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ACTIVITY_REQUESTCODE == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                /*Facebook logout*/
                LoginManager.getInstance().logOut()
                Toast.makeText(this, "logout!", Toast.LENGTH_LONG).show()
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == RC_SIGN_IN) {
            // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }else if (ACTIVITY_REQUESTCODEPOLICY == requestCode){
            if (resultCode == Activity.RESULT_OK) {
                Log.d("STARTSERVICE", "Activity.RESULT_OK  sendbroadcast startService")
                val intent = Intent(this, DeviceMainActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else{
            /* Log.d(TAG,"$requestCode")
                Log.d(TAG,"$resultCode")*/
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }


        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {
        try {
            val account = task!!.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            updateUI(account!!)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun useLoginInformation(accessToken: AccessToken) {
        /**
         * Creating the GraphRequest to fetch user details
         * 1st Param - AccessToken
         * 2nd Param - Callback (which will be invoked once the request is successful)
         */
        val request = GraphRequest.newMeRequest(
                accessToken
        ) { `object`, response ->
            //OnCompleted is invoked once the GraphRequest is successful
            try {
                Log.d(TAG, "json $response")
                val firstName = `object`.getString("first_name")
                val lastName = `object`.getString("last_name")
                val email = `object`.getString("email")
                val id = `object`.getString("id")
                val imageUrl = "https://graph.facebook.com/$id/picture?type=normal"

                Log.d(TAG, "json $firstName")
                Log.d(TAG, "json $lastName")
                Log.d(TAG, "json $email")
                Log.d(TAG, "json $response")
                Log.d(TAG, "json $imageUrl")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        // We set parameters to the GraphRequest using a Bundle.
        val parameters = Bundle()
        parameters.putString("fields", "id,name,email,picture.width(200)")
        request.parameters = parameters
        // Initiate the GraphRequest
        request.executeAsync()
    }

    public override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()

        /*--------Facebook login--------*/
        accessTokenTracker.startTracking()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            //Google auto sign
            updateUI(account)
        } else {
            //Facebook auto sign
            val accessToken = AccessToken.getCurrentAccessToken()
            if (accessToken != null) {
                Log.d(TAG, "modify accessToken")
                useLoginInformation(accessToken)
            }
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        Toast.makeText(this, "Login ok", Toast.LENGTH_LONG).show()
        Log.e(TAG, account.toString())
    }

    public override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        /*--------Facebook login--------*/
        // We stop the tracking before destroying the activity
        accessTokenTracker.stopTracking()
    }

    override fun onResume() {
        super.onResume()
        loading_layout_includetag_login.visibility = View.GONE
        login.visibility = View.VISIBLE
    }

    //-----------Account manager ----------
    /**
     * Create new account to the account manager
     */
    private fun createAccount() {
        addNewAccount(AccountGeneral.ACCOUNT_TYPE, mAuthTokenType, username.text.toString(), password.text.toString())
    }

    @Throws(Exception::class)
    private fun addNewAccount(accountType: String, mAuthTokenType: String?, username: String, pass: String) {

        val accountInfoBundle = Bundle()
        accountInfoBundle.putString("username", username)
        accountInfoBundle.putString("pass", pass)
        Log.d(TAGLOGIN, "$TAG $username")
        Log.d(TAGLOGIN, "$TAG $pass")
        mAccountManager!!.addAccount(accountType, mAuthTokenType, null, accountInfoBundle, this, { future ->
            /*handler this is*/
            try {
                Log.d(TAGLOGIN, "$TAG  mAccountManager  try")
                val bnd = future.result
                Log.d(TAGLOGIN, "$TAG responseIntent.extras LoginActivity/addNewAccount-->   $bnd")

                val error = (bnd[KEY_ERROR_SERVER]) as Boolean?
                val bigServerError = (bnd[KEY_ERROR_MESSAGE]) as String?
                val incorrectPass = (bnd[AccountGeneral.INCORRECT_PASS]) as Boolean?

                //hiba esetén lépj be!
                if ((error != null || error == true) || !bigServerError.isNullOrEmpty()) {
                    Log.d(TAGLOGIN, "$TAG KEY_ERROR_SERVER (nem elérhető elvileg )--> $error ,bigServerError.isNullOrEmpty()  ---> ${bigServerError.isNullOrEmpty()} tehát nem volt hiba a CreateAcManAsynkTask doinbackground feldolgozása közben ")
                    val account = mAccountManager!!.getAccountsByType(AccountGeneral.ACCOUNT_TYPE)
                    if(account.isNotEmpty()){
                        mAccountManager!!.getAuthToken(account[0], AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, null, this, { response ->
                            try {
                                Log.d(TAGLOGIN, "$TAG  mAccountManager!!.getAuthToken --->  try")
                                val tokenResponse = response.result
                                Log.d(TAGLOGIN, "$TAG  mAccountManager!!.getAuthToken  tokenResponse --->  $tokenResponse")
                                if (tokenResponse.isEmpty) {
                                    AlertDialog.Builder(this)
                                            .setTitle("Internet")
                                            .setMessage("Please connect the Internet and try again!!")
                                            .setCancelable(false)
                                            .setPositiveButton("ok") { dialogInterface: DialogInterface, _: Int ->
                                                dialogInterface.dismiss()
                                            }.show()
                                    // nem volt szerver komm és a token üres vagy lejárt vagy nics account
                                } else {
                                    //ha nem üres de nem volt net kapcsolat akkor mehet tovább az appba
                                    val myintent = Intent(this, PolicyActivity::class.java)
                                    startActivityForResult(myintent, ACTIVITY_REQUESTCODEPOLICY)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Log.d(TAGLOGIN, "$TAG getAuthToken error ${e.message}")
                            }
                        }, null)
                    }else{
                        AlertDialog.Builder(this)
                                .setTitle("Failed!")
                                .setMessage("Server is not available!")
                                .setCancelable(false)
                                .setPositiveButton("ok") { dialogInterface: DialogInterface, _: Int ->
                                    dialogInterface.dismiss()
                                }.show()
                    }
                } else {
                    if (incorrectPass == null || !incorrectPass) {
                        val myintent = Intent(this, PolicyActivity::class.java)
                        startActivityForResult(myintent, ACTIVITY_REQUESTCODE)
                    } else {
                        Log.d(TAGLOGIN, "$TAG incorrectPass")
                        AlertDialog.Builder(this)
                                .setTitle("Faild!")
                                .setMessage("Username or password incorrect!")
                                .setCancelable(false)
                                .setPositiveButton("ok") { dialogInterface: DialogInterface, _: Int ->
                                    dialogInterface.dismiss()
                                }.show()
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Log.d(TAGLOGIN, "$TAG mAccountManager!!.addAccoun -->  ${e.message}")
                Toast.makeText(this, "Please connect the Internet and try again!", Toast.LENGTH_LONG).show()
            }
        }, null)
    }

    /**
     * Image Base64 decode
     *
     * @return
     */
    private fun String.decode() : String{
        return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
    }
}


