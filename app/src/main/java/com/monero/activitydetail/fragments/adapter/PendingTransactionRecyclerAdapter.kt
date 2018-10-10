package com.monero.activitydetail.fragments.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.monero.R
import com.monero.models.PendingTransaction

/**
 * Created by Dreamz on 10-10-2018.
 */
class PendingTransactionRecyclerAdapter(var pendingTransactions:ArrayList<PendingTransaction>, var context: Context):RecyclerView.Adapter<PendingTransactionViewHolder>() {



    override fun getItemCount(): Int {
      return  pendingTransactions.size
    }

    override fun onBindViewHolder(holder: PendingTransactionViewHolder, position: Int) {
        var amountInLowerDenomination = pendingTransactions[position].amount
        var amountInHigherDenomination = "%.2f".format((amountInLowerDenomination/100).toDouble())



        var text ="<b>"+ pendingTransactions[position].payer.user_name+"</b>"+" must pay "+ "<font color=\"#08511f\"><b>"+amountInHigherDenomination+"</b>"+"</font>"+" to "+ "<b>"+pendingTransactions[position].reciepient.user_name+"</b>"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.title.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.title.setText(Html.fromHtml(text));
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingTransactionViewHolder {
        return PendingTransactionViewHolder(LayoutInflater.from(context).inflate(R.layout.pendingtransaction_list_item, parent, false))
    }

}
class PendingTransactionViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    var title = view.findViewById<TextView>(R.id.pendingText)
}