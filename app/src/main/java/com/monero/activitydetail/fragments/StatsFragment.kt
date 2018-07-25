package com.monero.activitydetail.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.monero.R
import com.monero.activitydetail.presenter.stats.IStatsPresenter
import com.monero.activitydetail.presenter.stats.IStatsView
import com.monero.activitydetail.presenter.stats.StatsPresenter


class StatsFragment : Fragment(),IStatsView {

    private var mListener: StatsFragmentListener? = null
    private lateinit var mStatsPresenter:IStatsPresenter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mStatsPresenter =  StatsPresenter(activity,this)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_stats, container, false)
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
}// Required empty public constructor
