package com.monero.Dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.database.Observable
import com.monero.models.Activities
import com.monero.models.ActivitiesMinimal
import com.monero.models.User
import io.reactivex.Single

/**
 * Created by tom.saju on 3/8/2018.
 */
@Dao interface ActivitiesDao   {

    @Query("SELECT * FROM "+DBContract.ACTIVITY_TABLE.TABLE_NAME+" ORDER BY "+DBContract.ACTIVITY_TABLE.ACTIVITY_MODIFIED_TIME+" DESC")
    fun getAllActivities():LiveData<List<Activities>>

    @Query("SELECT "+DBContract.ACTIVITY_TABLE.ACTIVITY_ID+" FROM "+DBContract.ACTIVITY_TABLE.TABLE_NAME)
    fun getAllActivityIds():List<String>

    @Query("SELECT * FROM "+DBContract.ACTIVITY_TABLE.TABLE_NAME+" WHERE "+DBContract.ACTIVITY_TABLE.ACTIVITY_ID+" = :id")
    fun getActivityForId(id:String):Activities

    @Query("SELECT members FROM "+DBContract.ACTIVITY_TABLE.TABLE_NAME+" WHERE "+DBContract.ACTIVITY_TABLE.ACTIVITY_ID+" = :id")
    fun getAllUsersForActivity(id:String):Single<String>

    @Query("SELECT "+ DBContract.ACTIVITY_TABLE.ACTIVITY_ID+" , "+DBContract.ACTIVITY_TABLE.ACTIVITY_MODIFIED_TIME +" FROM "+DBContract.ACTIVITY_TABLE.TABLE_NAME)
    fun getAllActivitiesModifiedDate():Single<List<ActivitiesMinimal>>

    @Query("SELECT * FROM "+DBContract.ACTIVITY_TABLE.TABLE_NAME+" WHERE "+DBContract.ACTIVITY_TABLE.ACTIVITY_SYNC_STATUS+" = 0")
    fun getPendingSyncActivities():List<Activities>

    @Insert(onConflict = REPLACE)
    fun insertIntoActivitiesTable(activity:Activities)

    @Update(onConflict = REPLACE)
    fun updateActivity(activity: Activities)

    @Delete
    fun deleteActivity(activity:Activities)

    @Query("UPDATE "+DBContract.ACTIVITY_TABLE.TABLE_NAME+" SET "+DBContract.ACTIVITY_TABLE.ACTIVITY_ID+" = :newId ,"+DBContract.ACTIVITY_TABLE.ACTIVITY_SYNC_STATUS +" =1 WHERE "+DBContract.ACTIVITY_TABLE.ACTIVITY_ID+" = :oldId")
    fun updateActivityId(oldId: String, newId: String)

    @Query("UPDATE "+DBContract.ACTIVITY_TABLE.TABLE_NAME+" SET "+DBContract.ACTIVITY_TABLE.ACTIVITY_SYNC_STATUS+" = :status WHERE "+DBContract.ACTIVITY_TABLE.ACTIVITY_ID+" = :Id")
    fun updateActivitySyncStatus(Id: String,status:Boolean)

    @Query("UPDATE "+DBContract.ACTIVITY_TABLE.TABLE_NAME+" SET "+DBContract.ACTIVITY_TABLE.ACTIVITY_MODIFIED_TIME+" = :time  WHERE "+DBContract.ACTIVITY_TABLE.ACTIVITY_ID+" = :activityId")
    fun updateActivityModifiedTime(activityId: String, time: String)

    @Query("DELETE FROM "+DBContract.ACTIVITY_TABLE.TABLE_NAME)
    fun deleteTable()
}