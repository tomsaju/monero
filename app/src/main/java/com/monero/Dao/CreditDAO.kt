package com.monero.Dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.monero.models.Credit
import com.monero.models.Tag

/**
 * Created by tom.saju on 7/19/2018.
 */

//Credit table contains all the expenses each person owes.
//if john spends 100rs for mathew, then there will be an entry in credit table for mathew with amount 100rs
@Dao interface CreditDAO {

    @Query("SELECT * FROM "+DBContract.CREDIT_TABLE.TABLE_NAME+" WHERE "+DBContract.CREDIT_TABLE.EXPENSE_ID+" = :id")
    fun getAllCreditForExpense(id:Long): LiveData<List<Credit>>

    @Query("SELECT * FROM "+DBContract.CREDIT_TABLE.TABLE_NAME+" WHERE "+DBContract.CREDIT_TABLE.CREDIT_ID+" = :id")
    fun getCreditForId(id:Long): Credit

    @Query("SELECT * FROM "+DBContract.CREDIT_TABLE.TABLE_NAME+" WHERE "+DBContract.CREDIT_TABLE.USER_ID+" = :id")
    fun getCreditsForUser(id:Long): List<Credit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIntoCreditTable(credit: Credit)

    @Delete
    fun deleteCredit(credit: Credit)

    @Query("DELETE FROM "+DBContract.CREDIT_TABLE.TABLE_NAME)
    fun deleteTable()
}