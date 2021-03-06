package com.monero.payeeSelector.presenter

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.monero.helper.AppDatabase
import com.monero.models.User
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

/**
 * Created by tom.saju on 7/26/2018.
 */
class PayerSelectorPresenter:IPayerSelectorPresenter {

    var context: Context
    var view:IPayerSelectorView

    constructor(context: Context, view: IPayerSelectorView){
        this.context = context
        this.view = view
    }

    /*override fun getAllUsersForActivity(activity_id: Long): ArrayList<User> {
        var allUserList:ArrayList<User> = ArrayList()
        var gson = Gson()
        var users:String? = ""
        Single.fromCallable {

            AppDatabase.db = AppDatabase.getAppDatabase(context)
           users = AppDatabase.db?.activitesDao()?.getAllUsersForActivity(activity_id)

        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(Consumer {

            var list :List<User> =gson.fromJson(users , Array<User>::class.java).toList()
            allUserList = ArrayList(list)
            return@Consumer

        })
        return allUserList
    }*/


    override fun getAllUsersForActivity(activity_id: String) {

        AppDatabase.db = AppDatabase.getAppDatabase(context)
        AppDatabase.db?.activitesDao()?.getAllUsersForActivity(activity_id)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(
                        { allusersJson ->
                            var gson = Gson()
                            var list :List<User> =gson.fromJson(allusersJson , Array<User>::class.java).toList()
                            var allUserList = ArrayList(list)
                            view.onUsersFetched(allUserList)
                        },
                        { error ->
                            Log.d("Payerselector", error.message)
                        })


       /* var allUserList:ArrayList<User> = ArrayList()
        var gson = Gson()
        var users:String? = ""
        Single.fromCallable {

            AppDatabase.db = AppDatabase.getAppDatabase(context)
            users = AppDatabase.db?.activitesDao()?.getAllUsersForActivity(activity_id)

        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(Consumer {

            var list :List<User> =gson.fromJson(users , Array<User>::class.java).toList()
            allUserList = ArrayList(list)
            view.onUsersFetched(allUserList)

        })*/

    }
}