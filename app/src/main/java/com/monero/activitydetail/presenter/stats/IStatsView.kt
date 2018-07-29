package com.monero.activitydetail.presenter.stats

import com.monero.models.PendingTransaction

/**
 * Created by tom.saju on 7/25/2018.
 */
interface IStatsView {
    fun onPendingTransactionsObtained(pendingTransaction: ArrayList<PendingTransaction>)
}