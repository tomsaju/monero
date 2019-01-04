package com.monero.main.presenter.accountbook

/**
 * Created by Dreamz on 12-08-2018.
 */
interface IAccountBookPresenter {
    fun getAllPendingTransactions()
    fun getAllActivitiesList()
    fun getPendingTransactionForActivity(id:String)
    fun getAllExpensesForMonth(timeinMillis:String)
}