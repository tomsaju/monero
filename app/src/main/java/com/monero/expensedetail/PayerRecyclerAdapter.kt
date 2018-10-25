package com.monero.expensedetail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.monero.R
import com.monero.models.Payment
import com.monero.utility.Utility

/**
 * Created by Dreamz on 16-10-2018.
 */
class PayerRecyclerAdapter(var payerList:ArrayList<Payment>):RecyclerView.Adapter<payerRecylerViewHolder>() {

    override fun getItemCount(): Int {
        return payerList.size
    }

    override fun onBindViewHolder(holder: payerRecylerViewHolder, position: Int) {
        holder.name.text = payerList[position].Paiduser.user_name

        var amountInLowerDenomination = payerList[position].amount

        var amountInHigherDenomination = "%.2f".format((amountInLowerDenomination/100).toDouble())

        holder.amount.text =  Utility.getCurrencySymbol()+amountInHigherDenomination
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): payerRecylerViewHolder {
            return  payerRecylerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.expense_detail_payment_list,parent,false))
    }
}
class payerRecylerViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    var name = view.findViewById<TextView>(R.id.payer_name)
    var amount = view.findViewById<TextView>(R.id.paid_amount);
}