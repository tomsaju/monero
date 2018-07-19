package com.monero.addActivities.fragments

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ListView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.monero.R
import com.monero.addActivities.adapter.ContactListAdapter
import com.monero.models.Contact
import com.monero.models.Tag
import kotlinx.android.synthetic.main.select_contact_fragment_layout.*
import android.widget.TextView
import com.monero.Views.CircularProfileImage
import com.monero.Views.ContactsView
import com.pchmn.materialchips.ChipView
import me.gujun.android.taggroup.TagGroup





/**
 * Created by tom.saju on 3/13/2018.
 */
class SelectContactsFragment : DialogFragment(),CircularProfileImage.ICircularProfileImageListener {


    var contactsListView:ListView?=null
    var doneButton:Button?=null
    var cancelButton:Button?=null
    lateinit var contacts:List<Contact>
    lateinit var horizontalList:LinearLayout
    var mListener:OnCotactSelectedListener?=null
    var selectedContactList:MutableList<Contact>?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listString:String? = arguments?.getString("list")
        val gson = Gson()
        val type = object : TypeToken<List<Contact>>() {
        }.type
        contacts= gson.fromJson(listString, type)
        selectedContactList = mutableListOf<Contact>()

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.select_contact_fragment_layout, container,
                false)
        contactsListView = rootView?.findViewById<ListView>(R.id.all_contacts_list) as ListView
        horizontalList = rootView?.findViewById<LinearLayout>(R.id.horizontal_list) as LinearLayout
        doneButton = rootView?.findViewById<Button>(R.id.done_action_select_contacts) as Button
        cancelButton = rootView?.findViewById<Button>(R.id.cancel_action_select_contacts) as Button

        loadContacts(contacts)
        dialog?.setTitle("Select participants")
        // Do something else

        doneButton?.setOnClickListener(View.OnClickListener {
            mListener?.onContactSelected(selectedContactList)
            dialog?.dismiss()
        })

        return rootView
    }

    override fun onResume() {
        super.onResume()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        dialog.window!!.setLayout(width, height)
    }


    public fun loadContacts(contactsList:List<Contact>){
        val contactsAdapter:ContactListAdapter = ContactListAdapter(activity,contactsList,this)
        contactsListView?.adapter = contactsAdapter
    }

     fun onContactSelected(contact:Contact) {




         var profileImage = CircularProfileImage(getActivity(), resources.getDrawable(R.drawable.pete), contact.name, true, contact.phoneNumber + " id ")



         selectedContactList?.add(contact)
         horizontalList.addView(profileImage)

         horizontal_scrollview.post(Runnable { horizontal_scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT) })


     }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(context is OnCotactSelectedListener){
          mListener = context
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if(activity is OnCotactSelectedListener){
            mListener = activity
        }
    }
    interface OnCotactSelectedListener {
        fun onContactSelected(contactList:MutableList<Contact>?)
    }
    override fun onProfileClosed(name: String?) {

    }

}