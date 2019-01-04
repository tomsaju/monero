package com.monero.expensedetail

import android.app.ActionBar
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.graphics.Color
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.TableLayout
import android.widget.TableRow
import com.monero.R
import com.monero.expensedetail.presenter.ExpenseDetailPresenter
import com.monero.expensedetail.presenter.IExpenseDetailPresenter
import com.monero.expensedetail.presenter.IExpenseDetailView
import kotlinx.android.synthetic.main.activity_expense.*
import android.widget.TextView
import com.monero.models.*
import android.widget.LinearLayout
import com.monero.activitydetail.fragments.AddExpenseFragment
import com.monero.utility.Utility


class ExpenseActivity : AppCompatActivity(),IExpenseDetailView, AddExpenseFragment.OnFragmentInteractionListener {
var expenseId:String =""
lateinit var mPresenter:IExpenseDetailPresenter
lateinit var mRecyclerView:RecyclerView
lateinit var mAdapter:PayerRecyclerAdapter
lateinit var toolbar:Toolbar
lateinit var table:TableLayout
    lateinit var tableRow:TableRow
    lateinit var editMenuItem:MenuItem
    lateinit var deleteMenuItem:MenuItem


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)
        toolbar = findViewById(R.id.toolbar_custom)
        table = findViewById(R.id.table)
        tableRow = findViewById(R.id.tablerowLabels)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setTitle("Expense Detail")
        mPresenter = ExpenseDetailPresenter(this,this)



        if(intent.extras==null){
            finish()
        }else{
            expenseId = intent.extras.getString("expenseid")
        }

        if(expenseId!=null){
            mPresenter.getExpenseForId(expenseId)
        }
    }

    override fun onExpenseFetched(expense: LiveData<Expense>) {
        expense?.observe(this, object : Observer<Expense> {
            override fun onChanged(thisExpense: Expense?) {

                expense_Title_detailpage.text = thisExpense?.title
                var amountInLowerDenomination = thisExpense?.amount
                var amountInHigherDenomination = "%.2f".format((amountInLowerDenomination!!/100).toDouble())
                expense_Amount_detail_page.text = Utility.getCurrencySymbol()+ amountInHigherDenomination
                var paymentList:ArrayList<Payment> = ArrayList()
                if(thisExpense?.debitList!=null) {
                    for (debit in thisExpense.debitList) {
                        paymentList.add(Payment(debit.amount, User(debit.user_id,debit.userName,"unknown","unknown")))
                    }
                }
                mAdapter = PayerRecyclerAdapter(paymentList)
                detailPayRecyler.layoutManager = LinearLayoutManager(this@ExpenseActivity)
                detailPayRecyler.adapter = mAdapter

              setupTable(thisExpense)
            }

        });



    }

    private fun setupTable(thisExpense: Expense?) {

        table.removeAllViews()
        table.addView(tableRow)
        var creditList = thisExpense?.creditList
        var numberOfMembers =creditList?.size //assuming that the payer is also included in dividing th expense
        var tabledata =  ArrayList<splitTableItem>()
        var amountPaid = 0
        if(creditList!=null) {

            for (credit in creditList) {// assuming payer is part of this
                amountPaid = 0
                for(debit in thisExpense?.debitList!!){
                    if(debit.user_id==credit.user_id){
                        amountPaid = debit.amount
                    }
                }
                var individualShare = credit.amount
                tabledata.add(splitTableItem(credit.user_id, credit.userName, individualShare, amountPaid))

                }
        }
        val params = TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT)
       // params.setMargins(15, 10, 10, 10)



        for(data in tabledata){
            val name =TextView(this)
            name.text =data.username+"    "
            name.setPadding(18,10,10,10)
            name.setTextColor(Color.BLACK)

            val actualShare = TextView(this)
            name.textSize = 15f

            actualShare.text =Utility.getCurrencySymbol()+ getInHIgherDenomination(data.actualShare)
            actualShare.textSize = 15f

            val paidAmount = TextView(this)
            paidAmount.text =Utility.getCurrencySymbol()+ getInHIgherDenomination(data.paidAMount)
            paidAmount.textSize = 15f
            val remaining = TextView(this)
            var remainingAmount = data.actualShare - data.paidAMount
            remaining.textSize = 15f

            if(remainingAmount<0){
                remaining.text =Utility.getCurrencySymbol()+ "0.00"
            }else {
                remaining.text =Utility.getCurrencySymbol()+ getInHIgherDenomination(data.actualShare - data.paidAMount)
            }
            val rowHeader = TableRow(this)
            rowHeader.layoutParams = params
            rowHeader.addView(name)
            rowHeader.addView(actualShare)
            rowHeader.addView(paidAmount)
            rowHeader.addView(remaining)

            table.addView(rowHeader)

        }



    }

  fun getInHIgherDenomination(amount:Int):String{
      var amountInHigherDenomination = "%.2f".format((amount/100).toDouble())
      return amountInHigherDenomination
  }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.expense_detail_menu, menu)

        editMenuItem = menu!!.findItem(R.id.action_edit)
        deleteMenuItem = menu.findItem(R.id.action_delete)

        return super.onCreateOptionsMenu(menu)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_edit -> {

                //edit expense
                editExpense(expenseId)

            }
            R.id.action_delete->{
               //delete expense
            }

        }
        return super.onOptionsItemSelected(item)
    }

    fun editExpense(expenseId:String){
        var ft = supportFragmentManager?.beginTransaction()
        var frag =  AddExpenseFragment()
        var bundle = Bundle()
        bundle.putString("expense_id",expenseId)
        frag.arguments = bundle
        ft?.setCustomAnimations(R.anim.design_bottom_sheet_slide_in,R.anim.design_bottom_sheet_slide_out)
        ft?.add(android.R.id.content, frag,"activity_add_fragment")?.commit()
    }


    //implemented for handling editing of expenses

    override fun onFragmentInteraction(uri: Uri) {

    }

    override fun getcurrentWorkingActivity(): Activities? {
        return null
    }

    override fun closeFragment() {
        var currentFragment: Fragment =  supportFragmentManager.findFragmentById(android.R.id.content)!!
        if(currentFragment is AddExpenseFragment &&currentFragment.isVisible){
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.design_bottom_sheet_slide_in,R.anim.design_bottom_sheet_slide_out)
                    .detach(currentFragment)
                    //  .addToBackStack(currentFragment.javaClass.simpleName)
                    .commit()
        }
    }
}
