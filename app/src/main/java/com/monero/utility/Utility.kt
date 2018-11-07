package com.monero.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.monero.Application.ApplicationController
import com.monero.activitydetail.banner.BannerViewPager

/**
 * Created by Dreamz on 24-10-2018.
 */
class Utility {
    companion object {
        fun getInHigherDenimonation(value:Int):String{
            return  "%.2f".format((value.toDouble()/100))
        }

        fun getCurrencySymbol():String{
            var prefs = ApplicationController.preferenceManager
            var currencySymbol:String = prefs!!.preferredCurrencySymbol
            var currencyCode:String = prefs!!.preferredCurrencyCode
            var prefix = ""
            if(currencySymbol!="0") {
                prefix = currencySymbol
            } else {
                prefix = currencyCode+" "
            }
            return prefix
        }

         fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            return if (connectivityManager is ConnectivityManager) {
                val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
                networkInfo?.isConnected ?: false
            } else false
        }
    }
}