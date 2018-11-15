package com.monero.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.TypeConverters
import com.monero.Dao.DBContract
import com.monero.helper.converters.TagConverter

/**
 * Created by tom.saju on 11/15/2018.
 */
@Entity(tableName = DBContract.CONTACT_GROUP_TABLE.TABLE_NAME)
data class ContactGroup ( @PrimaryKey val Group_id:Long,
                          val Group_name:String,
                          val Group_items:List<ContactMinimal>)