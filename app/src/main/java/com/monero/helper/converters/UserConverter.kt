package com.monero.helper.converters

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.monero.models.Tag
import com.monero.models.User

/**
 * Created by tom.saju on 7/6/2018.
 */
class UserConverter {

    @TypeConverter
    fun convertUserListtoString(userList:List<User>):String{
        val gson = Gson()
        val type = object : TypeToken<List<User>>() {}.type
        return gson.toJson(userList, type)
    }

    @TypeConverter
    fun convertJSONtoUser(json:String):List<User>{
        val gson = Gson()
        val type = object : TypeToken<List<User>>() {

        }.type
        return gson.fromJson(json, type)
    }
}