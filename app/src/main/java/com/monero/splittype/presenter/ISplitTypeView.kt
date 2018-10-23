package com.monero.splittype.presenter

import com.monero.models.User

/**
 * Created by tom.saju on 10/23/2018.
 */
interface ISplitTypeView {
    fun onAllusersFetched(userList: ArrayList<User>)
}