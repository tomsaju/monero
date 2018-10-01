package com.monero.service

import android.app.job.JobParameters
import android.app.job.JobService
import com.firebase.jobdispatcher.Job
import com.monero.helper.AppDatabase
import com.monero.helper.AppDatabase.Companion.db
import com.monero.models.Contact
import com.monero.network.RestService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Dreamz on 28-09-2018.
 */
class ContactsSyncService : com.firebase.jobdispatcher.JobService() {


    override fun onStopJob(job: com.firebase.jobdispatcher.JobParameters?): Boolean {
      return true
    }

    override fun onStartJob(job: com.firebase.jobdispatcher.JobParameters?): Boolean {
        db = AppDatabase.getAppDatabase(this)
        var allContacts = db?.contactDao()?.getAllContacts()
        if(allContacts!=null) {
            for(contact in allContacts) {
                syncContactWithServer(contact)
            }
        }
        return true
    }


    val RestAPIService by lazy {
        RestService.create()
    }

    var disposable: Disposable? = null




    fun syncContactWithServer(contact: Contact){
                  /*disposable = RestAPIService.getRegisteredContactForNumber(contact.Contact_phone)
                          .subscribeOn(Schedulers.io())
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribe(
                                  { result -> showResult(result) },
                                  { error -> showError(error.message) }
                          )*/

    }

    private fun showError(message: String?) {
      //do nothing
        //set sync status false
    }

    private fun showResult(result: String) {
       //if contact not null, search that number and get details
    }
}