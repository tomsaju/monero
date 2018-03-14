package com.monero.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.monero.Dao.DBContract

/**
 * Created by tom.saju on 3/14/2018.
 */
@Entity(tableName = DBContract.TAG_TABLE.TABLE_NAME)
data class Tag(@ColumnInfo(name = "Tag_Id") @PrimaryKey(autoGenerate = true) val id:Long,val title:String)


