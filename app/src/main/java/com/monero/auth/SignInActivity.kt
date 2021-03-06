package com.monero.auth

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.monero.Application.ApplicationController
import com.monero.BuildConfig
import com.monero.R
import com.monero.main.MainActivity
import java.util.*

class SignInActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 154 //the request code could be any Integer
    val auth = FirebaseAuth.getInstance()!!
    val TIME_INTERVAL:Long =2000
    var mBackPressed:Long=0
    lateinit var skipNowText:TextView
    lateinit var loginButton:Button
    lateinit var registerButton:Button


    fun showSnackbar(id : Int){
        Snackbar.make(findViewById(R.id.sign_in_container), resources.getString(id), Snackbar.LENGTH_LONG).show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        skipNowText = findViewById(R.id.skipBtn)
        loginButton =findViewById(R.id.loginBtn)
        registerButton = findViewById(R.id.registerBtn)

        skipNowText.setOnClickListener { v: View ->
            nextActivity()
        }

        loginButton.setOnClickListener { v:View ->
            login()
        }

        registerButton.setOnClickListener { v:View ->
            register()

        }

       /* if(auth.currentUser != null){ //If user is signed in
//                startActivity(Next Activity)
        }
        else {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                            .setAvailableProviders(
                                    Arrays.asList(AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                                            AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                            .setTosUrl("link to app terms and service")
                            .setPrivacyPolicyUrl("link to app privacy policy")
                            .build(),
                    RC_SIGN_IN)
        }*/
    }

    fun login(){
        var mainIntent = Intent(this,FireBaseAuthActivity::class.java)
        startActivity(mainIntent)
    }

    fun register(){
        var mainIntent = Intent(this,FireBaseAuthActivity::class.java)
        startActivity(mainIntent)
    }

    fun nextActivity(){
        var mainIntent = Intent(this,MainActivity::class.java)
        startActivity(mainIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN){
            /*
                this checks if the activity result we are getting is for the sign in
                as we can have more than activity to be started in our Activity.
             */
           // val response = IdpResponse.fromResultIntent(data)
            /*if(resultCode == Activity.RESULT_OK){
                *//*
                    Checks if the User sign in was successful
                 *//*
                ApplicationController.preferenceManager!!.myCredential = auth.currentUser!!.phoneNumber!!
                ApplicationController.preferenceManager!!.myUid = auth.currentUser!!.uid

                var mainIntent = Intent(this,MainActivity::class.java)
                startActivity(mainIntent)

                showSnackbar(R.string.signed_in)
                finish()
                return
            }
            else {
                if(response == null){
                    //If no response from the Server
                    showSnackbar(R.string.sign_in_cancelled)
                    return
                }
                if(response.error?.errorCode == ErrorCodes.NO_NETWORK){
                    //If there was a network problem the user's phone
                    showSnackbar(R.string.no_internet_connection)
                    return
                }
                if(response.error?.errorCode == ErrorCodes.UNKNOWN_ERROR){
                    //If the error cause was unknown
                    showSnackbar(R.string.unknown_error)
                    return
                }
            }*/
        }
        showSnackbar(R.string.unknown_sign_in_response) //if the sign in response was unknown
    }


    override fun onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            "Press back button again to exit".shortToast(this)
            mBackPressed = System.currentTimeMillis()
        }
    }

    fun Any.shortToast(context: Context) {
        Toast.makeText(context, this.toString(), Toast.LENGTH_SHORT).show()
    }
}
