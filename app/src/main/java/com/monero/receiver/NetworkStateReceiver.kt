package com.monero.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.monero.Application.ApplicationController
import com.monero.utility.Utility

/**
 * Created by tom.saju on 1/3/2019.
 */
class NetworkStateReceiver:BroadcastReceiver() {
    lateinit var auth: FirebaseAuth

    override fun onReceive(p0: Context?, p1: Intent?) {

        auth = FirebaseAuth.getInstance()!!

        if(auth.currentUser!=null) {

            if (Utility.isNetworkAvailable(p0!!)) {
                Log.d("Listener", "Network available")
                //try syncing if there are unsynced items
                ApplicationController.instance?.syncDataWithServer()
            } else {
                Log.d("Listener", "Network unavailable")
            }
        }
    }
}