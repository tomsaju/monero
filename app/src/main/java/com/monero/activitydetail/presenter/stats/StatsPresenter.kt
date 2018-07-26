package com.monero.activitydetail.presenter.stats

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.support.v4.app.Fragment
import com.google.gson.Gson
import com.monero.helper.AppDatabase
import com.monero.models.Expense
import com.monero.models.PendingTransaction
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
    var allUsers:ArrayList<User> = ArrayList()
    var allPendingTransaction:ArrayList<PendingTransaction> = ArrayList()

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

        if(expenseList!=null&&!expenseList.isEmpty()) {
            allUsers = getAllParticipantsForthisActivity(expenseList[0].activity_id)
        }
        var totalPaidList:HashMap<Long,Double> = HashMap() //<User id,amount> ,p(n)
        var totalOwedList:HashMap<Long,Double> = HashMap() //                     o(n)

        for (expense in expenseList){
            var creditList = expense.creditList
            for(credit in creditList){
                if(totalPaidList.containsKey(credit.user_id)){
                    val currentamount =totalPaidList.get(credit.user_id)
                    if(currentamount!=null) {
                        totalPaidList.put(credit.user_id, currentamount + credit.amount)
                    }
                }else{
                    totalPaidList.put(credit.user_id,credit.amount)
                }
            }

            var debitList =expense.debitList
            for(debit in debitList) {
                if(totalOwedList.containsKey(debit.user_id)){
                    val currentamount =totalOwedList.get(debit.user_id)
                    if(currentamount!=null) {
                        totalOwedList.put(debit.user_id, currentamount + debit.amount)
                    }
                }else{
                    totalOwedList.put(debit.user_id,debit.amount)
                }
            }

        }
        //clear pending transaction list
        allPendingTransaction = ArrayList()

        //find the items with same value in p an o
        for(payment in totalPaidList){
            var paidAmount = payment.value
            for(debt in totalOwedList){
                if(paidAmount==debt.value){
                    //group those two together
                    createPendingTransaction(expenseList.get(0).activity_id,payment.key,debt.key,paidAmount)
                    payment.setValue(0.0)
                    debt.setValue(0.0)
                }
            }
        }

        //set up a while loop that executes until all values of p(n) and o(n) are zero
        //while loop should
        //1)check for the largest value in p list and o list
        //2)make a pending transaction for users of those p and o respectively
        //3)update values for that p and o after the pending transaction n step 2
        //repeat


    }


    fun createPendingTransaction(activity_id:Long,payer_userId:Long,recepient_userId:Long,amount:Double){
        //find user from ID
        var payer = getUserForId(payer_userId,allUsers)
        var recepient = getUserForId(recepient_userId,allUsers)

        if(payer!=null&&recepient!=null){
            var pendingTransaction  = PendingTransaction(System.currentTimeMillis(),payer,recepient,amount)
            allPendingTransaction.add(pendingTransaction)
        }

    }


    fun getAllParticipantsForthisActivity(id: Long): ArrayList<User> {
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

    fun getUserForId(user_id:Long,allUserList:ArrayList<User>):User?{
        for(user in allUserList){
            if(user.user_id==user_id){
                return user
            }
        }
        return null
    }


}