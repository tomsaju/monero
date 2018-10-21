package com.monero.payeeSelector

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.monero.R
import com.monero.models.User
import com.monero.payeeSelector.fragment.SelectPayerFragment

/**
 * Created by tom.saju on 7/20/2018.
 */
class UserListAdapter : BaseAdapter {

    var userList:List<User>;
    var context: Context?=null;
    var mListener:SelectPayerFragment.SelectPayerFragmentInteractionListener?;
    var payerList:HashMap<User,Int> = HashMap()
    constructor(context: Context, userList: List<User>, mListener: SelectPayerFragment.SelectPayerFragmentInteractionListener?){
        this.context = context
        this.userList = userList
        this.mListener = mListener
    }

    override fun getView(position: Int, parent: View?, rootView: ViewGroup?): View {
        var inflater: LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view: View = inflater?.inflate(R.layout.contact_list_item_select_layout,rootView,false)
        var layoutParent: RelativeLayout = view.findViewById<RelativeLayout>(R.id.contact_item_parent) as RelativeLayout
        var name: TextView = view.findViewById<TextView>(R.id.contact_name) as TextView
        var number: TextView = view.findViewById<TextView>(R.id.contact_number) as TextView
        var selectionIndicator:ImageView = view.findViewById(R.id.selection_indicator)
        selectionIndicator.visibility = View.INVISIBLE
        name.text = userList.get(position)?.user_name
        number.text = userList.get(position)?.user_phone

        layoutParent.setOnClickListener { view: View ->
            if(payerList.containsKey(userList[position])){
                payerList.remove(userList[position])
                selectionIndicator.visibility  = View.INVISIBLE
            }else{
                payerList.put(userList[position],0)
                selectionIndicator.visibility  = View.VISIBLE
            }
          //  mListener?.onUserSelected(userList[position])
        }
        return view

    }

    override fun getItem(p0: Int): Any {
        return userList.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return userList.size
    }
}