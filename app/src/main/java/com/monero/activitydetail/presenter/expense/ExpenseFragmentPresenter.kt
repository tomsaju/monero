package com.monero.activitydetail.presenter.expense

import android.content.Context
import com.google.gson.Gson
import com.monero.helper.AppDatabase
import com.monero.models.Expense
import com.monero.models.User
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

    override fun getAllParticipantsForthisActivity(id: Long): ArrayList<User> {
       var allUserList:ArrayList<User> = ArrayList()

        Single.fromCallable {
            var gson = Gson()
            AppDatabase.db = AppDatabase.getAppDatabase(context)
            var users = AppDatabase.db?.activitesDao()?.getAllUsersForActivity(id)
            var list :List<User> =gson.fromJson(users , Array<User>::class.java).toList()
            allUserList = ArrayList(list)


        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
        return allUserList
    }
}