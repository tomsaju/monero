package com.monero.activitydetail.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
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
import com.monero.models.*
import com.monero.payeeSelector.PayerSelectorActivity
import kotlinx.android.synthetic.main.add_expense_payment_line.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AddExpenseFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 */
class AddExpenseFragment : Fragment(),IExpenseFragmentView {
    val TAG = "AddExpenseFragment"
    val SPLIT_EQUALLY_AMONG_ALL =0
    val SPLIT_EQUALLY_AMONG_ALL_EXCEPT_ME =1
    val SPLIT_AMONG_CUSTOM =2
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
    var splitType:Int =SPLIT_EQUALLY_AMONG_ALL
    var amountSpend:Double =0.0
    lateinit var totalParticipantList:ArrayList<User>
    var activityId:Long = 0
    var currentlyWorkingActivity :Activities?=null
    var expenseId:Long = 0

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
         currentlyWorkingActivity = mListener?.getcurrentWorkingActivity()
         Log.d(TAG,"currentlyworkingact "+currentlyWorkingActivity?.title)
         if(currentlyWorkingActivity?.members!=null) {
             totalParticipantList = ArrayList(currentlyWorkingActivity?.members)
         }

         if(expenseId<1){
             expenseId = System.currentTimeMillis()
         }

         val currentAct = currentlyWorkingActivity
         if(currentAct!=null) {
             activityId = currentAct.id
         }

         paidUsersList = HashMap()
         splitPaymentList = HashMap()


         //

        paidByTV.setOnClickListener { v: View? ->

            var intent = Intent(context,PayerSelectorActivity::class.java)
            intent.putExtra("activity_id",activityId)
            startActivityForResult(intent,REQUEST_CODE_PAYER_SELECTION)

        }

         saveButton.setOnClickListener { v: View? ->
           if(checkInputValid()){

               amountSpend = amount_edittext_add_expense.text.toString().toDouble()

               var tempDebitList = ArrayList<Debit>()
               var tempCreditList = ArrayList<Credit>()


               //add value to paiduserlist & splitpaymentList
               //ex. if paid by me for all, then paid user list is <me,amount> and splitpayment list is
               //<userN, amount/no of users> for N times

               for(entry in paidUsersList){
                    var debit = Debit(System.currentTimeMillis()*(1 until 10).random(),
                                        activityId,
                                         expenseId,
                                         entry.key.user_id.toLong(),
                                         entry.key.user_name,
                                         entry.value)

                   tempDebitList.add(debit)
               }

               splitCredits(splitType)

               for(entry in splitPaymentList){
                   var credit = Credit(System.currentTimeMillis()*(1 until 10).random(),
                                activityId,
                                expenseId,
                                entry.key.user_id,
                                entry.key.user_name,
                                entry.value)

                   tempCreditList.add(credit)
               }



               var expense = Expense(expenseId,title.text.toString(),"Some comments",activityId,amountEditText.text.toString().toDouble(),tempCreditList,tempDebitList)
               mExpenseFragmentPresenter.saveExpense(expense)
               mListener?.closeFragment()
           }
         }

         discardButton.setOnClickListener { v: View? ->
           mListener?.closeFragment()
         }

        return view
    }

    private fun splitCredits(splitType: Int) {

        if(splitType===SPLIT_EQUALLY_AMONG_ALL){
           var amountOwed =  amountSpend/totalParticipantList.size

            for(user in totalParticipantList){
                splitPaymentList.put(user,amountOwed)
            }
        }else if(splitType===SPLIT_EQUALLY_AMONG_ALL_EXCEPT_ME){
            var amountOwed =  amountSpend/totalParticipantList.size-1

            for(user in totalParticipantList){
                splitPaymentList.put(user, amountOwed)
            }
        }


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
            throw RuntimeException(context!!.toString() + " must implement OnExpenseListFragmentInteractionListener")
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



    interface OnFragmentInteractionListener {
        // TODO: Update argument type and user_name
        fun onFragmentInteraction(uri: Uri)
        fun getcurrentWorkingActivity():Activities?
        fun closeFragment()
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
            paidUsersList = data.getSerializableExtra("PayeeList") as HashMap<User, Double>
           paidByTV.setText("${paidUsersList.size} people")
        }
    }

}// Required empty public constructor
