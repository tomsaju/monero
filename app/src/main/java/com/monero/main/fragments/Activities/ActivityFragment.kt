package com.monero.main.fragments.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import com.monero.R
import com.monero.activitydetail.DetailActivity
import com.monero.main.adapter.ActivityListAdapter
import com.monero.models.Activities
import com.monero.models.Tag


/**
 * Created by tom.saju on 3/6/2018.
 */
class ActivityFragment: Fragment() {
    var mActivityFragmentListener: ActivityFragmentListener?=null
    var activitiesList:ListView?=null
    var adapter:ActivityListAdapter?=null
    companion object {
        fun newInstance(): ActivityFragment {
            var activityFragment = ActivityFragment()
            var args = Bundle()
            activityFragment.arguments = args;
            return activityFragment
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onResume() {
        super.onResume()
        mActivityFragmentListener?.getAllActivitiesList()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater?.inflate(R.layout.activitylist_fragment,container,false)
        activitiesList = rootView?.findViewById<ListView>(R.id.activity_listview) as ListView
        val addActivityButton: FloatingActionButton = rootView?.findViewById<FloatingActionButton>(R.id.add_activity_button) as FloatingActionButton
        addActivityButton.setOnClickListener { _:View->
            var taglist:MutableList<Tag> = mutableListOf<Tag>()

            mActivityFragmentListener?.addNewActivity()
        }
        return rootView
    }



    fun onAllActivitiesFetched(activities: List<Activities>?) {
        //pass result to fragment
        if(activities!=null) {
            adapter = ActivityListAdapter(requireContext(), activities)
            activitiesList?.adapter = adapter
        }

        activitiesList?.setOnItemClickListener { _, _, position, _ ->
            var intent:Intent = Intent(requireContext(), DetailActivity::class.java)
            var selection = activities?.get(position)
            intent.putExtra("activityId",selection?.id)
            requireContext()?.startActivity(intent)
        }

    }

    public fun refreshList(){
        adapter?.notifyDataSetChanged();
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(context is ActivityFragmentListener){
            mActivityFragmentListener =  context
        }else{

        }


    }

   public interface ActivityFragmentListener{
       fun getAllActivitiesList()
       fun addNewActivity()

    }
}