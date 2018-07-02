package com.monero.Dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.database.Observable
import com.monero.models.Activities

/**
 * Created by tom.saju on 3/8/2018.
 */
@Dao interface ActivitiesDao   {

    @Query("SELECT * FROM "+DBContract.ACTIVITY_TABLE.TABLE_NAME)
    fun getAllActivities():LiveData<List<Activities>>

    @Query("SELECT * FROM "+DBContract.ACTIVITY_TABLE.TABLE_NAME+" WHERE "+DBContract.ACTIVITY_TABLE.ACTIVITY_ID+" = :id")
    fun getActivityForId(id:Long):Activities

    @Insert(onConflict = REPLACE)
    fun insertIntoActivitiesTable(activity:Activities)

    @Update(onConflict = REPLACE)
    fun updateActivity(activity: Activities)

    @Delete
    fun deleteActivity(activity:Activities)

}