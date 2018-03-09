package com.monero.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.monero.Dao.DBContract

/**
 * Created by tom.saju on 3/7/2018.
  */
@Entity(tableName = DBContract.ACTIVITY_TABLE.TABLE_NAME)
data class Activities(@ColumnInfo(name = "activity_id") @PrimaryKey(autoGenerate = true) val id:Long,
                      val title:String,
                      val description:String)