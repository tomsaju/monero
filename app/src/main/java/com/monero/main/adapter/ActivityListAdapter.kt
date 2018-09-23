package com.monero.main.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.monero.R
import com.monero.activitydetail.DetailActivity
import com.monero.main.fragments.Activities.IOnSelectionModifiedListener
import com.monero.models.Activities
import kotlinx.android.synthetic.main.activities_list_item.view.*
import me.gujun.android.taggroup.TagGroup
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by tom.saju on 3/7/2018.
 */
class ActivityListAdapter : BaseAdapter {

    var context:Context
    var activitiesList:List<Activities>
    lateinit var selectedActivitieslist:ArrayList<String>
    private val listSelectedRows: ArrayList<View>?= ArrayList()
    constructor(context: Context,activitiesList: List<Activities>){
        this.context=context
        this.activitiesList = activitiesList
        this.selectedActivitieslist = ArrayList()

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view:View?
        var inflater:LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.activities_list_item,parent,false)

        var title:TextView = view.findViewById(R.id.activites_title)
        var description:TextView = view.findViewById(R.id.activities_users_names)
        var parent:LinearLayout = view.findViewById(R.id.mainListItemParent)
        val mTagGroup:TagGroup = view.findViewById(R.id.tag_group)
        var createdDate:TextView = view.findViewById(R.id.created_date)



        var tagsList = activitiesList[position].tags
        var stringListTag = ArrayList<String>()
        for(tag in tagsList){
            stringListTag.add(tag.title)
        }

        title.text=activitiesList[position].title
        description.text = activitiesList[position].description
        mTagGroup.setTags(stringListTag)


        val calendar = Calendar.getInstance()
        calendar.time = Date(activitiesList[position].createdDate)
        val day:String = calendar.get(Calendar.DAY_OF_MONTH).toString()
        val month = (calendar.get(Calendar.MONTH)+1).toString() //month index starts from 0
        val year = calendar.get(Calendar.YEAR).toString()

        var formattedDate: String = "$day/$month/$year";

        createdDate.setText(formattedDate)

        if(stringListTag.contains(activitiesList[position].id)){
            view.card_parent_activities_list_item.setCardBackgroundColor(context.resources.getColor(R.color.colorListSelector))
        }else{
            view.card_parent_activities_list_item.setCardBackgroundColor(context.resources.getColor(R.color.colorPrimary))
        }

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

    fun handleLongPress(position: Int, view: View) {
        if (listSelectedRows!!.contains(view)) {
            listSelectedRows!!.remove(view)
            selectedActivitieslist.remove(activitiesList[position].id)
            view.card_parent_activities_list_item.setCardBackgroundColor(context.resources.getColor(R.color.colorPrimary))

        } else {
            selectedActivitieslist.add(activitiesList[position].id)
            listSelectedRows.add(view)
            view.card_parent_activities_list_item.setCardBackgroundColor(context.resources.getColor(R.color.colorListSelector))

        }
    }
}