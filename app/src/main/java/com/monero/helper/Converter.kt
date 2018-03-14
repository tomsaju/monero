package com.monero.helper

import android.arch.persistence.room.TypeConverter
import com.monero.models.Tag
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson



/**
 * Created by tom.saju on 3/14/2018.
 */
class Converter {

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
}