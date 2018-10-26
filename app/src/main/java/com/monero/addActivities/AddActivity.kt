package com.monero.addActivities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.monero.R
import com.monero.addActivities.fragments.SelectContactsFragment
import com.monero.models.*
import com.pchmn.materialchips.ChipView

import me.gujun.android.taggroup.TagGroup

class AddActivity : AppCompatActivity(),IAddActivityView,SelectContactsFragment.OnCotactSelectedListener {
    override fun syncContactsWithServer(contactList: ArrayList<Contact>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    var toolbar:Toolbar?=null
    var title:EditText?=null
    var description:EditText?=null
    var tagGroup:TagGroup?=null
    var tagSectionParent:FrameLayout?=null
    var addTagLabel:TextView?=null
    lateinit var tags:Array<String>
    lateinit var taglist:MutableList<Tag>
    var addMembers:TextView?=null
    var mAddActivityPresenter:IAddActivityPresenter?=null
    lateinit var selectContactsFragment: SelectContactsFragment
    var contactsSection: LinearLayout?=null
    var cancel:Button?=null
    var save:Button?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_add)

        toolbar = findViewById<Toolbar>(R.id.toolbar) as Toolbar
        title = findViewById<EditText>(R.id.title_input) as EditText
        description = findViewById<EditText>(R.id.description_input) as EditText
        tagGroup = findViewById<TagGroup>(R.id.tag_group) as TagGroup
        tagSectionParent = findViewById<FrameLayout>(R.id.tag_section_parent) as FrameLayout
        addTagLabel = findViewById<TextView>(R.id.add_tag_text) as TextView
        addMembers = findViewById<TextView>(R.id.add_members_text) as TextView
        contactsSection = findViewById<LinearLayout>(R.id.contacts_linearlayout) as LinearLayout
        save = findViewById<Button>(R.id.saveAddPage) as Button
        cancel = findViewById<Button>(R.id.cancelAddPage) as Button

        save?.setOnClickListener { _: View ->

            saveActivity();
        }
        addMembers?.setOnClickListener{_:View->

           mAddActivityPresenter?.getAllContactsList()
       }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mAddActivityPresenter = AddActivityPresenter(baseContext,this)
         tags =arrayOf("home", "unexpected", "travel","ooty","paradise","cold","strangers","more expensive")
         taglist = mutableListOf<Tag>()
         for (i in 1..5) {
            var tag: Tag = Tag(i.toLong(), tags[i])
            taglist.add(tag)
         }

        if(tags?.isEmpty()){
            addTagLabel?.visibility=View.VISIBLE
            tagGroup?.visibility = View.INVISIBLE
        }else{
            addTagLabel?.visibility=View.INVISIBLE
            tagGroup?.visibility = View.VISIBLE
            tagGroup?.setTags(tags.toMutableList())
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater:MenuInflater = menuInflater
        inflater.inflate(R.menu.add_activity_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.save_activity -> {
                saveActivity()
        }
        }

        return super.onOptionsItemSelected(item)

    }

    private fun saveActivity() {
        /*if(title?.text.toString().isNotEmpty()) {
            val activity: Activities = Activities(System.currentTimeMillis(), title?.text.toString(), description?.text.toString(),taglist )
            Single.fromCallable {
                AppDatabase.db?.activitesDao()?.insertIntoActivitiesTable(activity) // .database?.personDao()?.insert(person)
                for(tag in taglist){
                    AppDatabase.db?.tagDao()?.insertIntoTagTable(tag)
                }

            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe()
            finish()
        }else{
            Toast.makeText(baseContext,"Please enter title",Toast.LENGTH_SHORT)
        }*/
    }

    override fun onContactsfetched(contactsList:List<ContactMinimal>){

        selectContactsFragment= SelectContactsFragment()
        supportFragmentManager.beginTransaction().add(selectContactsFragment,"select_contacts").commit()

           }

    override fun onContactSelected(contactList: MutableList<ContactMinimal>?) {
        Log.d("contact","sele")


        contactList?.let {
            for(contact in contactList){
                val chip = ChipView(this)
                chip.label = contact.name
                chip.setDeletable(true)
                chip.setAvatarIcon(resources.getDrawable(R.drawable.avatar))
                chip.setPadding(5, 0, 5, 0)
                chip.setChipBackgroundColor(resources.getColor(android.R.color.holo_blue_light))

                contactsSection?.addView(chip)
                addMembers?.visibility = View.INVISIBLE
            }
        }

    }

    override fun getAllContactList() {

    }

    override fun closeContactSelectFragment() {
        (supportFragmentManager.findFragmentByTag("select_contacts") as SelectContactsFragment)
    }

    override fun setCurrentActivityUserList(userList: ArrayList<User>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCurrentActivityUserList(): ArrayList<User> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityFetched(activity: Activities) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityFetchError() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
