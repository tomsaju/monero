package com.monero.main.presenter.accountbook

import com.monero.models.PendingTransaction

/**
 * Created by Dreamz on 12-08-2018.
 */
interface IAccountBookView {
    fun onTransactionsfetched(list:ArrayList<PendingTransaction>)
}