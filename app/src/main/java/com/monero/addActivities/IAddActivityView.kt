package com.monero.addActivities

import com.monero.models.Activities
import com.monero.models.ContactMinimal

/**
 * Created by tom.saju on 3/14/2018.
 */
interface IAddActivityView {
    fun onContactsfetched(contactList:List<ContactMinimal>)
    fun onActivityFetched(activity:Activities)
    fun onActivityFetchError()
}