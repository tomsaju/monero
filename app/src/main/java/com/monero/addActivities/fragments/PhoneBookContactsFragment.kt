package com.monero.addActivities.fragments

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ListView
import android.widget.Toast

import com.monero.R
import com.monero.Views.CircularProfileImage
import com.monero.addActivities.adapter.ContactListAdapter
import com.monero.addActivities.adapter.IContactSelectedListener
import com.monero.helper.AppDatabase
import com.monero.models.Contact
import com.monero.models.ContactMinimal
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.select_contact_fragment_layout.*
import java.util.*


class PhoneBookContactsFragment : Fragment(),IContactSelectedListener {

    private var mListenerPhonebookContacts: OnPhonebookContactsFragmentInteractionListener? = null
    lateinit var listView:ListView
    var listType = "phone";
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootview:View =  inflater.inflate(R.layout.fragment_phone_book_contacts, container, false)
        listView = rootview.findViewById(R.id.contacts_list)


        if(arguments!=null){
            if(arguments?.getString("listType")!=null&&arguments?.getString("listType")!=""){
                listType = arguments?.getString("listType").toString()
            }else{
                listType = "phone";
            }
        }else{
            listType = "phone";
        }

        if(listType=="phone"){
            refreshContacts()
        }else{
            loadEmailIds()
        }


        return rootview
    }

    private fun loadEmailIds() {
        var single: Single<List<Contact>>? = AppDatabase.db?.contactDao()?.getAllEmailContactsMinimal()
        if (single != null) {
            single.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doAfterSuccess({ listFromDB: List<Contact> ->
                        var minimalContactList = ArrayList<ContactMinimal>()
                        for(contact in listFromDB){
                            minimalContactList.add(ContactMinimal(contact.Contact_uuid,contact.Contact_name_local,contact.Contact_phone,contact.Contact_email))
                        }
                        loadContacts(minimalContactList)
                    })
                    .subscribe()

        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListenerPhonebookContacts != null) {
            mListenerPhonebookContacts!!.onFragmentInteraction(uri)
        }
    }



    override fun onDetach() {
        super.onDetach()
        mListenerPhonebookContacts = null
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
    interface OnPhonebookContactsFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }


    fun refreshContacts(){
        var contactList =  getContacts()

        //syncContactsWithServer(contactList);

        var db = AppDatabase.getAppDatabase(requireContext())


        Single.fromCallable({
            db?.contactDao()?.insertAllContactIntoContactTable(contactList)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterSuccess {
                    loadAllContacts();
                }
                .subscribe()
    }

    private fun syncContactsWithServer(contactList: ArrayList<Contact>) {
      //  mListener?.syncContactsWithServer(contactList)
        Toast.makeText(requireContext(),"Not implemented",Toast.LENGTH_SHORT).show()
    }


    private fun loadAllContacts() {

        var single: Single<List<Contact>>? = AppDatabase.db?.contactDao()?.getAllContactsMinimal()
        if (single != null) {
            single.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doAfterSuccess({ listFromDB: List<Contact> ->
                        var minimalContactList = ArrayList<ContactMinimal>()
                        for(contact in listFromDB){
                            minimalContactList.add(ContactMinimal(contact.Contact_uuid,contact.Contact_name_local,contact.Contact_phone,""))
                        }
                        loadContacts(minimalContactList)
                    })
                    .subscribe()

        }
    }


    fun loadContacts(contactsList:List<ContactMinimal>){

        var sortedList = contactsList.sortedWith(compareBy({ it.name }))

        val contactsAdapter = ContactListAdapter(requireContext(),sortedList,this)
          listView?.adapter = contactsAdapter
    }

    override fun onResume() {
        super.onResume()

    }




    fun getContacts(): ArrayList<Contact> {
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)//plus any other properties you wish to query
        var contactsList = ArrayList<Contact>()
        var cursor: Cursor? = null
        try {
            cursor = context?.getContentResolver()?.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null)
        } catch (e: SecurityException) {
            //SecurityException can be thrown if we don't have the right permissions
        }


        if (cursor != null) {
            try {
                val normalizedNumbersAlreadyFound = HashSet<Any?>()
                val indexOfNormalizedNumber = cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)
                val indexOfDisplayName = cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val indexOfDisplayNumber = cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor!!.moveToNext()) {
                    val normalizedNumber = cursor!!.getString(indexOfNormalizedNumber)
                    if (normalizedNumbersAlreadyFound.add(normalizedNumber)) {
                        val displayName = cursor!!.getString(indexOfDisplayName)
                        val displayNumber = cursor!!.getString(indexOfDisplayNumber)
                        //haven't seen this number yet: do something with this contact!
                        var defaultId = displayNumber.replace("+","")
                        var trimmed  = defaultId.replace("\\s".toRegex(), "")
                        try {
                            var intId = trimmed.toLong()
                            var contact  = Contact(intId,displayName,"",displayNumber,"",intId.toString(),"")

                            contactsList.add(contact)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        //don't do anything with this contact because we've already found this number
                    }
                }
            } finally {
                cursor!!.close()
            }
        }
        return contactsList
    }

    override fun onContactSelected(contactsList: ArrayList<ContactMinimal>) {
        (parentFragment as IContactSelectedListener).onContactSelected(contactsList)
    }
}// Required empty public constructor
