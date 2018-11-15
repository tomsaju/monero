package com.monero.addActivities.fragments

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast

import com.monero.R
import com.monero.addActivities.adapter.ContactListAdapter
import com.monero.addActivities.adapter.IContactSelectedListener
import com.monero.helper.AppDatabase
import com.monero.models.Contact
import com.monero.models.ContactGroup
import com.monero.models.ContactMinimal
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.HashSet
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CreateGroupFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CreateGroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateGroupFragment : Fragment(),IContactSelectedListener {



    private var mListener: OnFragmentInteractionListener? = null
    lateinit var contactList:ListView
    lateinit var doneBtn:FloatingActionButton
    lateinit var searchview:SearchView
    lateinit var backButton:ImageView
    lateinit var edittext:EditText
    var selectedContactList: ArrayList<ContactMinimal> = java.util.ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view =  inflater.inflate(R.layout.fragment_create_group, container, false)
        contactList = view.findViewById(R.id.create_group_list)
        doneBtn = view.findViewById(R.id.done_btn_select_group)
        backButton = view.findViewById(R.id.back_button_create_group)
        searchview = view.findViewById(R.id.contacs_searchView)
        edittext = view.findViewById(R.id.group_name_edittext)
        backButton.setOnClickListener {
            getActivity()?.getSupportFragmentManager()?.beginTransaction()?.remove(this)?.commit()
        }

        doneBtn.setOnClickListener {

            if(edittext.text.toString().isNullOrEmpty()){
                Toast.makeText(requireContext(),"Please enter group name",Toast.LENGTH_SHORT).show()
            }else{
                if(selectedContactList.size<0){
                    Toast.makeText(requireContext(),"Please select members",Toast.LENGTH_SHORT).show()
                }else{
                    saveGroupinDB()
                }
            }

        }
       loadAllContacts()
        return view
    }

    private fun saveGroupinDB() {
        var db = AppDatabase.getAppDatabase(requireContext())


        var group = ContactGroup(System.currentTimeMillis(),edittext.text.toString(),selectedContactList)
        Single.fromCallable({
            db?.contactGroupDao()?.insertIntoGroupTable(group)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterSuccess {

                    Toast.makeText(requireContext(),"Group save done",Toast.LENGTH_SHORT).show()
                }
                .subscribe()
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


    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
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

    private fun loadAllContacts() {

        var single: Single<List<Contact>>? = AppDatabase.db?.contactDao()?.getAllTypeContacts()
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


    fun loadContacts(contactsList:List<ContactMinimal>){

        var sortedList = contactsList.sortedWith(compareBy({ it.name }))

        val contactsAdapter = ContactListAdapter(requireContext(),sortedList,this)
        contactList?.adapter = contactsAdapter
    }

    override fun onContactSelected(contactList: ArrayList<ContactMinimal>) {
        selectedContactList = contactList
    }
}// Required empty public constructor
