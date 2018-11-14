package com.monero.signin

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.monero.Application.ApplicationController
import com.monero.R
import com.monero.main.MainActivity
import com.monero.signup.SignUpActivity
import com.monero.utility.Utility
import kotlinx.android.synthetic.main.activity_sign_in2.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener



class SignInActivity : AppCompatActivity() {
    var TAG = "SignupActivity"
    private lateinit var firebaseAuth: FirebaseAuth
    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null
    var myImageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in2)
        progressBarSignIn.visibility = View.INVISIBLE
        firebaseAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance();
        if(storage!=null) {
            val firebaseStorage = storage
            storageReference = firebaseStorage?.getReference();
        }




        buttonSignin.setOnClickListener {

            if (Utility.isNetworkAvailable(this)) {
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
            } else {
                Toast.makeText(this, "No Internet.Please check your internet connection", Toast.LENGTH_LONG).show()
            }
        }

        goto_sign_up.setOnClickListener {
            startActivity(Intent(this@SignInActivity,SignUpActivity::class.java))
        }

    }


    private fun signInUser(email:String,password:String) {
        progressBarSignIn.visibility = View.VISIBLE
        //creating a new user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    //checking if success
                    if (task.isSuccessful) {
                        //display some message here
                        Log.d(TAG,"sign in successfull")
                        Toast.makeText(this,"Sign in successfull", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SignInActivity,MainActivity::class.java))
                        //register for fcm notifications
                        var notificationKey = HashMap<String, Any>()
                        notificationKey.put("Token", ApplicationController.preferenceManager!!.fcmToken)
                        //save user id, phone number and email
                        if(firebaseAuth.currentUser?.uid!=null) {
                            ApplicationController.preferenceManager?.myUid = firebaseAuth.currentUser?.uid!!
                        }

                        if(firebaseAuth.currentUser?.phoneNumber!=null) {
                            ApplicationController.preferenceManager?.myPhone = firebaseAuth.currentUser?.phoneNumber!!
                        }

                        if(firebaseAuth.currentUser?.email!=null){
                            ApplicationController.preferenceManager?.myEmail = firebaseAuth.currentUser?.email!!
                        }


                        // Create a reference to 'images/mountains.jpg'
                        myImageReference = storageReference?.child("displayImages/"+firebaseAuth.currentUser?.uid+".jpg")

                        myImageReference?.downloadUrl?.addOnSuccessListener(OnSuccessListener<Any> { uri->

                            ApplicationController.preferenceManager!!.myDisplayPicture = uri.toString()

                            // Got the download URL for 'users/me/profile.png'
                        })?.addOnFailureListener(OnFailureListener {
                            // Handle any errors
                        })

                        ApplicationController.firestore?.collection("NotificationTokens")?.document(firebaseAuth.currentUser!!.uid)?.set(notificationKey)

                                ?.addOnSuccessListener { DocumentReference ->
                                    Log.d(TAG,"success")
                                }

                                ?.addOnFailureListener { e ->
                                    Log.d(TAG,"failure")
                                }
                    } else {
                        //display some message here
                        Log.d(TAG,"unsuccessfull"+task.exception?.message)
                        Toast.makeText(this,task.exception?.message, Toast.LENGTH_SHORT).show()
                        progressBarSignIn.visibility = View.INVISIBLE
                    }
                }


    }


}
