package com.monero.activitydetail.presenter.expense

import android.content.Context
import com.monero.helper.AppDatabase
import com.monero.models.Expense
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by tom.saju on 7/19/2018.
 */
class ExpenseFragmentPresenter: IExpenseFragmentPresenter {


    var context:Context
    var view:IExpenseFragmentView

    constructor(context: Context, view: IExpenseFragmentView){
        this.context = context
        this.view = view
    }

    override fun saveExpense(expense: Expense) {
        Single.fromCallable {

            AppDatabase.db = AppDatabase.getAppDatabase(context)
            AppDatabase.db?.expenseDao()?.insertIntoAExpensesTable(expense)

            for(credit in expense.creditList){
                AppDatabase.db?.creditDao()?.insertIntoCreditTable(credit)
            }

            for(debit in expense.debitList){
                AppDatabase.db?.debitDao()?.insertIntoDebitTable(debit)
            }


        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

}