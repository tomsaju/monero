package com.monero.main.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.monero.R

/**
 * Created by tom.saju on 3/6/2018.
 */
class ProfileFragment:Fragment() {
    val auth = FirebaseAuth.getInstance()!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater?.inflate(R.layout.profile_fragment,container,false)
        //var name:TextView = rootView?.findViewById<TextView>(R.id.username_display) as TextView
        if(auth.currentUser!=null){
           // name.text = auth.currentUser?.displayName.toString()
        }else{
          //  name.text = "No users signed in"
        }
        return rootView
    }
}