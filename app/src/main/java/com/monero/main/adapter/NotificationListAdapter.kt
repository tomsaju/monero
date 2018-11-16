package com.monero.main.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.monero.R
import com.monero.models.Expense
import com.monero.models.NotificationItem
import java.util.*

/**
 * Created by tom.saju on 11/16/2018.
 */
class NotificationListAdapter(var notificationList:ArrayList<NotificationItem>, var context: Context):RecyclerView.Adapter<NotiViewHolder>() {



    override fun onBindViewHolder(holder: NotiViewHolder, position: Int) {
      holder.text.text = notificationList[position].message

        var timestamp =  notificationList[position].notificationId // bare with me
        var date = Date(timestamp)

        val niceDateStr = DateUtils.getRelativeTimeSpanString(date.time, Calendar.getInstance().timeInMillis, DateUtils.MINUTE_IN_MILLIS)

        holder.time.text = niceDateStr
    }

    override fun getItemCount(): Int {
       return notificationList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotiViewHolder {
        return NotiViewHolder(LayoutInflater.from(context).inflate(R.layout.notifictaion_list_item, parent, false))
    }


}


class NotiViewHolder(view:View):RecyclerView.ViewHolder(view){

    var parentLyaout = view.findViewById<RelativeLayout>(R.id.notification_item_parent)
    var text = view.findViewById<TextView>(R.id.notification_text)
    var time = view.findViewById<TextView>(R.id.notifcation_time)
}