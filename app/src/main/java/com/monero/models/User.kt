package com.monero.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by tom.saju on 3/7/2018.
 */
@Parcelize data class User(val user_id:Int, val name:String, val phone:String, val email:String):Parcelable