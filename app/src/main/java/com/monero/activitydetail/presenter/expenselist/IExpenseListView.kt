package com.monero.activitydetail.presenter.expenselist

import android.arch.lifecycle.LiveData
import com.monero.models.Expense

/**
 * Created by tom.saju on 7/25/2018.
 */
interface IExpenseListView {
    fun onExpensesFetched(expenses: LiveData<List<Expense>>)
}