package com.monero.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.monero.Dao.DBContract

/**
 * Created by tom.saju on 3/7/2018.
  */
@Entity(tableName = DBContract.ACTIVITY_TABLE.TABLE_NAME)
data class Activities(@ColumnInfo(name = "activity_id") @PrimaryKey var id: String,
                      val title:String,
                      val description:String,
                      val tags:List<Tag>,
                      val mode:Int,
                      val members:List<User>,
                      val author:User,
                      var syncStatus:Boolean)