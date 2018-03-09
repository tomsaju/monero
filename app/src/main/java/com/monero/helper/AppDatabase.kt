package com.monero.helper

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.monero.Dao.ActivitiesDao
import com.monero.models.Activities

/**
 * Created by tom.saju on 3/8/2018.
 */
@Database(entities = arrayOf(Activities::class),version = 1,exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun activitesDao():ActivitiesDao
}