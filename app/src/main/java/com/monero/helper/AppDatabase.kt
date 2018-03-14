package com.monero.helper

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.monero.Dao.ActivitiesDao
import com.monero.Dao.TagDao
import com.monero.models.Activities
import com.monero.models.Tag

/**
 * Created by tom.saju on 3/8/2018.
 */
@TypeConverters(Converter::class)
@Database(entities = arrayOf(Activities::class, Tag::class),version = 2,exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun activitesDao():ActivitiesDao
    abstract fun tagDao():TagDao
}