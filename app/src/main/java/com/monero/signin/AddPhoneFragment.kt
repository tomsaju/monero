package com.monero.signin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

import com.monero.R
import com.monero.main.MainActivity
import net.rimoto.intlphoneinput.IntlPhoneInput
import java.util.concurrent.TimeUnit


class AddPhoneFragment : Fragment() {


    var TAG ="AddPhoneFragment"
    private var mListener: OnAddPhoneFragmentInteractionListener? = null
    lateinit var phoneEdittext:IntlPhoneInput
    lateinit var progressBar:ProgressBar
    lateinit var submitBtn:Button
    lateinit var skipBtn:Button
    lateinit var enterPhoneLayout:RelativeLayout
    lateinit var enterOtpLayout:RelativeLayout
    private lateinit var storedVerificationId: String
    private lateinit var enterOtpEdittext:EditText
    private lateinit var submitOtpButton:Button

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


         callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                progressBar.visibility  = View.INVISIBLE
                mListener?.signInWithPhoneCredential(credential)

               // signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                progressBar.visibility  = View.INVISIBLE
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

            override fun onCodeSent(
                    verificationId: String?,
                    token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId!!)
                progressBar.visibility  = View.INVISIBLE
                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token

                // ...
                //show the view to enter otp
                showOtpForm()

            }
        }

    }

    private fun showOtpForm() {
        enterOtpLayout.visibility = View.VISIBLE
        enterPhoneLayout.visibility = View.INVISIBLE
    }





    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view:View? = inflater.inflate(R.layout.add_phone_number_layout, container, false)
        phoneEdittext = view!!.findViewById(R.id.edit_text_phone_input)
        enterOtpLayout = view!!.findViewById(R.id.enter_otp_parent)
        enterPhoneLayout = view!!.findViewById(R.id.enter_phone_layout)
        enterOtpEdittext = view!!.findViewById(R.id.enter_otp_edittext)
        submitOtpButton = view!!.findViewById(R.id.submit_otp_btn)

        enterOtpLayout.visibility = View.GONE
        enterPhoneLayout.visibility = View.VISIBLE

        phoneEdittext.setEmptyDefault("")
        progressBar = view!!.findViewById(R.id.progressbar)
        submitBtn = view!!.findViewById(R.id.submitBtn)
        skipBtn = view!!.findViewById(R.id.skipButn)

        progressBar.visibility  = View.INVISIBLE
        submitBtn.setOnClickListener {
            if(phoneEdittext.isValid){
                progressBar.visibility  = View.VISIBLE
                sendToServer(phoneEdittext.number)
            }else{
                Toast.makeText(requireContext(),"Invalid Number",Toast.LENGTH_SHORT).show()
            }
        }

        submitOtpButton.setOnClickListener {

            var enteredOtp = enterOtpEdittext.text.toString()
            if(enteredOtp!=null&&enteredOtp.isNotEmpty()){
                progressBar.visibility  = View.VISIBLE
                val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, enteredOtp)
                signInWithPhoneCredential(credential)
            }

        }

        skipBtn.setOnClickListener {
            startActivity(Intent(requireActivity(),MainActivity::class.java))
        }

        return view
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential?) {
        mListener?.signInWithPhoneCredential(credential)
    }

    private fun sendToServer(number: String?) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number!!,      // Phone number to verify
                60,               // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                requireActivity(),             // Activity (for callback binding)
                callbacks) // OnVerificationStateChangedCallbacks


    }




    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnAddPhoneFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement onContactSelectedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }


    interface OnAddPhoneFragmentInteractionListener {
        // TODO: Update argument type and name
        fun signInWithPhoneCredential(credential: PhoneAuthCredential?)
    }




}// Required empty public constructor
