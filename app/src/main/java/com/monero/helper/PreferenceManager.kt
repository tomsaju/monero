package com.monero.helper

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by tom.saju on 8/23/2018.
 */
class PreferenceManager(context: Context) {

    val PREFS_FILENAME = "monero.preference"
    val MY_REGISTERED_NUMBER = "mRegisterdNumber" //stores the registered phone number
    val MY_REGISTERED_EMAIL = "mRegisteredEmail" //stores the registered phone number
    val MY_PROFILE_IMAGE = "mDisplayImage"
    val PREFERRED_CURRENCY_CODE = "mPreferredCurrencyCode"
    val PREFERRED_CURRENCY_SYMBOL = "mPreferredCurrencySymbol"
    val PREFERRED_CURRENCY_NAME = "mPreferredCurrencyName"
    val CONTACT_SYNC_DATE = "mContactSyncDate"
    val MY_UID="mUID"
    val FCM_TOKEN="fcmToken"

    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME,0)

    var myPhone: String
        get() = prefs.getString(MY_REGISTERED_NUMBER,"")
        set(value) = prefs.edit().putString(MY_REGISTERED_NUMBER, value).apply()
    var myEmail: String
        get() = prefs.getString(MY_REGISTERED_EMAIL,"")
        set(value) = prefs.edit().putString(MY_REGISTERED_EMAIL, value).apply()

    var myUid:String
        get() = prefs.getString(MY_UID,"")
        set(value) = prefs.edit().putString(MY_UID,value).apply()

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

    var fcmToken:String
        get() = prefs.getString(FCM_TOKEN,"")
        set(value) = prefs.edit().putString(FCM_TOKEN,value).apply()

    var contactSyncDate:Long
        get() = prefs.getLong(CONTACT_SYNC_DATE,0)
        set(value) = prefs.edit().putLong(CONTACT_SYNC_DATE,value).apply()

}