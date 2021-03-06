package com.monero.auth

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.monero.BuildConfig
import com.monero.R
import com.monero.main.MainActivity
import java.util.*
import kotlin.collections.ArrayList



class FireBaseAuthActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 154 //the request code could be any Integer
    var auth = FirebaseAuth.getInstance()!!
    // var providers: ArrayList<AuthUI.IdpConfig>? = null
    fun showSnackbar(id: Int) {
        Snackbar.make(findViewById(R.id.sign_in_container), resources.getString(id), Snackbar.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fire_base_auth)


        /*AuthUI.IdpConfig.EmailBuilder().build()
         if(auth.currentUser != null){ //If user is signed in
                nextActivity()
       }
       else {
           startActivityForResult(
                   AuthUI.getInstance()
                           .createSignInIntentBuilder()
                           .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                           .setAvailableProviders(Arrays.asList(
                                    AuthUI.IdpConfig.PhoneBuilder().build()))
                           .setTosAndPrivacyPolicyUrls("link","link")
                           .build(),
                   RC_SIGN_IN)
       }*/
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            /*
                this checks if the activity result we are getting is for the sign in
                as we can have more than activity to be started in our Activity.
             */
            /* val response = IdpResponse.fromResultIntent(data)
            if(resultCode == Activity.RESULT_OK){
                *//*
                    Checks if the User sign in was successful
                 *//*
                auth = FirebaseAuth.getInstance();
                if(auth.currentUser?.displayName!=null) {

                    var mainIntent = Intent(this, MainActivity::class.java)
                    startActivity(mainIntent)

                    showSnackbar(R.string.signed_in)
                    finish()
                    return
                }else{
                    var registerName = Intent(this,EditProfileActivity::class.java)
                    startActivity(registerName)
                }
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
            }
        }*/
            showSnackbar(R.string.unknown_sign_in_response) //if the sign in response was unknown
        }
        fun nextActivity() {
            var mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }
    }
}
