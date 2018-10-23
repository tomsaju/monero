package com.monero.splittype.presenter

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.monero.helper.AppDatabase
import com.monero.models.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by tom.saju on 10/23/2018.
 */
class SplitTypePresenter:ISplitTypePresenter {
    var context: Context
    var view:ISplitTypeView

    constructor(context: Context,view: ISplitTypeView){
        this.context = context
        this.view = view
    }

    override fun getAllUsersForActivity(id: String) {
        AppDatabase.db = AppDatabase.getAppDatabase(context)
        AppDatabase.db?.activitesDao()?.getAllUsersForActivity(id)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(
                        { allusersJson ->
                            var gson = Gson()
                            var list :List<User> =gson.fromJson(allusersJson , Array<User>::class.java).toList()
                            var allUserList = ArrayList(list)
                            view.onAllusersFetched(allUserList)
                        },
                        { error ->
                            Log.d("Payerselector", error.message)
                        })
    }
}