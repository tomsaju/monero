package com.monero.addActivities.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.monero.R
import com.monero.addActivities.fragments.SelectContactsFragment
import com.monero.models.ContactMinimal

/**
 * Created by tom.saju on 3/14/2018.
 */
class ContactListAdapter:BaseAdapter, Filterable {

    var contactList:List<ContactMinimal>;
    var context:Context?=null;
    var parent:IContactSelectedListener
    lateinit var orig: List<ContactMinimal>


    constructor(context: Context, contactList: List<ContactMinimal>, parent: IContactSelectedListener){
        this.context = context
        this.contactList = contactList
        this.parent = parent
        orig = contactList
    }

    override fun getView(position: Int, parent: View?, rootView: ViewGroup?): View {
        var inflater:LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view: View
        if(contactList.get(position)?.name.isEmpty()){
            view = inflater?.inflate(R.layout.contact_list_add_new_item_layout, rootView, false)
            var layoutParent: RelativeLayout = view.findViewById<RelativeLayout>(R.id.contact_item_parent) as RelativeLayout

            var number: TextView = view.findViewById<TextView>(R.id.contact_email_number) as TextView
            if(contactList.get(position)?.phoneNumber.isNotEmpty()) {
                number.text = contactList.get(position)?.phoneNumber
            }else if(contactList.get(position)?.email.isNotEmpty()){
                number.text = contactList.get(position)?.email
            }

            layoutParent.setOnClickListener { view: View ->
                this.parent.onContactSelected(contactList[position])
            }
        }else {

            view = inflater?.inflate(R.layout.contact_list_item_layout, rootView, false)
            var layoutParent: RelativeLayout = view.findViewById<RelativeLayout>(R.id.contact_item_parent) as RelativeLayout
            var name: TextView = view.findViewById<TextView>(R.id.contact_name) as TextView
            var number: TextView = view.findViewById<TextView>(R.id.contact_number) as TextView

            name.text = contactList.get(position)?.name
            number.text = contactList.get(position)?.phoneNumber

            layoutParent.setOnClickListener { view: View ->
                this.parent.onContactSelected(contactList[position])
            }
        }
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


    fun setNewItem(contact:ContactMinimal){
       var contactArrayList = ArrayList<ContactMinimal>()
        contactArrayList.add(contact)
        contactList = contactArrayList
        notifyDataSetChanged()
    }

   override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val oReturn = FilterResults()
                val results = ArrayList<ContactMinimal>()

                if (orig == null) {
                    orig = contactList
                }
                synchronized (oReturn) {
                    if (constraint != null&&constraint.isNotEmpty()) {
                        if (orig != null && orig.isNotEmpty()) {
                            for (g in orig) {
                                if (g.name.toLowerCase().contains(constraint.toString())||g.phoneNumber.contains(constraint.toString()))
                                    results.add(g)
                            }
                        }
                        oReturn.values = results
                        oReturn.count = results.size
                    }else{
                    Log.d("danger","reached no items")
                        oReturn.values = ArrayList<ContactMinimal>()
                        oReturn.count = 0
                    }
                    return oReturn
                }

            }

            override fun publishResults(constraint: CharSequence,
                                         results: FilterResults) {
                if(results!=null&&results.count>0) {
                    contactList = results!!.values as ArrayList<ContactMinimal>
                    notifyDataSetChanged()
                }else{
                    //contactList = emptyList()
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}