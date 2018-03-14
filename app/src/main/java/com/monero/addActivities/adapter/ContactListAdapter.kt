package com.monero.addActivities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.monero.R
import com.monero.models.Contact

/**
 * Created by tom.saju on 3/14/2018.
 */
class ContactListAdapter:BaseAdapter {

    var contactList:List<Contact>;
    var context:Context?=null;


    constructor(context: Context,contactList: List<Contact>){
        this.context = context
        this.contactList = contactList

    }

    override fun getView(position: Int, parent: View?, rootView: ViewGroup?): View {
        var inflater:LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view:View = inflater?.inflate(R.layout.contact_list_item_layout,rootView,false)

        var name:TextView = view.findViewById<TextView>(R.id.contact_name) as TextView
        var number:TextView = view.findViewById<TextView>(R.id.contact_number) as TextView

        name.text = contactList.get(position)?.name
        number.text = contactList.get(position)?.phoneNumber

        return view

    }

    override fun getItem(p0: Int): Any {
        return contactList.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return contactList.size
    }
}