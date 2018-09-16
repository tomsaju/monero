package com.monero.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.monero.Dao.DBContract

/**
 * Created by tom.saju on 3/7/2018.
  */
var emptyulist= emptyList<User>()
var emptytags= emptyList<Tag>()
@Entity(tableName = DBContract.ACTIVITY_TABLE.TABLE_NAME)

data class Activities(@ColumnInfo(name = "activity_id") @PrimaryKey var id: String="",
                      var title:String="",
                      var description:String="",
                      var tags:List<Tag> = emptyList(),
                      var mode:Int=1,
                      var members:List<User> = emptyList(),
                      var author:User= User("","","",""),
                      var syncStatus:Boolean=false,
                      var createdDate:Long=0,
                      var expenseIdList:String="",
                      var historyLogIds:String="",
                      var transactionIds:String="",
                      var lastModifiedTime:String="")

