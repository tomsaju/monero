package com.monero.Application

import android.app.Application
import android.support.multidex.MultiDex
import com.facebook.stetho.Stetho
import com.google.firebase.FirebaseApp
import com.monero.R
import com.monero.helper.PreferenceManager
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.FirebaseFirestore




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