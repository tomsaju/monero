package com.monero.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.monero.Dao.DBContract

/**
 * Created by Dreamz on 23-09-2018.
 */


@Entity(tableName = DBContract.CONTACTS_TABLE.TABLE_NAME,indices = arrayOf(Index(value = "Contact_phone", unique = true)))
data class Contact (@PrimaryKey val Contact_id:Long,
                    val Contact_name_local:String,
                    val Contact_name_public:String,
                    val Contact_phone:String,
                    val Contact_email:String,
                    val Contact_uuid:String,
                    val Contact_profile_image:String)