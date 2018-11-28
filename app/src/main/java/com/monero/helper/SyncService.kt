package com.monero.helper

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import java.util.*

/**
 * Created by tom.saju on 11/28/2018.
 */
class SyncService : Service(){

    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Send a notification that service is started
       // toast("Service started.")
        Log.d("syncService","started")
        // Do a periodic task
        mHandler = Handler()
        mRunnable = Runnable { showRandomNumber() }
        mHandler.postDelayed(mRunnable, 5000)

        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("syncService","stopped")
        mHandler.removeCallbacks(mRunnable)
    }

    // Custom method to do a task
    private fun showRandomNumber() {
        val rand = Random()
        val number = rand.nextInt(100)
        Log.d("syncService","working "+number)
        mHandler.postDelayed(mRunnable, 5000)
    }
}