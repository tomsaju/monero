package com.monero.activitydetail.fragments.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.monero.R
import com.monero.R.id.view
import com.monero.expensedetail.ExpenseActivity
import com.monero.models.Expense
import com.monero.utility.Utility

/**
 * Created by tom.saju on 10/10/2018.
 */
class ExpenseListRecyclerAdapter(var expenseList:ArrayList<Expense>, var context: Context):RecyclerView.Adapter<ViewHolder>(),View.OnCreateContextMenuListener {
    private var position: Int = 0

    override fun getItemCount(): Int {
       return expenseList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.expense_list_item_layout, parent, false))
    }



    fun getPosition(): Int {
        return position
    }

    fun setPosition(position: Int) {
        this.position = position
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
        holder.tvamountTv.text = Utility.getCurrencySymbol()+amountInHigherDenomination

        holder.parentLayout.setOnClickListener{
            var intent: Intent = Intent(context,ExpenseActivity::class.java)
            intent.putExtra("expenseid",expenseList[position].id)
            context.startActivity(intent)
        }

        holder.parentLayout.setOnLongClickListener {

            setPosition(holder.adapterPosition)
            false
        }

    }


    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, p2: ContextMenu.ContextMenuInfo?) {

        menu?.add(Menu.NONE, R.id.edit_actv, Menu.NONE, "Edit Expense")
        menu?.add(Menu.NONE, R.id.delete_actv, Menu.NONE, "Delete Expense")

    }
}



class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {

    // Holds the TextView that will add each animal to
    var parentLayout = view.findViewById<CardView>(R.id.expense_list_item_parent)
    var tvtitle = view.findViewById<TextView>(R.id.expense_title_list_item)
    var tvpaidBy = view.findViewById<TextView>(R.id.paidBy_text_expense_list_item)
    var tvamountTv = view.findViewById<TextView>(R.id.amount_textview_expense_list_item)


}

