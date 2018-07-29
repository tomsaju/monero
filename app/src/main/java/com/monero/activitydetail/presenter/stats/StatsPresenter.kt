package com.monero.activitydetail.presenter.stats

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
import com.monero.sample.FindPath
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.single
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.Map


/**
 * Created by tom.saju on 7/25/2018.
 */
class StatsPresenter:IStatsPresenter {
    var logTag:String = "StatsPresenter"
    var testLimit:Int = 0
    var context: Context
    var view: IStatsView
    var allExpenses: LiveData<List<Expense>>?=null
    var allUsers:ArrayList<User> = ArrayList()
    var allPendingTransaction:ArrayList<PendingTransaction> = ArrayList()
    var rawtransactionList:ArrayList<RawTransaction> = ArrayList()


    constructor(context: Context, view: IStatsView) {
        this.context = context
        this.view = view
    }

    override fun getAllPendingTransactions(activityId: Long) {

        getAllExpensesForActivity(activityId)



    }

    fun getAllExpensesForActivity(activity_id: Long){
        Log.d(logTag,"getALlexpensesForActivity")
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
        Log.d(logTag,"onExpensesFetched")
        //group all the money paid[p(n)] and owed [o(n)] of all users (n) sepeartely
        //if p(n) - o(n) ==0 -->No pending transactions for that user
        //if p(n) - o(n) >0 -->Someone should pay him x amount (x=p(n)-o(n))
        //if p(n) - o(n) <0 -->He should pay someone x amount
        //Look for users who have same value and opposite sign for x if found, group them in same PendingTransaction
        //find the user with largest value of x, find the user with largest value for (-x) and group them together

        expenses.observe(view as Fragment,object: Observer<List<Expense>> {
            override fun onChanged(expenseList: List<Expense>?) {
                if(expenseList!=null) {
                    startGrouping(expenseList)
                }
            }

        })


    }


    private fun startGrouping(expenseList: List<Expense>){
        Log.d(logTag,"startGrouping")
        if(expenseList!=null&&!expenseList.isEmpty()) {
            AppDatabase.db = AppDatabase.getAppDatabase(context)
            AppDatabase.db?.activitesDao()?.getAllUsersForActivity(expenseList[0].activity_id)
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe(
                            { allusersJson ->
                                var gson = Gson()
                                var list :List<User> =gson.fromJson(allusersJson , Array<User>::class.java).toList()
                                allUsers = ArrayList(list)
                                groupCreditsAndDebits(expenseList)

                            },
                            { error ->
                                Log.d("Payerselector", error.message)
                            })
        }
    }


    private fun groupCreditsAndDebits(expenseList: List<Expense>) {

        Log.d(logTag,"froup credits and debits")
        var totalPaidList:HashMap<Long,Double> = HashMap() //<User id,amount> ,p(n)
        var totalOwedList:HashMap<Long,Double> = HashMap() //                     o(n)

        for (expense in expenseList){
            var creditList = expense.creditList
            for(credit in creditList){
                if(totalOwedList.containsKey(credit.user_id)){
                    val currentamount =totalOwedList.get(credit.user_id)
                    if(currentamount!=null) {
                        totalOwedList.put(credit.user_id, currentamount + credit.amount)
                    }
                }else{
                    totalOwedList.put(credit.user_id,credit.amount)
                }
            }

            var debitList =expense.debitList
            for(debit in debitList) {
                if(totalPaidList.containsKey(debit.user_id)){
                    val currentamount =totalPaidList.get(debit.user_id)
                    if(currentamount!=null) {
                        totalPaidList.put(debit.user_id, currentamount + debit.amount)
                    }
                }else{
                    totalPaidList.put(debit.user_id,debit.amount)
                }
            }

        }
        //clear pending transaction list
        allPendingTransaction = ArrayList()


       if(getsumof(totalPaidList) !=getsumof(totalOwedList)){
           Log.d(logTag,"Error. Payments doesnt match")
           return
       }

        if(true){
            var map = HashMap<Long,Double>()
            var sum = getsumof(totalPaidList)
            var amountPaid:Double = 0.0
            for(recepient in totalOwedList){

                var paymentByThisUser = totalPaidList.get(recepient.key)
                if(paymentByThisUser==null){
                    amountPaid =0.0
                }else{
                    amountPaid= paymentByThisUser
                }


                map.put(recepient.key,amountPaid -recepient.value)


            }
           // FindPath.findPath(map)
            rawtransactionList.clear()
              divideTransactions(map)
            var pendingTransactions = getPendingTransaction(rawtransactionList)
            for(pendingtxn in pendingTransactions){
                println(pendingtxn.payer.user_name+" should pay "+pendingtxn.reciepient.user_name+" "+pendingtxn.amount)
            }
            view.onPendingTransactionsObtained(pendingTransactions)

            return
        }

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
        while(pendingtransactionsExist(totalPaidList,totalOwedList)){
            Log.d(logTag,"while loop entry")
          var nextLargestPayment = getNextLargestPayment(totalPaidList)
          var nextLargestReceipt = getNextLargestReceipt(totalOwedList)

            val payerId = nextLargestPayment?.key
            val recepientId = nextLargestReceipt?.key

            val paidAmount = nextLargestPayment?.value
            val recievedAmount = nextLargestReceipt?.value

            if(paidAmount!=null&&recievedAmount!=null&&payerId!=null&&recepientId!=null) {
                var amountToBePaid = recievedAmount
                if(recievedAmount<=paidAmount) {
                     amountToBePaid = recievedAmount
                }else{
                    amountToBePaid = paidAmount
                }
                if(payerId!=recepientId) {
                    createPendingTransaction(expenseList.get(0).activity_id, payerId, recepientId, amountToBePaid)
                }
                totalPaidList.put(payerId,paidAmount-amountToBePaid)
                totalOwedList.put(recepientId,recievedAmount-amountToBePaid)
            }



        }


        Log.d("after simpleifying","/////////////////////////////////////////")
        cancelRepeatingPayments(allPendingTransaction)


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

    private fun cancelRepeatingPayments(allPendingTransaction: ArrayList<PendingTransaction>) {
        var simplifiedTransactionList = ArrayList<PendingTransaction>()
        var simplifiedTxnIdList = ArrayList<Long>()
        for(transaction in allPendingTransaction){
            for(anothertransaction in allPendingTransaction){
                if(transaction.transaction_id!=anothertransaction.transaction_id){
                    if(transaction.payer.user_id==anothertransaction.reciepient.user_id
                            &&transaction.reciepient.user_id==anothertransaction.payer.user_id){
                        simplifiedTxnIdList.add(transaction.transaction_id)
                        simplifiedTxnIdList.add(anothertransaction.transaction_id)

                        if(transaction.amount>anothertransaction.amount){
                            var amountToBePayed = transaction.amount-anothertransaction.amount
                            var pendingtransaction = PendingTransaction(System.currentTimeMillis(),transaction.payer,transaction.reciepient,amountToBePayed)
                            simplifiedTransactionList.add(pendingtransaction)
                        }else{
                            var amountToBePayed = anothertransaction.amount-transaction.amount
                            var pendingtransaction = PendingTransaction(System.currentTimeMillis(),anothertransaction.payer,anothertransaction.reciepient,amountToBePayed)
                            simplifiedTransactionList.add(pendingtransaction)
                        }
                    }
                }
            }
        }
        var finalList = ArrayList<PendingTransaction>()
        for(transactions in allPendingTransaction){
            if(!simplifiedTxnIdList.contains(transactions.transaction_id)){
                finalList.add(transactions)
            }
        }
        finalList.addAll(simplifiedTransactionList)
        Log.d("After simplifying ","/////////////////////////////////////////////")
        for(item in finalList){
            Log.d("pending payment :",item.reciepient.user_name+" should pay "+item.amount+" to "+item.payer.user_name)
        }
    }

    private fun getsumof(totalPaidList: HashMap<Long, Double>): Double {
        var sum=0.0
        for(items in totalPaidList){
            sum+=items.value
        }
        return sum
    }

    private fun getNextLargestReceipt(totalOwedList: HashMap<Long, Double>): kotlin.collections.Map.Entry<Long, Double>? {
        var maxVal = totalOwedList.maxBy { it.value }
        return maxVal
    }

    private fun getNextLargestPayment(totalPaidList: HashMap<Long, Double>): kotlin.collections.Map.Entry<Long, Double>? {
        var maxVal = totalPaidList.maxBy { it.value }
        return maxVal
    }

    private fun pendingtransactionsExist(totalPaidList: HashMap<Long, Double>, totalOwedList: HashMap<Long, Double>): Boolean {
        if(testLimit>100){

            return false
        }
        testLimit++
        for(payment in totalPaidList){
            if(payment.value>0){
                return true
            }
        }

        for(receipts in totalOwedList){
            if(receipts.value>0){
                return true
            }
        }

        return false
    }


    fun createPendingTransaction(activity_id:Long,payer_userId:Long,recepient_userId:Long,amount:Double){
        Log.d(logTag,"createPendingTransaction")
        //find user from ID
        var payer = getUserForId(payer_userId,allUsers)
        var recepient = getUserForId(recepient_userId,allUsers)

        if(payer!=null&&recepient!=null){
            var pendingTransaction  = PendingTransaction(System.currentTimeMillis(),payer,recepient,amount)
            allPendingTransaction.add(pendingTransaction)
            //payer is the person who paid the bill initially(during expense creation) and recepient
            //is the one who was responsible. so in a pending transaction the recepient should
            //pay the money back to payer. Thus payer becomes a receipient and vice versa
            Log.d("pending payment :",recepient.user_name+" should pay "+amount+" to "+payer.user_name)
        }

    }




    fun getUserForId(user_id:Long,allUserList:ArrayList<User>):User?{
        for(user in allUserList){
            if(user.user_id==user_id){
                return user
            }
        }
        return null
    }

///////////////////////////////
internal var parm: HashMap<String, Double> = HashMap()


    fun divideTransactions(poolList: HashMap<Long, Double>) {


        val Max_Value = Collections.max(poolList.values) as Double
        val Min_Value = Collections.min(poolList.values) as Double
        if (Max_Value !== Min_Value) {
            val Max_Key:Long = getKeyFromValue(poolList, Max_Value)
            val Min_Key:Long = getKeyFromValue(poolList, Min_Value)
            var result: Double? = Max_Value + Min_Value
            result = round(result!!, 1)
            if (result >= 0.0) {
                //printBill.add(Min_Key + " needs to pay " + Max_Key + ":" + round(Math.abs(Min_Value), 2));
                   println(Min_Key.toString() + " needs to pay " + Max_Key + ":" + round(Math.abs(Min_Value), 2))
                var transaction = RawTransaction(System.currentTimeMillis(),Min_Key,Max_Key,round(Math.abs(Min_Value), 2))
                rawtransactionList.add(transaction)
                poolList.remove(Max_Key)
                poolList.remove(Min_Key)
                poolList.put(Max_Key, result)
                poolList.put(Min_Key, 0.0)
            } else {
                // printBill.add(Min_Key + " needs to pay " + Max_Key + ":" + round(Math.abs(Max_Value), 2));
                  println(Min_Key.toString() + " needs to pay " + Max_Key + ":" + round(Math.abs(Max_Value), 2))
                var transaction = RawTransaction(System.currentTimeMillis(),Min_Key,Max_Key,round(Math.abs(Max_Value), 2))
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

    fun getKeyFromValue(hm: HashMap<Long, Double>, value: Double?): Long {
        for (o in hm.keys) {
            if (hm[o] == value) {
                return o
            }
        }
        return 0
    }

    fun round(value: Double, places: Int): Double {
        if (places < 0)
            throw IllegalArgumentException()

        var bd = BigDecimal(value)
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        return bd.toDouble()
    }






}