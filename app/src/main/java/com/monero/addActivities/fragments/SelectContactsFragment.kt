package com.monero.addActivities.fragments

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.monero.R
import com.monero.addActivities.adapter.ContactListAdapter
import com.monero.models.Contact
import com.monero.models.Tag

/**
 * Created by tom.saju on 3/13/2018.
 */
class SelectContactsFragment : DialogFragment() {
    var contactsListView:ListView?=null
    lateinit var contacts:List<Contact> 
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listString:String? = arguments?.getString("list")
        val gson = Gson()
        val type = object : TypeToken<List<Contact>>() {
        }.type
        contacts= gson.fromJson(listString, type)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.select_contact_fragment_layout, container,
                false)
        contactsListView = rootView?.findViewById<ListView>(R.id.all_contacts_list) as ListView
        loadContacts(contacts)
        dialog?.setTitle("Select participants")
        // Do something else
        return rootView
    }


    public fun loadContacts(contactsList:List<Contact>){
        val contactsAdapter:ContactListAdapter = ContactListAdapter(activity,contactsList)
        contactsListView?.adapter = contactsAdapter
    }
}