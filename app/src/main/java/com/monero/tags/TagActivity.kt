package com.monero.tags

import android.arch.lifecycle.LiveData
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar

import com.monero.R
import android.view.Gravity
import com.monero.models.Tag


class TagActivity : AppCompatActivity(),ITagView {

    var toolbar:Toolbar?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag)
        toolbar = findViewById<Toolbar>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

    }


    override fun setTags(allTagList: LiveData<List<Tag>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
