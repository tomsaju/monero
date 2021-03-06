package com.monero.Dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.monero.models.Activities
import com.monero.models.HistoryLogItem

/**
 * Created by Dreamz on 11-10-2018.
 */
@Dao interface HistoryLogDAO {

    @Query("SELECT * FROM "+DBContract.HISTORY_LOG_ITEM_TABLE.TABLE_NAME+" WHERE "+DBContract.HISTORY_LOG_ITEM_TABLE.LOG_ITEM_ID+" = :id")
    fun getHistoryLogForId(id:String): HistoryLogItem

    @Query("SELECT * FROM "+DBContract.HISTORY_LOG_ITEM_TABLE.TABLE_NAME+" WHERE "+DBContract.HISTORY_LOG_ITEM_TABLE.ACTIVITY_ID+" = :id")
    fun getAllHistoryLogsForActivity(id:String): LiveData<List<HistoryLogItem>>

    @Query("SELECT "+DBContract.HISTORY_LOG_ITEM_TABLE.LOG_ITEM_ID+" FROM "+DBContract.HISTORY_LOG_ITEM_TABLE.TABLE_NAME+" WHERE "+DBContract.HISTORY_LOG_ITEM_TABLE.ACTIVITY_ID+" = :id")
    fun getAllHistoryLogsIdsForActivity(id:String): List<String>

    @Query("UPDATE "+DBContract.HISTORY_LOG_ITEM_TABLE.TABLE_NAME+" SET "+DBContract.HISTORY_LOG_ITEM_TABLE.SYNC_STATUS+" = :status WHERE "+DBContract.HISTORY_LOG_ITEM_TABLE.LOG_ITEM_ID+" = :logId")
    fun updateSyncStatusForHistory(status:Boolean,logId: String)

    @Query("SELECT * FROM "+DBContract.HISTORY_LOG_ITEM_TABLE.TABLE_NAME+" WHERE "+DBContract.HISTORY_LOG_ITEM_TABLE.SYNC_STATUS+" = 0")
    fun getPendingSyncHistoryLogs():List<HistoryLogItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIntoHistoryTable(log: HistoryLogItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllHistoryLogs(logs: List<HistoryLogItem>)

    @Delete
    fun deleteLog(logItem: HistoryLogItem)

    @Query("DELETE FROM "+DBContract.HISTORY_LOG_ITEM_TABLE.TABLE_NAME)
    fun deleteTable()
}