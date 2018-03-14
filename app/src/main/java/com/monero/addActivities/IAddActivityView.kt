package com.monero.addActivities

import com.monero.models.Contact

/**
 * Created by tom.saju on 3/14/2018.
 */
interface IAddActivityView {
    fun onContactsfetched(contactList:List<Contact>)
}