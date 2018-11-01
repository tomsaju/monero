package com.monero.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by tom.saju on 10/31/2018.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

     override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        super.onMessageReceived(remoteMessage)
    }

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
    }



}