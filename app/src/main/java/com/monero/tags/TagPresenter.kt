package com.monero.tags

import android.arch.lifecycle.LiveData
import android.content.Context
import com.monero.helper.AppDatabase
import com.monero.models.Tag
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by tom.saju on 7/4/2018.
 */
class TagPresenter : ITagPresenter {


    var context: Context
    var view: ITagView

    constructor(context: Context, view: ITagView) {
        this.context = context
        this.view = view
    }



    override fun saveTag(tag: Tag) {
        Single.fromCallable{
            AppDatabase.db?.tagDao()?.insertIntoTagTable(tag)}
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object :SingleObserver<Unit?>{
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(t:Unit) {

                    }

                    override fun onError(e: Throwable) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                })


    }

    override fun getAllTags() {
       var allTags = AppDatabase.db?.tagDao()?.getAllTags()
        if(allTags!=null) {
            view.setTags(allTags)
        }
    }


}