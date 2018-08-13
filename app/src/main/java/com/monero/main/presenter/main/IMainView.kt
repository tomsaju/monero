package com.monero.main.presenter.main

import android.arch.lifecycle.LiveData
import com.monero.models.Activities

/**
 * Created by tom.saju on 3/7/2018.
 */
interface IMainView {
    fun onActivitiesFetched(activityList:LiveData<List<Activities>>?)
}