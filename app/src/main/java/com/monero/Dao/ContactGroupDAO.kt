package com.monero.Dao

import android.arch.persistence.room.*
import com.monero.models.ContactGroup
import io.reactivex.Single

/**
 * Created by tom.saju on 11/15/2018.
 */
@Dao interface ContactGroupDAO {
    @Query("SELECT * FROM "+DBContract.CONTACT_GROUP_TABLE.TABLE_NAME+" WHERE "+DBContract.CONTACT_GROUP_TABLE.GROUP_ID+" = :id")
    fun getGroupForId(id:Long): List<ContactGroup>

    @Query("SELECT * FROM "+DBContract.CONTACT_GROUP_TABLE.TABLE_NAME)
    fun getAllGroups(): Single<List<ContactGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIntoGroupTable(group: ContactGroup)

    @Delete
    fun deleteGroup(group: ContactGroup)

    @Query("DELETE FROM "+DBContract.CONTACT_GROUP_TABLE.TABLE_NAME)
    fun deleteTable()
}