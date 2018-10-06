package com.monero.models

/**
 * Created by tom.saju on 7/25/2018.
 */
data class PendingTransaction(val transaction_id:Long, val payer:User, val reciepient:User, val amount: Int)