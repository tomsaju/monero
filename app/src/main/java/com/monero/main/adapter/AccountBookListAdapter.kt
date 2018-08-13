package com.monero.main.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.monero.R
import com.monero.models.PendingTransaction
import kotlinx.android.synthetic.main.pendingtransaction_list_item.view.*

/**
 * Created by Dreamz on 12-08-2018.
 */
class AccountBookListAdapter(val pendingtransactions:ArrayList<PendingTransaction>,val context: Context)
    :RecyclerView.Adapter<AccountBookListAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var text =pendingtransactions[position].payer.user_name+" must pay "+pendingtransactions[position].amount+" to "+pendingtransactions[position].reciepient.user_name
        holder?.transaction_tv?.text = text
        }



    override fun getItemCount(): Int {
        return pendingtransactions.size
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountBookListAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.pendingtransaction_list_item,parent,false))
          }

    class ViewHolder(view:View) :RecyclerView.ViewHolder(view){
        val transaction_tv = view.pendingText
    }


}