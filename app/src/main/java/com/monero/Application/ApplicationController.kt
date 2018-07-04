package com.monero.Application

import android.app.Application
import com.monero.helper.AppDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.support.multidex.MultiDex
import com.facebook.stetho.Stetho


/**
 * Created by tom.saju on 3/8/2018.
 */
class ApplicationController:Application() {

    //var db = Room.databaseBuilder(baseContext.applicationContext, AppDatabase::class.java, "fair-db").build()

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)

        Stetho.initializeWithDefaults(this)
    }
}