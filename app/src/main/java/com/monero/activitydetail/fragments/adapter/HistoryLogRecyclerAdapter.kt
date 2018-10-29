package com.monero.activitydetail.fragments.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.monero.Dao.DBContract
import com.monero.R
import com.monero.models.HistoryLogItem
import android.widget.RelativeLayout
import com.monero.R.id.view
import android.R.attr.y
import android.graphics.Point
import android.view.Display
import android.view.WindowManager



/**
 * Created by Dreamz on 12-10-2018.
 */
class HistoryLogRecyclerAdapter (var historyLogList:ArrayList<HistoryLogItem>, var context: Context): RecyclerView.Adapter<HistoryLogViewHolder>() {



    override fun getItemCount(): Int {
        return  historyLogList.size
    }

    override fun onBindViewHolder(holder: HistoryLogViewHolder, position: Int) {


        var action = "";
        if(historyLogList[position].Event_Type==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_EXPENSE){

            action= "added an expense"
        }else if(historyLogList[position].Event_Type==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_ACTIVITY){
            action= "added an activity"
        }else if(historyLogList[position].Event_Type==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_COMMENT){
            action= "added a comment"
        }else if(historyLogList[position].Event_Type==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_IMAGE){
            action= "added an image"
        }else if(historyLogList[position].Event_Type==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_USER){
            action= "added a user"
        }else if(historyLogList[position].Event_Type==DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_EDITTED_EXPENSE){
            action= "editted an expense"
        }

        var text = historyLogList[position].Author_Id +" "+action+" "+historyLogList[position].Subject_Name
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.title.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.title.setText(Html.fromHtml(text));
        }

        if(position==0){
            holder.timeLineTop.visibility = View.INVISIBLE
        }else{
            holder.timeLineTop.visibility = View.VISIBLE
        }

        if(position==historyLogList.size-1){
            holder.timeLineBottom.visibility = View.INVISIBLE
        }else{
            holder.timeLineBottom.visibility = View.VISIBLE
        }

       // holder.timeLine.setLayoutParams(params)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryLogViewHolder {
        return HistoryLogViewHolder(LayoutInflater.from(context).inflate(R.layout.test_layout, parent, false))
    }

}
class HistoryLogViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    var title = view.findViewById<TextView>(R.id.pendingText)
    var timeLineTop = view.findViewById<View>(R.id.topPortion_history_line)
    var timeLineBottom = view.findViewById<View>(R.id.bottomPortion_history_line)
}