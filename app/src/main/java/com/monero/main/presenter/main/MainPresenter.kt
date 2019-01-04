package com.monero.main.presenter.main

import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import com.google.gson.Gson
import com.monero.Dao.DBContract
import com.monero.helper.AppDatabase
import com.monero.helper.AppDatabase.Companion.db
import com.monero.helper.AppDatabase.Companion.getAppDatabase
import com.monero.helper.converters.TagConverter
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.*
import com.monero.Application.ApplicationController
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.reflect.TypeToken
import com.monero.models.*
import com.monero.network.RestService
import com.monero.network.ServiceRest
import io.reactivex.Observable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import io.reactivex.disposables.Disposable
import io.reactivex.SingleObserver
import org.json.JSONArray
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.monero.R
import com.monero.helper.ImageSaver
import com.monero.utility.Utility
import io.reactivex.functions.Consumer


/**
 * Created by tom.saju on 3/7/2018.
 */
class MainPresenter : IMainPresenter {

    var TAG: String = "MainPresenter"
    var context: Context
    var view: IMainView
    var firestoreDb: FirebaseFirestore? = null
    var auth = FirebaseAuth.getInstance()!!
    private lateinit var storageReference: StorageReference
    constructor(context: Context, view: IMainView) {
        this.context = context
        this.view = view
        firestoreDb = FirebaseFirestore.getInstance()
        var storage = FirebaseStorage.getInstance();
        storageReference = storage?.getReference();
    }

    var PendingDownloadItems: ArrayList<String>? = null


    override fun getAllActivitiesList() {
        db = getAppDatabase(context)
        var allActivities = db?.activitesDao()?.getAllActivities()
        view.onActivitiesFetched(allActivities)
    }


    val RestAPIService by lazy {
        RestService.create()
    }

    var disposable: Disposable? = null

    override fun saveActivity(activity: Activities) {

//////////////////////////

        Observable.fromCallable {
            db = getAppDatabase(context)
            db?.activitesDao()?.insertIntoActivitiesTable(activity) // .database?.personDao()?.insert(person)
            for (tag in activity.tags) {
                AppDatabase.db?.tagDao()?.insertIntoTagTable(tag)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ orderItem ->


            //save the log to history
            var historyItem = HistoryLogItem(UUID.randomUUID().toString(),
                    activity.author.user_id,
                    activity.author.user_name,
                    DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_ACTIVITY,
                    activity.createdDate.toString(),
                    activity.title,
                    "",
                    activity.id,
                    activity.id, false)

            saveLog(historyItem)

            // set values to UI
            Log.d("tag", "done")
            ///
            var gson = Gson()
            var convertor = TagConverter()
            var membersJson: String = convertor.convertUserListtoString(activity.members)
            var tagsJson: String = convertor.convertTagListtoString(activity.tags)
            var author: String = gson.toJson(activity.author, User::class.java)

            var permittedUserArrayList = arrayListOf<String>()
            for (user in activity.members) {

                if(user.user_email.isNotEmpty()&&user.user_id==user.user_email||user.user_phone.isNotEmpty()&&user.user_id==user.user_phone){
                    //not registered user
                    if(user.user_email.isNotEmpty()){
                        permittedUserArrayList.add(user.user_email)
                    }else if(user.user_phone.isNotEmpty()) {
                        permittedUserArrayList.add(user.user_phone)
                    }
                }else{
                    if(user.user_id.isNotEmpty()){
                        permittedUserArrayList.add(user.user_id)
                    }
                }

            }


            if(Utility.isNetworkAvailable(context)) {
                var newActivity = HashMap<String, Any>()
                newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_TITLE, activity.title)
                newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_DESCRIPTION, activity.description)
                newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_MODE, activity.mode)
                newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_TAGS, tagsJson)
                newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_USERS, membersJson)
                newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_AUTHOR, author)
                newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_ALLOWED_READ_PERMISSION_USERS, permittedUserArrayList)
                newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_CREATED_DATE, activity.createdDate)


                firestoreDb?.collection("activities")?.document(activity.id)?.set(newActivity)

                        ?.addOnSuccessListener { DocumentReference ->

                            //success
                            activity.syncStatus = true

                        }

                        ?.addOnFailureListener { e ->
                            //failure

                        }
            }
            ///
        }, { error ->
            // handle exception if any
            Log.d("tag", "exception")
        }, {
            // on complete
            Log.d("tag", "completed")
        })


    }


    override fun updateActivity(activity: Activities) {

        Observable.fromCallable {
            db = getAppDatabase(context)
            db?.activitesDao()?.updateActivity(activity) // .database?.personDao()?.insert(person)
            for (tag in activity.tags) {
                AppDatabase.db?.tagDao()?.insertIntoTagTable(tag)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ orderItem ->
            //save the log to history
            var historyItem = HistoryLogItem(System.currentTimeMillis().toString(),
                    activity.author.user_id,
                    activity.author.user_name,
                    DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_EDITTED_ACTIVITY,
                    activity.createdDate.toString(),
                    activity.title,
                    "",
                    activity.id,
                    activity.id, false)

            saveLog(historyItem)

            // set values to UI
            Log.d("tag", "done")
            ///
            var gson = Gson()
            var convertor = TagConverter()
            var membersJson: String = convertor.convertUserListtoString(activity.members)
            var tagsJson: String = convertor.convertTagListtoString(activity.tags)
            var author: String = gson.toJson(activity.author, User::class.java)

            var permittedUserArrayList = arrayListOf<String>()
            for (user in activity.members) {
                permittedUserArrayList.add(user.user_phone)
            }


            var updatedActivity = HashMap<String, Any>()
            updatedActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_TITLE, activity.title)
            updatedActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_DESCRIPTION, activity.description)
            updatedActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_MODE, activity.mode)
            updatedActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_TAGS, tagsJson)
            updatedActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_USERS, membersJson)
            updatedActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_AUTHOR, author)
            updatedActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_ALLOWED_READ_PERMISSION_USERS, permittedUserArrayList)
            updatedActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_CREATED_DATE, activity.createdDate)


            firestoreDb?.collection("activities")?.document(activity.id)?.update(updatedActivity)

                    ?.addOnSuccessListener { DocumentReference ->

                        //success
                        activity.syncStatus = true

                    }

                    ?.addOnFailureListener { e ->
                        //failure
                    }

            ///
        }, { error ->
            // handle exception if any
            Log.d("tag", "exception")
        }, {
            // on complete
            Log.d("tag", "completed")
        })

    }

    private fun saveLog(historyItem: HistoryLogItem) {
        Observable.fromCallable {
            db = getAppDatabase(context)
            db?.historyDao()?.insertIntoHistoryTable(historyItem) // .database?.personDao()?.insert(person)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ history ->

            saveHistorytoCloud(historyItem)
            Log.d(TAG, "log saving complete")
        })

    }

    private fun saveHistorytoCloud(historyItem: HistoryLogItem) {

        var historylog = HashMap<String, Any>()
        historylog.put(DBContract.HISTORY_LOG_ITEM_TABLE.LOG_ITEM_ID, historyItem.log_id)
        historylog.put(DBContract.HISTORY_LOG_ITEM_TABLE.AUTHOR_ID, historyItem.Author_Id)
        historylog.put(DBContract.HISTORY_LOG_ITEM_TABLE.AUTHOR_NAME, historyItem.Author_name)
        historylog.put(DBContract.HISTORY_LOG_ITEM_TABLE.EVENT_TYPE, historyItem.Event_Type)
        historylog.put(DBContract.HISTORY_LOG_ITEM_TABLE.TIMESTAMP, historyItem.Timestamp)
        historylog.put(DBContract.HISTORY_LOG_ITEM_TABLE.SUBJECT_NAME, historyItem.Subject_Name)
        historylog.put(DBContract.HISTORY_LOG_ITEM_TABLE.SUBJECT_URL, historyItem.Subject_Url)
        historylog.put(DBContract.HISTORY_LOG_ITEM_TABLE.SUBJECT_ID, historyItem.Subject_Id)
        historylog.put(DBContract.HISTORY_LOG_ITEM_TABLE.ACTIVITY_ID, historyItem.Activity_Id)


        firestoreDb?.collection("HistoryLog")?.document(historyItem.log_id)?.set(historylog)

                ?.addOnSuccessListener { DocumentReference ->

                    historyItem.SyncStatus = true
                    //success
                    Observable.fromCallable {
                        db = getAppDatabase(context)
                        db?.historyDao()?.insertIntoHistoryTable(historyItem) // .database?.personDao()?.insert(person)
                    }.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()).subscribe({ history ->

                        Log.d(TAG,"log saving complete")
                    })
                }

                ?.addOnFailureListener { e ->
                    //failure
                    Log.d(TAG,e.toString())
                }
    }

    override fun getAllActivitiesFromServer() {
        Log.d(TAG,"getAllActivitiesFromServer")
        view.showLoader()
        getActivityIdList()

        /* */
    }


    fun downloadAllActivities(activityIdList: ArrayList<String>) {
        Log.d(TAG,"downloadActivitiesin list")
        view.showLoader()
        for (id in activityIdList) {
            downloadActivity(id)
        }
    }


    fun getAllActivitiesForMe(){

    }

    fun downloadActivity(activityId: String) {
        Log.d(TAG,"getAllActivity : "+activityId)
        var gson = Gson()
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
                view.hideLoader()
            }

        })
                .addOnFailureListener({exception->

                    Log.d("error",exception.message)
                    view.hideLoader()
                })
    }

    private fun saveActivityToLocal(downloadedActivity: Activities?) {
        Log.d(TAG,"saveActivityToLocal")
        Observable.fromCallable {
            db = getAppDatabase(context)
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
            view.hideLoader()
        }, {
            // on complete
            Log.d("tag", "completed")
            view.hideLoader()
            saveExpensesForActivity(downloadedActivity);


        })

    }

    private fun saveExpensesForActivity(downloadedActivity: Activities?) {
        view.showLoader()
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
                    view.hideLoader()
                }, {
                    // on complete
                    Log.d("tag", "completed")
                    view.hideLoader()


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
                    view.hideLoader()
                }, {
                    // on complete
                    Log.d("tag", "completed")
                    view.hideLoader()


                })
            }else{
                Log.d(TAG,"Nothing to download")
                view.hideLoader()
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
                view.hideLoader()
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

                        AppDatabase.db = AppDatabase.getAppDatabase(context)
                        AppDatabase.db?.historyDao()?.insertIntoHistoryTable(historyItem)
                    }.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe()

                }else{

                }

            }else{
                view.hideLoader()
            }
        })
    }

    fun downLoadExpensesFromServer(expenseListFromServer: ArrayList<String>, localList: List<String>) {
        view.showLoader()
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


    override fun getAllNotificationsFromDB() {
        db = getAppDatabase(context)
        var allActivities = db?.notificationItemDao()?.getAllNotifications()
        view.onNotificationsFetched(allActivities)
    }

    private fun downloadExpense(expenseId: String) {
        Log.d(TAG,"downloadExpense")
        FirebaseFirestore.getInstance()
                .collection("expenses").document(expenseId).get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
                view.hideLoader()
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

                var debitJson:JSONArray
                var creditJson :JSONArray
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

                    AppDatabase.db = AppDatabase.getAppDatabase(context)
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
                view.hideLoader()
            }
        })

    }

    private fun shouldRemove(it: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun getActivityIdList() {

        Log.d(TAG,"getActivityIdLIst")
        var userId = ApplicationController.preferenceManager!!.myUid
        var myActivityIds: ArrayList<String> = ArrayList()
        var stringlist: String = ""
        FirebaseFirestore.getInstance()
                .collection("userData").document(userId).get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
                try {
                    view.hideLoader()
                    val document = task.result
                    if (document!!.exists()) {
                        var finalList: HashMap<String, String> = HashMap();

                        val activitiesData: Map<String, Any>? = document!!.data!!
                        val activityiesDetails = activitiesData?.get("activities_details") as HashMap<String, Any>;
                        for ((key, value) in activityiesDetails) {
                            println("$key = $value")
                            try {
                                var last_modified_time = (value as HashMap<String, Any>).get("last_modified_time").toString();
                                var lastModifiedTimeWithoutNanoseconds = last_modified_time.substringBefore(",")
                                var lastModifiedSeconds = lastModifiedTimeWithoutNanoseconds.substringAfter("=")
                                finalList[key] = lastModifiedSeconds
                            } catch (e: Exception) {

                            }
                        }
                        downloadUpdatedActivities(finalList)

                    }
                } catch (e: Exception) {
                    view.hideLoader()
                }
            }
        })
    }

    private fun downloadUpdatedActivities(finalList: HashMap<String, String>) {
        Log.d(TAG,"downloadUpdatedActivities")
        view.showLoader()
        var updatedActivityIdList: ArrayList<String> = ArrayList()


        var single: Single<List<ActivitiesMinimal>>? = db?.activitesDao()?.getAllActivitiesModifiedDate()
        if (single != null) {
            single.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<List<ActivitiesMinimal>> {
                        override fun onSubscribe(d: Disposable) {
                            // add it to a CompositeDisposable
                        }

                        override fun onSuccess(users: List<ActivitiesMinimal>) {
                            // update the UI
                            view.hideLoader()
                            var allActivitiesModifiedTime = users
                            if (allActivitiesModifiedTime != null) {
                                for (activity in allActivitiesModifiedTime) {
                                    if (finalList.containsKey(activity.id) &&
                                            areModifiedTImeSame(finalList.get(activity.id), activity.lastModifiedTime)) {
                                        finalList.remove(activity.id)
                                    } else {
                                        updatedActivityIdList.add(activity.id)
                                    }
                                }
                            }

                            if (!finalList.isEmpty()) {
                                for ((key, value) in finalList) {
                                    updatedActivityIdList.add(key)
                                }
                            }

                            if (updatedActivityIdList != null && updatedActivityIdList.isNotEmpty()) {
                                Log.d(TAG, "found new items")
                                downloadAllActivities(updatedActivityIdList)
                            } else {
                                Log.d(TAG, "No new items")
                                view.hideLoader()
                            }

                        }

                        override fun onError(e: Throwable) {
                            // show an error message
                            Log.d("download", "Error")
                            view.hideLoader()
                        }
                    })
        }


    }

    private fun areModifiedTImeSame(serverModifiedTime: String?, localModifiedTime: String): Boolean {
        var isEqual = false
        //Compare the modified timestamps
        //Firebase timestamp when fetched as milliseconds and nanseconds... shows difference in value for same timestamp
        //ex September 5 2018 12:00:00 timestamp fetched from two different sections give different values for milliseconds(sometimes) and nanoseconds(always)
        //so we are converting them to standard format for comparison
        if(localModifiedTime==""){
            return  false;
        }

        var serverTime = serverModifiedTime?.toLong()
        var localTime = localModifiedTime?.toLong()

        val pattern = "yyyy-MM-dd HH:mm:ss"
        val simpleDateFormat = SimpleDateFormat(pattern)
        if (serverTime != null && localTime != null) {
            var servertimeFormatted = simpleDateFormat.format(Date(serverTime))
            var localtimeFormatted = simpleDateFormat.format(Date(localTime))
            if (servertimeFormatted.equals(localtimeFormatted)) {
                isEqual = true
            }
        } else {
            isEqual = false
        }
        return isEqual
    }

    fun printAllIds(list: ArrayList<String>) {
        for (id in list) {
            Log.d("Print", id);
        }
    }


   override fun syncContactsWithServer() {

        var contactListFromPhoneBook = getContactsFromPhoneBook()
        sendContactsToServer(contactListFromPhoneBook)
    }


    private fun sendContactsToServer(contactList: ArrayList<Contact>) {
        var numberLIst:ArrayList<String> = ArrayList()
        for(contact in contactList){
            //  var number:String=contact.Contact_phone.replace("+","");
            var  number = contact.Contact_phone.replace("\\s".toRegex(), "")
            numberLIst.add(number)
        }


        normalizePhoneNumbers(numberLIst)


        var contactsJSON = "["
        for(i in 0 until numberLIst.size){
            contactsJSON+="\""+ numberLIst[i]+"\""
            if(i==numberLIst.size-1){

            }else{
                contactsJSON+=","
            }
        }

        contactsJSON+="]"



        var user  = auth!!.currentUser
        user?.getIdToken(true)
                ?.addOnCompleteListener(OnCompleteListener<GetTokenResult> { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result!!.token
                        var service = ServiceRest()

                        var params = java.util.HashMap<String, String>()
                        params.put("token",idToken!!)
                        params.put("contactList",contactsJSON)

                        service.getRegisteredContacts(context,"getRegisteredUsers",params,{response ->
                         //   Log.d("backend response",response)
                            if(response!=null&&response.length>0){

                                //find a way to distinguish success and error response
                                //incase of success only ,proceed
                                //compare results and save to db
                                saveToDB(response,contactList)

                            }else{

                                saveToDB("[]",contactList)
                            }
                        })

                    } else {
                        // Handle error -> task.getException();
                        saveToDB("[]",contactList)
                    }
                })

        /*if(contactArray!=null&&contactArray.length()>0){
            sendContactsJSON(contactArray)
        }*/
    }

    private fun saveToDB(response: String,localCOntacts:ArrayList<Contact>) {
        var registeredList = JSONArray(response)
        if(registeredList!=null&&registeredList.length()>0){

            for(i in 0 until registeredList.length()){
                for(phoneContact in localCOntacts){

                    var  number = phoneContact.Contact_phone.replace("\\s".toRegex(), "")
                    if(registeredList.getJSONObject(i).getString("phoneNumber").contains(number)){
                        phoneContact.Contact_uuid = registeredList.getJSONObject(i).getString("uid")
                        phoneContact.Contact_email = registeredList.getJSONObject(i).getString("email")
                        phoneContact.Contact_name_public = registeredList.getJSONObject(i).getString("name")
                        phoneContact.Contact_phone = registeredList.getJSONObject(i).getString("phoneNumber")

                        var profileImageUrl =  storageReference?.child("displayImages/"+phoneContact.Contact_uuid+".jpg")


                        val ONE_MEGABYTE: Long = 1024 * 1024
                        profileImageUrl.getBytes(ONE_MEGABYTE).addOnSuccessListener {bytes ->
                            // Data for "images/island.jpg" is returned, use this as needed

                            Single.fromCallable {

                                var options =  BitmapFactory.Options()
                                options.inMutable = true
                                var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options);


                                ImageSaver(context)
                                        .setFileName(phoneContact.Contact_uuid+".jpg")
                                        .setExternal(false)//image save in external directory or app folder default value is false
                                        .setDirectory("profile")
                                        .save(bmp); //Bitmap from your code

                            }.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(Consumer {

                                    })




                        }.addOnFailureListener {
                            // Handle any errors
                        }


                    }
                }
            }

        }

        //insert in DB
        var db = AppDatabase.getAppDatabase(context)


        Single.fromCallable({
            db?.contactDao()?.insertAllContactIntoContactTable(localCOntacts)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterSuccess {
                   ApplicationController.preferenceManager?.contactSyncDate = System.currentTimeMillis()
                   Log.d(TAG,"saved contacts to DB after syncing")
                }
                .subscribe()

    }


    private fun normalisePhoneNumbers(numberLIst: ArrayList<Contact>){
        var myphoneNumberUtil = PhoneNumberUtil.getInstance()
        for(i in 0 until numberLIst.size){

            var number = numberLIst[i]

            try {
                var phoneNumber = myphoneNumberUtil.parse(number.Contact_phone,null)

                val isValid = myphoneNumberUtil.isValidNumber(phoneNumber)&&number.Contact_phone.length>=5 // returns true if valid
                if (isValid) {
                    // Actions to perform if the number is valid


                } else {
                    // Do necessary actions if its not valid

                    numberLIst[i].Contact_phone=""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                /* if(e.message?.trim()==="INVALID_COUNTRY_CODE. Missing or invalid default region.")
                 {*/
                var tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                var countryCode = tm.simCountryIso;
                var daillingCodeForCountry = getCountryDiallingCode(countryCode)
                number.Contact_phone=daillingCodeForCountry+number.Contact_phone
                numberLIst[i] = number

                //   }
            }

            if(!numberLIst[i].Contact_phone.startsWith("+")){
                numberLIst[i].Contact_phone="+"+numberLIst[i].Contact_phone
            }

        }
    }


    private fun normalizePhoneNumbers(numberLIst: ArrayList<String>) {
        //check if first 1 to 3 characters match any country code . if false--> append the user's country code to the number
        //if true, check if string contains "+" if true, return .else --> add "+"
        //make use of phonenumberUtils class

        var myphoneNumberUtil = PhoneNumberUtil.getInstance()
        for(i in 0 until numberLIst.size){

            var number = numberLIst[i]

            try {
                var phoneNumber = myphoneNumberUtil.parse(number,null)

                val isValid = myphoneNumberUtil.isValidNumber(phoneNumber)&&number.length>=5 // returns true if valid
                if (isValid) {
                    // Actions to perform if the number is valid


                } else {
                    // Do necessary actions if its not valid

                    numberLIst[i]=""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                /* if(e.message?.trim()==="INVALID_COUNTRY_CODE. Missing or invalid default region.")
                 {*/
                var tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                var countryCode = tm.simCountryIso;
                var daillingCodeForCountry = getCountryDiallingCode(countryCode)
                number=daillingCodeForCountry+number
                numberLIst[i] = number

                //   }
            }

            if(!numberLIst[i].startsWith("+")){
                numberLIst[i]="+"+numberLIst[i]
            }

        }

    }


    fun getCountryDiallingCode(countryCode:String):String{
        var contryDialCode=""
        var countryId = countryCode.toUpperCase()
        val arrContryCode = context.resources.getStringArray(R.array.DialingCountryCode)
        for (i in arrContryCode.indices) {
            val arrDial = arrContryCode[i].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (arrDial[1].trim { it <= ' ' } == countryId.trim()) {
                contryDialCode = arrDial[0]
                break
            }
        }
        return contryDialCode
    }
    private fun showError(message: String?) {
        //do nothing
        //set sync status false
        Log.d("tag",message)
    }

    private fun showResult(result: String) {
        //if contact not null, search that number and get details
        Log.d("result",result);
    }




    fun getContactsFromPhoneBook(): ArrayList<Contact> {
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)//plus any other properties you wish to query
        var contactsList = ArrayList<Contact>()
        var cursor: Cursor? = null
        try {
            cursor = context?.getContentResolver()?.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null)
        } catch (e: SecurityException) {
            //SecurityException can be thrown if we don't have the right permissions
        }


        if (cursor != null) {
            try {
                val normalizedNumbersAlreadyFound = HashSet<Any?>()
                val indexOfNormalizedNumber = cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)
                val indexOfDisplayName = cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val indexOfDisplayNumber = cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor!!.moveToNext()) {
                    val normalizedNumber = cursor!!.getString(indexOfNormalizedNumber)
                    if (normalizedNumbersAlreadyFound.add(normalizedNumber)) {
                        val displayName = cursor!!.getString(indexOfDisplayName)
                        val displayNumber = cursor!!.getString(indexOfDisplayNumber)
                        //haven't seen this number yet: do something with this contact!
                        var defaultId = displayNumber.replace("+","")
                        var trimmed  = defaultId.replace("\\s".toRegex(), "")
                        try {
                            var intId = trimmed.toLong()
                            var contact  = Contact(intId,displayName,"",displayNumber,"",intId.toString(),"")

                            contactsList.add(contact)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        //don't do anything with this contact because we've already found this number
                    }
                }
            } finally {
                cursor!!.close()
            }
        }


        normalisePhoneNumbers(contactsList)

        return contactsList
    }


}
