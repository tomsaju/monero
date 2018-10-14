package com.monero.activitydetail.presenter.history

import android.arch.lifecycle.LiveData
import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.monero.activitydetail.presenter.expense.IExpenseFragmentView
import com.monero.helper.AppDatabase
import com.monero.helper.AppDatabase.Companion.db
import com.monero.helper.AppDatabase.Companion.getAppDatabase
import com.monero.models.HistoryLogItem
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

/**
 * Created by Dreamz on 12-10-2018.
 */
class HistoryPresenter:IHistoryPresenter {


    var context: Context
    var view: IHistoryView
    var firestoreDb: FirebaseFirestore? = null
    lateinit var activityLog:LiveData<List<HistoryLogItem>>
    constructor(context: Context, view: IHistoryView) {
        this.context = context
        this.view = view
        firestoreDb = FirebaseFirestore.getInstance()
    }

    override fun getAllHistoryLogForActivity(activityId: String) {

        Single.fromCallable{
            AppDatabase.db = AppDatabase.getAppDatabase(context)
            activityLog = AppDatabase.db!!.historyDao()?.getAllHistoryLogsForActivity(activityId) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    val list = activityLog
                    if(list!=null) {
                        view.onAllLogsFetched(list)
                    }
                })
    }
}