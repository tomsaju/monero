package com.monero.activitydetail.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
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
import com.monero.activitydetail.fragments.adapter.ExpenseListRecyclerAdapter
import com.monero.activitydetail.presenter.expenselist.ExpenseListPresenter
import com.monero.activitydetail.presenter.expenselist.IExpenseListPresenter
import com.monero.activitydetail.presenter.expenselist.IExpenseListView
import com.monero.models.Expense


class ExpenseListFragment : Fragment(),IExpenseListView {



    private var mListenerExpenseList: OnExpenseListFragmentInteractionListener? = null
    lateinit private var expenseList:ArrayList<Expense>
    private lateinit var expenseAdapter: ExpenseListRecyclerAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var mPresenter:IExpenseListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

         var view:View = inflater!!.inflate(R.layout.fragment_expense_list, container, false)
        //fab = view.findViewById<FloatingActionButton>(R.id.addexpenseButton) as FloatingActionButton
        recyclerView = view.findViewById(R.id.expense_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayout.VERTICAL, false)
        mPresenter=ExpenseListPresenter(requireContext(),this)

        return view
    }

    fun addNewExpense(){
        var ft = activity?.supportFragmentManager?.beginTransaction()
        var frag =  AddExpenseFragment()
        ft?.setCustomAnimations(R.anim.design_bottom_sheet_slide_in,R.anim.design_bottom_sheet_slide_out)
        ft?.add(android.R.id.content, frag,"activity_add_fragment")?.commit()
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
        fun onTotal(total:Double)
    }

    companion object {

        fun newInstance(): ExpenseListFragment {
            val fragment = ExpenseListFragment()
            return fragment
        }
    }


    fun setExpenseList(expenseList:ArrayList<Expense>){
        setExpenseTotal(expenseList)
        this.expenseList = expenseList
        expenseAdapter = ExpenseListRecyclerAdapter(expenseList,requireActivity())
        if(recyclerView !=null){
            recyclerView.adapter = expenseAdapter
        }
    }

    private fun setExpenseTotal(expenseList: ArrayList<Expense>) {
        var sum:Double =0.0
        for(expense in expenseList){
            sum+=expense.amount
        }
        mListenerExpenseList?.onTotal(sum)
    }

    override fun onResume() {
        super.onResume()
        if(mPresenter!=null){
            mPresenter?.getAllExpensesForActivity((activity as DetailActivity).activityId)
        }

    }

    override fun onExpensesFetched(expenses: LiveData<List<Expense>>) {
        expenses.observe(this,object: Observer<List<Expense>> {
            override fun onChanged(expenseList: List<Expense>?) {
                if(expenseList!=null) {
                    setExpenseList(ArrayList(expenseList)) //expenses
                }
            }

        })
    }

}// Required empty public constructor
