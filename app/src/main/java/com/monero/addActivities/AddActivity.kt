package com.monero.addActivities

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.monero.Application.ApplicationController
import com.monero.R
import com.monero.addActivities.fragments.SelectContactsFragment
import com.monero.helper.AppDatabase
import com.monero.models.Activities
import com.monero.models.Contact
import com.monero.models.Tag
import com.pchmn.materialchips.ChipView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_add.*
import me.gujun.android.taggroup.TagGroup

class AddActivity : AppCompatActivity(),IAddActivityView,SelectContactsFragment.OnCotactSelectedListener {
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
        if(title?.text.toString().isNotEmpty()) {
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
        }
    }

    override fun onContactsfetched(contactsList:List<Contact>){
        Log.d("contacts fetched","now")
        val bundle = Bundle()
        val gson = Gson()
        val type = object : TypeToken<List<Contact>>() {}.type
        val listString = gson.toJson(contactsList, type)

        bundle.putString("list",listString)
        selectContactsFragment= SelectContactsFragment()
        selectContactsFragment.arguments = bundle
        selectContactsFragment.show(fragmentManager,"selectContacts")

    }

    override fun onContactSelected(contactList: MutableList<Contact>?) {
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
}
