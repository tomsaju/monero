package com.monero.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.monero.Dao.DBContract

/**
 * Created by tom.saju on 7/19/2018.
 */
@Entity(tableName = DBContract.CREDIT_TABLE.TABLE_NAME)
data class Credit(@ColumnInfo(name = "credit_id") @PrimaryKey val id:Long,
                  val activity_id:Long,
                  val expenseId:Long,
                  val userId:Long,
                  val userName:String,
                  val amount:Double)
