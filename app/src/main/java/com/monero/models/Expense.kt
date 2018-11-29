package com.monero.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.monero.Dao.DBContract

/**
 * Created by Dreamz on 06-05-2018.
 */
@Entity(tableName = DBContract.EXPENSE_TABLE.TABLE_NAME)
data class Expense(@ColumnInfo(name = "expense_id") @PrimaryKey var id: String,
                   val title:String,
                   val comments:String,
                   val activity_id: String,
                   val amount: Int,
                   val creditList:List<Credit>,
                   val debitList:List<Debit>,
                   val splitType:Int,
                   var created_date:String,
                   var sync_status:Boolean)