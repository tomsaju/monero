package com.monero.main.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.monero.R
import com.monero.main.adapter.ActivityListAdapter
import com.monero.models.Activities


/**
 * Created by tom.saju on 3/6/2018.
 */
class ActivityFragment: Fragment() {
    var mActivityFragmentListener:ActivityFragmentListener?=null
    var activitiesList:ListView?=null
    var adapter:ActivityListAdapter?=null
    companion object {
        fun newInstance():ActivityFragment{
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
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater?.inflate(R.layout.activitylist_fragment,container,false)
        activitiesList = rootView?.findViewById<ListView>(R.id.activity_listview) as ListView


        return rootView
    }

    fun onAllActivitiesFetched(activites: List<Activities>) {
        //pass result to fragment
        adapter = ActivityListAdapter(context,activites)
        activitiesList?.adapter=adapter
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(context is ActivityFragmentListener){
            mActivityFragmentListener =  context
        }


    }

   public interface ActivityFragmentListener{
       fun getAllActivitiesList()

    }
}