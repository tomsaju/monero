package com.monero.Dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.monero.models.User

/**
 * Created by tom.saju on 7/26/2018.
 */
@Dao interface UserDao {

    @Query("SELECT * FROM "+DBContract.USER_TABLE.TABLE_NAME)
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM "+DBContract.USER_TABLE.TABLE_NAME+" WHERE "+DBContract.USER_TABLE.USER_ID+" = :id")
    fun getUserForId(id:String): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIntoUserTable(user: User)

    @Delete
    fun deleteUser(user: User)

}