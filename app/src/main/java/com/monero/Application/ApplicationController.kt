package com.monero.Application

import android.app.Application
import android.support.multidex.MultiDex
import com.facebook.stetho.Stetho
import com.monero.helper.PreferenceManager


/**
 * Created by tom.saju on 3/8/2018.
 */
class ApplicationController:Application() {

    companion object {
        var preferenceManager:PreferenceManager?=null
    }
    //var db = Room.databaseBuilder(baseContext.applicationContext, AppDatabase::class.java, "fair-db").build()

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        preferenceManager = PreferenceManager(applicationContext)
        Stetho.initializeWithDefaults(this)
    }
}