package com.monero.helper

import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.work.Worker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.monero.Dao.DBContract
import com.monero.helper.AppDatabase.Companion.db
import com.monero.helper.AppDatabase.Companion.getAppDatabase
import com.monero.helper.converters.TagConverter
import com.monero.models.Activities
import com.monero.models.Expense
import com.monero.models.HistoryLogItem
import com.monero.models.User
import com.monero.utility.Utility
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by tom.saju on 11/28/2018.
 */
class SyncService : Worker() {


    override fun doWork(): WorkerResult {
        context = applicationContext
        start()
        return if(allOperationSuccess) {
            WorkerResult.SUCCESS
        }else{
            WorkerResult.RETRY
        }
    }

    var TAG = "SyncService"
    private lateinit var storageReference: StorageReference
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable
    var firestoreDb: FirebaseFirestore? = null
    var auth = FirebaseAuth.getInstance()!!
    lateinit var context:Context
    var allOperationSuccess = true



     fun start() {
        // Send a notification that service is started
       // toast("Service started.")
         Log.d(TAG,"Sync started")
        // Do a periodic task

        firestoreDb = FirebaseFirestore.getInstance()
        var storage = FirebaseStorage.getInstance();
        storageReference = storage?.getReference();

        syncPendingItems()

    }






    private fun syncPendingItems(){
        //check activities table,expenses table,history table for any pending items
        //if found , sync them
        allOperationSuccess = true
        checkPendingSyncActivities()
        checkPendingSyncExpensesLogs()
        checkPendingSyncHistoryLogs()
    }

    private fun checkPendingSyncHistoryLogs() {

        Observable.fromCallable {
            if(db==null) {
                db = AppDatabase.getAppDatabase(context)
            }
            db?.historyDao()?.getPendingSyncHistoryLogs() // .database?.personDao()?.insert(person)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ logs:List<HistoryLogItem> ->

            if(logs.isNotEmpty()){
                //upload one by one
                for(i in 0 until logs.size){
                    uploadHistory(logs[i])
                }

            }

            Log.d(TAG,"Sync ended")
        })
    }

    private fun uploadHistory(historyItem: HistoryLogItem) {
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

                    //success
                    Observable.fromCallable {
                        if(db==null) {
                            db = getAppDatabase(context)
                        }
                        db?.historyDao()?.updateSyncStatusForHistory(true,historyItem.log_id) // .database?.personDao()?.insert(person)
                    }.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()).subscribe({ history ->


                    })
                }

                ?.addOnFailureListener { e ->
                    //failure
                    allOperationSuccess = false

                }
    }

    private fun checkPendingSyncExpensesLogs() {
        Observable.fromCallable {
            if(db==null) {
                db = AppDatabase.getAppDatabase(context)
            }
            db?.expenseDao()?.getPendingSyncExpenses() // .database?.personDao()?.insert(person)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ expenses:List<Expense> ->

            if(expenses.isNotEmpty()){
                //upload one by one
                for(i in 0 until expenses.size){
                    uploadExpense(expenses[i])
                }

            }

        })
    }

    private fun uploadExpense(expense: Expense) {

        var convertor = TagConverter()
        var debitJson = convertor.convertDebitListtoString(expense.debitList)
        var creditJson = convertor.convertCreditListtoString(expense.creditList)

        var newExpense = HashMap<String, Any>()
        newExpense.put(DBContract.EXPENSE_TABLE.EXPENSE_ACTIVITY_ID, expense.activity_id)
        newExpense.put(DBContract.EXPENSE_TABLE.EXPENSE_COMMENTS, expense.comments)
        newExpense.put(DBContract.EXPENSE_TABLE.EXPENSE_TITLE, expense.title)
        newExpense.put(DBContract.EXPENSE_TABLE.EXPENSE_AMOUNT, expense.amount)
        newExpense.put(DBContract.EXPENSE_TABLE.EXPENSE_DEBIT, debitJson)
        newExpense.put(DBContract.EXPENSE_TABLE.EXPENSE_CREDITS, creditJson)
        newExpense.put(DBContract.EXPENSE_TABLE.EXPENSE_CREATED_DATE, expense.created_date)

        firestoreDb?.collection("expenses")?.document(expense.id)?.set(newExpense)

                ?.addOnSuccessListener { DocumentReference ->
                   // expense.sync_status = true

                    Observable.fromCallable {
                        if(db==null) {
                            db = AppDatabase.getAppDatabase(context)
                        }
                        db?.expenseDao()?.updateSyncStatusForExpense(true,expense.id) // .database?.personDao()?.insert(person)
                    }.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()).subscribe()

                }
                ?.addOnFailureListener { e ->
                    //failure
                    allOperationSuccess = false
                }
    }


    ///Activities SYnc from local to cloud

    private fun checkPendingSyncActivities() {


        Observable.fromCallable {
            db = AppDatabase.getAppDatabase(context)
            db?.activitesDao()?.getPendingSyncActivities() // .database?.personDao()?.insert(person)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ activities:List<Activities> ->

                if(activities.isNotEmpty()){
                    //upload one by one
                    for(i in 0 until activities.size){
                        uploadActivity(activities[i])
                    }

                }

        })
    }

    private fun uploadActivity(activity: Activities) {

        if(Utility.isNetworkAvailable(context)) {


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
                        updateSyncStatus(activity.id,true)

                    }

                    ?.addOnFailureListener { e ->
                        //failure
                        allOperationSuccess = false

                    }
        }
    }

    private fun updateSyncStatus(activityId: String, status: Boolean) {
        Observable.fromCallable {
            if(db==null) {
                db = AppDatabase.getAppDatabase(context)
            }
            db?.activitesDao()?.updateActivitySyncStatus(activityId,status) // .database?.personDao()?.insert(person)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }



    ////Expenses Sync



}