package com.monero.addActivities


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.monero.Application.ApplicationController

import com.monero.R
import com.monero.models.Activities
import com.monero.models.ContactMinimal
import com.monero.models.Tag
import com.monero.models.User
import com.monero.tags.TagActivity
import me.gujun.android.taggroup.TagGroup
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
public class AddActivityFragment : Fragment(),IAddActivityView {
    var REQUEST_CODE_TAG_SELECTION = 1
    lateinit var title: AutoCompleteTextView;
    lateinit var description: AutoCompleteTextView;
    lateinit var modeSelector: Spinner
    lateinit var addTagButton: ImageButton
    lateinit var addMembersButton: ImageButton
    lateinit var tagContainer: TagGroup
    lateinit var memberListParent: LinearLayout
    lateinit var addTagText: TextView
    lateinit var addMemberBanner: TextView
    lateinit var doneButton: TextView
    lateinit var cancelButton: TextView
    lateinit var selectedTagList: ArrayList<Tag>
    lateinit var mListener: IAddActivityFragmentListener
    lateinit var addMembersParent: LinearLayout
    lateinit var addActivityPresenter:IAddActivityPresenter
    lateinit var contactsList:List<ContactMinimal>
    var selectedUserList: ArrayList<User> = ArrayList()
    var auth = FirebaseAuth.getInstance()!!
    lateinit var myCredential:String
    lateinit var myContactName:TextView
    lateinit var myContactPhone:TextView
    lateinit var addMembersLayout:FrameLayout
    lateinit var progressBarContacts:ProgressBar
    lateinit var myUser:User

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.new_activity_fragment, container, false);
        title = view.findViewById(R.id.title_activity_autotextview)
        description = view.findViewById(R.id.description_activity_autotextview)
        modeSelector = view.findViewById(R.id.mode_spinner_activity)
        addTagButton = view.findViewById(R.id.add_tag_button)
        addMembersButton = view.findViewById(R.id.add_members_button)
        tagContainer = view.findViewById(R.id.tag_group)
        memberListParent = view.findViewById(R.id.members_layout)
        addTagText = view.findViewById(R.id.add_tag_text) //add_tag_text
        addMemberBanner = view.findViewById(R.id.add_mebers_banner) // add_members_banner
        doneButton = view.findViewById(R.id.done_button_new_activity) // done_button_new_activity
        cancelButton = view.findViewById(R.id.cancel_button_new_activity)  // cancel_button_new_activity
        addMembersParent = view.findViewById(R.id.add_members_parent)
        myContactName = view.findViewById(R.id.contact_name)
        myContactPhone = view.findViewById(R.id.contact_number)
        addMembersLayout = view.findViewById(R.id.add_member_layout)
        progressBarContacts = view.findViewById(R.id.contactLoadingProgressBar)


        myCredential = ApplicationController.preferenceManager!!.myCredential
        addActivityPresenter = AddActivityPresenter(requireContext(),this)
        progressBarContacts.visibility = View.GONE

        selectedUserList = ArrayList(emptyList<User>())
        selectedTagList = ArrayList(emptyList<Tag>())
        myUser = User(auth.currentUser!!.uid,auth.currentUser!!.displayName!!,ApplicationController.preferenceManager!!.myCredential,"sample@yopmail.com")

        selectedUserList.add(myUser)
        mListener.setCurrentActivityUserList(selectedUserList)

        doneButton.setOnClickListener { v: View? ->

            if (checkifInputValid()) {

                if(auth!=null) {
                    //val author = User(auth.currentUser?.uid!!, auth.currentUser?.displayName!!, auth.currentUser?.phoneNumber!!, auth.currentUser?.email!!)
                    if(auth==null){
                        auth = FirebaseAuth.getInstance()!!
                    }

                    var author = User("dummy string", "dummy author", "dummy phone", "dummy mail")
                    /*if(auth.currentUser!=null) {
                        var userId = auth.currentUser!!.uid
                        var displayName =auth.currentUser!!.displayName
                        var userPhone = auth.currentUser!!.phoneNumber
                        var email  = auth.currentUser!!.email
                        author = User(userId,displayName!!,userPhone!!,email!!)
                    }*/
                  //  val author = User(auth.currentUser!!.uid,auth.currentUser!!.displayName,auth.currentUser!!.phoneNumber,"")

                    var expenseIdList = ""
                    var historyIdList = ""
                    var transactionIdList = ""
                    if(selectedUserList.size<2){
                        Toast.makeText(context, "Please select atleast 2 participants", Toast.LENGTH_SHORT)

                    }else {

                        val activity: Activities = Activities(UUID.randomUUID().toString(), title?.text.toString(), description?.text.toString(), selectedTagList, 1, selectedUserList, myUser, false, System.currentTimeMillis(), expenseIdList, historyIdList, transactionIdList, System.currentTimeMillis().toString())
                        mListener.saveActivity(activity)
                    }
                }else{
                    Toast.makeText(context, "Error for user", Toast.LENGTH_SHORT).show()
                }
            }
        }

        addTagText.setOnClickListener { v: View? ->

            var intent = Intent(context, TagActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_TAG_SELECTION)
        }

        addMembersParent.setOnClickListener{  v:View? ->
            progressBarContacts.visibility = View.VISIBLE
            mListener.setupPermissions()
        }

        cancelButton.setOnClickListener{ v:View? ->

           mListener.cancelAddActivityFragment()
        }


        return view;
    }



    public fun onContactPermissionGranted(){

        addActivityPresenter.getAllContactsList()
    }

    private fun checkifInputValid(): Boolean {
        var valid: Boolean = true
        if (title.text.isEmpty()) {
            Toast.makeText(context, "Enter a valid title", Toast.LENGTH_SHORT)
            valid = false
        } else if (description.text.isEmpty()) {
            Toast.makeText(context, "Enter a description", Toast.LENGTH_SHORT)
            valid = false
        }

        return valid

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is IAddActivityFragmentListener) {
            mListener = context as IAddActivityFragmentListener
        } else {
            throw Exception("Activity must implement IAddActivityFragmentlistener")
        }
    }


    override fun onResume() {
        super.onResume()
        if(selectedUserList.size<2){
            addMembersParent.visibility = View.VISIBLE
        }else{
            addMembersParent.visibility = View.GONE
        }
        //populate my number in members listview
        if(auth.currentUser!!.phoneNumber!=null) {
            myContactName.text = "You"
            myContactPhone.text = ApplicationController.preferenceManager!!.myCredential
        }else{
            //go to sign in page
        }

    }

    interface IAddActivityFragmentListener {
        fun saveActivity(activity: Activities)
        fun getActivity(id: String): Activities
        fun showAddContactsPage()
        fun hideAddContactsPage()
        fun setupPermissions()
        fun setCurrentActivityUserList(userList:ArrayList<User>)
        fun getCurrentActivityUserList():ArrayList<User>
        fun cancelAddActivityFragment()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_TAG_SELECTION && data != null) {
             selectedTagList= data.getParcelableArrayListExtra("Tag")
            if (!selectedTagList.isEmpty()) {
                addTagText.visibility = View.INVISIBLE
                var tagArray = ArrayList<String>()

                for (i in 0 until selectedTagList.size) {
                    tagArray.add(selectedTagList[i].title)
                }

                tagContainer.setTags(tagArray)
            }
        }
    }


    override fun onContactsfetched(contactList: List<ContactMinimal>) {
        progressBarContacts.visibility = View.GONE
        this.contactsList = contactList
      /*  Log.d("contacts fetched","now")
        val bundle = Bundle()
        val gson = Gson()
        val type = object : TypeToken<List<ContactMinimal>>() {}.type
        val listString = gson.toJson(contactsList, type)
        bundle.putString("list",listString)*/
        mListener.showAddContactsPage()
    }

    fun setSelectedContacts(contactList: List<ContactMinimal>){

        if(!contactList.isEmpty()){

            memberListParent.removeAllViews()
            selectedUserList.clear()
            //add my contact
           /* var myContact = ContactMinimal("You",auth.currentUser!!.phoneNumber!!)
            selectedUserList.add(User((System.currentTimeMillis()*(1 until 10).random()).toString(),auth.currentUser!!.displayName!!,ApplicationController.preferenceManager!!.myCredential,"sample@yopmail.com"))*/
          //  memberListParent.addView(getContactView(myContact))

            for(contact in contactList){
                selectedUserList.add(User(contact.contact_id,contact.name,contact.phoneNumber,"sample@yopmail.com"))
                memberListParent.addView(getContactView(contact))
            }

            addMembersParent.visibility = View.GONE

        }


    }


    fun getContactView(contact: ContactMinimal):View{
        var inflater:LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view:View = inflater?.inflate(R.layout.contact_list_item_layout,null,false)
        var name:TextView = view.findViewById<TextView>(R.id.contact_name) as TextView
        var number:TextView = view.findViewById<TextView>(R.id.contact_number) as TextView

        if(contact.phoneNumber==myCredential){
            name.text = "You"
        }else {
            name.text = contact.name
        }
        number.text = contact.phoneNumber
        return  view
    }

    fun ClosedRange<Int>.random() =
            Random().nextInt((endInclusive + 1) - start) +  start

}
