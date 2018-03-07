package com.monero.main.presenter

import com.monero.models.Activities

/**
 * Created by tom.saju on 3/7/2018.
 */
interface IMainView {
    fun onActivitiesFetched(activityList:List<Activities>)
}