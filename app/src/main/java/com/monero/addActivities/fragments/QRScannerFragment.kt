package com.monero.addActivities.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.integration.android.IntentIntegrator

import com.monero.R
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import com.google.zxing.integration.android.IntentResult
import android.content.Intent
import android.widget.Button


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [QRScannerFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 */
class QRScannerFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    private var qrScan: IntentIntegrator? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //intializing scan object
        qrScan = IntentIntegrator(requireActivity())
        var view = inflater.inflate(R.layout.fragment_qrscanner, container, false)
        var scanbtn: Button = view.findViewById(R.id.scan_qr_code)
        scanbtn.setOnClickListener {
            qrScan!!.initiateScan()
        }
        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }



    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    //Getting the scan results
     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            //if qrcode has nothing in it
            if (result.contents == null) {
                Toast.makeText(requireContext(), "Result Not Found", Toast.LENGTH_LONG).show()
            } else {
                //if qr contains data
                try {
                    //converting the data to json
                    val obj = JSONObject(result.contents)
                    //setting values to textviews
                   // textViewName.setText(obj.getString("name"))
                    //textViewAddress.setText(obj.getString("address"))
                } catch (e: JSONException) {
                    e.printStackTrace()
                    //if control comes here
                    //that means the encoded format not matches
                    //in this case you can display whatever data is available on the qrcode
                    //to a toast
                    Toast.makeText(requireContext(), result.contents, Toast.LENGTH_LONG).show()
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}// Required empty public constructor
