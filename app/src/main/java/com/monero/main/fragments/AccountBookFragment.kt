package com.monero.main.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.monero.R
import com.monero.main.adapter.AccountBookListAdapter
import com.monero.main.presenter.accountbook.AccountBookPresenter
import com.monero.main.presenter.accountbook.IAccountBookPresenter
import com.monero.main.presenter.accountbook.IAccountBookView
import com.monero.models.PendingTransaction
import com.monero.models.User
import kotlinx.android.synthetic.main.accountbook_fragment.*

/**
 * Created by tom.saju on 3/6/2018.
 */
class AccountBookFragment:Fragment(),IAccountBookView {
    var list:ArrayList<PendingTransaction> = ArrayList()
    lateinit var recyclerview:RecyclerView
    lateinit var mPresenter:IAccountBookPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter = AccountBookPresenter(requireContext(),this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater?.inflate(R.layout.accountbook_fragment,container,false)
        recyclerview = rootView.findViewById<RecyclerView>(R.id.account_list)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        mPresenter.getAllPendingTransactions()
    }

    override fun onTransactionsfetched(list: ArrayList<PendingTransaction>) {

        var adapter = AccountBookListAdapter(list,requireContext())

        recyclerview!!.layoutManager = LinearLayoutManager(requireContext())
        recyclerview!!.adapter = adapter
    }


}