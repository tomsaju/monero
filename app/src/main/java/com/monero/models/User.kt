package com.monero.models

import android.arch.persistence.room.Entity
import android.os.Parcelable
import com.monero.Dao.DBContract
import kotlinx.android.parcel.Parcelize

/**
 * Created by tom.saju on 3/7/2018.
 */
@Parcelize data class User(val user_id:Int, val name:String, val phone:String, val email:String):Parcelable