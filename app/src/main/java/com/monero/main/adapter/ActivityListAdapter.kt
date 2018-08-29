package com.monero.main.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.monero.R
import com.monero.activitydetail.DetailActivity
import com.monero.models.Activities

/**
 * Created by tom.saju on 3/7/2018.
 */
class ActivityListAdapter : BaseAdapter {

    var context:Context
    var activitiesList:List<Activities>

    constructor(context: Context,activitiesList: List<Activities>){
        this.context=context
        this.activitiesList = activitiesList
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view:View?
        var inflater:LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.activities_list_item,parent,false)

        var title:TextView = view.findViewById(R.id.activites_title)
        var description:TextView = view.findViewById(R.id.activities_users_names)
        var parent:LinearLayout = view.findViewById(R.id.mainListItemParent)

        parent.setOnClickListener{_:View->
            var intent:Intent = Intent(context,DetailActivity::class.java)
            intent.putExtra("activityId",activitiesList[position].id)
            context.startActivity(intent)

        }

        title.text=activitiesList[position].title
        description.text = activitiesList[position].description

        return view
    }

    override fun getItem(p0: Int): Any {
       return activitiesList[p0]
    }

    override fun getItemId(p0: Int): Long {
       return 0
    }

    override fun getCount(): Int {
      return activitiesList.size
    }
}