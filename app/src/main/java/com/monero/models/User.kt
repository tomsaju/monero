package com.monero.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import com.monero.Dao.DBContract
import kotlinx.android.parcel.Parcelize

/**
 * Created by tom.saju on 3/7/2018.
 */
@Entity(tableName = DBContract.USER_TABLE.TABLE_NAME)
@Parcelize data class User(@PrimaryKey val user_id: Long, val user_name:String, val user_phone:String, val user_email:String):Parcelable