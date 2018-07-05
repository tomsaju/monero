package com.monero.tags

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*

import com.monero.R
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import com.monero.models.Tag
import com.monero.tags.adapter.TagListAdapter


class TagActivity : AppCompatActivity(),ITagView {
    var REQUEST_CODE_TAG_SELECTION = 1
    var toolbar:Toolbar?=null
    var addTagEdittext:EditText? =null
    var closeButtonAddTag:ImageView?=null
    var doneButtonAddTag:ImageView?=null
    var mTagPresenter:ITagPresenter?=null
    var tagListAdapter:TagListAdapter?=null
    var tagListView:ListView?=null
    var listToLoad:ArrayList<Tag> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag)
        toolbar = findViewById<Toolbar>(R.id.toolbar) as Toolbar
        addTagEdittext = findViewById<EditText>(R.id.add_tag_edittext) as EditText
        closeButtonAddTag = findViewById<ImageView>(R.id.close_button_add_tag) as ImageView
        doneButtonAddTag = findViewById<ImageView>(R.id.done_button_add_tag) as ImageView
        tagListView = findViewById<ListView>(R.id.tagList) as ListView

        mTagPresenter = TagPresenter(baseContext,this)


        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)




        addTagEdittext?.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(view: View?, focused: Boolean) {
               if(focused){
                   doneButtonAddTag?.visibility = View.VISIBLE
                   closeButtonAddTag?.visibility = View.VISIBLE
               }else{
                   doneButtonAddTag?.visibility = View.GONE
                   closeButtonAddTag?.visibility = View.GONE
               }
            }
        }

        doneButtonAddTag?.setOnClickListener(View.OnClickListener {
            _: View? ->
               var tag:Tag = Tag(System.currentTimeMillis(),addTagEdittext?.text.toString())
                mTagPresenter?.saveTag(tag)
                addTagEdittext?.setText("")
        })

        closeButtonAddTag?.setOnClickListener(View.OnClickListener {
            _: View? ->
            addTagEdittext?.setText("")
        })


    }

    override fun onResume() {
        super.onResume()
        mTagPresenter?.getAllTags()
    }

    override fun setTags(allTagList: LiveData<List<Tag>>) {

      allTagList.observe(this, object :Observer<List<Tag>>{
          override fun onChanged(tagList: List<Tag>?) {


                  listToLoad?.clear()
                  listToLoad?.addAll(ArrayList(tagList))
              if(tagListAdapter==null) {
                  tagListAdapter = TagListAdapter(baseContext, listToLoad)
                  tagListView?.adapter = tagListAdapter
              }else{
                  tagListAdapter?.notifyDataSetChanged()
              }

          }

      })




    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.add_tag_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.done_tags -> {
                var selectedTags = tagListAdapter?.getSelectedList()
                intent.putParcelableArrayListExtra("Tag",selectedTags)
                setResult(REQUEST_CODE_TAG_SELECTION,intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
