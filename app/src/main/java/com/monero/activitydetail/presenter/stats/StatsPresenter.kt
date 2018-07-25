package com.monero.activitydetail.presenter.stats

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.support.v4.app.Fragment
import com.monero.helper.AppDatabase
import com.monero.models.Expense
import com.monero.models.User
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

/**
 * Created by tom.saju on 7/25/2018.
 */
class StatsPresenter:IStatsPresenter {

    var context: Context
    var view: IStatsView
    var allExpenses: LiveData<List<Expense>>?=null


    constructor(context: Context, view: IStatsView) {
        this.context = context
        this.view = view
    }

    override fun getAllPendingTransactions(activityId: Long) {
        getAllExpensesForActivity(activityId)
    }

    fun getAllExpensesForActivity(activity_id: Long){
        Single.fromCallable{
            AppDatabase.db = AppDatabase.getAppDatabase(context)
            allExpenses = AppDatabase.db?.expenseDao()?.getAllExpensesForActivity(activity_id) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    val list = allExpenses
                    if(list!=null) {
                        onExpensesFetched(list)
                    }
                })

    }

    fun onExpensesFetched(expenses: LiveData<List<Expense>>){
        //group all the money paid[p(n)] and owed [o(n)] of all users (n) sepeartely
        //if p(n) - o(n) ==0 -->No pending transactions for that user
        //if p(n) - o(n) >0 -->Someone should pay him x amount (x=p(n)-o(n))
        //if p(n) - o(n) <0 -->He should pay someone x amount
        //Look for users who have same value and opposite sign for x if found, group them in same PendingTransaction
        //find the user with largest value of x, find the user with largest value for (-x) and group them together

        expenses.observe(view as Fragment,object: Observer<List<Expense>> {
            override fun onChanged(expenseList: List<Expense>?) {
                if(expenseList!=null) {
                    groupCreditsAndDebits(expenseList)
                }
            }

        })

    }

    private fun groupCreditsAndDebits(expenseList: List<Expense>) {

        var totalPaidList:HashMap<Long,Double> = HashMap() //<User id,amount> ,p(n)
        var totalOwedList:HashMap<Long,Double> = HashMap() //                     o(n)

        for (expense in expenseList){
            var creditList = expense.creditList
            for(credit in creditList){
                if(totalPaidList.containsKey(credit.user_id)){
                    val currentamount =totalPaidList.get(credit.user_id)
                    totalPaidList.put(credit.user_id,currentamount+credit.amount)
                }else{
                    totalPaidList.put(credit.user_id,credit.amount)
                }
            }

            var debitList =expense.debitList
            for(debit in debitList) {
                if(totalOwedList.containsKey(debit.user_id)){
                    val currentamount =totalOwedList.get(debit.user_id)
                    totalOwedList.put(debit.user_id,currentamount+debit.amount)
                }else{
                    totalOwedList.put(debit.user_id,debit.amount)
                }
            }

        }
    }
}