package com.monero.activitydetail.presenter.expenselist

import android.arch.lifecycle.LiveData
import android.content.Context
import com.monero.activitydetail.presenter.detail.IDetailView
import com.monero.helper.AppDatabase
import com.monero.models.Expense
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

/**
 * Created by tom.saju on 7/25/2018.
 */
class ExpenseListPresenter:IExpenseListPresenter {
    var context: Context
    var view: IExpenseListView
    var result: LiveData<List<Expense>>?=null

    constructor(context: Context, view: IExpenseListView) {
        this.context = context
        this.view = view
    }

    override fun getAllExpensesForActivity(activity_id: Long){

        Single.fromCallable{
            AppDatabase.db = AppDatabase.getAppDatabase(context)
            result = AppDatabase.db?.expenseDao()?.getAllExpensesForActivity(activity_id) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    val list = result
                    if(list!=null) {
                        view.onExpensesFetched(list)
                    }
                })

    }
}