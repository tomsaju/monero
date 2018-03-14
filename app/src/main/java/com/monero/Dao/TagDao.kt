package com.monero.Dao

import android.arch.persistence.room.*
import com.monero.models.Tag

/**
 * Created by tom.saju on 3/14/2018.
 */
@Dao interface TagDao {

    @Query("SELECT * FROM "+DBContract.TAG_TABLE.TABLE_NAME)
    fun getAllTags():List<Tag>

    @Query("SELECT * FROM "+DBContract.TAG_TABLE.TABLE_NAME+" WHERE "+DBContract.TAG_TABLE.TAG_ID+" = :id")
    fun getTagForId(id:Long): Tag

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIntoTagTable(tag: Tag)

    @Delete
    fun deleteTag(tag: Tag)
}