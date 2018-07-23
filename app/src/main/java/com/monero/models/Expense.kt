package com.monero.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.monero.Dao.DBContract

/**
 * Created by Dreamz on 06-05-2018.
 */
@Entity(tableName = DBContract.EXPENSE_TABLE.TABLE_NAME)
data class Expense(@ColumnInfo(name = "expense_id") @PrimaryKey(autoGenerate = true) val id:Long,
                   val title:String,
                   val comments:String,
                   val activity_id:Long,
                   val creditList:List<Credit>,
                   val debitList:List<Debit>)