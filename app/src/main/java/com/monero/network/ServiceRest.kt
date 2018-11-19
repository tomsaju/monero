package com.monero.network

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.monero.Application.ApplicationController
import org.json.JSONObject



/**
 * Created by Dreamz on 04-10-2018.
 */
class ServiceRest {
    val TAG ="ServiceRest"
   // val BASE_URL = "https://us-central1-monero-efbcb.cloudfunctions.net/webApi/"
    val BASE_URL = "https://murmuring-reef-27621.herokuapp.com/"


    fun getRegisteredContacts(context:Context,path: String, params: HashMap<String,String>, completionHandler: (response: String?) -> Unit) {

      var  mRequestQueue = Volley.newRequestQueue(context);

        val stringRequest = object : StringRequest(Method.POST, BASE_URL + path,
                Response.Listener<String> { response ->
                    Log.d(TAG, "/post request OK! Response: $response")
                    completionHandler(response)
                },
                Response.ErrorListener { error ->
                    VolleyLog.e(TAG, "/post request fail! Error: ${error.message}")
                    completionHandler(null)
                }) {

            /*override fun getHeaders(): MutableMap<String, String> {
                var header = HashMap<String,String>()
                header.put("Content-Type", "application/x-www-form-urlencoded");
                return header
            }*/

            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }

            /*override fun getParams(): MutableMap<String, String> {
                return params
            }*/
        }

        mRequestQueue.add(stringRequest)
      //  ApplicationController.instance?.addToRequestQueue(stringRequest, TAG)
    }

    fun onUserRegistrationSuccess(context:Context,path: String, params: HashMap<String,String>, completionHandler: (response: String?) -> Unit) {

        var  mRequestQueue = Volley.newRequestQueue(context);

        val stringRequest = object : StringRequest(Method.POST, BASE_URL + path,
                Response.Listener<String> { response ->
                    Log.d(TAG, "/post request OK! Response: $response")
                    completionHandler(response)
                },
                Response.ErrorListener { error ->
                    VolleyLog.e(TAG, "/post request fail! Error: ${error.message}")
                    completionHandler(error.toString())
                }) {

            override fun getHeaders(): MutableMap<String, String> {
               return params
            }

            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }

            override fun getParams(): MutableMap<String, String> {
                return params
            }
        }

        mRequestQueue.add(stringRequest)
        //  ApplicationController.instance?.addToRequestQueue(stringRequest, TAG)
    }

}