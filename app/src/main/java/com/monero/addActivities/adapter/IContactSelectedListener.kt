package com.monero.addActivities.adapter

import com.monero.models.ContactMinimal

/**
 * Created by tom.saju on 10/23/2018.
 */
interface IContactSelectedListener {
    fun onContactSelected(contact: ContactMinimal)
}