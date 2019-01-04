package com.monero

import android.content.Context
import android.util.Log
import androidx.work.Worker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.monero.helper.SyncService


/**
 * Created by tom.saju on 12/3/2018.
 */
class SyncWorkManager(context: Context) : Worker() {

    var myContext = context

    override fun doWork(): WorkerResult {
        Log.d("SyncService","inside dowork")
        syncData()

        return WorkerResult.SUCCESS

    }

    private fun syncData() {
      Log.d("SyncService","syncdata to call service");
      var syncService = SyncService()
       syncService.start()
    }
}