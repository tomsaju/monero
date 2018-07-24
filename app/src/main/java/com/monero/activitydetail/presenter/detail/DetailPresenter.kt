package com.monero.activitydetail.presenter.detail

import android.content.Context
import com.monero.helper.AppDatabase
import com.monero.helper.AppDatabase.Companion.db
import com.monero.helper.AppDatabase.Companion.getAppDatabase
import com.monero.main.presenter.IMainView
import com.monero.models.Activities
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

/**
 * Created by tom.saju on 7/24/2018.
 */
class DetailPresenter:IDetailPresenter {


    var context: Context
    var view: IDetailView

    constructor(context: Context, view: IDetailView) {
        this.context = context
        this.view = view
    }

    override fun getActivityForId(id: Long) {


         var workingActivity:Activities?=null

        Single.fromCallable {
            db= getAppDatabase(context)
             workingActivity = db?.activitesDao()?.getActivityForId(id)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(Consumer {
            val activity = workingActivity
            if(activity!=null){
                view.onActivityFetched(activity)
            }
        })




    }



}