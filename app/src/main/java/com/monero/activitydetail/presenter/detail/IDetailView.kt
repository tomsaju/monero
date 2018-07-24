package com.monero.activitydetail.presenter.detail

import com.monero.models.Activities

/**
 * Created by tom.saju on 7/24/2018.
 */
interface IDetailView {
    fun onActivityFetched(activity:Activities)
}