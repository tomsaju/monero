package com.monero.main.presenter.accountbook

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.support.v4.app.Fragment
import android.util.Log
import com.google.gson.Gson
import com.monero.Application.ApplicationController
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
import kotlin.collections.ArrayList

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
    private var allActivityIdList: List<String>?=null
    private var allExpensesForActivity: LiveData<List<Expense>>? = null
    var safetyCounter =0
    var loopLimit = 0
    var totalValuesSum =0

    constructor(context: Context, view: IAccountBookView) {
        this.context = context
        this.view = view
    }



    override fun getAllPendingTransactions() {
        //1.Get list of all activities
        //2.For each activities, get list of expense and get pending transactions
        //3.Add pending transactions from all activities to a list

        //clear pending transaction list
        allPendingTransaction = ArrayList()
        getAllActivitiesList()


    }



    override fun getAllActivitiesList() {
        Single.fromCallable {
            AppDatabase.db = AppDatabase.getAppDatabase(context)
             allActivityIdList = AppDatabase.db?.activitesDao()?.getAllActivityIds()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    val idList = allActivityIdList
                    if (idList != null) {
                        view.onAllActivitiesFetched(idList)
                    }
                })
    }



    override fun getPendingTransactionForActivity(id: String) {
        Single.fromCallable {
            AppDatabase.db = AppDatabase.getAppDatabase(context)
            allExpensesForActivity = AppDatabase.db?.expenseDao()?.getAllExpensesForActivity(id)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    val list = allExpensesForActivity
                    if (list != null) {
                        onExpensesFetched(list)
                    }
                })
    }



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
        var totalPaidList: HashMap<String, Int> = HashMap() //<User id,amount> ,p(n)
        var totalOwedList: HashMap<String, Int> = HashMap() //                     o(n)

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



        /* if(getsumof(totalPaidList) !=getsumof(totalOwedList)){
             Log.d(logTag,"Error. Payments doesnt match")
             return
         }*/

        if (true) {
            var map = HashMap<String, Int>()
            var sum = getsumof(totalPaidList)
            var amountPaid: Int = 0
            for (recepient in totalOwedList) {

                var paymentByThisUser = totalPaidList.get(recepient.key)
                if (paymentByThisUser == null) {
                    amountPaid = 0
                } else {
                    amountPaid = paymentByThisUser
                }


                map.put(recepient.key, amountPaid - recepient.value)


            }
            // FindPath.findPath(map)
            rawtransactionList.clear()
            safetyCounter = 0
            loopLimit = map.size*2 //actually it is map.size but just incase... we'll provide double freedom
            totalValuesSum = map.map { it.value }.sum()
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
        allPendingTransaction.addAll(removeAllTransactionsWithoutme(pendingTransactions))
        view.onTransactionsfetched(allPendingTransaction)
    }

    private fun removeAllTransactionsWithoutme(pendingTransactions: ArrayList<PendingTransaction>):ArrayList<PendingTransaction> {
        var myCredential = ApplicationController.preferenceManager!!.myPhone
        var filteredList:ArrayList<PendingTransaction> = ArrayList()
        for(transaction in pendingTransactions){
            if(transaction.payer.user_phone==myCredential||transaction.reciepient.user_phone==myCredential){
                //either i owe or they owe to me
                filteredList.add(transaction)
            }
        }
        return filteredList
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

        fun getsumof(totalPaidList: HashMap<String, Int>): Int {
            var sum = 0
            for (items in totalPaidList) {
                sum += items.value
            }
            return sum
        }

    fun divideTransactions(poolList: HashMap<String, Int>) {

        //change this functions implementation such that there should be transaction only with people that
        //know each other
        //example is if i have an activity among my family members and other with my friends, do nt settle between
        //people in these groups since they dont know each other



        //
        /**
         * what this method does is actually decides "who pays whom and how much"
         *
         * The input contains a hashmap showing a list of user id's to some money amount
         *  "9233869683354" -> "6667"
        "4616934841698" -> "-3333"
        "1538978280562" -> "-3333"


        user id -> amount
        if amount is positive,it is the amount he should get (coz he has spend that money some point earlier)
        if amount is negative, it is the amount he should give(coz some body have spend that amount for him)

        the function loops through the hashmap and tries to settle (starting by combining largest and smallest money)
        settles until the amount become less than or equal to 1
         */


        //safety counter to prevent stack overflow errors by infinite loops
        if(safetyCounter>loopLimit){
            return
        }

        safetyCounter++





        val Max_Value = Collections.max(poolList.values) as Int
        val Min_Value = Collections.min(poolList.values) as Int
        if (Max_Value !== Min_Value&&Max_Value-Min_Value>totalValuesSum) {
            val Max_Key:String = getKeyFromValue(poolList, Max_Value)
            val Min_Key:String = getKeyFromValue(poolList, Min_Value)
            var result: Int = Max_Value + Min_Value
            if (result >= 1) {
                //printBill.add(Min_Key + " needs to pay " + Max_Key + ":" + round(Math.abs(Min_Value), 2));
                println(Min_Key.toString() + " needs to pay " + Max_Key + ":" + Min_Value)
                var transaction = RawTransaction(System.currentTimeMillis(),Min_Key,Max_Key,Math.abs(Min_Value))
                rawtransactionList.add(transaction)
                poolList.remove(Max_Key)
                poolList.remove(Min_Key)
                poolList.put(Max_Key, result)
                poolList.put(Min_Key, 0)
            } else {
                // printBill.add(Min_Key + " needs to pay " + Max_Key + ":" + round(Math.abs(Max_Value), 2));
                println(Min_Key.toString() + " needs to pay " + Max_Key + ":" + Max_Value)
                var transaction = RawTransaction(System.currentTimeMillis(),Min_Key,Max_Key,Math.abs(Max_Value))
                rawtransactionList.add(transaction)

                poolList.remove(Max_Key)
                poolList.remove(Min_Key)
                poolList.put(Max_Key, 0)
                poolList.put(Min_Key, result)
            }
            divideTransactions(poolList)
        }



        println("Completed loop")
        Log.d("Iterations",safetyCounter.toString())

    }

        fun getKeyFromValue(hm: HashMap<String, Int>, value: Int): String {
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
