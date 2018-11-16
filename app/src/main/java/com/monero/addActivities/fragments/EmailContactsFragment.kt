package com.monero.addActivities.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.monero.R


class EmailContactsFragment : Fragment(),SelectContactsFragment.searchChangeListener {

    private var mListener: OnFragmentInteractionListener? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_email_contacts, container, false)
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
        val SelectContactsFragment = this@EmailContactsFragment.getParentFragment() as SelectContactsFragment
        SelectContactsFragment.unregisterSearchListener(this@EmailContactsFragment)
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


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val SelectContactsFragment = this@EmailContactsFragment.getParentFragment() as SelectContactsFragment
        SelectContactsFragment.registerSearchListener(this@EmailContactsFragment)
    }



    override fun onSearchQueryChanged(query: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}// Required empty public constructor
