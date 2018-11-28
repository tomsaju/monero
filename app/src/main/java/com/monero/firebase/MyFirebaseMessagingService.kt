package com.monero.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.monero.Application.ApplicationController
import com.monero.Dao.DBContract
import com.monero.R
import com.monero.helper.AppDatabase
import com.monero.main.MainActivity
import com.monero.models.HistoryLogItem
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
    private lateinit var mBuilder: NotificationCompat.Builder

    private lateinit var mNotificationManager: NotificationManager

    private val ALERT_NOTIFICATION_CHANNEL_ID: String = "AlertNotifications"
    override fun onCreate() {
        super.onCreate()
        auth = FirebaseAuth.getInstance()!!
        firestoreDb = FirebaseFirestore.getInstance()
    }


     override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        super.onMessageReceived(remoteMessage)
        /* val title = remoteMessage?.notification?.title!!
         val message = remoteMessage?.notification?.body!!*/

        val title = "added something"
        val message = "nothing"

         val data = remoteMessage?.data

         var activityId = data!!.get("Activity_Id")
         var authorId = data?.get("Author_Id")
         var authorName = data?.get("Author_name")
         var eventType = data?.get("Event_Type")
         var logItemId = data?.get("Log_Item_Id")
         var subjectId = data?.get("Subject_Id")
         var subjectName = data?.get("Subject_Name")
         var subjectUrl = data?.get("Subject_Url")
         var timeStamp = data?.get("Timestamp")


         var historyItem = HistoryLogItem(logItemId!!,authorId!!,authorName!!,eventType!!,timeStamp!!,subjectName!!,subjectUrl!!,subjectId!!,activityId!!,
                 true)

         var action = "";
         if(eventType== DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_EXPENSE){

             action= "added a new expense"
         }else if(eventType==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_ACTIVITY){
             action= "added you to an activity"
         }else if(eventType==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_COMMENT){
             action= "added a new comment"
         }else if(eventType==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_IMAGE){
             action= "added a new image"
         }else if(eventType==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_USER){
             action= "added a new user"
         }else if(eventType==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_EDITTED_EXPENSE){
             action= "editted an expense"
         }
         var text =""

         if(ApplicationController.preferenceManager?.myUid==authorId){
             text = "You"+" "+action+" "+subjectName
         }else{
             text = authorName +" "+action+" "+subjectName
         }






         var nItem = NotificationItem(System.currentTimeMillis(),text,title,1,subjectId)
         Observable.fromCallable {

             AppDatabase.db = AppDatabase.getAppDatabase(baseContext)
             AppDatabase.db?.notificationItemDao()?.insertIntoNotiTable(nItem) // .database?.personDao()?.insert(person)

             //check the subject of notification and save it to DB
             if(eventType== DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_EXPENSE){
                //download expense
                 ApplicationController.instance?.downloadExpense(subjectId)

             }else if(eventType==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_ACTIVITY){
                //download activity
                 ApplicationController.instance?.downloadActivity(subjectId)

             }else if(eventType==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_COMMENT){
                 action= "added a new comment"
             }else if(eventType==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_IMAGE){
                 action= "added a new image"
             }else if(eventType==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_USER){
                 //download activity data
                 ApplicationController.instance?.downloadActivity(subjectId)


             }else if(eventType==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_EDITTED_EXPENSE){
                 //download expense
                 ApplicationController.instance?.downloadExpense(subjectId)

             }

             AppDatabase.db?.historyDao()?.insertIntoHistoryTable(historyItem)
         }.subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread()).subscribe({ notificationItem ->
             Log.d("Notifications","Save Success")
             createNotification(title,text)
         })
     }

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)

        ApplicationController.preferenceManager?.fcmToken = p0!!
        if(auth.currentUser!=null&&p0.isNotBlank()){
            var notificationKey = HashMap<String, Any>()
            notificationKey.put("Token",p0!!)

            firestoreDb?.collection("NotificationTokens")?.document(auth.currentUser!!.uid)?.set(notificationKey)

                    ?.addOnSuccessListener { DocumentReference ->

                    }

                    ?.addOnFailureListener { e ->

                    }
        }else if(p0.isBlank()){

        }
    }

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

        mNotificationManager.notify(4, mBuilder.build())
    }

}