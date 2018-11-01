package com.monero.signup

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.monero.R
import kotlinx.android.synthetic.main.activity_sign_up2.*
import android.widget.Toast
import android.text.TextUtils
import android.util.Log
import com.monero.addActivities.AddActivityFragment
import com.monero.signin.AddPhoneFragment
import com.monero.signin.SignInActivity


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
        buttonSignup.setOnClickListener(this)
        sign_in_page_button.setOnClickListener(this)
    }

    private fun registerUser(email: String, password: String) {

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    //checking if success
                    if (task.isSuccessful) {
                        //display some message here
                        Log.d(TAG, "successfull creation")

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

            //checking if email and passwords are empty
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
            registerUser(email, password)

    }else if(view?.id == R.id.sign_in_page_button) {

            startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
    }
}

    override fun onFragmentInteraction(uri: Uri) {

    }
}
