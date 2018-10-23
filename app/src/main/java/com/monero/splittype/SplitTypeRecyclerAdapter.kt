package com.monero.splittype

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import com.monero.R
import com.monero.models.SplitItem
import com.monero.models.User

/**
 * Created by tom.saju on 10/23/2018.
 */
class SplitTypeRecyclerAdapter(var splitList:ArrayList<SplitItem>,var context: Context,var splitType:Int,var totalAmount:Int) :RecyclerView.Adapter<ViewHolder>() {
    var SPLIT_TYPE_PERCENTAGE = 1
    var SPLIT_TYPE_MONEY = 2
    var SPLIT_TYPE_EQUALLY = 2


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.username.setText(splitList[position].user.user_name)
        if(splitType==SPLIT_TYPE_EQUALLY) {
            var amounttospend = totalAmount/splitList.size
            var amountInHigherDenomination = "%.2f".format((amounttospend/100).toDouble())
            holder.amount.text = "$"+amountInHigherDenomination
            holder.delete.visibility ==View.INVISIBLE
        }else if(splitType==SPLIT_TYPE_MONEY) {
            var amountInHigherDenomination = "%.2f".format((splitList[position].amount/100).toDouble())
            holder.amount.text = "$"+amountInHigherDenomination
            holder.delete.visibility ==View.VISIBLE
        }else if(splitType ==SPLIT_TYPE_PERCENTAGE){
            holder.amount.text =  "%.2f".format((splitList[position].percentage))+"%"
            holder.delete.visibility ==View.VISIBLE
        }

        holder.delete.setOnClickListener {
            splitList.remove(splitList[position])
            notifyDataSetChanged()
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
    var delete = view.findViewById<ImageView>(R.id.remove_item)

}