package com.monero.main.presenter

import android.content.Context
import android.support.annotation.WorkerThread
import com.monero.Application.ApplicationController
import com.monero.models.Activities
import com.monero.models.User
import io.reactivex.Observable
import io.reactivex.Observable.fromCallable
import io.reactivex.Observable.switchOnNext
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Created by tom.saju on 3/7/2018.
 */
class MainPresenter:IMainPresenter {


    var context: Context
    var view:IMainView

    constructor(context: Context, view: IMainView) {
        this.context = context
        this.view = view
    }



    override fun getAllActivitiesList() {

        view.onActivitiesFetched(ApplicationController.db.activitesDao().getAllActivities())
        val  activitiesObservable:Observable<List<Activities>> = Observable.just(ApplicationController.db.activitesDao().getAllActivities())

        val subscribe = activitiesObservable.subscribe { it: List<Activities> ->
            view.onActivitiesFetched(it)
        }


    }

}