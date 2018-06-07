package com.monero.activitydetail.fragments.adapter

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.monero.R
import com.monero.models.History

/**
 * Created by tom.saju on 6/7/2018.
 */
class HistoryListAdapter(var historyList:ArrayList<History>,var context:Context): BaseAdapter() {



    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        val returnView:View
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


        if(historyList[position].type.equals("comment")){
            returnView = inflater.inflate(R.layout.history_list_item_comment,parent,false)
            var titleText:TextView =  returnView.findViewById<TextView>(R.id.title_text) as TextView
            var dateText:TextView = returnView.findViewById<TextView>(R.id.timeDateText) as TextView
            var commentText:TextView = returnView.findViewById<TextView>(R.id.textComment) as TextView

            titleText.text = historyList[position].user.name+" added a comment"
            dateText.text = historyList[position].time.toString()
            commentText.text = historyList[position].description


        }else{

            returnView = inflater.inflate(R.layout.history_list_item_image,parent,false)
            var titleText:TextView =  returnView.findViewById<TextView>(R.id.title_text) as TextView
            var dateText:TextView = returnView.findViewById<TextView>(R.id.timeDateText) as TextView
            var imageContent:ImageView = returnView.findViewById<ImageView>(R.id.imageContent) as ImageView

            titleText.text = historyList[position].user.name+" added an image"
            dateText.text = historyList[position].time.toString()
           // imageContent.setIma = historyList[position].description

        }

        return returnView
    }

    override fun getItem(p0: Int): Any {
      return historyList[p0]
    }

    override fun getItemId(p0: Int): Long {
       return 0
    }

    override fun getCount(): Int {
      return historyList.size
    }
}