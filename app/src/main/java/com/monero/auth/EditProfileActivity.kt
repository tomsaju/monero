package com.monero.auth

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.monero.R
import kotlinx.android.synthetic.main.activity_edit_profile.*
import com.google.firebase.auth.UserProfileChangeRequest
import com.monero.main.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.monero.Dao.DBContract
import com.monero.models.User


class EditProfileActivity : AppCompatActivity() {
    val auth = FirebaseAuth.getInstance()!!
    var db: FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        db = FirebaseFirestore.getInstance();

        getStartButton.setOnClickListener{v: View ->
            var displayName:String =edittext_display_name.text.toString()

            if(auth.currentUser!=null){
                val user = auth.currentUser

                val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName).build()

                user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener(this){task ->
                            if (task.isSuccessful()) {
                                Toast.makeText(this,"Sign in success",Toast.LENGTH_LONG).show()
                                var newUser = User( auth.currentUser?.uid!!,
                                        auth.currentUser?.displayName.toString(),
                                        auth.currentUser?.phoneNumber.toString(),
                                        auth.currentUser?.email.toString())

                                pushUserToFireBaseDB(newUser)
                                var mainIntent = Intent(this, MainActivity::class.java)
                                startActivity(mainIntent)
                            }
                        }

                        /*?.addOnCompleteListener( object: OnCompleteListener<Void> {
                            @Override
                              fun onComplete(task: (Task<Void>) ) {
                                if (task.isSuccessful()) {
                                   showSnackbar(R.string.sign_up_completed)
                                    var mainIntent = Intent(this, MainActivity::class.java)
                                    startActivity(mainIntent)
                                }
                            }
                        });*/
            }

        }


    }

    private fun pushUserToFireBaseDB(user: User) {

        var newUser = HashMap<String, Any>()


        newUser.put(DBContract.USER_TABLE.USER_ID,user.user_id)
        newUser.put(DBContract.USER_TABLE.USER_NAME,user.user_name)
        newUser.put(DBContract.USER_TABLE.USER_EMAIL,user.user_email)
        newUser.put(DBContract.USER_TABLE.USER_PHONE,user.user_phone)

        db?.collection("users")?.document(user.user_id.toString())?.set(newUser)

                ?.addOnSuccessListener {
                   //success
                }

                ?.addOnFailureListener { e ->
                   //failure
                }
    }
}
