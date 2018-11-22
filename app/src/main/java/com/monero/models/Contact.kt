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
                    var Contact_name_local:String,
                    var Contact_name_public:String,
                    var Contact_phone:String,
                    var Contact_email:String,
                    var Contact_uuid:String,
                    var Contact_profile_image:String)