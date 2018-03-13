package com.monero.addActivities

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import com.monero.Application.ApplicationController
import com.monero.R
import com.monero.models.Activities
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_add.*

class AddActivity : AppCompatActivity() {
    var toolbar:Toolbar?=null
    var title:EditText?=null
    var description:EditText?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_add)
        toolbar = findViewById<Toolbar>(R.id.toolbar) as Toolbar
        title = findViewById<EditText>(R.id.title_input) as EditText
        description = findViewById<EditText>(R.id.description_input) as EditText
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)

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
            val activity: Activities = Activities(System.currentTimeMillis(), title?.text.toString(), description?.text.toString())
            Single.fromCallable {
                ApplicationController.db?.activitesDao().insertIntoActivitiesTable(activity) // .database?.personDao()?.insert(person)
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe()
            finish()
        }else{
            Toast.makeText(baseContext,"Please enter title",Toast.LENGTH_SHORT)
        }
    }

}
