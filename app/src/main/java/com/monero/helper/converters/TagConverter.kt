package com.monero.helper.converters

import android.arch.persistence.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import com.monero.models.*


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
    public  fun convertUserListtoString(userList:List<User>):String{
        val gson = Gson()
        val type = object : TypeToken<List<User>>() {}.type
        return gson.toJson(userList, type)
    }

    @TypeConverter
    public fun convertJSONtoUser(json:String):List<User>{
        val gson = Gson()
        val type = object : TypeToken<List<User>>() {

        }.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun convertUserToString(user: User):String{
        val gson = Gson()
        val type = object :TypeToken<User>(){}.type
        return gson.toJson(user,type)
    }

    @TypeConverter
    fun convertJsonToUserObject(json: String):User{
        val gson = Gson()
        val type = object :TypeToken<User>(){}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun convertExpensetoString(expense: Expense):String{
        val gson = Gson()
        val type = object : TypeToken<Expense>() {}.type
        return gson.toJson(expense, type)
    }

    @TypeConverter
    fun convertStringToExpense(json:String):Expense{
        val gson = Gson()
        val type = object : TypeToken<Expense>() {

        }.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun convertCredittoString(credit: Credit):String{
        val gson = Gson()
        val type = object : TypeToken<Credit>() {}.type
        return gson.toJson(credit, type)
    }

    @TypeConverter
    fun convertStringToCredit(json:String):Credit{
        val gson = Gson()
        val type = object : TypeToken<Credit>() {

        }.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun convertDebittoString(debit: Debit):String{
        val gson = Gson()
        val type = object : TypeToken<Debit>() {}.type
        return gson.toJson(debit, type)
    }

    @TypeConverter
    fun convertStringToDebit(json:String):Debit{
        val gson = Gson()
        val type = object : TypeToken<Debit>() {

        }.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun convertCreditListtoString(creditList:List<Credit>):String{
        val gson = Gson()
        val type = object : TypeToken<List<Credit>>() {}.type
        return gson.toJson(creditList, type)
    }

    @TypeConverter
    fun convertJSONtoCreditList(json:String):List<Credit>{
        val gson = Gson()
        val type = object : TypeToken<List<Credit>>() {

        }.type
        return gson.fromJson<List<Credit>>(json, type)
    }
    @TypeConverter
    fun convertDebitListtoString(debitList:List<Debit>):String{
        val gson = Gson()
        val type = object : TypeToken<List<Debit>>() {}.type
        return gson.toJson(debitList, type)
    }

    @TypeConverter
    fun convertJSONtoDebitList(json:String):List<Debit>{
        val gson = Gson()
        val type = object : TypeToken<List<Debit>>() {}.type
        return gson.fromJson<List<Debit>>(json, type)
    }

    @TypeConverter
    fun convertGroupListtoString(tagList:List<ContactGroup>):String{
        val gson = Gson()
        val type = object : TypeToken<List<ContactGroup>>() {}.type
        return gson.toJson(tagList, type)
    }

    @TypeConverter
    fun convertGroupJSONtoList(json:String):List<ContactGroup>{
        val gson = Gson()
        val type = object : TypeToken<List<ContactGroup>>() {

        }.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun convertGrouptoString(tagList:ContactGroup):String{
        val gson = Gson()
        val type = object : TypeToken<ContactGroup>() {}.type
        return gson.toJson(tagList, type)
    }

    @TypeConverter
    fun convertJSONtoGroup(json:String):ContactGroup{
        val gson = Gson()
        val type = object : TypeToken<ContactGroup>() {

        }.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun convertContactMinimaltoString(tagList:ContactMinimal):String{
        val gson = Gson()
        val type = object : TypeToken<ContactMinimal>() {}.type
        return gson.toJson(tagList, type)
    }

    @TypeConverter
    fun convertJSONtoContactMinimal(json:String):ContactMinimal{
        val gson = Gson()
        val type = object : TypeToken<ContactMinimal>() {

        }.type
        return gson.fromJson(json, type)
    }


    @TypeConverter
    fun convertContactMinimalListtoString(tagList:List<ContactMinimal>):String{
        val gson = Gson()
        val type = object : TypeToken<List<ContactMinimal>>() {}.type
        return gson.toJson(tagList, type)
    }

    @TypeConverter
    fun convertContactMinimalJSONtoList(json:String):List<ContactMinimal>{
        val gson = Gson()
        val type = object : TypeToken<List<ContactMinimal>>() {

        }.type
        return gson.fromJson(json, type)
    }


}