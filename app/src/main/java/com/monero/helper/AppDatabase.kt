package com.monero.helper

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.Room
import android.content.Context
import com.monero.Dao.*
import com.monero.helper.converters.TagConverter
import com.monero.models.*


/**
 * Created by tom.saju on 3/8/2018.
 */
@TypeConverters(TagConverter::class)
@Database(entities = arrayOf(Activities::class,
                                Tag::class,
                                Expense::class,
                                Credit::class,
                                Debit::class,
                                User::class,
                                HistoryLogItem::class,
                                Contact::class),version = 4,exportSchema = false)


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
    abstract fun expenseDao():ExpenseDAO
    abstract fun creditDao():CreditDAO
    abstract fun debitDao():DebitDAO
    abstract fun userDao():UserDao
    abstract fun contactDao():ContactDAO
    abstract fun historyDao():HistoryLogDAO
}