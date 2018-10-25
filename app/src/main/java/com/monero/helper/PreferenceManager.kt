package com.monero.helper

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by tom.saju on 8/23/2018.
 */
class PreferenceManager(context: Context) {

    val PREFS_FILENAME = "monero.preference"
    val MY_CREDENTIAL = "mCredential" //stores the registered phone number
    val MY_PROFILE_IMAGE = "mDisplayImage"
    val PREFERRED_CURRENCY_CODE = "mPreferredCurrencyCode"
    val PREFERRED_CURRENCY_SYMBOL = "mPreferredCurrencySymbol"
    val PREFERRED_CURRENCY_NAME = "mPreferredCurrencyName"

    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME,0)

    var myCredential: String
        get() = prefs.getString(MY_CREDENTIAL,"")
        set(value) = prefs.edit().putString(MY_CREDENTIAL, value).apply()

    var myDisplayPicture: String
        get() = prefs.getString(MY_PROFILE_IMAGE,"")
        set(value) = prefs.edit().putString(MY_PROFILE_IMAGE, value).apply()

    var preferredCurrencyCode:String
        get() = prefs.getString(PREFERRED_CURRENCY_CODE,"USD")
        set(value) = prefs.edit().putString(PREFERRED_CURRENCY_CODE,value).apply()

    var preferredCurrencySymbol:String
        get() = prefs.getString(PREFERRED_CURRENCY_SYMBOL,"$")
        set(value) = prefs.edit().putString(PREFERRED_CURRENCY_SYMBOL,value).apply()

    var preferredCurrencyName:String
        get() = prefs.getString(PREFERRED_CURRENCY_NAME,"United States Dollar")
        set(value) = prefs.edit().putString(PREFERRED_CURRENCY_NAME,value).apply()



}