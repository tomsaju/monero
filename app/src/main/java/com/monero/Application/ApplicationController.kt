package com.monero.Application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
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
import android.content.*
import android.provider.Settings
import android.support.multidex.MultiDexApplication
import android.support.v4.app.NotificationCompat
import android.util.Log
import androidx.work.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.monero.Dao.DBContract
import com.monero.SyncWorkManager
import com.monero.helper.SyncService
import com.monero.helper.converters.TagConverter
import com.monero.main.MainActivity
import com.monero.models.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import java.util.concurrent.TimeUnit


/**
 * Created by tom.saju on 3/8/2018.
 */
class ApplicationController:MultiDexApplication(),SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        var preferenceManager:PreferenceManager?=null
        private val TAG = ApplicationController::class.java.simpleName
        @get:Synchronized var instance: ApplicationController? = null
            private set
        val accountAlertNotifications = "Alerts"

        lateinit var auth:FirebaseAuth
        lateinit var firestore:FirebaseFirestore
        var selectedContactList:ArrayList<ContactMinimal> = ArrayList()
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
        instance = this

        val intentFilter =  IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");

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

        mNotificationManager.notify(4, mBuilder.build())
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

      Observable.fromCallable {
          db = AppDatabase.getAppDatabase(baseContext)
          db?.activitesDao()?.deleteTable()
          db?.contactDao()?.deleteTable()
          db?.creditDao()?.deleteTable()
          db?.debitDao()?.deleteTable()
          db?.expenseDao()?.deleteTable()
          db?.historyDao()?.deleteTable()
          db?.tagDao()?.deleteTable()
          db?.userDao()?.deleteTable()
      }.subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread()).subscribe({ _ ->


      })


  }

    public fun clearAllSharedPreferences(){

        preferenceManager?.clearAll()

    }

    public fun setSelectedList(contactList: ArrayList<ContactMinimal>){
        selectedContactList = contactList
    }

    public fun getselectedContactList(): ArrayList<ContactMinimal>{
        return selectedContactList
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun downloadActivity(activityId: String) {

        FirebaseFirestore.getInstance()
                .collection("activities").document(activityId).get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {

                val document = task.result
                var timestampWithoutNanoseconds = "";
                var timestampinSeconds = "";


                val tagType = object : TypeToken<List<Tag>>() {}.type
                val tagsList = Gson().fromJson<List<Tag>>(document!!.get(DBContract.ACTIVITY_TABLE.ACTIVITY_TAGS).toString(), tagType)

                val userListType = object : TypeToken<List<User>>() {}.type
                val usersList = Gson().fromJson<List<User>>(document!!.get(DBContract.ACTIVITY_TABLE.ACTIVITY_USERS).toString(), userListType)

                var activityAuthor = TagConverter().convertJsonToUserObject(document!!.get(DBContract.ACTIVITY_TABLE.ACTIVITY_AUTHOR).toString())

                var id: String = activityId
                var title: String = document!!.get(DBContract.ACTIVITY_TABLE.ACTIVITY_TITLE).toString()
                var description: String = document!!.get(DBContract.ACTIVITY_TABLE.ACTIVITY_DESCRIPTION).toString()
                var tags: List<Tag> = tagsList
                var mode: Int = Integer.parseInt(document!!.get(DBContract.ACTIVITY_TABLE.ACTIVITY_MODE).toString())
                var members: List<User> = usersList
                var author: User = activityAuthor
                var syncStatus: Boolean = true //syncstatus is true
                var createdDate: Long = document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_CREATED_DATE).toString().toLong()

                var lastModifiedTime = "";
                if (document.get("last_modified_time") == null) {
                    lastModifiedTime = "";
                } else {


                    var dateObject = document!!.get("last_modified_time")
                    lastModifiedTime = dateObject.toString();
                    timestampWithoutNanoseconds = lastModifiedTime.substringBefore(",")
                    timestampinSeconds = timestampWithoutNanoseconds.substringAfter("=")
                }
                var transactionIds = ""
                var historyLogIds = document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_HISTORY).toString()
                var expenseListId = document!!.get(DBContract.ACTIVITY_TABLE.ACTIVITY_EXPENSE_LIST).toString()
                var downloadedActivity = Activities(id, title, description, tags, mode, members, author, syncStatus, createdDate, expenseListId, historyLogIds, transactionIds, timestampinSeconds)


                saveActivityToLocal(downloadedActivity)

            } else {
                Log.d("tasklist", "unsuccessfull")
             //   view.hideLoader()
            }

        })
                .addOnFailureListener({exception->

                    Log.d("error",exception.message)
                //    view.hideLoader()
                })
    }

    private fun saveActivityToLocal(downloadedActivity: Activities?) {
        Log.d(TAG,"saveActivityToLocal")
        Observable.fromCallable {
            db = AppDatabase.getAppDatabase(baseContext)
            db?.activitesDao()?.insertIntoActivitiesTable(downloadedActivity!!) // .database?.personDao()?.insert(person)
            for (tag in downloadedActivity!!.tags) {
                AppDatabase.db?.tagDao()?.insertIntoTagTable(tag)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ orderItem ->
            // set values to UI
            //  PendingDownloadItems?.remove(downloadedActivity?.id)

        }, { error ->
            // handle exception if any
            Log.d("tag", "exception")
         //   view.hideLoader()
        }, {
            // on complete
            Log.d("tag", "completed")
         //   view.hideLoader()
            saveExpensesForActivity(downloadedActivity);


        })

    }

    private fun saveExpensesForActivity(downloadedActivity: Activities?) {
       // view.showLoader()
        Log.d(TAG,"saveExpensesForAvtivity : "+downloadedActivity?.id)
        if (downloadedActivity != null) {
            var expenseListFromServer: ArrayList<String> = ArrayList()
            if (downloadedActivity.expenseIdList != null) {
                if (downloadedActivity.expenseIdList.contains(",")) {
                    //more than one item
                    expenseListFromServer = ArrayList(downloadedActivity.expenseIdList.split(","))
                } else {
                    //single item
                    expenseListFromServer.add(downloadedActivity.expenseIdList)
                }
            }

            var historyListFromServer :ArrayList<String> = ArrayList()
            if (downloadedActivity.historyLogIds != null) {
                if (downloadedActivity.historyLogIds.contains(",")) {
                    //more than one item
                    historyListFromServer = ArrayList(downloadedActivity.historyLogIds.split(","))
                } else {
                    //single item
                    historyListFromServer.add(downloadedActivity.historyLogIds)
                }
            }

            if(historyListFromServer.isNotEmpty()){
                Observable.fromCallable {
                    db?.historyDao()?.getAllHistoryLogsIdsForActivity(downloadedActivity.id)
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe({ localList ->

                    downloadHistoryFromServer(historyListFromServer, localList)

                }, { error ->
                    // handle exception if any
                    Log.d("tag", "exception")
                  //  view.hideLoader()
                }, {
                    // on complete
                    Log.d("tag", "completed")
                   // view.hideLoader()


                })
            }


            if (expenseListFromServer != null && expenseListFromServer.isNotEmpty()) {
                Observable.fromCallable {
                    db?.expenseDao()?.getAllExpenseIdListForActivity(downloadedActivity.id)
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe({ localList ->

                    downLoadExpensesFromServer(expenseListFromServer, localList)

                }, { error ->
                    // handle exception if any
                    Log.d("tag", "exception")
                   // view.hideLoader()
                }, {
                    // on complete
                    Log.d("tag", "completed")



                })
            }else{
                Log.d(TAG,"Nothing to download")

            }

        }

    }
    private fun downloadHistoryFromServer(historyListFromServer: ArrayList<String>, localList: List<String>) {
        if (historyListFromServer != null && historyListFromServer.isNotEmpty()) {

            if (localList != null && localList.isNotEmpty()) {

                var temphistoryIdList = historyListFromServer

                //check with each element
                historyListFromServer.removeAll(localList)
                //find deleted expenses
                ArrayList(localList).removeAll(temphistoryIdList)
                //now local list contains id's which are deleted from server --1
                //expenseListFromServer contains new id's which are not present in local --2
                //delete 1 and save 2
            }
            for (expenseId in historyListFromServer) {
                downloadHistory(expenseId)
            }
        }
    }

    private fun downloadHistory(historyId: String) {
        FirebaseFirestore.getInstance()
                .collection("HistoryLog").document(historyId).get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
              //  view.hideLoader()
                val document = task.result
                if(document!!.exists()){
                    var activityId:String =  document.get(DBContract.HISTORY_LOG_ITEM_TABLE.ACTIVITY_ID).toString()
                    var authorId:String = document.get(DBContract.HISTORY_LOG_ITEM_TABLE.AUTHOR_ID).toString()
                    var authorName:String = document.get(DBContract.HISTORY_LOG_ITEM_TABLE.AUTHOR_NAME).toString()
                    var eventType:String = document.get(DBContract.HISTORY_LOG_ITEM_TABLE.EVENT_TYPE).toString()
                    var logItemId:String = historyId
                    var subjectId:String = document.get(DBContract.HISTORY_LOG_ITEM_TABLE.SUBJECT_ID).toString()
                    var subjectName:String = document.get(DBContract.HISTORY_LOG_ITEM_TABLE.SUBJECT_NAME).toString()
                    var subjectUrl:String = document.get(DBContract.HISTORY_LOG_ITEM_TABLE.SUBJECT_URL).toString()
                    var syncstatus = true
                    var timeStamp:String = document.get(DBContract.HISTORY_LOG_ITEM_TABLE.TIMESTAMP).toString()


                    var historyItem = HistoryLogItem(logItemId,authorId,authorName,eventType,timeStamp,subjectName,subjectUrl,subjectId,activityId,syncstatus)


                    Single.fromCallable {

                        AppDatabase.db = AppDatabase.getAppDatabase(baseContext)
                        AppDatabase.db?.historyDao()?.insertIntoHistoryTable(historyItem)
                    }.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe()

                }else{

                }

            }else{
            //    view.hideLoader()
            }
        })
    }

    fun downLoadExpensesFromServer(expenseListFromServer: ArrayList<String>, localList: List<String>) {
       // view.showLoader()
        Log.d(TAG,"downLoadExpensesFromServer")
        if (expenseListFromServer != null && expenseListFromServer.isNotEmpty()) {

            if(localList!=null&&localList.isNotEmpty()) {

                var tempExpenseIdList = expenseListFromServer

                //check with each element
                expenseListFromServer.removeAll(localList)
                //find deleted expenses
                ArrayList(localList).removeAll(tempExpenseIdList)
                //now local list contains id's which are deleted from server --1
                //expenseListFromServer contains new id's which are not present in local --2
                //delete 1 and save 2
            }
            for (expenseId in expenseListFromServer) {
                downloadExpense(expenseId)
            }

            Log.d("new", expenseListFromServer.size.toString())

        }

    }

     fun downloadExpense(expenseId: String) {
        Log.d(TAG,"downloadExpense")
        FirebaseFirestore.getInstance()
                .collection("expenses").document(expenseId).get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
               // view.hideLoader()
                val document = task.result

                var title: String = document!!.get(DBContract.EXPENSE_TABLE.EXPENSE_TITLE).toString()
                var Comments: String = document!!.get(DBContract.EXPENSE_TABLE.EXPENSE_COMMENTS).toString()
                var activityId: String = document!!.get(DBContract.EXPENSE_TABLE.EXPENSE_ACTIVITY_ID).toString()
                var creditList: String = document!!.get(DBContract.EXPENSE_TABLE.EXPENSE_CREDITS).toString()
                var debitList: String = document!!.get(DBContract.EXPENSE_TABLE.EXPENSE_DEBIT).toString()
                var createdDate :String = document!!.get(DBContract.EXPENSE_TABLE.EXPENSE_CREATED_DATE).toString()
                var splitType =0
                try {
                    splitType = document!!.get(DBContract.EXPENSE_TABLE.EXPENSE_SPLIT_TYPE) as Int
                }catch (e:Exception){

                }


                var amount: Int = 0
                try {
                    amount = document!!.get(DBContract.EXPENSE_TABLE.EXPENSE_AMOUNT).toString().toInt()
                }catch (e:Exception){

                }
                var convertor = TagConverter()
                // var debitArrayList = convertor.convertJSONtoDebitList(debitList)
                // var creditArrayList = convertor.convertJSONtoCreditList(creditList)

                var debitJson: JSONArray
                var creditJson : JSONArray
                var debitArrayList = ArrayList<Debit>()
                var creditArrayList = ArrayList<Credit>()

                if(debitList!=null&&creditList!=null) {
                    try {
                        debitJson = JSONArray(debitList)
                        creditJson = JSONArray(creditList)



                        for (i in 0..(debitJson.length() - 1)) {
                            var debit = convertor.convertStringToDebit(debitJson[i].toString())
                            debitArrayList.add(debit)
                        }


                        for (i in 0..(creditJson.length() - 1)) {
                            var credit = convertor.convertStringToCredit(creditJson[i].toString())
                            creditArrayList.add(credit)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }


                var expense = Expense(expenseId, title, Comments, activityId, amount, creditArrayList, debitArrayList,splitType,createdDate,true)

                Single.fromCallable {

                    AppDatabase.db = AppDatabase.getAppDatabase(baseContext)
                    AppDatabase.db?.expenseDao()?.insertIntoAExpensesTable(expense)

                    for (credit in expense.creditList) {
                        AppDatabase.db?.creditDao()?.insertIntoCreditTable(credit)
                    }

                    for (debit in expense.debitList) {
                        AppDatabase.db?.debitDao()?.insertIntoDebitTable(debit)
                    }


                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe()


            }else{
               // view.hideLoader()
            }
        })

    }

    private lateinit var compressionWork: OneTimeWorkRequest

    fun syncDataWithServer(){
        Log.d("Listener","Syncing started")
        Log.d(TAG,"synCdataCalled")
         compressionWork =  OneTimeWorkRequest.Builder(SyncService::class.java)
                 .addTag("syncTag")
                 .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                 .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                 .build()
         WorkManager.getInstance().enqueue(compressionWork)
        Log.d(TAG,"enqueued")

    }


}