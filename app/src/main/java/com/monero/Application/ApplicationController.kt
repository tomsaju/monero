package com.monero.Application

import android.app.Application
import android.support.multidex.MultiDex
import android.text.TextUtils
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
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
        private val TAG = ApplicationController::class.java.simpleName
        @get:Synchronized var instance: ApplicationController? = null
            private set
    }
    //var db = Room.databaseBuilder(baseContext.applicationContext, AppDatabase::class.java, "fair-db").build()

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        preferenceManager = PreferenceManager(applicationContext)
        Stetho.initializeWithDefaults(this)

    }

    val requestQueue: RequestQueue? = null
        get() {
            if (field == null) {
                return Volley.newRequestQueue(applicationContext)
            }
            return field
        }

    fun <T> addToRequestQueue(request: Request<T>, tag: String) {
        request.tag = if (TextUtils.isEmpty(tag)) TAG else tag
        requestQueue?.add(request)
    }

    fun <T> addToRequestQueue(request: Request<T>) {
        request.tag = TAG
        requestQueue?.add(request)
    }

    fun cancelPendingRequests(tag: Any) {
        if (requestQueue != null) {
            requestQueue!!.cancelAll(tag)
        }
    }



}