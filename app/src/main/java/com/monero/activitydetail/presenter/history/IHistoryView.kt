package com.monero.activitydetail.presenter.history

import android.arch.lifecycle.LiveData
import com.monero.models.HistoryLogItem

/**
 * Created by Dreamz on 12-10-2018.
 */
interface IHistoryView {
    fun onAllLogsFetched(logList:LiveData<List<HistoryLogItem>>)
}