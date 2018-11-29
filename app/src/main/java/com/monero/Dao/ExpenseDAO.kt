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

    @Query("SELECT * FROM "+DBContract.EXPENSE_TABLE.TABLE_NAME)
    fun getExpensesForAllActivities(): List<Expense>

    @Query("SELECT * FROM "+DBContract.EXPENSE_TABLE.TABLE_NAME+" WHERE "+DBContract.EXPENSE_TABLE.EXPENSE_ID +" = :id")
    fun getExpenseForId(id:String): LiveData<Expense>

    @Query("SELECT * FROM "+DBContract.EXPENSE_TABLE.TABLE_NAME+" WHERE "+DBContract.EXPENSE_TABLE.EXPENSE_ID +" = :id")
    fun getExpenseForIdSingle(id:String): Expense

    @Query("SELECT * FROM "+DBContract.EXPENSE_TABLE.TABLE_NAME+" WHERE "+DBContract.EXPENSE_TABLE.EXPENSE_ACTIVITY_ID +" = :id")
    fun getAllExpensesForActivity(id:String): LiveData<List<Expense>>

    @Query("SELECT * FROM "+DBContract.EXPENSE_TABLE.TABLE_NAME+" WHERE "+DBContract.EXPENSE_TABLE.EXPENSE_ACTIVITY_ID +" = :id")
    fun getAllExpenseListForActivity(id:String): List<Expense>

    @Query("SELECT "+DBContract.EXPENSE_TABLE.EXPENSE_ID +" FROM "+DBContract.EXPENSE_TABLE.TABLE_NAME+" WHERE "+DBContract.EXPENSE_TABLE.EXPENSE_ACTIVITY_ID +" = :id")
    fun getAllExpenseIdListForActivity(id:String): List<String>

    @Query("UPDATE "+DBContract.ACTIVITY_TABLE.TABLE_NAME+" SET "+DBContract.ACTIVITY_TABLE.ACTIVITY_ID+" = :newId ,"+DBContract.ACTIVITY_TABLE.ACTIVITY_SYNC_STATUS +" =1 WHERE "+DBContract.ACTIVITY_TABLE.ACTIVITY_ID+" = :oldId")
    fun updateActivityId(oldId: String, newId: String)

    @Query("UPDATE "+DBContract.EXPENSE_TABLE.TABLE_NAME+" SET "+DBContract.EXPENSE_TABLE.EXPENSE_SYNC_STATUS+" = :status WHERE "+DBContract.EXPENSE_TABLE.EXPENSE_ID+" = :expenseId")
    fun updateSyncStatusForExpense(status:Boolean,expenseId: String)

    @Query("SELECT * FROM "+DBContract.EXPENSE_TABLE.TABLE_NAME+" WHERE "+DBContract.EXPENSE_TABLE.EXPENSE_SYNC_STATUS+" = 0")
    fun getPendingSyncExpenses():List<Expense>

    @Query("UPDATE "+DBContract.EXPENSE_TABLE.TABLE_NAME+" SET "+DBContract.EXPENSE_TABLE.EXPENSE_ID+" = :newId WHERE "+DBContract.EXPENSE_TABLE.EXPENSE_ID+" = :oldId")
    fun updateExpenseId(oldId: String, newId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIntoAExpensesTable(expense: Expense)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateExpense(expense: Expense)

    @Delete
    fun deleteExpense(expense: Expense)

    @Query("DELETE FROM "+DBContract.EXPENSE_TABLE.TABLE_NAME)
    fun deleteTable()
}