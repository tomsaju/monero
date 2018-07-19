package com.monero.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.monero.Dao.DBContract

/**
 * Created by tom.saju on 7/19/2018.
 */
@Entity(tableName = DBContract.DEBIT_TABLE.TABLE_NAME)
data class Debit(@ColumnInfo(name = "debit_id") @PrimaryKey val id:Long,
                  val activityId:Long,
                  val expenseId:Long,
                  val userId:Long,
                  val userName:String,
                  val amount:Double)