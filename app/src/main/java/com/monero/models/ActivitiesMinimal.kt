package com.monero.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.PrimaryKey

/**
 * Created by tom.saju on 9/12/2018.
 */
data class ActivitiesMinimal(@ColumnInfo(name = "activity_id") @PrimaryKey var id: String="",
                      var lastModifiedTime:String="")