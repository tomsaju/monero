package com.monero.addActivities.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.monero.R

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [GroupContactsFragment.OnGroupContactsFragmentInteractionListener] interface
 * to handle interaction events.
 */
class GroupContactsFragment : Fragment() {

    private var mListenerGroupContacts: OnGroupContactsFragmentInteractionListener? = null
    lateinit var addBtn:Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_group_contacts, container, false)
        addBtn = view.findViewById(R.id.add_group_button)
        addBtn.setOnClickListener {
            mListenerGroupContacts?.addGroup()
        }
        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListenerGroupContacts != null) {
            mListenerGroupContacts!!.onFragmentInteraction(uri)
        }
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(context is GroupContactsFragment.OnGroupContactsFragmentInteractionListener){
            mListenerGroupContacts =context
        }else{
            throw Exception("Activity must implement OnGroupContactsFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListenerGroupContacts = null
    }


    interface OnGroupContactsFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
        fun addGroup()
    }
}// Required empty public constructor
