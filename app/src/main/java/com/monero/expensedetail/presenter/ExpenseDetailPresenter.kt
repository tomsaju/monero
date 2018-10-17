package com.monero.expensedetail.presenter

import android.arch.lifecycle.LiveData
import android.content.Context
import com.monero.activitydetail.presenter.detail.IDetailView
import com.monero.helper.AppDatabase.Companion.db
import com.monero.helper.AppDatabase.Companion.getAppDatabase
import com.monero.models.Expense
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

/**
 * Created by Dreamz on 16-10-2018.
 */
class ExpenseDetailPresenter:IExpenseDetailPresenter {
    var context: Context
    var view: IExpenseDetailView


    constructor(context: Context, view: IExpenseDetailView) {
        this.context = context
        this.view = view
    }

    override fun getExpenseForId(id: String) {
        var expenseData:LiveData<Expense>?=null

        Single.fromCallable {
            db= getAppDatabase(context)
            expenseData = db?.expenseDao()?.getExpenseForId(id)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(Consumer {
            val expense = expenseData
            if(expense!=null){
                view.onExpenseFetched(expense)
            }
        })
    }
}