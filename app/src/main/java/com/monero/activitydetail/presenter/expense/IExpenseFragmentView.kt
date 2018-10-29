package com.monero.activitydetail.presenter.expense

import com.monero.models.Expense

/**
 * Created by tom.saju on 7/19/2018.
 */

interface IExpenseFragmentView{
    fun onExpenseFetched(expense: Expense)
}
