package com.monero.Application

import android.app.Application
import android.support.multidex.MultiDex
import com.facebook.stetho.Stetho
import com.monero.R
import com.monero.helper.PreferenceManager
import uk.co.chrisjenx.calligraphy.CalligraphyConfig


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

        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/RobotoCondensed-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build())
    }
}