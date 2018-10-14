package com.monero.addActivities.fragments

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.Fragment
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.monero.Application.ApplicationController
import com.monero.Dao.ContactDAO
import com.monero.R
import com.monero.addActivities.adapter.ContactListAdapter
import com.monero.models.ContactMinimal
import kotlinx.android.synthetic.main.select_contact_fragment_layout.*
import com.monero.Views.CircularProfileImage
import com.monero.helper.AppDatabase
import com.monero.helper.AppDatabase.Companion.getAppDatabase
import com.monero.models.ActivitiesMinimal
import com.monero.models.Contact
import com.monero.models.User
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by tom.saju on 3/13/2018.
 */
class SelectContactsFragment : Fragment(),CircularProfileImage.ICircularProfileImageListener,SearchView.OnQueryTextListener {


    var contactsListView:ListView?=null
    var doneButton:Button?=null
    var cancelButton:Button?=null
    lateinit var contacts:List<ContactMinimal>
    lateinit var horizontalList:LinearLayout
    var mListener:OnCotactSelectedListener?=null
    var selectedContactList:MutableList<ContactMinimal>?= null
    lateinit var mSearchView:SearchView
    lateinit var mContext:Context
    lateinit var myContact: ContactMinimal
    lateinit var refreshButton:TextView
    lateinit var myUser:User
    var auth = FirebaseAuth.getInstance()!!

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
        refreshButton = rootView?.findViewById(R.id.refresh_contact_list)

        myUser = User(auth.currentUser!!.uid,auth.currentUser!!.displayName!!, ApplicationController.preferenceManager!!.myCredential,"sample@yopmail.com")
        var myContact = ContactMinimal(myUser.user_id,myUser.user_name,myUser.user_phone)
        contactsListView?.isTextFilterEnabled = true
        setupSearchView()
        selectedContactList = ArrayList<ContactMinimal>()
        var currentUserList = mListener?.getCurrentActivityUserList()

        for(user in currentUserList!!){
            selectedContactList?.add(ContactMinimal(user.user_id,user.user_name,user.user_phone))
        }
     //   loadContacts(contacts)
        //dialog?.setTitle("Select participants")
        // Do something else

        refreshButton.setOnClickListener{
            _:View?->
            refreshContacts()
        }

        cancelButton?.setOnClickListener{v: View? ->
            mListener?.closeContactSelectFragment()
        }

        doneButton?.setOnClickListener(View.OnClickListener {
            mListener?.onContactSelected(selectedContactList)
            mListener?.closeContactSelectFragment()
           // dialog?.dismiss()
        })

        var myProfileImage = CircularProfileImage(getActivity(), resources.getDrawable(R.drawable.pete), "You", false, "my " + " id ")
        horizontalList.addView(myProfileImage)

        return rootView
    }

    private fun setupSearchView() {
        mSearchView.setIconifiedByDefault(false)
        mSearchView.setOnQueryTextListener(this)
        mSearchView.isSubmitButtonEnabled = true
        mSearchView.queryHint = "Search Here"
    }

    fun refreshContacts(){
        var contactList =  getContacts()

        syncContactsWithServer(contactList);

        var db = getAppDatabase(requireContext())


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
        mListener?.syncContactsWithServer(contactList)
    }

    override fun onResume() {
        super.onResume()
        loadAllContacts()
    }

    private fun loadAllContacts() {

        var single: Single<List<Contact>>? = AppDatabase.db?.contactDao()?.getAllContactsMinimal()
        if (single != null) {
            single.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doAfterSuccess({ listFromDB: List<Contact> ->
                        var minimalContactList = ArrayList<ContactMinimal>()
                        for(contact in listFromDB){
                            minimalContactList.add(ContactMinimal(contact.Contact_uuid,contact.Contact_name_local,contact.Contact_phone))
                        }
                        loadContacts(minimalContactList)
                    })
                    .subscribe()

        }
    }


     fun loadContacts(contactsList:List<ContactMinimal>){

        var sortedList = contactsList.sortedWith(compareBy({ it.name }))

        val contactsAdapter:ContactListAdapter = ContactListAdapter(requireContext(),sortedList,this)
        contactsListView?.adapter = contactsAdapter
    }

     fun onContactSelected(contact: ContactMinimal) {
         var profileImage = CircularProfileImage(getActivity(), resources.getDrawable(R.drawable.pete), contact.name, true, contact.phoneNumber + " id ")
         profileImage.setProfileImageListener { this@SelectContactsFragment as CircularProfileImage.ICircularProfileImageListener }
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
        fun onContactSelected(contactList:MutableList<ContactMinimal>?)
        fun getAllContactList()
        fun closeContactSelectFragment()
        fun setCurrentActivityUserList(userList: java.util.ArrayList<User>)
        fun getCurrentActivityUserList(): java.util.ArrayList<User>
        fun syncContactsWithServer(contactList: ArrayList<Contact>)
    }
    override fun onProfileClosed(name: String?) {
        var selectedItem = selectedContactList?.filter { contactMinimal: ContactMinimal -> contactMinimal.name==name }
        if(selectedItem!=null&&selectedContactList!=null) {
            selectedContactList?.remove(selectedItem as ContactMinimal)
            horizontalList.removeAllViews();
            for(item in selectedContactList!!){
                onContactSelected(item);

            }
        }

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
                        var contact  = Contact(0,displayName,"unknown",displayNumber,"unknoen","unknoen","unknown")

                        contactsList.add(contact)
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

}

