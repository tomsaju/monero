package com.monero.activitydetail.presenter.stats

import com.monero.models.PendingTransaction
import com.monero.models.RawTransaction
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by Dreamz on 29-07-2018.
 */
class Divider {

    internal var parm: HashMap<String, Double> = HashMap()


    fun divideTransactions(poolList: HashMap<Long, Double>):ArrayList<RawTransaction> {
        var rawtransactionList:ArrayList<RawTransaction> = ArrayList()

        val Max_Value = Collections.max(poolList.values) as Double
        val Min_Value = Collections.min(poolList.values) as Double
        if (Max_Value !== Min_Value) {
            val Max_Key:Long = getKeyFromValue(poolList, Max_Value)
            val Min_Key:Long = getKeyFromValue(poolList, Min_Value)
            var result: Double? = Max_Value + Min_Value
            result = round(result!!, 1)
            if (result >= 0.0) {
                //printBill.add(Min_Key + " needs to pay " + Max_Key + ":" + round(Math.abs(Min_Value), 2));
            //    println(Min_Key.toString() + " needs to pay " + Max_Key + ":" + round(Math.abs(Min_Value), 2))
                var transaction = RawTransaction(System.currentTimeMillis(),Min_Key,Max_Key,round(Math.abs(Min_Value), 2))
                rawtransactionList.add(transaction)
                poolList.remove(Max_Key)
                poolList.remove(Min_Key)
                poolList.put(Max_Key, result)
                poolList.put(Min_Key, 0.0)
            } else {
                // printBill.add(Min_Key + " needs to pay " + Max_Key + ":" + round(Math.abs(Max_Value), 2));
              //  println(Min_Key.toString() + " needs to pay " + Max_Key + ":" + round(Math.abs(Max_Value), 2))
                var transaction = RawTransaction(System.currentTimeMillis(),Min_Key,Max_Key,round(Math.abs(Max_Value), 2))
                rawtransactionList.add(transaction)

                poolList.remove(Max_Key)
                poolList.remove(Min_Key)
                poolList.put(Max_Key, 0.0)
                poolList.put(Min_Key, result)
            }
            divideTransactions(poolList)
        }


        return rawtransactionList
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