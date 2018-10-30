package com.monero.activitydetail.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import com.monero.R
import com.monero.activitydetail.DetailActivity
import com.monero.activitydetail.fragments.adapter.HistoryListAdapter
import com.monero.activitydetail.fragments.adapter.HistoryLogRecyclerAdapter
import com.monero.activitydetail.presenter.history.HistoryPresenter
import com.monero.activitydetail.presenter.history.IHistoryPresenter
import com.monero.activitydetail.presenter.history.IHistoryView
import com.monero.main.fragments.Activities.ActivityFragment
import com.monero.models.Activities
import com.monero.models.History
import com.monero.models.HistoryLogItem
import com.monero.models.User

/**
 * Created by tom.saju on 6/6/2018.
 */
class HistoryFragment:Fragment(),IHistoryView {

    //history log added when
    // user creates an activity
    //user adds an expense
    //user settles a bill
    //user adds a new comment on an expense

    lateinit var recyclerView:RecyclerView
    lateinit var mPresenter:IHistoryPresenter
    lateinit  var adapter:HistoryLogRecyclerAdapter
    var logList: ArrayList<HistoryLogItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

         var view:View = inflater?.inflate(R.layout.history_fragment,container,false)

         recyclerView =view.findViewById<RecyclerView>(R.id.history_list)
         recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayout.VERTICAL, false)
         mPresenter = HistoryPresenter(requireContext(),this)

         mPresenter.getAllHistoryLogForActivity((activity as DetailActivity).activityId)
         adapter = HistoryLogRecyclerAdapter(logList,requireContext())
         recyclerView.adapter = adapter
         return view

    }

    companion object {

        fun newInstance(): HistoryFragment {
            val fragment = HistoryFragment()
            return fragment
        }
    }

    override fun onAllLogsFetched(hLogList: LiveData<List<HistoryLogItem>>) {

        hLogList?.observe(this, object : Observer<List<HistoryLogItem>> {
            override fun onChanged(allList: List<HistoryLogItem>?) {
                if(allList!=null) {

                    var sortedList = allList.sortedWith(compareBy({ it.Timestamp.toLong() }))
                    logList = ArrayList(sortedList.reversed())
                    adapter = HistoryLogRecyclerAdapter(logList,requireContext())
                    recyclerView.adapter = adapter
                }
            }

        });
    }

}