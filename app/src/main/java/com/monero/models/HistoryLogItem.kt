package com.monero.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.monero.Dao.DBContract

/**
 * Created by Dreamz on 11-10-2018.
 */
@Entity(tableName = DBContract.HISTORY_LOG_ITEM_TABLE.TABLE_NAME)
data class HistoryLogItem( @ColumnInfo(name ="Log_Item_Id") @PrimaryKey var log_id:String,
                           val Author_Id:String,
                           val Author_name:String,
                           val Event_Type:String,
                           val Timestamp:String,
                           val Subject_Name:String,
                           val Subject_Url:String,
                           val Subject_Id:String,
                           val Activity_Id:String,
                           var SyncStatus:Boolean) {
}



