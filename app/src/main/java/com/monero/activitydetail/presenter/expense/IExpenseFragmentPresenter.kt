package com.monero.activitydetail.presenter.expense

import com.monero.models.Expense
import com.monero.models.User

/**
 * Created by tom.saju on 7/19/2018.
 */
interface IExpenseFragmentPresenter {

    fun saveExpense(expense: Expense)
    fun getAllParticipantsForthisActivity(id:Long) :ArrayList<User>
}