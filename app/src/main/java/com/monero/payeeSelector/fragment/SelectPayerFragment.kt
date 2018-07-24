package com.monero.payeeSelector.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView

import com.monero.R
import com.monero.models.User
import com.monero.payeeSelector.UserListAdapter

class SelectPayerFragment : DialogFragment() {

    private var mListener: SelectPayerFragmentInteractionListener? = null
    lateinit var userList: ListView
    var userArrayList = ArrayList<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {

        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var  view:View= inflater!!.inflate(R.layout.fragment_select_payer, container, false)
        userList = view.findViewById(R.id.userList)
        //fake data
        var tony = User(123,"Tony Stark","787287382","tg@fty.com")
        var thor = User(78,"Thor","76747374623","ashgdakjahsd@hkfshd.com")
        var steve = User(79,"Steve Rogers","767473742343","ashgdakjahsd@hkfshd.com")
        var Bruce = User(71,"Bruce Banners","7674003742343","ashgdakjahsd@hkfshd.com")
        userArrayList.add(tony)
        userArrayList.add(thor)
        userArrayList.add(steve)
        userArrayList.add(Bruce)

        var userListAdapter:UserListAdapter = UserListAdapter(context,userArrayList,mListener)

        userList.adapter = userListAdapter
        return view
    }

    // TODO: Rename method, update argument and hook method into UI event


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is SelectPayerFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnExpenseListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }


    interface SelectPayerFragmentInteractionListener {

       fun onUserSelected(user:User)
    }

    companion object {


        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): SelectPayerFragment {
            val fragment = SelectPayerFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
