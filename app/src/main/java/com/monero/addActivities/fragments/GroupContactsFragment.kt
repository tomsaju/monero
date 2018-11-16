package com.monero.addActivities.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ExpandableListView

import com.monero.R
import com.monero.addActivities.adapter.GroupsExpandableAdapter
import com.monero.addActivities.adapter.IContactSelectedListener
import com.monero.helper.AppDatabase
import com.monero.models.ContactGroup
import com.monero.models.ContactMinimal
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import android.widget.ExpandableListView.OnGroupClickListener
import com.monero.addActivities.adapter.ContactListAdapter


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [GroupContactsFragment.OnGroupContactsFragmentInteractionListener] interface
 * to handle interaction events.
 */
class GroupContactsFragment : Fragment(),SelectContactsFragment.searchChangeListener, IContactSelectedListener {

    private var mListenerGroupContacts: OnGroupContactsFragmentInteractionListener? = null
    lateinit var addBtn:Button
    lateinit var groupListView:ExpandableListView
    lateinit var adapter:GroupsExpandableAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_group_contacts, container, false)
        addBtn = view.findViewById(R.id.add_group_button)
        groupListView = view.findViewById<ExpandableListView>(R.id.expandable_groups_list) as ExpandableListView



        addBtn.setOnClickListener {
            mListenerGroupContacts?.addGroup()
        }

        groupListView.setOnGroupClickListener(OnGroupClickListener { parent, v, groupPosition, id ->
            parent.expandGroup(groupPosition)
            false
        })

        loadAllGroups()
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

        val SelectContactsFragment = this@GroupContactsFragment.getParentFragment() as SelectContactsFragment
        SelectContactsFragment.registerSearchListener(this@GroupContactsFragment)
    }

    override fun onDetach() {
        super.onDetach()
        mListenerGroupContacts = null
        val SelectContactsFragment = this@GroupContactsFragment.getParentFragment() as SelectContactsFragment
        SelectContactsFragment.unregisterSearchListener(this@GroupContactsFragment)
    }


    interface OnGroupContactsFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
        fun addGroup()
    }

    override fun onSearchQueryChanged(query: String) {
        (groupListView.expandableListAdapter as GroupsExpandableAdapter).filter.filter(query)
    }

    private fun loadAllGroups() {

        var single: Single<List<ContactGroup>>? = AppDatabase.db?.contactGroupDao()?.getAllGroups()
        if (single != null) {
            single.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doAfterSuccess({ listFromDB: List<ContactGroup> ->

                        loadGroup(listFromDB)
                    })
                    .subscribe()

        }
    }

    private fun loadGroup(groupList: List<ContactGroup>) {

        adapter = GroupsExpandableAdapter(requireContext(),groupList,this)
        groupListView.setAdapter(adapter)
    }

    override fun onContactSelected(contactList: ArrayList<ContactMinimal>) {
        (parentFragment as IContactSelectedListener).onContactSelected(contactList)
    }
}// Required empty public constructor
