package com.monero.expensedetail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.monero.R
import com.monero.activitydetail.DetailViewPagerAdapter
import com.monero.activitydetail.fragments.adapter.ExpenseListRecyclerAdapter
import com.monero.expensedetail.presenter.ExpenseDetailPresenter
import com.monero.expensedetail.presenter.IExpenseDetailPresenter
import com.monero.expensedetail.presenter.IExpenseDetailView
import com.monero.models.Expense
import com.monero.models.Payment
import com.monero.models.User
import kotlinx.android.synthetic.main.activity_expense.*

class ExpenseActivity : AppCompatActivity(),IExpenseDetailView {
var expenseId:String =""
lateinit var mPresenter:IExpenseDetailPresenter
lateinit var mRecyclerView:RecyclerView
lateinit var mAdapter:PayerRecyclerAdapter
lateinit var toolbar:Toolbar




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)
        toolbar = findViewById(R.id.toolbar_custom)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);
        getSupportActionBar()?.setTitle("")
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
                expense_Amount_detail_page.text = amountInHigherDenomination
                var paymentList:ArrayList<Payment> = ArrayList()
                if(thisExpense?.debitList!=null) {
                    for (debit in thisExpense.debitList) {
                        paymentList.add(Payment(debit.amount, User(debit.user_id,debit.userName,"unknown","unknown")))
                    }
                }
                mAdapter = PayerRecyclerAdapter(paymentList)
                detailPayRecyler.layoutManager = LinearLayoutManager(this@ExpenseActivity)
                detailPayRecyler.adapter = mAdapter
            }

        });



    }
}
