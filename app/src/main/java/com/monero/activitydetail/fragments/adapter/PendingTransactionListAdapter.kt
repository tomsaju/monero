package com.monero.activitydetail.fragments.adapter

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.monero.R
import com.monero.models.PendingTransaction

/**
 * Created by Dreamz on 29-07-2018.
 */
class PendingTransactionListAdapter(var pendingTransactions:ArrayList<PendingTransaction>,var context: Context):BaseAdapter() {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val returnView:View
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        returnView = inflater.inflate(R.layout.pendingtransaction_list_item,parent,false)
        var title = returnView.findViewById<TextView>(R.id.pendingText)

        var amountInLowerDenomination = pendingTransactions[position].amount
        var amountInHigherDenomination = "%.2f".format((amountInLowerDenomination/100).toDouble())



        var text ="<b>"+ pendingTransactions[position].payer.user_name+"</b>"+" must pay "+ "<font color=\"#08511f\"><b>"+amountInHigherDenomination+"</b>"+"</font>"+" to "+ "<b>"+pendingTransactions[position].reciepient.user_name+"</b>"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            title.setText(Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY));
        } else {
            title.setText(Html.fromHtml(text));
        }
       // title.text = text
        return returnView
    }

    override fun getItem(position: Int): Any {
       return pendingTransactions[position]
    }

    override fun getItemId(position: Int): Long {
       return pendingTransactions[position].transaction_id
    }

    override fun getCount(): Int {
       return pendingTransactions.size
    }
}