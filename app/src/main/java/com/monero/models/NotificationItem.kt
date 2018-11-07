package com.monero.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.monero.Dao.DBContract

/**
 * Created by tom.saju on 11/6/2018.
 */
@Entity(tableName = DBContract.NOTIFICATION_ITEM_TABLE.TABLE_NAME)
data class NotificationItem(@PrimaryKey var notificationId:Long, var message:String, var title:String, var type: Int, var targetId:String)