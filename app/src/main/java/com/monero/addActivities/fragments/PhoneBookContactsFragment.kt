package com.monero.addActivities.fragments

import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.Fragment
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.monero.Application.ApplicationController

import com.monero.R
import com.monero.addActivities.adapter.ContactListAdapter
import com.monero.addActivities.adapter.IContactSelectedListener
import com.monero.helper.AppDatabase
import com.monero.helper.ImageSaver
import com.monero.models.Contact
import com.monero.models.ContactMinimal
import com.monero.network.ServiceRest
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList


class PhoneBookContactsFragment : Fragment(),IContactSelectedListener, SelectContactsFragment.searchChangeListener {

    private var mListenerPhonebookContacts: onContactSelectedListener? = null
    lateinit var listView:ListView
    var listType = "phone";
    var selectedContactsList: ArrayList<ContactMinimal> = ArrayList()
    var auth = FirebaseAuth.getInstance()!!
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseStorage: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var storage = FirebaseStorage.getInstance();
        if(storage!=null) {
            firebaseStorage = storage
            storageReference = firebaseStorage?.getReference();
        }
    }

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
            loadAllContacts()
          //  refreshContacts()
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

    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val SelectContactsFragment = this@PhoneBookContactsFragment.getParentFragment() as SelectContactsFragment

        SelectContactsFragment.registerSearchListener(this@PhoneBookContactsFragment)
    }

    override fun onDetach() {
        super.onDetach()
        mListenerPhonebookContacts = null
        val SelectContactsFragment = this@PhoneBookContactsFragment.getParentFragment() as SelectContactsFragment
        SelectContactsFragment.unregisterSearchListener(this@PhoneBookContactsFragment)
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
    interface onContactSelectedListener {
        // TODO: Update argument type and name
        fun onSingleContactSelected(contactMinimal:ContactMinimal)
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




    fun getContactsFromPhoneBook(): ArrayList<Contact> {
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
        selectedContactsList = contactsList
        (parentFragment as IContactSelectedListener).onContactSelected(contactsList)
    }


    override fun onSearchQueryChanged(query: String) {
        (listView.adapter as ContactListAdapter).filter.filter(query)
    }

    fun deleteSelectedContact(name: String?, phone: String?, email: String?) {
        selectedContactsList = ArrayList((parentFragment as SelectContactsFragment).selectedContactList)
        for(contact in selectedContactsList){
            if(contact.name==name&&contact.phoneNumber==phone&&contact.email==email){
                selectedContactsList.remove(contact)
                break
            }
        }
        onContactSelected(selectedContactsList)
        (listView.adapter as ContactListAdapter).setSelectedContacts(selectedContactsList)
    }

    fun refreshContactsWithServer(){

        var contactListFromPhoneBook = getContactsFromPhoneBook()
        sendContactsToServer(contactListFromPhoneBook)
    }



    private fun sendContactsToServer(contactList: ArrayList<Contact>) {
        var numberLIst:ArrayList<String> = ArrayList()
        for(contact in contactList){
            //  var number:String=contact.Contact_phone.replace("+","");
            var  number = contact.Contact_phone.replace("\\s".toRegex(), "")
            numberLIst.add(number)
        }


        normalizePhoneNumbers(numberLIst)


        var contactsJSON = "["
        for(i in 0 until numberLIst.size){
            contactsJSON+="\""+ numberLIst[i]+"\""
            if(i==numberLIst.size-1){

            }else{
                contactsJSON+=","
            }
        }

        contactsJSON+="]"



        var user  = auth!!.currentUser
        user?.getIdToken(true)
                ?.addOnCompleteListener(OnCompleteListener<GetTokenResult> { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result!!.token
                        var service = ServiceRest()

                        var params = java.util.HashMap<String, String>()
                        params.put("token",idToken!!)
                        params.put("contactList",contactsJSON)

                        service.getRegisteredContacts(requireContext(),"getRegisteredUsers",params,{response ->
                            //   Log.d("backend response",response)
                            if(response!=null&&response.length>0){

                                //find a way to distinguish success and error response
                                //incase of success only ,proceed
                                //compare results and save to db
                                saveToDB(response,contactList)

                            }else{

                            }
                        })

                    } else {
                        // Handle error -> task.getException();
                    }
                })

        /*if(contactArray!=null&&contactArray.length()>0){
            sendContactsJSON(contactArray)
        }*/
    }

    private fun saveToDB(response: String,localCOntacts:ArrayList<Contact>) {
        var registeredList = JSONArray(response)
        if(registeredList!=null&&registeredList.length()>0){

            for(i in 0 until registeredList.length()){
                for(phoneContact in localCOntacts){

                    var  number = phoneContact.Contact_phone.replace("\\s".toRegex(), "")
                    if(registeredList.getJSONObject(i).getString("phoneNumber").contains(number)){
                        phoneContact.Contact_uuid = registeredList.getJSONObject(i).getString("uid")
                        phoneContact.Contact_email = registeredList.getJSONObject(i).getString("email")
                        phoneContact.Contact_name_public = registeredList.getJSONObject(i).getString("name")
                        phoneContact.Contact_phone = registeredList.getJSONObject(i).getString("phoneNumber")
                        var profileImageUrl =  storageReference?.child("displayImages/"+phoneContact.Contact_uuid+".jpg")


                        val ONE_MEGABYTE: Long = 1024 * 1024
                        profileImageUrl.getBytes(ONE_MEGABYTE).addOnSuccessListener {bytes ->
                            // Data for "images/island.jpg" is returned, use this as needed

                            Single.fromCallable {

                                var options =  BitmapFactory.Options()
                                options.inMutable = true
                                var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options);


                                ImageSaver(requireContext())
                                        .setFileName(phoneContact.Contact_uuid+".jpg")
                                        .setExternal(false)//image save in external directory or app folder default value is false
                                        .setDirectory("profile")
                                        .save(bmp); //Bitmap from your code

                            }.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(Consumer {

                                    })




                        }.addOnFailureListener {
                            // Handle any errors
                        }
                    }
                }
            }

        }

        //insert in DB
        var db = AppDatabase.getAppDatabase(requireContext())


        Single.fromCallable({
            db?.contactDao()?.insertAllContactIntoContactTable(localCOntacts)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterSuccess {
                    ApplicationController.preferenceManager?.contactSyncDate = System.currentTimeMillis()
                    Log.d("PhoneBook","saved contacts to DB after syncing")
                    loadAllContacts()
                }
                .subscribe()

    }


     fun normalizePhoneNumbers(numberLIst: ArrayList<String>) {
        //check if first 1 to 3 characters match any country code . if false--> append the user's country code to the number
        //if true, check if string contains "+" if true, return .else --> add "+"
        //make use of phonenumberUtils class

        var myphoneNumberUtil = PhoneNumberUtil.getInstance()
        for(i in 0 until numberLIst.size){

            var number = numberLIst[i]

            try {
                var phoneNumber = myphoneNumberUtil.parse(number,null)

                val isValid = myphoneNumberUtil.isValidNumber(phoneNumber)&&number.length>=5 // returns true if valid
                if (isValid) {
                    // Actions to perform if the number is valid


                } else {
                    // Do necessary actions if its not valid

                    numberLIst[i]=""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                /* if(e.message?.trim()==="INVALID_COUNTRY_CODE. Missing or invalid default region.")
                 {*/
                var tm = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                var countryCode = tm.simCountryIso;
                var daillingCodeForCountry = getCountryDiallingCode(countryCode)
                number=daillingCodeForCountry+number
                numberLIst[i] = number

                //   }
            }

            if(!numberLIst[i].startsWith("+")){
                numberLIst[i]="+"+numberLIst[i]
            }

        }

    }


    fun getCountryDiallingCode(countryCode:String):String{
        var contryDialCode=""
        var countryId = countryCode.toUpperCase()
        val arrContryCode = context?.resources?.getStringArray(R.array.DialingCountryCode)
        for (i in arrContryCode!!.indices) {
            val arrDial = arrContryCode[i].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (arrDial[1].trim { it <= ' ' } == countryId.trim()) {
                contryDialCode = arrDial[0]
                break
            }
        }
        return contryDialCode
    }

    override fun onSingleContactSelected(contactMinimal: ContactMinimal) {
        val SelectContactsFragment = this@PhoneBookContactsFragment.getParentFragment() as SelectContactsFragment
        SelectContactsFragment.onSingleUserSelected(contactMinimal)
    }
}// Required empty public constructor
