package com.monero.utility

/**
 * Created by Dreamz on 24-10-2018.
 */
class Utility {
    companion object {
        fun getInHigherDenimonation(value:Int):String{
            return  "%.2f".format((value.toDouble()/100))
        }
    }
}