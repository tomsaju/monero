package com.monero.main.fragments.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton

import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import android.widget.ProgressBar
import com.monero.R
import com.monero.activitydetail.DetailActivity
import com.monero.main.adapter.ActivityListAdapter
import com.monero.models.Activities
import com.monero.models.Tag
import android.widget.AdapterView.AdapterContextMenuInfo
import android.view.ContextMenu.ContextMenuInfo


/**
 * Created by tom.saju on 3/6/2018.
 */
class ActivityFragment: Fragment() {
    var mActivityFragmentListener: ActivityFragmentListener?=null
    var activitiesList:ListView?=null
    var adapter:ActivityListAdapter?=null
    lateinit var progressBar: ProgressBar
    lateinit var fabAdd: FloatingActionButton

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
        fabAdd = rootView?.findViewById(R.id.fabadd)

        /*val addActivityButton: FloatingActionButton = rootView?.findViewById<FloatingActionButton>(R.id.add_activity_button) as FloatingActionButton
        addActivityButton.setOnClickListener { _:View->
            var taglist:MutableList<Tag> = mutableListOf<Tag>()

            mActivityFragmentListener?.addNewActivity()
        }*/

        registerForContextMenu(activitiesList)
        fabAdd.setOnClickListener(View.OnClickListener {
            mActivityFragmentListener?.addNewActivity()
        })




        fabAdd.visibility=View.VISIBLE

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
            var intent: Intent = Intent(requireContext(), DetailActivity::class.java)
            var selection = activities?.get(position)
            intent.putExtra("activityId", selection?.id)
            requireContext()?.startActivity(intent)
        }


    }


    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)

        if (v.id == R.id.activity_listview) {
            val inflater = requireActivity().getMenuInflater()
            inflater.inflate(R.menu.main_context_menu, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {

        // Here's how you can get the correct item in onContextItemSelected()
        val info = item.menuInfo as AdapterContextMenuInfo
        val longPressedActivity = activitiesList?.adapter?.getItem(info.position) as Activities

        when (item.getItemId()) {
            R.id.edit_actv ->{ // edit stuff here
                Log.d("ContextMenu" ,"clickd with edit on "+longPressedActivity.title)
                showEditDialog(longPressedActivity.id)
                return true
            }

             R.id.delete_actv -> {
                 // remove stuff here
                 showDeleteDialog(longPressedActivity.id)
                 Log.d("ContextMenu", "clickd with delte on "+longPressedActivity.title)
                 return true
             }
            else -> return super.onContextItemSelected(item)
        }
    }


    public fun refreshList(){
        adapter?.notifyDataSetChanged();
    }


    fun showEditDialog(activityId:String){

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Activity")
        builder.setMessage("Are you want to edit this activity?")

        builder.setPositiveButton("YES"){dialog, which ->
            // Do something when user press the positive button
            mActivityFragmentListener?.editActivity(activityId)
            dialog.dismiss()
        }

        builder.setNegativeButton("No"){dialog,which ->
            dialog.dismiss()
        }

        builder.setNeutralButton("Cancel"){dialog,_ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()

        dialog.show()

    }

    fun showDeleteDialog(activityId: String){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Activity")
        builder.setMessage("Are you want to Delete this activity?")

        builder.setPositiveButton("YES"){dialog, which ->
            // Do something when user press the positive button

        }

        builder.setNegativeButton("No"){dialog,which ->

        }

        builder.setNeutralButton("Cancel"){_,_ ->

        }

        val dialog: AlertDialog = builder.create()

        dialog.show()
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
       fun editActivity(id:String)

    }





}


