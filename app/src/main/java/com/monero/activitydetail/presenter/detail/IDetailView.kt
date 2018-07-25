package com.monero.activitydetail.presenter.detail

import android.arch.lifecycle.LiveData
import com.monero.models.Activities
import com.monero.models.Expense

/**
 * Created by tom.saju on 7/24/2018.
 */
interface IDetailView {
    fun onActivityFetched(activity:Activities)

}