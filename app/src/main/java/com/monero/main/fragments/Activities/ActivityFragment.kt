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
import android.widget.ProgressBar
import com.monero.R
import com.monero.activitydetail.DetailActivity
import com.monero.main.adapter.ActivityListAdapter
import com.monero.models.Activities
import com.monero.models.Tag
import kotlinx.android.synthetic.main.activities_list_item.view.*


/**
 * Created by tom.saju on 3/6/2018.
 */
class ActivityFragment: Fragment() {
    var mActivityFragmentListener: ActivityFragmentListener?=null
    var activitiesList:ListView?=null
    var adapter:ActivityListAdapter?=null
    lateinit var progressBar: ProgressBar

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
        progressBar = rootView?.findViewById(R.id.progressBar)
        val addActivityButton: FloatingActionButton = rootView?.findViewById<FloatingActionButton>(R.id.add_activity_button) as FloatingActionButton
        addActivityButton.setOnClickListener { _:View->
            var taglist:MutableList<Tag> = mutableListOf<Tag>()

            mActivityFragmentListener?.addNewActivity()
        }
        return rootView
    }



    fun onAllActivitiesFetched(activities: List<Activities>?) {
        //  hideProgressBar()
        //pass result to fragment
        if (activities != null) {
            adapter = ActivityListAdapter(requireContext(), activities)
            activitiesList?.adapter = adapter
        }

        activitiesList?.setOnItemClickListener { _, view, position, _ ->
            val adapter = adapter
            if(adapter!=null&&adapter.selectedActivitieslist?.size>=1){
                adapter?.handleLongPress(position,view) //to add all clicked items after longpressing any item

                if (adapter.selectedActivitieslist?.size == 1) {
                    toggleEditIcon(true)
                    toggleDeleteIcon(true)
                } else {
                    if (adapter.selectedActivitieslist.size > 1) {
                        toggleEditIcon(false)
                        toggleDeleteIcon(true)
                    } else {
                        toggleEditIcon(false)
                        toggleDeleteIcon(false)
                    }
                }


            }else {

                var intent: Intent = Intent(requireContext(), DetailActivity::class.java)
                var selection = activities?.get(position)
                intent.putExtra("activityId", selection?.id)
                requireContext()?.startActivity(intent)
            }
        }

        activitiesList?.onItemLongClickListener = object : AdapterView.OnItemLongClickListener {
            override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
                val adapter = adapter
                if (view != null && adapter != null && adapter.selectedActivitieslist != null) {
                    adapter?.handleLongPress(position, view)

                    if (adapter.selectedActivitieslist?.size == 1) {
                        toggleEditIcon(true)
                        toggleDeleteIcon(true)
                    } else {
                        if (adapter.selectedActivitieslist.size > 1) {
                            toggleEditIcon(false)
                            toggleDeleteIcon(true)
                        } else {
                            toggleEditIcon(false)
                            toggleDeleteIcon(false)
                        }
                    }
                }
                return true
            }

        }
    }

    private fun toggleEditIcon(show: Boolean) {
       mActivityFragmentListener?.toggleEditIcon(show)
    }

    private fun toggleDeleteIcon(show: Boolean) {
        mActivityFragmentListener?.toggleDeleteIcon(show)
    }

    public fun refreshList(){
        adapter?.notifyDataSetChanged();
    }


   public fun showProgressBar(){
        if(progressBar!=null){
            progressBar.visibility = View.VISIBLE
        }
    }
   public fun hideProgressBar(){
        if(progressBar!=null){
            progressBar.visibility = View.GONE
        }
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
       fun toggleEditIcon(show:Boolean)
       fun toggleDeleteIcon(show: Boolean)

    }



}


