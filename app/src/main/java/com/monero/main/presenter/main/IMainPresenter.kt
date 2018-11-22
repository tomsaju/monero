package com.monero.main.presenter.main

import com.monero.models.Activities
import com.monero.models.Contact

/**
 * Created by tom.saju on 3/7/2018.
 */
interface IMainPresenter {

    fun getAllActivitiesList()
    fun saveActivity(activity:Activities)
    fun getAllActivitiesFromServer();
    fun syncContactsWithServer()
    fun updateActivity(activity:Activities)
    fun getAllNotificationsFromDB()

}