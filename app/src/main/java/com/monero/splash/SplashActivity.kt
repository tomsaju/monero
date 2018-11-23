package com.monero.splash

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.monero.R
import com.monero.main.MainActivity

class SplashActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        auth = FirebaseAuth.getInstance()!!
        if(auth.currentUser!=null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }else{
            loadSignInActivity()
            finish()
        }
    }

    private fun loadSignInActivity() {
        startActivity(Intent(this@SplashActivity,com.monero.signin.SignInActivity::class.java))
    }
}
