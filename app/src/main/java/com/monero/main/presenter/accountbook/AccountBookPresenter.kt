package com.monero.main.presenter.accountbook

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.support.v4.app.Fragment
import android.util.Log
import com.google.gson.Gson
import com.monero.helper.AppDatabase
import com.monero.models.Expense
import com.monero.models.PendingTransaction
import com.monero.models.RawTransaction
import com.monero.models.User
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

/**
 * Created by Dreamz on 12-08-2018.
 */
class AccountBookPresenter :IAccountBookPresenter {
    var context: Context
    var view: IAccountBookView
    var allExpenses: LiveData<List<Expense>>?=null
    var allUsers:ArrayList<User> = ArrayList()
    var allPendingTransaction:ArrayList<PendingTransaction> = ArrayList()
    var rawtransactionList:ArrayList<RawTransaction> = ArrayList()

    constructor(context: Context, view: IAccountBookView) {
        this.context = context
        this.view = view
    }

    override fun getAllPendingTransactions() {
        Single.fromCallable {
            AppDatabase.db = AppDatabase.getAppDatabase(context)
            allExpenses = AppDatabase.db?.expenseDao()?.getAllExpenses()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    val list = allExpenses
                    if (list != null) {
                        onExpensesFetched(list)
                    }
                })
    }


    /*fun getAllActivityIds() {
        AppDatabase.db = AppDatabase.getAppDatabase(context)
        var idList = AppDatabase.db?.activitesDao()?.getAllActivityIds()
        for(id in idList!!) {
            getAllPendingTransactions(id)
        }

    }

    fun getAllPendingTransactions(activityId: String) {
        getAllExpensesForActivity(activityId)
    }*/

   /* fun getAllExpensesForActivity(activity_id: String) {
        // Log.d(logTag,"getALlexpensesForActivity")
        Single.fromCallable {
            AppDatabase.db = AppDatabase.getAppDatabase(context)
            allExpenses = AppDatabase.db?.expenseDao()?.getAllExpenses()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    val list = allExpenses
                    if (list != null) {
                        onExpensesFetched(list)
                    }
                })

    }*/

    fun onExpensesFetched(expenses: LiveData<List<Expense>>) {
        //Log.d(logTag,"onExpensesFetched")
        //group all the money paid[p(n)] and owed [o(n)] of all users (n) sepeartely
        //if p(n) - o(n) ==0 -->No pending transactions for that user
        //if p(n) - o(n) >0 -->Someone should pay him x amount (x=p(n)-o(n))
        //if p(n) - o(n) <0 -->He should pay someone x amount
        //Look for users who have same value and opposite sign for x if found, group them in same PendingTransaction
        //find the user with largest value of x, find the user with largest value for (-x) and group them together

        expenses.observe(view as Fragment, object : Observer<List<Expense>> {
            override fun onChanged(expenseList: List<Expense>?) {
                if (expenseList != null) {
                    startGrouping(expenseList)
                }
            }

        })


    }


    private fun startGrouping(expenseList: List<Expense>) {
        // Log.d(logTag,"startGrouping")
        if (expenseList != null && !expenseList.isEmpty()) {
            AppDatabase.db = AppDatabase.getAppDatabase(context)
            AppDatabase.db?.activitesDao()?.getAllUsersForActivity(expenseList[0].activity_id)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe(
                            { allusersJson ->
                                var gson = Gson()
                                var list: List<User> = gson.fromJson(allusersJson, Array<User>::class.java).toList()
                                allUsers = ArrayList(list)
                                groupCreditsAndDebits(expenseList)

                            },
                            { error ->
                                Log.d("Payerselector", error.message)
                            })
        }
    }


    private fun groupCreditsAndDebits(expenseList: List<Expense>) {

        // Log.d(logTag,"froup credits and debits")
        var totalPaidList: HashMap<String, Double> = HashMap() //<User id,amount> ,p(n)
        var totalOwedList: HashMap<String, Double> = HashMap() //                     o(n)

        for (expense in expenseList) {
            var creditList = expense.creditList
            for (credit in creditList) {
                if (totalOwedList.containsKey(credit.user_id)) {
                    val currentamount = totalOwedList.get(credit.user_id)
                    if (currentamount != null) {
                        totalOwedList.put(credit.user_id, currentamount + credit.amount)
                    }
                } else {
                    totalOwedList.put(credit.user_id, credit.amount)
                }
            }

            var debitList = expense.debitList
            for (debit in debitList) {
                if (totalPaidList.containsKey(debit.user_id)) {
                    val currentamount = totalPaidList.get(debit.user_id)
                    if (currentamount != null) {
                        totalPaidList.put(debit.user_id, currentamount + debit.amount)
                    }
                } else {
                    totalPaidList.put(debit.user_id, debit.amount)
                }
            }

        }
        //clear pending transaction list
        allPendingTransaction = ArrayList()


        /* if(getsumof(totalPaidList) !=getsumof(totalOwedList)){
             Log.d(logTag,"Error. Payments doesnt match")
             return
         }*/

        if (true) {
            var map = HashMap<String, Double>()
            var sum = getsumof(totalPaidList)
            var amountPaid: Double = 0.0
            for (recepient in totalOwedList) {

                var paymentByThisUser = totalPaidList.get(recepient.key)
                if (paymentByThisUser == null) {
                    amountPaid = 0.0
                } else {
                    amountPaid = paymentByThisUser
                }


                map.put(recepient.key, amountPaid - recepient.value)


            }
            // FindPath.findPath(map)
            rawtransactionList.clear()
            divideTransactions(map)
            var pendingTransactions = getPendingTransaction(rawtransactionList)
            for (pendingtxn in pendingTransactions) {
                println(pendingtxn.payer.user_name + " should pay " + pendingtxn.reciepient.user_name + " " + pendingtxn.amount)
            }
            onPendingTransactionsObtained(pendingTransactions)

            return
        }
    }

    private fun onPendingTransactionsObtained(pendingTransactions: ArrayList<PendingTransaction>) {
        allPendingTransaction.addAll(pendingTransactions)
        view.onTransactionsfetched(allPendingTransaction)
    }

    private fun getPendingTransaction(rawList: ArrayList<RawTransaction>): ArrayList<PendingTransaction> {
        var list = ArrayList<PendingTransaction>()
        for (item in rawList) {
            val payer = getUserForId(item.payerId, allUsers)
            val recepient = getUserForId(item.recpientId, allUsers)
            if (payer != null && recepient != null) {
                var pendingTransaction =
                        PendingTransaction(item.transactionId, payer, recepient, item.amount)
                list.add(pendingTransaction)
            }
        }
        return list
    }

    fun getUserForId(user_id:String,allUserList:ArrayList<User>):User?{
        for(user in allUserList){
            if(user.user_id==user_id){
                return user
            }
        }
        return null
    }

        fun getsumof(totalPaidList: HashMap<String, Double>): Double {
            var sum = BigDecimal(0).setScale(2, RoundingMode.HALF_UP)
            for (items in totalPaidList) {
                sum += BigDecimal(items.value).setScale(2, RoundingMode.HALF_UP)
            }
            return sum.toDouble()
        }

        fun divideTransactions(poolList: HashMap<String, Double>) {


            val Max_Value = Collections.max(poolList.values) as Double
            val Min_Value = Collections.min(poolList.values) as Double
            if (Max_Value !== Min_Value && Max_Value - Min_Value > 0.09) {
                val Max_Key: String = getKeyFromValue(poolList, Max_Value)
                val Min_Key: String = getKeyFromValue(poolList, Min_Value)
                var result: Double? = Max_Value + Min_Value
                result = round(result!!, 2)
                if (result >= 0.09) {
                    //printBill.add(Min_Key + " needs to pay " + Max_Key + ":" + round(Math.abs(Min_Value), 2));
                    println(Min_Key.toString() + " needs to pay " + Max_Key + ":" + round(Math.abs(Min_Value), 2))
                    var transaction = RawTransaction(System.currentTimeMillis(), Min_Key, Max_Key, round(Math.abs(Min_Value), 2))
                    rawtransactionList.add(transaction)
                    poolList.remove(Max_Key)
                    poolList.remove(Min_Key)
                    poolList.put(Max_Key, result)
                    poolList.put(Min_Key, 0.0)
                } else {
                    // printBill.add(Min_Key + " needs to pay " + Max_Key + ":" + round(Math.abs(Max_Value), 2));
                    println(Min_Key.toString() + " needs to pay " + Max_Key + ":" + round(Math.abs(Max_Value), 2))
                    var transaction = RawTransaction(System.currentTimeMillis(), Min_Key, Max_Key, round(Math.abs(Max_Value), 2))
                    rawtransactionList.add(transaction)

                    poolList.remove(Max_Key)
                    poolList.remove(Min_Key)
                    poolList.put(Max_Key, 0.0)
                    poolList.put(Min_Key, result)
                }
                divideTransactions(poolList)
            }



            println("Completed loop")

        }

        fun getKeyFromValue(hm: HashMap<String, Double>, value: Double?): String {
            for (o in hm.keys) {
                if (hm[o] == value) {
                    return o
                }
            }
            return ""
        }

        fun round(value: Double, places: Int): Double {
            if (places < 0)
                throw IllegalArgumentException()

            var bd = BigDecimal(value)
            bd = bd.setScale(places, RoundingMode.HALF_UP)
            return bd.toDouble()
        }
    }

    fun getKeyFromValue(hm: HashMap<String, Double>, value: Double?): String {
        for (o in hm.keys) {
            if (hm[o] == value) {
                return o
            }
        }
        return ""
    }

    fun round(value: Double, places: Int): Double {
        if (places < 0)
            throw IllegalArgumentException()

        var bd = BigDecimal(value)
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    private fun getsumof(totalPaidList: HashMap<String, Double>): Double {
        var sum=BigDecimal(0).setScale(2,RoundingMode.HALF_UP)
        for(items in totalPaidList){
            sum+=BigDecimal(items.value).setScale(2,RoundingMode.HALF_UP)
        }
        return sum.toDouble()
    }
