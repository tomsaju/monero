package com.monero.addActivities.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.monero.R
import com.monero.models.ContactGroup

/**
 * Created by tom.saju on 11/16/2018.
 */
class GroupsExpandableAdapter:BaseExpandableListAdapter,Filterable {

    var contactList:List<ContactGroup>;
    var context:Context?=null;
    var mListener:IContactSelectedListener
    lateinit var orig: List<ContactGroup>
    lateinit var selectedgroupContacts:ArrayList<ContactGroup>

    constructor(context: Context, contactList: List<ContactGroup>, parent: IContactSelectedListener){

        this.context = context
        this.contactList = contactList
        this.mListener = parent
        orig = contactList
        selectedgroupContacts = ArrayList()
    }


    override fun getGroup(p0: Int): Any {
      return contactList[p0]
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
       return true
    }

    override fun hasStableIds(): Boolean {
     return  false
    }

    override fun getGroupView(listPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {

        var inflater: LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var title = contactList[listPosition].Group_name
        var  view = inflater.inflate(R.layout.groupcontact_list_item_parent,parent,false)

        var titleTv:TextView = view.findViewById(R.id.group_title)
        var selectButton: Button = view.findViewById(R.id.group_add_btn)

        titleTv.text = title
        selectButton.setOnClickListener {
            mListener.onContactSelected(ArrayList(contactList[listPosition].Group_items))
        }

        return view
    }

    override fun getChildrenCount(p0: Int): Int {
      return contactList[p0].Group_items.size
    }

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
      return contactList[listPosition].Group_items[expandedListPosition]
    }

    override fun getGroupId(p0: Int): Long {
      return contactList[p0].Group_id
    }

    override fun getChildView(listPosition: Int, expandedListPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var inflater: LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view = inflater?.inflate(R.layout.contact_list_item_phone_layout, parent, false)
        var layoutParent: RelativeLayout = view.findViewById<RelativeLayout>(R.id.contact_item_parent) as RelativeLayout
        var name: TextView = view.findViewById<TextView>(R.id.contact_name) as TextView
        var number: TextView = view.findViewById<TextView>(R.id.contact_number) as TextView
        var check: CheckBox = view.findViewById<CheckBox>(R.id.check_status_box) as CheckBox


        name.text = contactList.get(listPosition)?.Group_items[expandedListPosition].name
        if(!contactList.get(listPosition)?.Group_items[expandedListPosition].phoneNumber.isNullOrEmpty()){
            number.text = contactList.get(listPosition)?.Group_items[expandedListPosition]?.phoneNumber
        }else if(!contactList.get(listPosition)?.Group_items[expandedListPosition].email.isNullOrEmpty()){
            number.text = contactList.get(listPosition)?.Group_items[expandedListPosition]?.email
        }

        check.visibility = View.GONE
        layoutParent.setOnClickListener { view: View ->

        }

        return view
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return 0
    }

    override fun getGroupCount(): Int {
       return contactList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val oReturn = FilterResults()
                val results = ArrayList<ContactGroup>()

                if (orig == null) {
                    orig = contactList
                }
                synchronized (oReturn) {
                    if (constraint != null&&constraint.isNotEmpty()) {
                        if (orig != null && orig.isNotEmpty()) {
                            for (g in orig) {
                                if (g.Group_name.toLowerCase().contains(constraint.toString()))
                                    results.add(g)
                            }
                        }
                        oReturn.values = results
                        oReturn.count = results.size
                    }else{
                        Log.d("danger","reached no items")
                        oReturn.values = ArrayList<ContactGroup>()
                        oReturn.count = 0
                    }
                    return oReturn
                }

            }

            override fun publishResults(constraint: CharSequence,
                                        results: FilterResults) {
                if(results!=null&&results.count>0) {
                    contactList = results!!.values as ArrayList<ContactGroup>
                    notifyDataSetChanged()
                }else{
                    //contactList = emptyList()
                    notifyDataSetInvalidated()
                }
            }
        }
    }

}