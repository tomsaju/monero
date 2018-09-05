package com.monero.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import com.monero.Dao.DBContract
import kotlinx.android.parcel.Parcelize

/**
 * Created by tom.saju on 3/14/2018.
 */

@Entity(tableName = DBContract.TAG_TABLE.TABLE_NAME)

@Parcelize data class Tag (@ColumnInfo(name = "Tag_Id") @PrimaryKey(autoGenerate = true) var id:Long=0, var title:String=""):Parcelable




