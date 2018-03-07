package com.monero.main.presenter

import android.content.Context
import com.monero.models.Activities
import com.monero.models.User

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
       var activityList: MutableList<Activities>  = mutableListOf()
        var userList:List<User> = emptyList()
        for (i:Int  in 1..10){
             var acts:Activities = Activities(i.toLong(),"one","desc",userList)
            activityList.add(acts)
        }
        view.onActivitiesFetched(activityList)
    }

}