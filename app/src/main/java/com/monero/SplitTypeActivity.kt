package com.monero

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_split_type.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener



class SplitTypeActivity : AppCompatActivity() {

    var SPLIT_TYPE_PERCENTAGE = 1
    var SPLIT_TYPE_MONEY = 2
    var SPLIT_TYPE = SPLIT_TYPE_PERCENTAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_type)

        if(SPLIT_TYPE==SPLIT_TYPE_PERCENTAGE){
            type_select_btn_percentage.isEnabled = true
            type_select_btn_money.isEnabled = false
        }else{
            type_select_btn_percentage.isEnabled = false
            type_select_btn_money.isEnabled = true
        }


    }
}
