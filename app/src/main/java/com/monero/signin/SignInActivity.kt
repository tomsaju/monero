package com.monero.signin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.monero.R
import com.monero.main.MainActivity
import kotlinx.android.synthetic.main.activity_sign_in2.*

class SignInActivity : AppCompatActivity() {
    var TAG = "SignupActivity"
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in2)

        firebaseAuth = FirebaseAuth.getInstance()

        buttonSignin.setOnClickListener {

            var valid = true

            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            //checking if email and passwords are empty
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show()
                valid = false
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show()
                valid = false
            }

            //if the email and password are not empty
            //displaying a progress dialog
            if(valid) {
                signInUser(email, password)
            }
        }

    }


    private fun signInUser(email:String,password:String) {

        //creating a new user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    //checking if success
                    if (task.isSuccessful) {
                        //display some message here
                        Log.d(TAG,"sign in successfull")
                        startActivity(Intent(this@SignInActivity,MainActivity::class.java))
                    } else {
                        //display some message here
                        Log.d(TAG,"unsuccessfull"+task.exception?.message)
                        Toast.makeText(this,task.exception?.message, Toast.LENGTH_SHORT)
                    }
                }

    }


}
