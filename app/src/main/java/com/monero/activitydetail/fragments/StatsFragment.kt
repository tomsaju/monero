package com.monero.activitydetail.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.monero.R
import com.monero.activitydetail.DetailActivity
import com.monero.activitydetail.fragments.adapter.PendingTransactionRecyclerAdapter
import com.monero.activitydetail.presenter.stats.IStatsPresenter
import com.monero.activitydetail.presenter.stats.IStatsView
import com.monero.activitydetail.presenter.stats.StatsPresenter
import com.monero.models.PendingTransaction


class StatsFragment : Fragment(),IStatsView {

    private var mListener: StatsFragmentListener? = null
    private lateinit var mStatsPresenter:IStatsPresenter
    private lateinit var transactionsRecyclerVIew:RecyclerView
    private lateinit var adapter:PendingTransactionRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mStatsPresenter =  StatsPresenter(requireContext(),this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
       var view=inflater!!.inflate(R.layout.fragment_stats, container, false)
        transactionsRecyclerVIew = view.findViewById(R.id.transactionList)
        transactionsRecyclerVIew.layoutManager = LinearLayoutManager(requireContext(), LinearLayout.VERTICAL, false)
        return view
    }



    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is StatsFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement StatsFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }


    interface StatsFragmentListener {

    }

    override fun onResume() {
        super.onResume()
        if(mStatsPresenter!=null){
            mStatsPresenter?.getAllPendingTransactions((activity as DetailActivity).activityId)
        }
    }

    companion object {

        fun newInstance(): StatsFragment {
            val fragment = StatsFragment()
            return fragment
        }
    }

    override fun onPendingTransactionsObtained(pendingTransaction: ArrayList<PendingTransaction>) {
        adapter = PendingTransactionRecyclerAdapter(pendingTransaction,requireContext())
        transactionsRecyclerVIew.adapter = adapter
    }
}// Required empty public constructor
