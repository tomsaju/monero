package com.monero.main.presenter

import android.arch.lifecycle.LiveData
import android.content.Context
import android.support.annotation.WorkerThread
import android.util.Log
import com.monero.Application.ApplicationController
import com.monero.models.Activities
import com.monero.models.User
import io.reactivex.Observable
import io.reactivex.Observable.fromCallable
import io.reactivex.Observable.switchOnNext
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Created by tom.saju on 3/7/2018.
 */
class MainPresenter:IMainPresenter {


    var context: Context
    var view: IMainView

    constructor(context: Context, view: IMainView) {
        this.context = context
        this.view = view
    }



    override fun getAllActivitiesList() {


        var allActivities = ApplicationController.db.activitesDao().getAllActivities();

        view.onActivitiesFetched(allActivities);


       /* Observable.create(ObservableOnSubscribe<List<Activities>>
        { emitter -> emitter.onNext(ApplicationController.db.activitesDao().getAllActivities()) })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result -> view.onActivitiesFetched(result) }*/

    }

    override fun saveActivity(activity: Activities) {
        Single.fromCallable {
            ApplicationController.db?.activitesDao().insertIntoActivitiesTable(activity) // .database?.personDao()?.insert(person)
            for(tag in activity.tags){
                ApplicationController.db?.tagDao().insertIntoTagTable(tag)
            }

        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()

    }
}
