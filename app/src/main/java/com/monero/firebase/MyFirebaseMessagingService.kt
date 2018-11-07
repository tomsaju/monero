package com.monero.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.monero.Application.ApplicationController
import com.monero.helper.AppDatabase
import com.monero.models.NotificationItem
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by tom.saju on 10/31/2018.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    lateinit var auth: FirebaseAuth
    var firestoreDb: FirebaseFirestore? = null
    lateinit var myUid:String
    override fun onCreate() {
        super.onCreate()
        auth = FirebaseAuth.getInstance()!!
        firestoreDb = FirebaseFirestore.getInstance()
    }


     override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        super.onMessageReceived(remoteMessage)
         val title = remoteMessage?.notification?.title!!
         val message = remoteMessage?.notification?.body!!

         ApplicationController.instance?.createNotification(title,message)

         var nItem = NotificationItem(System.currentTimeMillis(),message,title,1,"someId")
         Observable.fromCallable {
             AppDatabase.db = AppDatabase.getAppDatabase(baseContext)
             AppDatabase.db?.notificationItemDao()?.insertIntoNotiTable(nItem) // .database?.personDao()?.insert(person)
         }.subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread()).subscribe({ _ ->
             Log.d("Notifications","Save Success")
         })
     }

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)

        ApplicationController.preferenceManager?.fcmToken = p0!!
        if(auth.currentUser!=null){
            var notificationKey = HashMap<String, Any>()
            notificationKey.put("Token",p0!!)

            firestoreDb?.collection("NotificationTokens")?.document(auth.currentUser!!.uid)?.set(notificationKey)

                    ?.addOnSuccessListener { DocumentReference ->

                    }

                    ?.addOnFailureListener { e ->

                    }
        }
    }



}