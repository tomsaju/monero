package com.monero.signup

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.monero.R
import kotlinx.android.synthetic.main.activity_sign_up2.*
import android.widget.Toast
import android.text.TextUtils
import android.util.Log
import com.google.firebase.auth.PhoneAuthCredential
import com.monero.main.MainActivity
import com.monero.signin.AddPhoneFragment
import com.monero.signin.SignInActivity
import java.lang.Exception
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.FirebaseUser




class SignUpActivity : AppCompatActivity(), View.OnClickListener, AddPhoneFragment.OnAddPhoneFragmentInteractionListener {
    var TAG = "SignupActivity"
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up2)

        firebaseAuth = FirebaseAuth.getInstance()
        //initializing views

        progressDialog = ProgressDialog(this)

        //attaching listener to button
        progressBarSignUp.visibility = View.INVISIBLE
        buttonSignup.setOnClickListener(this)
        sign_in_page_button.setOnClickListener(this)
    }

    private fun registerUser(userName:String,email: String, password: String) {
        progressBarSignUp.visibility = View.VISIBLE
        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    //checking if success
                    if (task.isSuccessful) {
                        //display some message here
                        Log.d(TAG, "successfull creation")
                        progressBarSignUp.visibility = View.INVISIBLE

                        val user = firebaseAuth.currentUser

                        val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(userName).build()

                        user?.updateProfile(profileUpdates)

                        showPhoneNumberFragment()

                    } else {
                        //display some message here
                        Log.d(TAG, "unsuccessfull" + task.exception?.message)
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT)
                    }
                }

    }

    private fun showPhoneNumberFragment() {
        var ft = supportFragmentManager.beginTransaction()
        var frag =  AddPhoneFragment()
        ft.setCustomAnimations(R.anim.design_bottom_sheet_slide_in,R.anim.design_bottom_sheet_slide_out)
        ft.add(android.R.id.content, frag,"add_phone_fragment").commit()
    }

    override fun onClick(view: View?) {

        if (view?.id == R.id.buttonSignup) {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val userName = editTextuserName.text.toString().trim()

            //checking if email and passwords are empty
            if (TextUtils.isEmpty(userName)) {
                Toast.makeText(this, "Please enter username", Toast.LENGTH_LONG).show()
                return
            }


            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show()
                return
            }


            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show()
                return
            }

            //if the email and password are not empty
            //displaying a progress dialog
            registerUser(userName,email, password)

    }else if(view?.id == R.id.sign_in_page_button) {

            startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
    }
}

    override fun signInWithPhoneCredential(credential: PhoneAuthCredential?) {

        /*firebaseAuth.signInWithCredential(credential!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")

                        val user = task.result?.user
                        linkUserPhoneNumber(user.phoneNumber)
                        // ...
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.d(TAG, "signInWithCredential:failure")
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                    }
                }*/

        firebaseAuth.currentUser?.linkWithCredential(credential!!)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "linkWithCredential:success")
                        val user = task.result?.user
                        var mainIntent = Intent(this,MainActivity::class.java)
                        startActivity(mainIntent)
                        finish()
                    } else {
                        Log.w(TAG, "linkWithCredential:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        progressBarSignUp.visibility = View.INVISIBLE
                        showAlert(task.exception)
                    }


                }

    }

    private fun showAlert(exception: Exception?) {

        // Initialize a new instance of
        val builder = AlertDialog.Builder(this@SignUpActivity)

        // Set the alert dialog title
        builder.setTitle("Alert")

        // Display a message on alert dialog
        builder.setMessage(exception?.message)

        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton("OK"){dialog, which ->
            // Do something when user press the positive button
           dialog.dismiss()

        }


        // Display a negative button on alert dialog
        builder.setNegativeButton("No"){dialog,which ->
           dialog.dismiss()
        }


        // Display a neutral button on alert dialog
        builder.setNeutralButton("Cancel"){dialog,_ ->
           dialog.cancel()
        }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()
    }

    private fun linkUserPhoneNumber(phoneNumber: String?) {

    }
}
