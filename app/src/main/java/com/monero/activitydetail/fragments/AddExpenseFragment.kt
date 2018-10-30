package com.monero.activitydetail.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.monero.Application.ApplicationController

import com.monero.R
import com.monero.splittype.SplitTypeActivity
import com.monero.activitydetail.presenter.expense.ExpenseFragmentPresenter
import com.monero.activitydetail.presenter.expense.IExpenseFragmentPresenter
import com.monero.activitydetail.presenter.expense.IExpenseFragmentView
import com.monero.models.*
import com.monero.payeeSelector.PayerSelectorActivity
import com.monero.utility.Utility
import kotlinx.android.synthetic.main.add_expense_payment_line.*
import java.math.BigDecimal
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
    var SPLIT_TYPE_EQUALLY = 0
    var SPLIT_TYPE_PERCENTAGE = 1
    var SPLIT_TYPE_MONEY = 2
    var REQUEST_CODE_PAYER_SELECTION = 3
    val customSplitRequest: Int = 9
    var amount:BigDecimal?=BigDecimal.ZERO
    var splitType:Int =SPLIT_TYPE_EQUALLY
    var amountSpend:Int = 0
    var activityId:String = ""
    var currentlyWorkingActivity :Activities?=null
    var expenseId:String = ""
    val auth = FirebaseAuth.getInstance()!!
    var myuser:User = User()
    lateinit var title:AutoCompleteTextView
    lateinit var currencySymbolTV: TextView
    lateinit var amountEditText:EditText
    lateinit var paidByTV:TextView
    lateinit var splitTypeTv: TextView
    lateinit var discardButton:Button
    lateinit var saveButton:Button
    lateinit var paidUsersList:HashMap<User,Int> //<each user,amount paid>
    lateinit var splitPaymentList:HashMap<User,Int>//<each user,amount owed>
    lateinit var debitList:ArrayList<Debit>
    lateinit var creditList:ArrayList<Credit>
    lateinit var mExpenseFragmentPresenter : IExpenseFragmentPresenter
    lateinit var totalParticipantList:ArrayList<User>
    private var mListener: OnFragmentInteractionListener? = null




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
             // Inflate the layout for this fragment
         var view:View = inflater!!.inflate(R.layout.add_expense_layout, container, false)
         title = view.findViewById(R.id.title_expense_autotextview)
         currencySymbolTV = view.findViewById(R.id.currency_symbol_textview_add_expense)
         amountEditText = view.findViewById(R.id.amount_edittext_add_expense)
         paidByTV = view.findViewById(R.id.autocomplete_tv_add_expense_payee)
         splitTypeTv = view.findViewById(R.id.split_type_edittext_add_expense)
         saveButton = view.findViewById(R.id.save_btn_add_expense)
         discardButton = view.findViewById(R.id.discard_btn_add_expense)
         mExpenseFragmentPresenter = ExpenseFragmentPresenter(requireContext(),this)
         currentlyWorkingActivity = mListener?.getcurrentWorkingActivity()


        currencySymbolTV.text = Utility.getCurrencySymbol()

         Log.d(TAG,"currentlyworkingact "+currentlyWorkingActivity?.title)
         if(currentlyWorkingActivity?.members!=null) {
             totalParticipantList = ArrayList(currentlyWorkingActivity?.members)
         }


         if(expenseId.isEmpty()){
             expenseId = System.currentTimeMillis().toString()
         }

         val currentAct = currentlyWorkingActivity
         if(currentAct!=null) {
             activityId = currentAct.id
         }

         paidUsersList = HashMap()
         splitPaymentList = HashMap()

         paidByTV.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
         paidByTV.setOnClickListener { v: View? ->

            var intent = Intent(context,PayerSelectorActivity::class.java)
            intent.putExtra("activity_id",activityId)
            intent.putExtra("PayeeList",paidUsersList)
            if(amountEditText.text.isEmpty()){
                intent.putExtra("entered_total", 0)
            }else {
                var valueInHigherDenomination = amountEditText.text.toString().toDouble()*100
                intent.putExtra("entered_total", valueInHigherDenomination.toInt())
            }
            startActivityForResult(intent,REQUEST_CODE_PAYER_SELECTION)

        }

         saveButton.setOnClickListener { v: View? ->
           if(checkInputValid()){

               amountSpend = (amount_edittext_add_expense.text.toString().toDouble()*100).toInt()

               var tempDebitList = ArrayList<Debit>()
               var tempCreditList = ArrayList<Credit>()


               //add value to paiduserlist & splitpaymentList
               //ex. if paid by me for all, then paid user list is <me,amount> and splitpayment list is
               //<userN, amount/no of users> for N times

               for(entry in paidUsersList){
                    var debit = Debit(System.currentTimeMillis()*(1 until 10).random(),
                                         activityId,
                                         expenseId,
                                         entry.key.user_id,
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



               var expense = Expense(expenseId,title.text.toString(),"Some comments",activityId,(amountEditText.text.toString().toDouble()*100).toInt(),tempCreditList,tempDebitList,splitType,System.currentTimeMillis().toString())
               mExpenseFragmentPresenter.saveExpense(expense)
               mListener?.closeFragment()
           }
         }

         discardButton.setOnClickListener { v: View? ->
           mListener?.closeFragment()
         }

         if(auth!=null&&auth.currentUser!=null){
             val uid = auth.currentUser?.uid
             if(uid!=null){
                  myuser = User(uid,auth.currentUser!!.displayName!!, ApplicationController.preferenceManager!!.myCredential,"sample@yopmail.com")
             }

         }

       splitTypeTv.setOnClickListener {

           if(amountEditText.text.toString().isEmpty()||amountEditText.text.toString().isBlank()){
               Toast.makeText(requireContext(),"Please specify total amount first",Toast.LENGTH_SHORT).show()
           }else {

               amountSpend = (amount_edittext_add_expense.text.toString().toDouble() * 100).toInt()
               if (amountSpend == null || amountSpend == 0) {
                   Toast.makeText(requireContext(), "Please specify total amount first", Toast.LENGTH_SHORT).show()

               } else {
                   var sIntent = Intent(requireContext(), SplitTypeActivity::class.java)
                   sIntent.putExtra("activityId", activityId)
                   sIntent.putExtra("total", amountSpend)


                   if(splitPaymentList!=null&&splitPaymentList.isNotEmpty()){
                       sIntent.putExtra("owedList",splitPaymentList)
                       sIntent.putExtra("splitType",splitType)
                   }

                   startActivityForResult(sIntent,customSplitRequest)
               }
           }
       }


        if(arguments!=null){
            if(arguments?.getString("expense_id")!=null&&arguments?.getString("expense_id")!=""){
                expenseId = arguments?.getString("expense_id").toString()
                loadExpenseDetails(expenseId)
            }else{
                expenseId = System.currentTimeMillis().toString()
            }
        }else{
            expenseId = System.currentTimeMillis().toString()
        }

        if(expenseId!=null&&expenseId.isNotEmpty()) {
            mExpenseFragmentPresenter.getExpenseForId(expenseId)
        }

        return view
    }

    private fun loadExpenseDetails(expenseId: String) {

    }

    private fun splitCredits(splitType: Int) {

        if(splitPaymentList==null||splitPaymentList.isEmpty()){
            var amountOwed =  (amountSpend/totalParticipantList.size)

            for(user in totalParticipantList){
                splitPaymentList.put(user,amountOwed)
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
        }/*else if(splitTypeTv.getse.isEmpty()){
            return false
        }*/

        if(paidByTV.text.equals("you")){
            if(paidUsersList==null||paidUsersList.isEmpty()){
                paidUsersList.put(myuser,(amount_edittext_add_expense.text.toString().toDouble()*100).toInt())
            }
            //return false
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
            paidUsersList = data.getSerializableExtra("PayeeList") as HashMap<User, Int>
            var addedUptotal:Int = data.getIntExtra("total",0)
            if(addedUptotal!=0){

                var amountinHigerDenomination =(addedUptotal/100).toDouble()

                amountEditText?.setText(amountinHigerDenomination.toString())
            }

            if(paidUsersList.size==1){
                var entry = paidUsersList.entries.iterator().next()
                if(entry.key.user_id==auth.currentUser?.uid){
                    paidByTV.setText("you")
                }else{
                    paidByTV.setText(entry.key.user_name)
                }
            }

        }else if(requestCode == customSplitRequest&& data != null){
            //get the credit lsit from splitactivity
            //it might be parcelable
            "owedList"
            splitPaymentList = data.getSerializableExtra("owedList") as HashMap<User, Int>
            splitType = data.getIntExtra("splitType",0)
            if(splitType==SPLIT_TYPE_EQUALLY){
                splitTypeTv.text = "Equally"
            }else if(splitType==SPLIT_TYPE_PERCENTAGE){
                splitTypeTv.text = "By Percentage"
            }else if(splitType==SPLIT_TYPE_MONEY){
                splitTypeTv.text="By Amount"
            }


        }
    }

    fun showListDialog( listItem:ArrayList<String>){

    }

    override fun onExpenseFetched(expense: Expense) {
        activityId = expense.activity_id
        title.setText(expense.title)

        var amountinHigerDenomination =(expense.amount/100).toDouble()

        amountEditText?.setText(amountinHigerDenomination.toString())

        splitType = expense.splitType
        if(splitType==SPLIT_TYPE_EQUALLY){
            splitTypeTv.text = "Equally"
        }else if(splitType==SPLIT_TYPE_PERCENTAGE){
            splitTypeTv.text = "By Percentage"
        }else if(splitType==SPLIT_TYPE_MONEY){
            splitTypeTv.text="By Amount"
        }



        for(debit in expense.debitList){
            paidUsersList.put(User(debit.user_id,debit.userName,""),debit.amount)
        }

            if (paidUsersList.size == 1) {
                var entry = paidUsersList.entries.iterator().next()
                if (entry.key.user_id == auth.currentUser?.uid) {
                    paidByTV.setText("you")
                } else {
                    paidByTV.setText(entry.key.user_name)
                }
            }


        for(credit in expense.creditList){
            splitPaymentList.put(User(credit.user_id,credit.userName,""),credit.amount)
        }



    }
}// Required empty public constructor
