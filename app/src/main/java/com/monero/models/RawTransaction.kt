package com.monero.models

/**
 * Created by Dreamz on 29-07-2018.
 */
data class RawTransaction (var transactionId:Long, var payerId:String, var recpientId:String, var amount: Int)