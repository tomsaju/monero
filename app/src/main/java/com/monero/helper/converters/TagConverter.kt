package com.monero.helper.converters

import android.arch.persistence.room.TypeConverter
import com.monero.models.Tag
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import com.monero.models.User


/**
 * Created by tom.saju on 3/14/2018.
 */
class TagConverter {

    @TypeConverter
    fun convertTagListtoString(tagList:List<Tag>):String{
        val gson = Gson()
        val type = object : TypeToken<List<Tag>>() {}.type
        return gson.toJson(tagList, type)
    }

    @TypeConverter
    fun convertJSONtoList(json:String):List<Tag>{
        val gson = Gson()
        val type = object : TypeToken<List<Tag>>() {

        }.type
        return gson.fromJson(json, type)
    }

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