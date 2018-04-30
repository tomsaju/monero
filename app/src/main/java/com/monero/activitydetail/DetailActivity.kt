package com.monero.activitydetail

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import com.monero.R

class DetailActivity : AppCompatActivity() {
var toolbar:Toolbar?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        toolbar = findViewById<Toolbar>(R.id.my_toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }
}
