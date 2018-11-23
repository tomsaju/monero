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
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.google.zxing.client.android.BeepManager
import android.content.Intent.getIntent
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.widget.TextView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.google.zxing.BarcodeFormat
import java.util.*
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeCallback
import com.monero.Application.ApplicationController
import com.monero.addActivities.adapter.IContactSelectedListener
import com.monero.helper.converters.TagConverter
import com.monero.models.ContactMinimal
import com.monero.models.User
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [QRScannerFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 */
class QRScannerFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    private var qrScan: IntentIntegrator? = null
    private lateinit var barcodeScanner:DecoratedBarcodeView
    lateinit var beepManager:BeepManager
    var selectedContacts:ArrayList<ContactMinimal> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userVisibleHint = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //intializing scan object

        var view = inflater.inflate(R.layout.fragment_qrscanner, container, false)
        var scanbtn: TextView = view.findViewById(R.id.scan_qr_code)
        barcodeScanner = view.findViewById(R.id.barcode_scanner)
      //  val formats = ArrayList(Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39))
       // barcodeScanner.getBarcodeView().setDecoderFactory(DefaultDecoderFactory(formats))
        barcodeScanner.initializeFromIntent(activity?.intent)
        barcodeScanner.decodeContinuous(callback)

        beepManager = BeepManager(requireActivity())
        scanbtn.setOnClickListener {
            qrScan!!.initiateScan()
        }
        return view
    }


    private var previousResult = ""

    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {

            if(result.text!=null&&result.text.isNotEmpty()&&result.text!=previousResult){
                try{
                    var resultJSON = JSONObject(result.text)
                    var user: User = TagConverter().convertJsonToUserObject(result.text)
                    if(user.user_id.isNotEmpty()&&
                            user.user_name.isNotEmpty()&&
                            user.user_email.isNotEmpty()){

                        var minimalContact = ContactMinimal(user.user_id,user.user_name,user.user_phone,user.user_email)
                        var alreadyAdded = false
                        selectedContacts = ApplicationController.selectedContactList
                        if(selectedContacts.contains(minimalContact)){
                            Toast.makeText(requireContext(),"User Already Added",Toast.LENGTH_SHORT).show()
                        }else{
                            selectedContacts.add(minimalContact)
                            (parentFragment as IContactSelectedListener).onContactSelected(selectedContacts)
                            requireContext().vibrate(100)
                        }

                       /* for(contact in selectedContacts){
                            if(contact.contact_id==minimalContact.contact_id){
                                alreadyAdded = true
                            }
                        }
                        if(alreadyAdded){
                            Toast.makeText(requireContext(),"User Already Added",Toast.LENGTH_SHORT).show()
                        }else{
                            selectedContacts.add(minimalContact)
                            (parentFragment as IContactSelectedListener).onContactSelected(selectedContacts)
                            requireContext().vibrate(100)
                        }*/

                    }
                    previousResult = result.text
                }catch(j:JSONException){
                    Toast.makeText(requireContext(),"Invalid QR code",Toast.LENGTH_SHORT).show()
                }catch(e:Exception){
                    Toast.makeText(requireContext(),"Error Scanning code",Toast.LENGTH_SHORT).show()
                }

            }else{
                return
            }





          //  beepManager.playBeepSoundAndVibrate()

            //Added preview of scanned barcode
           // val imageView = findViewById(R.id.barcodePreview) as ImageView
            //imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW))
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }


    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

     override fun onPause() {
        super.onPause()

        barcodeScanner.pause()
    }

    fun pause() {

        try {
            barcodeScanner.pause()
        } catch (e:Exception) {

        }

    }

    fun resume() {

        try {
            barcodeScanner.resume()
        }  catch (e:Exception) {

        }

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }


    fun triggerScan(view: View) {
        barcodeScanner.decodeSingle(callback)
    }

    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeScanner.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        barcodeScanner.resume()
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


    // Extension method to vibrate a phone programmatically
    fun Context.vibrate(milliseconds:Long = 200){
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Check whether device/hardware has a vibrator
        val canVibrate:Boolean = vibrator.hasVibrator()

        if(canVibrate){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                // void vibrate (VibrationEffect vibe)
                vibrator.vibrate(
                        VibrationEffect.createOneShot(
                                milliseconds,
                                // The default vibration strength of the device.
                                VibrationEffect.DEFAULT_AMPLITUDE
                        )
                )
            }else{
                // This method was deprecated in API level 26
                vibrator.vibrate(milliseconds)
            }
        }
    }


    // Extension property to check whether device has Vibrator
    val Context.hasVibrator:Boolean
        get() {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            return vibrator.hasVibrator()
        }

}// Required empty public constructor
