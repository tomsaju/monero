package com.monero.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import com.monero.Dao.DBContract

/**
 * Created by tom.saju on 3/14/2018.
 */

data class ContactMinimal(@ColumnInfo(name = "contact_id") val contact_id:String,@ColumnInfo(name = "Contact_name_local") val name:String,@ColumnInfo(name = "Contact_phone") val phoneNumber:String,@ColumnInfo(name= "Contact_email") val email:String)