package com.monero.expensedetail.presenter

import android.arch.lifecycle.LiveData
import com.monero.models.Expense

/**
 * Created by Dreamz on 16-10-2018.
 */
interface IExpenseDetailView {
    fun onExpenseFetched(expense:LiveData<Expense>)
}