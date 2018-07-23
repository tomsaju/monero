package com.monero.activitydetail.fragments

import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.content.Intent.parseIntent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.monero.R
import com.monero.activitydetail.presenter.expense.ExpenseFragmentPresenter
import com.monero.activitydetail.presenter.expense.IExpenseFragmentPresenter
import com.monero.activitydetail.presenter.expense.IExpenseFragmentView
import com.monero.models.Credit
import com.monero.models.Debit
import com.monero.models.Expense
import com.monero.models.User
import com.monero.payeeSelector.PayerSelectorActivity
import java.util.*
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AddExpenseFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 */
class AddExpenseFragment : Fragment(),IExpenseFragmentView {
    var REQUEST_CODE_PAYER_SELECTION = 3
    private var mListener: OnFragmentInteractionListener? = null

    lateinit var title:AutoCompleteTextView
    lateinit var currencySymbolTV: TextView
    lateinit var amountEditText:EditText
    lateinit var paidByTV:AutoCompleteTextView
    lateinit var splitTypeTv:AutoCompleteTextView
    lateinit var discardButton:Button
    lateinit var saveButton:Button
    var amount:Double?=0.0
    lateinit var paidUsersList:HashMap<User,Double> //<each user,amount paid>
    lateinit var splitPaymentList:HashMap<User,Double>//<each user,amount owed>
    lateinit var debitList:ArrayList<Debit>
    lateinit var creditList:ArrayList<Credit>
    lateinit var mExpenseFragmentPresenter : IExpenseFragmentPresenter


     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
             // Inflate the layout for this fragment
         var view:View = inflater!!.inflate(R.layout.add_expense_layout, container, false)
         title = view.findViewById(R.id.title_expense_autotextview)
         currencySymbolTV = view.findViewById(R.id.currency_symbol_textview_add_expense)
         amountEditText = view.findViewById(R.id.amount_edittext_add_expense)
         paidByTV = view.findViewById(R.id.autocomplete_tv_add_expense_payee)
         splitTypeTv = view.findViewById(R.id.autocomplete_tv_split_members_add_expense)
         saveButton = view.findViewById(R.id.save_btn_add_expense)
         discardButton = view.findViewById(R.id.discard_btn_add_expense)
         mExpenseFragmentPresenter = ExpenseFragmentPresenter(activity,this)

         paidUsersList = HashMap()



         //

        paidByTV.setOnClickListener { v: View? ->

            startActivityForResult(Intent(context,PayerSelectorActivity::class.java),REQUEST_CODE_PAYER_SELECTION)

        }

         saveButton.setOnClickListener { v: View? ->
           if(checkInputValid()){
               var tempDebitList = ArrayList<Debit>()
               var tempCreditList = ArrayList<Credit>()


               //add value to paiduserlist & splitpaymentList
               //ex. if paid by me for all, then paid user list is <me,amount> and splitpayment list is
               //<userN, amount/no of users> for N times

               for(entry in paidUsersList){
                    var debit = Debit(System.currentTimeMillis()*(0 until 10).random(),
                                        455434,
                                        234333,
                                         entry.key.user_id.toLong(),
                                         entry.key.name,
                                         entry.value)

                   tempDebitList.add(debit)
               }

               for(entry in splitPaymentList){
                   var credit = Credit(System.currentTimeMillis()*(0 until 10).random(),
                                455432,
                                23423423,
                                entry.key.user_id.toLong(),
                                entry.key.name,
                                entry.value)

                   tempCreditList.add(credit)
               }


               var expense: Expense = Expense(System.currentTimeMillis(),title.text.toString(),"",234234,tempCreditList,tempDebitList)
               mExpenseFragmentPresenter.saveExpense(expense)
           }
         }

         discardButton.setOnClickListener { v: View? ->

         }

        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    fun ClosedRange<Int>.random() =
            Random().nextInt((endInclusive + 1) - start) +  start

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }


    fun checkInputValid():Boolean{

        if(title!=null&&title.text.isEmpty()){
           return false
        }else if(amountEditText.text.isEmpty()){
            return false;

        }else if(paidByTV.text.isEmpty()){
            return false
        }else if(splitTypeTv.text.isEmpty()){
            return false
        }

        return true
    }

    fun saveExpense(){
        if(checkInputValid()){

        }
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        fun newInstance(): AddExpenseFragment {
            val fragment = AddExpenseFragment()
            return fragment
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PAYER_SELECTION && data != null) {
            splitPaymentList = data.getSerializableExtra("PayeeList") as HashMap<User, Double>
            //do something with payerlist
           paidByTV.setText("${splitPaymentList.size} people")
        }
    }

}// Required empty public constructor
