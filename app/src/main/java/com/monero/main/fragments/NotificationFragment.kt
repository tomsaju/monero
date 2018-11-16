package com.monero.main.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.monero.R
import com.monero.main.adapter.NotificationListAdapter
import com.monero.main.fragments.Activities.ActivityFragment
import com.monero.models.NotificationItem

/**
 * Created by tom.saju on 3/6/2018.
 */
class NotificationFragment:Fragment() {
    lateinit var mNotiFragmentListener:NotificationFragmentListener
    lateinit var adapter:NotificationListAdapter
    lateinit var recyclerView:RecyclerView
    lateinit var emptyView:RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater?.inflate(R.layout.notification_fragment,container,false)
        recyclerView = rootView.findViewById(R.id.noti_recyclerview)
        emptyView = rootView.findViewById(R.id.no_items_layout_parent)
        mNotiFragmentListener.getAllNotificationFromDb()
        return rootView
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(context is NotificationFragmentListener){
            mNotiFragmentListener =  context
        }else{

        }


    }


    fun onAllNotificationsFetched(notisList:List<NotificationItem>){

        if(notisList.isNotEmpty()) {
            adapter = NotificationListAdapter(ArrayList(notisList), requireContext())
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
            emptyView.visibility = View.GONE
        }else{
            emptyView.visibility = View.VISIBLE
        }
    }

    interface NotificationFragmentListener{
        fun getAllNotificationFromDb()
    }
}

