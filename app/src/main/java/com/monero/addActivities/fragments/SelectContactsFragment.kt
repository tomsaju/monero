package com.monero.addActivities.fragments

import android.app.Activity
import android.app.DialogFragment
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import android.text.TextUtils
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
import kotlinx.android.synthetic.main.select_contact_fragment_layout.*
import com.monero.Views.CircularProfileImage
import com.monero.models.User


/**
 * Created by tom.saju on 3/13/2018.
 */
class SelectContactsFragment : Fragment(),CircularProfileImage.ICircularProfileImageListener,SearchView.OnQueryTextListener {


    var contactsListView:ListView?=null
    var doneButton:Button?=null
    var cancelButton:Button?=null
    lateinit var contacts:List<Contact>
    lateinit var horizontalList:LinearLayout
    var mListener:OnCotactSelectedListener?=null
    var selectedContactList:MutableList<Contact>?= null
    lateinit var mSearchView:SearchView
    lateinit var mContext:Context
    lateinit var myContact:Contact
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // contacts = getContacts()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.select_contact_fragment_layout, container,
                false)
        contactsListView = rootView?.findViewById<ListView>(R.id.all_contacts_list) as ListView
        horizontalList = rootView?.findViewById<LinearLayout>(R.id.horizontal_list) as LinearLayout
        doneButton = rootView?.findViewById<Button>(R.id.done_action_select_contacts) as Button
        cancelButton = rootView?.findViewById<Button>(R.id.cancel_action_select_contacts) as Button
        mSearchView = rootView?.findViewById(R.id.contacs_searchView)

        contactsListView?.isTextFilterEnabled = true
        setupSearchView()
        selectedContactList = ArrayList()
        var currentUserList = mListener.getCurrentActivityUserList()

        for(user in currentUserList){
            selectedContactList?.add(Contact(user.user_name,user.user_phone))
        }
     //   loadContacts(contacts)
        //dialog?.setTitle("Select participants")
        // Do something else

        cancelButton?.setOnClickListener{v: View? ->
            mListener?.closeContactSelectFragment()
        }

        doneButton?.setOnClickListener(View.OnClickListener {
            mListener?.onContactSelected(selectedContactList)
            mListener?.closeContactSelectFragment()
           // dialog?.dismiss()
        })

        return rootView
    }

    private fun setupSearchView() {
        mSearchView.setIconifiedByDefault(false)
        mSearchView.setOnQueryTextListener(this)
        mSearchView.isSubmitButtonEnabled = true
        mSearchView.queryHint = "Search Here"
    }

    override fun onResume() {
        super.onResume()
        var contactList =  getContacts()
        loadContacts(contactList)

    }


    public fun loadContacts(contactsList:List<Contact>){
        val contactsAdapter:ContactListAdapter = ContactListAdapter(requireContext(),contactsList,this)
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
        mContext = context!!
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if(activity is OnCotactSelectedListener){
            mListener = activity
        }
    }
    interface OnCotactSelectedListener {
        fun onContactSelected(contactList:MutableList<Contact>?)
        fun getAllContactList()
        fun closeContactSelectFragment()
        fun setCurrentActivityUserList(userList: java.util.ArrayList<User>)
        fun getCurrentActivityUserList(): java.util.ArrayList<User>
    }
    override fun onProfileClosed(name: String?) {

    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (TextUtils.isEmpty(newText)) {
            contactsListView?.clearTextFilter()
        } else {
            (contactsListView?.adapter as ContactListAdapter).filter.filter(newText)
        }
        return true
    }


    fun getContacts(): ArrayList<Contact> {
        val PROJECTION = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER)

        val contactsList: ArrayList<Contact> = ArrayList()
        val builder = StringBuilder()
        val resolver: ContentResolver = mContext?.contentResolver


        val cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);
        if (cursor != null) {
            try {
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                var name: String
                var number: String
                while (cursor.moveToNext()) {
                    name = cursor.getString(nameIndex)
                    number = cursor.getString(numberIndex)

                    var newCOntact: Contact = Contact(name, number)
                    contactsList.add(newCOntact)
                }
            } finally {
                cursor.close()
            }

        }
        return contactsList
    }

}

