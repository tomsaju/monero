package com.monero.splittype

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import com.monero.R
import com.monero.models.SplitItem
import com.monero.models.User

/**
 * Created by tom.saju on 10/23/2018.
 */
class SplitTypeRecyclerAdapter(var splitList:ArrayList<SplitItem>,var context: Context,var splitType:Int) :RecyclerView.Adapter<ViewHolder>() {
    var SPLIT_TYPE_PERCENTAGE = 1
    var SPLIT_TYPE_MONEY = 2
    var SPLIT_TYPE_EQUALLY = 2


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.username.setText(splitList[position].user.user_name)
        if(splitType==SPLIT_TYPE_MONEY) {
            var amountInHigherDenomination = "%.2f".format((splitList[position].amount/100).toDouble())
            holder.amount.text = "$"+amountInHigherDenomination
        }else if(splitType ==SPLIT_TYPE_PERCENTAGE){
            holder.amount.text =  "%.2f".format((100/splitList.size).toDouble())+"%"
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.split_type_list_item_layout,parent, false))
    }

    override fun getItemCount(): Int {
      return splitList.size
    }
}

class ViewHolder(view:View) :RecyclerView.ViewHolder(view){
    var username =  view.findViewById<TextView>(R.id.expense_title_list_item)
    var amount  = view.findViewById<TextView>(R.id.amount_textview_expense_list_item)



}