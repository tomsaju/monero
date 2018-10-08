package com.monero.activitydetail.fragments.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.monero.R
import com.monero.models.Expense

/**
 * Created by tom.saju on 7/24/2018.
 */
class ExpenseListAdapter(var expenseList:ArrayList<Expense>,var context: Context): BaseAdapter() {




    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val returnView:View
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        returnView = inflater.inflate(R.layout.expense_list_item_layout,parent,false)
        var title = returnView.findViewById<TextView>(R.id.expense_title_list_item)
        var paidBy = returnView.findViewById<TextView>(R.id.paidBy_text_expense_list_item)
        var amountTv = returnView.findViewById<TextView>(R.id.amount_textview_expense_list_item)

        title.text=expenseList[position].title
        paidBy.text = "paid by "+expenseList[position].debitList.size+" people"

        var amountInLowerDenomination = expenseList[position].amount
        var amountInHigherDenomination = "%.2f".format((amountInLowerDenomination/100).toDouble())
        amountTv.text = amountInHigherDenomination

        return returnView

    }

    override fun getItem(position: Int): Any {
      return expenseList[position]
    }

    override fun getItemId(position: Int): Long {
       return 0
    }

    override fun getCount(): Int {
      return expenseList.size
    }
}