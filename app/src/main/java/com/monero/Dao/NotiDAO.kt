package com.monero.Dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.monero.models.HistoryLogItem
import com.monero.models.NotificationItem

/**
 * Created by tom.saju on 11/6/2018.
 */
@Dao interface NotiDAO {

    @Query("SELECT * FROM "+DBContract.NOTIFICATION_ITEM_TABLE.TABLE_NAME)
    fun getAllNotifications(): LiveData<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIntoNotiTable(log: NotificationItem)

    @Delete
    fun deleteLog(logItem: NotificationItem)

    @Query("DELETE FROM "+DBContract.NOTIFICATION_ITEM_TABLE.TABLE_NAME)
    fun deleteTable()
}