package com.monero.tags.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.monero.R
import com.monero.activitydetail.DetailActivity
import com.monero.models.Tag
import java.util.logging.Logger

/**
 * Created by tom.saju on 7/5/2018.
 */
class TagListAdapter:BaseAdapter {

    var context:Context
    var tagList:ArrayList<Tag>
    var selectedTagList:ArrayList<Tag> = ArrayList()

    constructor(context: Context,tagList: ArrayList<Tag>){
        this.context = context
        this.tagList = tagList
    }


    override fun getView(position: Int, p1: View?, parent: ViewGroup?): View {
        var view:View?
        var inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


        view = inflater.inflate(R.layout.tag_list_item_layout,parent,false)

        var title: TextView = view.findViewById(R.id.tag_name)
        var checkBox: CheckBox = view.findViewById(R.id.tag_checkbox)
        title.text = tagList[position].title


        checkBox.setOnCheckedChangeListener(null)

        if(selectedTagList.any{ Tag -> Tag.id == tagList[position].id }){
            checkBox.isChecked = true
        }

        checkBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener {
            compoundButton, isChecked ->
            val LOG = Logger.getLogger(TagListAdapter::class.java.name)
            if(isChecked){
               selectedTagList.add(tagList[position])

            }else{
                if(selectedTagList.any{ Tag -> Tag.id == tagList[position].id }){
                    selectedTagList.remove(tagList[position])
                }
            }

        })




        return view
    }

    override fun getItem(p0: Int): Any {
     return tagList[p0]
    }

    override fun getItemId(p0: Int): Long {
       return tagList[p0].id
    }

    override fun getCount(): Int {
      return tagList.size
    }

    public fun getSelectedList():ArrayList<Tag> {
        return selectedTagList
    }

}