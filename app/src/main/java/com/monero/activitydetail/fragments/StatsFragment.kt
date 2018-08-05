package com.monero.activitydetail.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView

import com.monero.R
import com.monero.activitydetail.DetailActivity
import com.monero.activitydetail.fragments.adapter.PendingTransactionListAdapter
import com.monero.activitydetail.presenter.stats.IStatsPresenter
import com.monero.activitydetail.presenter.stats.IStatsView
import com.monero.activitydetail.presenter.stats.StatsPresenter
import com.monero.models.PendingTransaction


class StatsFragment : Fragment(),IStatsView {

    private var mListener: StatsFragmentListener? = null
    private lateinit var mStatsPresenter:IStatsPresenter
    private lateinit var transactionsListVIew:ListView
    private lateinit var adapter:PendingTransactionListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mStatsPresenter =  StatsPresenter(requireContext(),this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
       var view=inflater!!.inflate(R.layout.fragment_stats, container, false)
        transactionsListVIew = view.findViewById(R.id.transactionList)
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
        adapter = PendingTransactionListAdapter(pendingTransaction,requireContext())
        transactionsListVIew.adapter = adapter
    }
}// Required empty public constructor
