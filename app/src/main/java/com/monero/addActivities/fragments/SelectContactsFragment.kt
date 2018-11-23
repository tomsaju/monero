package com.monero.addActivities.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.ContactsContract
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.monero.Application.ApplicationController
import com.monero.R
import com.monero.addActivities.adapter.ContactListAdapter
import com.monero.models.ContactMinimal
import kotlinx.android.synthetic.main.select_contact_fragment_layout.*
import com.monero.Views.CircularProfileImage
import com.monero.addActivities.adapter.IContactSelectedListener
import com.monero.helper.AppDatabase
import com.monero.helper.AppDatabase.Companion.getAppDatabase
import com.monero.models.Contact
import com.monero.models.User
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.text.InputType
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.add_user_layout.view.*
import java.io.File
import java.io.FileInputStream


/**
 * Created by tom.saju on 3/13/2018.
 */
class SelectContactsFragment : Fragment(),CircularProfileImage.ICircularProfileImageListener,SearchView.OnQueryTextListener,IContactSelectedListener,ViewPager.OnPageChangeListener {


    override fun onSingleContactSelected(contactMinimal: ContactMinimal) {

    }


    //  var contactsListView:ListView?=null
    var doneButton:Button?=null
    var cancelButton:ImageView?=null
    lateinit var contacts:List<ContactMinimal>
    lateinit var horizontalList:LinearLayout
    var mListener:OnCotactSelectedListener?=null
    public var selectedContactList:MutableList<ContactMinimal>?= null
    lateinit var mSearchView:SearchView
    lateinit var mContext:Context
    lateinit var myContact: ContactMinimal
    lateinit var refreshButton:ImageView
    lateinit var myUser:User
    var auth = FirebaseAuth.getInstance()!!
    lateinit var viewPager:ViewPager
    lateinit var tabLayout:TabLayout
    lateinit var doneFab:FloatingActionButton
    lateinit var mListeners:ArrayList<searchChangeListener> ;
    lateinit var phoneContactFragment:PhoneBookContactsFragment
    lateinit var emailContactsFragment:PhoneBookContactsFragment
    lateinit var qrScannerFragment:QRScannerFragment
    private lateinit var storageReference: StorageReference

    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var addContactButton:ImageView
    private val TYPE_PHONE=0
    private val TYPE_EMAIL=1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // contacts = getContactsFromPhoneBook()
        mListeners = ArrayList()
        var storage = FirebaseStorage.getInstance();
        if(storage!=null) {
            firebaseStorage = storage
            storageReference = firebaseStorage?.getReference();
        }

        ApplicationController.selectedContactList = ArrayList()


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.select_contact_fragment_layout, container,
                false)
       // contactsListView = rootView?.findViewById<ListView>(R.id.all_contacts_list) as ListView
        horizontalList = rootView?.findViewById<LinearLayout>(R.id.horizontal_list) as LinearLayout
        doneButton = rootView?.findViewById<Button>(R.id.done_action_select_contacts) as Button
        cancelButton = rootView?.findViewById<ImageView>(R.id.back_button_select_contacts) as ImageView
        mSearchView = rootView?.findViewById(R.id.contacs_searchView)
        refreshButton = rootView?.findViewById(R.id.refresh_contact_button)
        viewPager = rootView?.findViewById(R.id.viewpager)
        tabLayout = rootView?.findViewById(R.id.tab_layout_select_contacts)
        doneFab = rootView?.findViewById(R.id.done_button_fab)
        tabLayout.tabGravity = TabLayout.GRAVITY_CENTER;
        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE;
        addContactButton = rootView.findViewById(R.id.add_user_button_select_contacts)

        myUser = User(auth.currentUser!!.uid,auth.currentUser!!.displayName!!, ApplicationController.preferenceManager!!.myPhone,"sample@yopmail.com")
        myContact = ContactMinimal(myUser.user_id,myUser.user_name,myUser.user_phone,myUser.user_email)
      //  contactsListView?.isTextFilterEnabled = true
        setupSearchView()
        selectedContactList = ArrayList<ContactMinimal>()
        var currentUserList = mListener?.getCurrentActivityUserList()

        for(user in currentUserList!!){
            selectedContactList?.add(ContactMinimal(user.user_id,user.user_name,user.user_phone,user.user_email))
        }
     //   loadContacts(contacts)
        //dialog?.setTitle("Select participants")
        // Do something else

        refreshButton.setOnClickListener{
            _:View?->
            phoneContactFragment.refreshContactsWithServer()
        }

        cancelButton?.setOnClickListener{v: View? ->
            mListener?.closeContactSelectFragment()
        }

        doneFab?.setOnClickListener(View.OnClickListener {
            mListener?.onContactSelected(selectedContactList)
            mListener?.closeContactSelectFragment()
           // dialog?.dismiss()
        })


        addContactButton.setOnClickListener {
            when(lastPosition){
                0->{
                    //add user by phone
                    showAddUserDialog(TYPE_PHONE)
                }
                1->{
                    //add user by email
                    showAddUserDialog(TYPE_EMAIL)

                }
                2->{
                    // add group



                }
            }
        }
      //  profileImageUrl =  storageReference?.child("displayImages/"+myUser.user_id+".jpg")
        var myProfileImage = CircularProfileImage(getActivity(), null, "You",myContact.phoneNumber,myContact.email, false, myContact.contact_id)
        horizontalList.addView(myProfileImage)
        viewPager.offscreenPageLimit=4
        setupViewPager(viewPager)
        return rootView
    }


    fun onNewContactAdded(user:User){

        var minimalContact = ContactMinimal(user.user_id,user.user_name,user.user_phone,user.user_email)
        var alreadyAdded = false
        for(contact in selectedContactList!!){
            if(contact.contact_id==minimalContact.contact_id){
                alreadyAdded = true
            }
        }
        if(alreadyAdded){
            Toast.makeText(requireContext(),"User Already Added",Toast.LENGTH_SHORT).show()
        }else{
            selectedContactList?.add(minimalContact)
            onContactSelected(ArrayList(selectedContactList))
        }
    }


    private fun setupSearchView() {
        mSearchView.setIconifiedByDefault(false)
        mSearchView.setOnQueryTextListener(this)
        mSearchView.isSubmitButtonEnabled = true
        mSearchView.queryHint = "Search By name or number"
    }


   fun onSingleUserSelected(contact:ContactMinimal){
       var currentlySelectedUsers = ApplicationController.selectedContactList
       if(currentlySelectedUsers.contains(contact)){
           currentlySelectedUsers.remove(contact)
       }else{
           currentlySelectedUsers.add(contact)
       }

       selectedContactList = currentlySelectedUsers
       onContactSelected(ArrayList(selectedContactList))

       //selectedContactList?.add(contact)
   }

    @Synchronized
    fun registerSearchListener(listener: searchChangeListener) {
        mListeners.add(listener)
    }

    @Synchronized
    fun unregisterSearchListener(listener: searchChangeListener) {
        mListeners.remove(listener)
    }

    fun showAddUserDialog(type:Int){
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_user_layout, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(requireContext())
                .setView(mDialogView)
                .setCancelable(false)
        //show dialog
        val  mAlertDialog = mBuilder.show()

        if(type==TYPE_EMAIL){
            mDialogView.text_input_layout_email.hint = "Email"
            mDialogView.add_user_phone_autotextview.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }else{
            mDialogView.text_input_layout_email.hint ="Phone"
            mDialogView.add_user_phone_autotextview.inputType = InputType.TYPE_CLASS_PHONE
        }
        //login button click of custom layout
        mDialogView.add_user_btn.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
            //get text from EditTexts of custom layout
            val name = mDialogView.add_user_name_autotextview.text.toString()
            val credential = mDialogView.add_user_phone_autotextview.text.toString()
            if(type==TYPE_EMAIL){
                if(name.isNotEmpty()){
                    if(credential.isNotEmpty()){
                        var user  = User(credential,name,"",credential)
                        onNewContactAdded(user)
                    }else{
                        Toast.makeText(requireContext(),"Please Enter email",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(requireContext(),"Please Enter name",Toast.LENGTH_SHORT).show()
                }
            }else{
                if(name.isNotEmpty()){
                    if(credential.isNotEmpty()){
                        var user  = User(credential,name,credential,"")
                        onNewContactAdded(user)
                    }else{
                        Toast.makeText(requireContext(),"Please Enter phone",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(requireContext(),"Please Enter name",Toast.LENGTH_SHORT).show()
                }
            }

            //set the input text in TextView
        }
        //cancel button click of custom layout
        mDialogView.add_user_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        val selection = selectedContactList
        if(selection!=null&&selection.size>1){
            doneFab.show()
        }else{
            doneFab.hide()
        }

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
    override fun onProfileClosed(name: String?, phone: String?, email: String?) {
        phoneContactFragment.deleteSelectedContact(name,phone,email)
    }



    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (TextUtils.isEmpty(newText)) {

            for (listener in mListeners) {
                listener.onSearchQueryChanged("")
            }

        } else {



            if(newText!=null) {
                for (listener in mListeners) {
                    listener.onSearchQueryChanged(newText)
                }
            }else{
                for (listener in mListeners) {
                    listener.onSearchQueryChanged("")
                }
            }
           /* if(newText!!.contains("@")){
                var newUser = ContactMinimal(newText,"","",newText)
                (contactsListView?.adapter as ContactListAdapter).setNewItem(newUser)
            }else {

                (contactsListView?.adapter as ContactListAdapter).filter.filter(newText)
            }*/
        }
        return true
    }


    private lateinit var profileImageUrl: StorageReference

    override fun onContactSelected(contactList: ArrayList<ContactMinimal>) {

        ApplicationController.selectedContactList = contactList
        selectedContactList?.clear()
        horizontalList.removeAllViews()
        if(!contactList.contains(myContact)) {
            contactList?.add(0,myContact)
        }
        if(contactList.size>1){
            doneFab.show()
        }else{
            doneFab.hide()
        }

        for(contact in contactList) {

            var profileImage = CircularProfileImage(getActivity(), null, contact.name, contact.phoneNumber, contact.email, true, contact.contact_id)
            if(contact.contact_id==myContact.contact_id){
                profileImage = CircularProfileImage(getActivity(), null, "You", contact.phoneNumber, contact.email, false, contact.contact_id)
            }else if (contact.name.isNotEmpty()) {
                profileImage = CircularProfileImage(getActivity(), null, contact.name, contact.phoneNumber, contact.email, true, contact.contact_id)
            } else if (contact.phoneNumber.isNotEmpty()) {
                profileImage = CircularProfileImage(getActivity(), null, contact.name, contact.phoneNumber, contact.email, true, contact.contact_id)
            } else if (contact.email.isNotEmpty()) {
                profileImage = CircularProfileImage(getActivity(), null, contact.name, contact.phoneNumber, contact.email, true, contact.contact_id)
            }


            profileImage?.setProfileImageListener(this@SelectContactsFragment)
            selectedContactList?.add(contact)
            horizontalList.addView(profileImage)
            horizontal_scrollview.post(Runnable { horizontal_scrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT) })
        }
    }


    internal class Adapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragmentList:ArrayList<Fragment> = ArrayList(emptyList<Fragment>())
        private val mFragmentTitleList:ArrayList<String> = ArrayList(emptyList())

        override fun getItem(position: Int): Fragment {
            return mFragmentList.get(position)
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList.get(position)
        }
    }


    // Add Fragments to Tabs
    private fun setupViewPager(viewPager: ViewPager) {


        phoneContactFragment = PhoneBookContactsFragment()
        var phoneArgument = Bundle()
        phoneArgument.putString("listType","phone")
        phoneContactFragment.arguments = phoneArgument

        emailContactsFragment = PhoneBookContactsFragment()
        var emailArgument = Bundle()
        emailArgument.putString("listType","email")
        emailContactsFragment.arguments = emailArgument


        qrScannerFragment = QRScannerFragment()

        val adapter = Adapter(childFragmentManager)

        adapter.addFragment(phoneContactFragment, "Contacts")
        adapter.addFragment(emailContactsFragment, "Emails")


        adapter.addFragment(GroupContactsFragment(), "Groups")
        adapter.addFragment(qrScannerFragment, "Scan QR code")
        viewPager.adapter = adapter
        viewPager.setOnPageChangeListener(this)
        tabLayout.setupWithViewPager(viewPager)

    }


    interface searchChangeListener {
        fun onSearchQueryChanged(query:String)

    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    private var lastPosition: Int = 0

    override fun onPageSelected(position: Int) {

        lastPosition = position
       if(position==3){
           mSearchView.visibility = View.GONE
           qrScannerFragment.resume()
       }else{
           mSearchView.visibility = View.VISIBLE
           qrScannerFragment.pause()
       }
    }


}

