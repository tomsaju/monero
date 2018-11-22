package com.monero.addActivities


import android.content.Context
import android.content.Intent
import android.net.Uri
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
import kotlin.collections.ArrayList
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.io.File


/**
 * A simple [Fragment] subclass.
 */
public class AddActivityFragment : Fragment(),IAddActivityView {

    lateinit var title: AutoCompleteTextView;
    lateinit var description: AutoCompleteTextView;
    lateinit var modeSelector: Spinner
    lateinit var addTagButton: ImageButton
    lateinit var addMembersButton: ImageButton
    lateinit var tagContainer: TagGroup
    lateinit var memberListParent: LinearLayout
    lateinit var addTagText: TextView
    lateinit var addMemberBanner: TextView
    lateinit var doneButton: Button
    lateinit var cancelButton: Button
    lateinit var selectedTagList: ArrayList<Tag>
    lateinit var mListener: IAddActivityFragmentListener
    lateinit var addMembersParent: LinearLayout
    lateinit var addActivityPresenter:IAddActivityPresenter
    lateinit var contactsList:List<ContactMinimal>
    lateinit var myPhone:String
    lateinit var myEmail:String
    lateinit var myContactName:TextView
    //lateinit var myContactPhone:TextView
    lateinit var addMembersLayout:FrameLayout
    lateinit var progressBarContacts:ProgressBar
    lateinit var myUser:User
    lateinit var auth:FirebaseAuth
    lateinit var myProfile:ImageView
    private val MODE_PRIVATE = 1
    private val MODE_PUBLIC = 2
    private var expenseIdList = ""
    private var historyIdList = ""
    private var transactionIdList = ""
    private var SELECTED_MODE = 0
    private val createdDate: Long = System.currentTimeMillis()
    var REQUEST_CODE_TAG_SELECTION = 1
    var selectedUserList: ArrayList<User> = ArrayList()
    private lateinit var currentActivityId: String

    private lateinit var options: RequestOptions

    private lateinit var path: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()!!
        options = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)

    }
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
//        myContactPhone = view.findViewById(R.id.contact_number)
        addMembersLayout = view.findViewById(R.id.add_member_layout)
        progressBarContacts = view.findViewById(R.id.contactLoadingProgressBar)

        myProfile = view.findViewById(R.id.profileImage)

        modeSelector.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                    if(position==0){
                        SELECTED_MODE = 0
                    }else if(position==1){
                        SELECTED_MODE = MODE_PRIVATE
                    }else if(position==2){
                        SELECTED_MODE = MODE_PUBLIC
                    }
                }

                override fun onNothingSelected(parentView: AdapterView<*>) {
                    // your code here
                }

            }

        myPhone = ApplicationController.preferenceManager!!.myPhone
        myEmail = ApplicationController.preferenceManager!!.myEmail
        addActivityPresenter = AddActivityPresenter(requireContext(),this)
        progressBarContacts.visibility = View.GONE

        selectedUserList = ArrayList(emptyList<User>())
        selectedTagList = ArrayList(emptyList<Tag>())
        if(auth.currentUser!=null){
            var authorName = auth.currentUser?.displayName
            var authorUid = auth.currentUser?.uid
            var authorEmail = auth.currentUser?.email
            var authorPhone = ""
            if(auth.currentUser?.phoneNumber!=null){
                authorPhone  = auth.currentUser?.phoneNumber!!
            }

            myUser = User(authorUid!!,authorName!!,authorPhone,authorEmail!!)

        }


        selectedUserList.add(myUser)
        mListener.setCurrentActivityUserList(selectedUserList)

        doneButton.setOnClickListener { v: View? ->

            if (checkifInputValid()) {


                auth = FirebaseAuth.getInstance()!!

                val activity = Activities(currentActivityId, title?.text.toString(), description?.text.toString(), selectedTagList, SELECTED_MODE, selectedUserList, myUser, false, createdDate, expenseIdList, historyIdList, transactionIdList, System.currentTimeMillis().toString())
                mListener.saveActivity(activity)

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


        if(arguments!=null){
            if(arguments?.getString("activity_id")!=null&&arguments?.getString("activity_id")!=""){
                currentActivityId = arguments?.getString("activity_id").toString()
                loadActivityDetails(currentActivityId)
            }else{
                currentActivityId=UUID.randomUUID().toString()
            }
        }else{
            currentActivityId=UUID.randomUUID().toString()
        }


        return view;
    }

    private fun loadActivityDetails(currentActivityId: String) {
        addActivityPresenter.getActivityForId(currentActivityId)
    }


    public fun onContactPermissionGranted(){

        addActivityPresenter.getAllContactsList()
    }

    private fun checkifInputValid(): Boolean {
        var valid: Boolean = true
        if (title.text.isEmpty()) {
            Toast.makeText(context, "Enter a valid title", Toast.LENGTH_SHORT).show()
            valid = false
        } else if (description.text.isEmpty()) {
            Toast.makeText(context, "Enter a description", Toast.LENGTH_SHORT).show()
            valid = false
        } else if(modeSelector.selectedItemPosition==0){
            Toast.makeText(context, "Please select visibility", Toast.LENGTH_SHORT).show()
            valid = false
        } else if(selectedUserList.size<2){
            Toast.makeText(context, "Please select atleast 2 participants", Toast.LENGTH_SHORT).show()
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
            path = requireContext().getFilesDir().absolutePath + "/profile"

            try {

                file = File("$path/${myUser.user_id}.jpg")
                val uri = Uri.fromFile(file)
                Glide.with(requireContext()).load(uri).apply(options).into(myProfile)
            } catch (e: Exception) {
                e.printStackTrace()
                Glide.with(requireContext()).load(requireContext().resources.getDrawable(R.drawable.default_profile)).into(myProfile)
            }

            //myContactPhone.text = ApplicationController.preferenceManager!!.myPhone
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
            path = requireContext().getFilesDir().absolutePath + "/profile"
            addMembersParent.visibility = View.GONE
            memberListParent.removeAllViews()
            selectedUserList.clear()
            //add my contact
           /* var myContact = ContactMinimal("You",auth.currentUser!!.phoneNumber!!)
            selectedUserList.add(User((System.currentTimeMillis()*(1 until 10).random()).toString(),auth.currentUser!!.displayName!!,ApplicationController.preferenceManager!!.myPhone,"sample@yopmail.com"))*/
          //  memberListParent.addView(getContactView(myContact))

            for(contact in contactList){
                selectedUserList.add(User(contact.contact_id,contact.name,contact.phoneNumber,contact.email))
                memberListParent.addView(getContactView(contact))
            }

            addMembersParent.visibility = View.GONE

        }else{
            addMembersParent.visibility = View.VISIBLE
        }


    }

    private lateinit var file: File

    fun getContactView(contact: ContactMinimal):View{
        var inflater:LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view:View = inflater?.inflate(R.layout.contact_list_item_add_activity_fragment_layout,null,false)
        var name:TextView = view.findViewById<TextView>(R.id.contact_name) as TextView
        var image:ImageView = view.findViewById<ImageView>(R.id.profileImage) as ImageView



        if((contact.phoneNumber.isNotEmpty()&&contact.phoneNumber== myPhone)||(contact.email.isNotEmpty()&&contact.email==myEmail)){
            name.text = "You"
        }else {
            if(contact.name.isNotEmpty()) {
                name.text = contact.name
            }else if(contact.email.isNotEmpty()) {
                name.text = contact.email
            }else if(contact.phoneNumber.isNotEmpty()){
                name.text = contact.phoneNumber
            }
        }
        try {

             file = File("$path/${contact.contact_id}.jpg")
            val uri = Uri.fromFile(file)
            Glide.with(requireContext()).load(uri).apply(options).into(image)
        } catch (e: Exception) {
            e.printStackTrace()
            Glide.with(requireContext()).load(requireContext().resources.getDrawable(R.drawable.default_profile)).into(image)
        }


        return  view
    }


    override fun onActivityFetched(activity: Activities) {
        //set title and description
        title.setText(activity.title)
        description.setText(activity.description)

        //set mode spinner
        if(activity.mode==0){
            SELECTED_MODE = 0
            modeSelector.setSelection(0)
        }else if(activity.mode==1){
            SELECTED_MODE = MODE_PRIVATE
            modeSelector.setSelection(1)
        }else if(activity.mode==2){
            SELECTED_MODE = MODE_PUBLIC
            modeSelector.setSelection(2)
        }

        //set tags
        if(activity.tags!=null&&!activity.tags.isEmpty()){
            addTagText.visibility = View.INVISIBLE
            selectedTagList = ArrayList(activity.tags)
            var tagArray = ArrayList<String>()
            for (i in 0 until selectedTagList.size) {
                tagArray.add(selectedTagList[i].title)
            }
            tagContainer.setTags(tagArray)

        }

        //set user list
        if(activity.members!=null&&activity.members.isNotEmpty()){
            var memberlist = ArrayList<ContactMinimal>()
            for(user in activity.members!!){
                memberlist?.add(ContactMinimal(user.user_id,user.user_name,user.user_phone,user.user_email))
            }

            setSelectedContacts(memberlist)
        }


    }

    override fun onActivityFetchError() {

    }

    fun ClosedRange<Int>.random() =
            Random().nextInt((endInclusive + 1) - start) +  start

}
