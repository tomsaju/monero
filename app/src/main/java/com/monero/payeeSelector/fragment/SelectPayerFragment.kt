package com.monero.payeeSelector.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ListView

import com.monero.R
import com.monero.models.User
import com.monero.payeeSelector.UserListAdapter

class SelectPayerFragment : DialogFragment() {

    private var mListener: SelectPayerFragmentInteractionListener? = null
    lateinit var userList: ListView
    lateinit var selectbtn: Button
    lateinit var cancelBtn: Button
    lateinit var userListAdapter: UserListAdapter

     var userArrayList:ArrayList<User>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        var  view:View= inflater!!.inflate(R.layout.fragment_select_payer, container, false)
        selectbtn  = view.findViewById(R.id.selectBtn)
        cancelBtn = view.findViewById(R.id.cancelBtn)
        userList = view.findViewById(R.id.userList)

        selectbtn.setOnClickListener {
            mListener?.onUserSelected(userListAdapter.payerList)
            this.dialog.dismiss()
        }

        cancelBtn.setOnClickListener {
            this.dialog.dismiss()
        }

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
        fun getAllusers():ArrayList<User>
       fun onUserSelected(payerList:HashMap<User,Int>)
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



    override fun onResume() {
        super.onResume()
        userArrayList= mListener?.getAllusers()
        if(userArrayList!=null){
            val list = userArrayList
            if(list!=null) {
                userListAdapter = UserListAdapter(requireContext(), list, mListener)
                userList.adapter = userListAdapter
            }
        }

    }
}// Required empty public constructor
