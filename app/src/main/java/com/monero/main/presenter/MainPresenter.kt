package com.monero.main.presenter

import android.arch.lifecycle.LiveData
import android.content.Context
import android.support.annotation.WorkerThread
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.monero.Application.ApplicationController
import com.monero.Dao.DBContract
import com.monero.helper.AppDatabase
import com.monero.helper.AppDatabase.Companion.db
import com.monero.helper.AppDatabase.Companion.getAppDatabase
import com.monero.helper.converters.TagConverter
import com.monero.models.Activities
import com.monero.models.User
import io.reactivex.*
import io.reactivex.Observable.fromCallable
import io.reactivex.Observable.switchOnNext
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.SingleEmitter
import io.reactivex.SingleOnSubscribe



/**
 * Created by tom.saju on 3/7/2018.
 */
class MainPresenter:IMainPresenter {


    var context: Context
    var view: IMainView
    var firestoreDb: FirebaseFirestore? = null
    constructor(context: Context, view: IMainView) {
        this.context = context
        this.view = view
        firestoreDb = FirebaseFirestore.getInstance()
    }



    override fun getAllActivitiesList() {
        db= getAppDatabase(context)
        var allActivities = db?.activitesDao()?.getAllActivities()
        view.onActivitiesFetched(allActivities)
    }


    override fun saveActivity(activity: Activities) {

//////////////////////////

        Observable.fromCallable {
            db= getAppDatabase(context)
            db?.activitesDao()?.insertIntoActivitiesTable(activity) // .database?.personDao()?.insert(person)
            for(tag in activity.tags){
                AppDatabase.db?.tagDao()?.insertIntoTagTable(tag)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ orderItem ->
            // set values to UI
            Log.d("tag","done")
            ///
            var gson =Gson()
            var convertor = TagConverter()
            var membersJson:String =convertor.convertUserListtoString(activity.members)
            var tagsJson:String = convertor.convertTagListtoString(activity.tags)
            var author:String = gson.toJson(activity.author,User::class.java)

            var newActivity = HashMap<String, Any>()
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_TITLE,activity.title)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_DESCRIPTION,activity.description)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_MODE,activity.mode)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_TAGS,tagsJson)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_USERS,membersJson)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_AUTHOR,author)

            firestoreDb?.collection("activities")?.add(newActivity)

                    ?.addOnSuccessListener {DocumentReference ->

                        //success
                       // activity.id = DocumentReference.id
                        activity.syncStatus = true
                        Single.fromCallable {
                            db= getAppDatabase(context)
                            db?.activitesDao()?.updateActivityId(activity.id,DocumentReference.id) // .database?.personDao()?.insert(person)
                            /*for(tag in activity.tags){
                                AppDatabase.db?.tagDao()?.insertIntoTagTable(tag)
                            }*/
                        }.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread()).subscribe()
                    }

                    ?.addOnFailureListener { e ->
                        //failure
                    }

            ///
        }, { error ->
            // handle exception if any
            Log.d("tag","exception")
        }, {
            // on complete
            Log.d("tag","completed")
        })




    }
}
