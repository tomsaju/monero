package com.monero.activitydetail.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.monero.R
import com.monero.activitydetail.fragments.adapter.HistoryListAdapter
import com.monero.models.History
import com.monero.models.User

/**
 * Created by tom.saju on 6/6/2018.
 */
class HistoryFragment:Fragment() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view:View? = inflater?.inflate(R.layout.history_fragment,container,false)

        var listView =view?.findViewById<ListView>(R.id.history_list)

       /* val user: User = User(234,"Steve","3242342","23423423424")
        val user2:User= User(45,"Tony","3242342","23423423424")

        val history1 = History("676","comment",4555432,user2,"www.ghfhgf.jpg","lorem ipsum dolor sit adiyl let the sky fall, when it crumbles, we will stand tall")
        val history2 = History("616","image",4555432,user,"www.ghfhgf.jpg","sample descptn")
        val history3 = History("686","image",455542,user2,"www.ghfhgf.jpg","sample descptn")
        val history4 = History("86","comment",455542,user,"www.ghfhgf.jpg","Typography is the artful expression of ideas")*/
      /*  var list:ArrayList<History> = ArrayList()
        list.add(history1)
        list.add(history2)
        list.add(history3)
        list.add(history4)

        var adapter = HistoryListAdapter(list,requireContext())

        listView?.adapter=adapter*/
        return view

    }

    companion object {

        fun newInstance(): HistoryFragment {
            val fragment = HistoryFragment()
            return fragment
        }
    }
}