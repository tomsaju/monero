package com.monero.main.presenter.accountbook

import com.monero.models.Expense
import com.monero.models.PendingTransaction

/**
 * Created by Dreamz on 12-08-2018.
 */
interface IAccountBookView {
    fun onTransactionsfetched(list:ArrayList<PendingTransaction>)
    fun onAllActivitiesFetched(activityIds:List<String>)
    fun onAllmonthlyExpensesFetched(expenseList:ArrayList<Expense>)
}