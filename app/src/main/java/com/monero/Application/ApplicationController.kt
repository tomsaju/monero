package com.monero.Application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.support.multidex.MultiDex
import android.text.TextUtils
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.facebook.stetho.Stetho
import com.monero.R
import com.monero.helper.PreferenceManager
import com.monero.helper.AppDatabase
import com.monero.helper.AppDatabase.Companion.db
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.monero.main.MainActivity


/**
 * Created by tom.saju on 3/8/2018.
 */
class ApplicationController:Application(),SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        var preferenceManager:PreferenceManager?=null
        private val TAG = ApplicationController::class.java.simpleName
        @get:Synchronized var instance: ApplicationController? = null
            private set
        val accountAlertNotifications = "Alerts"

        lateinit var auth:FirebaseAuth
        lateinit var firestore:FirebaseFirestore
    }
    //var db = Room.databaseBuilder(baseContext.applicationContext, AppDatabase::class.java, "fair-db").build()

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        preferenceManager = PreferenceManager(applicationContext)
        Stetho.initializeWithDefaults(this)
        FirebaseApp.initializeApp(applicationContext)

        preferenceManager!!.prefs.registerOnSharedPreferenceChangeListener(this)

        auth = FirebaseAuth.getInstance()!!
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore = FirebaseFirestore.getInstance()
        firestore?.firestoreSettings = settings

    }




    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
      if(key==preferenceManager?.FCM_TOKEN) {
          var notificationKey = HashMap<String, Any>()
          notificationKey.put("Token", preferenceManager!!.fcmToken)

          if (firestore != null&&auth.currentUser!=null) {

                  firestore?.collection("NotificationTokens")?.document(auth.currentUser!!.uid)?.set(notificationKey)

                          ?.addOnSuccessListener { DocumentReference ->

                          }

                          ?.addOnFailureListener { e ->

                          }

          }
      }
    }

    private lateinit var mBuilder: NotificationCompat.Builder

    private lateinit var mNotificationManager: NotificationManager

    private val ALERT_NOTIFICATION_CHANNEL_ID: String = "AlertNotifications"

    fun createNotification(title: String, message: String) {
        /**Creates an explicit intent for an Activity in your app */
        val resultIntent = Intent(baseContext, MainActivity::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val resultPendingIntent = PendingIntent.getActivity(baseContext,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        mBuilder = NotificationCompat.Builder(baseContext)
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent)

        mNotificationManager = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(ALERT_NOTIFICATION_CHANNEL_ID, "Alerts", importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            assert(mNotificationManager != null)
            mBuilder.setChannelId(ALERT_NOTIFICATION_CHANNEL_ID)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }

        mNotificationManager.notify(3, mBuilder.build())
    }



    val requestQueue: RequestQueue? = null
        get() {
            if (field == null) {
                return Volley.newRequestQueue(applicationContext)
            }
            return field
        }

    fun <T> addToRequestQueue(request: Request<T>, tag: String) {
        request.tag = if (TextUtils.isEmpty(tag)) TAG else tag
        requestQueue?.add(request)
    }

    fun <T> addToRequestQueue(request: Request<T>) {
        request.tag = TAG
        requestQueue?.add(request)
    }

    fun cancelPendingRequests(tag: Any) {
        if (requestQueue != null) {
            requestQueue!!.cancelAll(tag)
        }
    }

  public fun deleteAllTables(){
      db = AppDatabase.getAppDatabase(baseContext)
      db?.activitesDao()?.deleteTable()
      db?.contactDao()?.deleteTable()
      db?.creditDao()?.deleteTable()
      db?.debitDao()?.deleteTable()
      db?.expenseDao()?.deleteTable()
      db?.historyDao()?.deleteTable()
      db?.tagDao()?.deleteTable()
      db?.userDao()?.deleteTable()
  }

}