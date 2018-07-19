package com.monero.Dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.monero.models.Activities
import com.monero.models.Expense

/**
 * Created by tom.saju on 7/19/2018.
 */
@Dao interface ExpenseDAO {

    @Query("SELECT * FROM "+DBContract.EXPENSE_TABLE.TABLE_NAME)
    fun getAllExpenses(): LiveData<List<Expense>>

    @Query("SELECT * FROM "+DBContract.EXPENSE_TABLE.TABLE_NAME+" WHERE "+DBContract.EXPENSE_TABLE.EXPENSE_ACTIVITY_ID +" = :id")
    fun getExpenseForId(id:Long): LiveData<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIntoAExpensesTable(expense: Expense)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateExpense(expense: Expense)

    @Delete
    fun deleteExpense(expense: Expense)
}