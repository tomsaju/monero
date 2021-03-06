package com.monero.Dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.monero.models.Credit
import com.monero.models.Debit

/**
 * Created by tom.saju on 7/19/2018.
 */
@Dao interface DebitDAO {

    @Query("SELECT * FROM "+DBContract.DEBIT_TABLE.TABLE_NAME+" WHERE "+DBContract.DEBIT_TABLE.EXPENSE_ID+" = :id")
    fun getAllDebitForExpense(id:String): LiveData<List<Debit>>

    @Query("SELECT * FROM "+DBContract.DEBIT_TABLE.TABLE_NAME+" WHERE "+DBContract.DEBIT_TABLE.DEBIT_ID+" = :id")
    fun getDebitForId(id:Long): Debit

    @Query("SELECT * FROM "+DBContract.DEBIT_TABLE.TABLE_NAME+" WHERE "+DBContract.DEBIT_TABLE.USER_ID+" = :id")
    fun getDebitsForUser(id:String): List<Debit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIntoDebitTable(debit: Debit)

    @Delete
    fun deleteDebit(debit: Debit)

    @Query("DELETE FROM "+DBContract.DEBIT_TABLE.TABLE_NAME)
    fun deleteTable()
}