package com.monero.payeeSelector.presenter

import com.monero.models.User

/**
 * Created by tom.saju on 7/26/2018.
 */
interface IPayerSelectorView {
    fun onUsersFetched(userList:ArrayList<User>)
}