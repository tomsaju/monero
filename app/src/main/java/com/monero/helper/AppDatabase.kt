package com.monero.helper

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.monero.Dao.ActivitiesDao
import com.monero.Dao.TagDao
import com.monero.models.Activities
import com.monero.models.Tag
import android.arch.persistence.room.Room
import android.content.Context


/**
 * Created by tom.saju on 3/8/2018.
 */
@TypeConverters(Converter::class)
@Database(entities = arrayOf(Activities::class, Tag::class),version = 3,exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    companion object {
         var db:AppDatabase? = null

         fun getAppDatabase(context: Context): AppDatabase? {
            if (db == null) {
                db=Room.databaseBuilder(context, AppDatabase::class.java, "fair-db").build()
            }
            return db
        }

    }





    abstract fun activitesDao():ActivitiesDao
    abstract fun tagDao():TagDao
}