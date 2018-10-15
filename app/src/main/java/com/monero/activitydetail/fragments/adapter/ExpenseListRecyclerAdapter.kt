package com.monero.activitydetail.fragments.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.monero.R
import com.monero.models.Expense

/**
 * Created by tom.saju on 10/10/2018.
 */
class ExpenseListRecyclerAdapter(var expenseList:ArrayList<Expense>, var context: Context):RecyclerView.Adapter<ViewHolder>() {


    override fun getItemCount(): Int {
       return expenseList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.expense_list_item_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvtitle.text=expenseList[position].title

        var sortedDebitList = expenseList[position].debitList.sortedWith(compareBy({ it.userName }))

        if(expenseList[position].debitList.size==1){
            holder.tvpaidBy.text = "paid by "+sortedDebitList[0].userName+""
        }else if(expenseList[position].debitList.size==2){
            holder.tvpaidBy.text = "paid by "+sortedDebitList[0].userName+" and 1 other"
        }else{
            holder.tvpaidBy.text = "paid by "+sortedDebitList[0].userName+" and some others"
        }

        //holder.tvpaidBy.text = "paid by "+expenseList[position].debitList.size+" people"

        var amountInLowerDenomination = expenseList[position].amount
        var amountInHigherDenomination = "%.2f".format((amountInLowerDenomination/100).toDouble())
        holder.tvamountTv.text = amountInHigherDenomination
    }


}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    var tvtitle = view.findViewById<TextView>(R.id.expense_title_list_item)
    var tvpaidBy = view.findViewById<TextView>(R.id.paidBy_text_expense_list_item)
    var tvamountTv = view.findViewById<TextView>(R.id.amount_textview_expense_list_item)
}

