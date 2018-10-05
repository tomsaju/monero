package com.monero.network

import android.graphics.ColorSpace
import com.monero.models.Contact
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.Result
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * Created by Dreamz on 25-09-2018.
 */
interface RestService {


   /* @GET("api/v1/submitcontacts")
    fun getAllRegisteredContacts(@Query("contact_list") contactList: String,
                                 @Query("requestToken") requestToken: String):Observable<Contact>*/

    @POST("api/v1/getRegisteredUsers")
    fun getRegisteredContactForNumber(@Body localContacts:HashMap<String,String>):Observable<String>



    companion object {
        fun create(): RestService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(
                            RxJava2CallAdapterFactory.create())
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .baseUrl("https://us-central1-monero-efbcb.cloudfunctions.net/webApi/")
                    .build()

            return retrofit.create(RestService::class.java)
        }
    }

}