package com.monero.activitydetail.presenter.expense

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.monero.Dao.DBContract
import com.monero.helper.AppDatabase
import com.monero.helper.converters.TagConverter
import com.monero.models.Expense
import com.monero.models.User
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.google.firebase.firestore.DocumentReference
import com.monero.helper.AppDatabase.Companion.db
import com.monero.helper.AppDatabase.Companion.getAppDatabase
import com.monero.helper.PreferenceManager
import com.monero.models.HistoryLogItem
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import java.util.*


/**
 * Created by tom.saju on 7/19/2018.
 */
class ExpenseFragmentPresenter: IExpenseFragmentPresenter {

    var TAG = "ExpenseFragmentPresenter"
    var context: Context
    var view: IExpenseFragmentView
    var firestoreDb: FirebaseFirestore? = null
    var auth: FirebaseAuth


    constructor(context: Context, view: IExpenseFragmentView) {
        this.context = context
        this.view = view
        firestoreDb = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()!!
    }

    override fun saveExpense(expense: Expense) {
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

        //first save in local.
        //On success, save in cloud.
        //on save to cloud success, replace the id with new id

        //We can get the document id before saving . but here we are not using it because we need to work in offline mode also
        Single.fromCallable {

            AppDatabase.db = AppDatabase.getAppDatabase(context)
            AppDatabase.db?.expenseDao()?.insertIntoAExpensesTable(expense)

            for (credit in expense.creditList) {
                AppDatabase.db?.creditDao()?.insertIntoCreditTable(credit)
            }

            for (debit in expense.debitList) {
                AppDatabase.db?.debitDao()?.insertIntoDebitTable(debit)
            }

            AppDatabase.db?.activitesDao()?.updateActivityModifiedTime(expense.activity_id,System.currentTimeMillis().toString())

        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterSuccess {

                    val authorId = auth.currentUser?.uid
                    val authorName = auth.currentUser?.displayName

                    if(authorName!=null&&authorId!=null) {

                        //add this event to history
                        var historyLog = HistoryLogItem(UUID.randomUUID().toString(),
                                authorId,
                                authorName,
                                DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_EXPENSE,
                                expense.created_date,
                                expense.title,
                                "",
                                expense.id,
                                expense.activity_id,
                                false)

                        saveHistory(historyLog)
                    }
                    //Log.d
                    Log.d("Tag","expense saved locally")
                    //push this expense id to activity db
                    firestoreDb?.collection("expenses")?.document(expense.id)?.set(newExpense)

                            ?.addOnSuccessListener { DocumentReference ->
                                expense.sync_status = true

                                Observable.fromCallable {
                                    AppDatabase.db = AppDatabase.getAppDatabase(context)
                                    AppDatabase.db?.expenseDao()?.updateSyncStatusForExpense(true,expense.id) // .database?.personDao()?.insert(person)
                                }.subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread()).subscribe()

                            }
                            ?.addOnFailureListener { e ->
                                //failure
                            }
                }
                .subscribe()


    }

    private fun saveHistory(historyLog: HistoryLogItem) {

        Observable.fromCallable {
            AppDatabase.db = AppDatabase.getAppDatabase(context)
            AppDatabase.db?.historyDao()?.insertIntoHistoryTable(historyLog) // .database?.personDao()?.insert(person)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ orderItem ->
            //sync to cloud

            saveHistorytoCloud(historyLog)

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
        historylog.put(DBContract.HISTORY_LOG_ITEM_TABLE.SYNC_STATUS, historyItem.SyncStatus)

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

    override fun getExpenseForId(expenseId: String) {
        var expense:Expense?=null

        Single.fromCallable {
            db= getAppDatabase(context)
            expense = db?.expenseDao()?.getExpenseForIdSingle(expenseId)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(Consumer {
            val workingExpense = expense
            if(workingExpense!=null){
                view.onExpenseFetched(workingExpense)
            }
        })
    }
}