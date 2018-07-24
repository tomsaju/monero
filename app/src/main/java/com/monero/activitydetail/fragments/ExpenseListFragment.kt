package com.monero.activitydetail.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.monero.R
import com.monero.models.Expense


class ExpenseListFragment : Fragment() {

    private var fab:FloatingActionButton? = null
    private var mListenerExpenseList: OnExpenseListFragmentInteractionListener? = null
    lateinit private var expenseList:ArrayList<Expense>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

         var view:View = inflater!!.inflate(R.layout.fragment_expense_list, container, false)
        fab = view.findViewById<FloatingActionButton>(R.id.addexpenseButton) as FloatingActionButton
        fab?.setOnClickListener { _:View ->
            //Show the add expense fragment
           addNewExpense()
        }
        return view
    }

    fun addNewExpense(){
        var ft = activity.supportFragmentManager.beginTransaction()
        var frag =  AddExpenseFragment()
        ft.add(android.R.id.content, frag,"activity_add_fragment").commit()
    }
    fun onButtonPressed(uri: Uri) {
        if (mListenerExpenseList != null) {
            mListenerExpenseList!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnExpenseListFragmentInteractionListener) {
            mListenerExpenseList = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnExpenseListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListenerExpenseList = null
    }


    interface OnExpenseListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        fun newInstance(): ExpenseListFragment {
            val fragment = ExpenseListFragment()
            return fragment
        }
    }


    fun setExpenseList(expenseList:ArrayList<Expense>){
        this.expenseList = expenseList
    }

}// Required empty public constructor
