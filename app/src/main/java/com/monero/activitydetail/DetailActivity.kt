package com.monero.activitydetail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.util.Log
import com.monero.R
import com.monero.activitydetail.fragments.AddExpenseFragment
import com.monero.activitydetail.fragments.ExpenseListFragment
import com.monero.activitydetail.fragments.adapter.ExpenseListAdapter
import com.monero.activitydetail.presenter.detail.DetailPresenter
import com.monero.activitydetail.presenter.detail.IDetailPresenter
import com.monero.activitydetail.presenter.detail.IDetailView
import com.monero.models.Activities
import com.monero.models.Expense

class DetailActivity : AppCompatActivity(),AddExpenseFragment.OnFragmentInteractionListener,IDetailView ,ExpenseListFragment.OnExpenseListFragmentInteractionListener {

    var REQUEST_CODE_PAYER_SELECTION = 3
    var toolbar:Toolbar?=null
    var tabLayout:TabLayout?=null
    private var mViewPager: ViewPager? = null
    private var mSectionsPagerAdapter:DetailViewPagerAdapter?=null
    var activityId:Long =0
    private var currentlyWorkingActivity:Activities?=null
    lateinit var mDetailPresenter:IDetailPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        toolbar = findViewById<Toolbar>(R.id.toolbar) as Toolbar
        tabLayout = findViewById<TabLayout>(R.id.detailPageTab) as TabLayout
        mViewPager = findViewById<ViewPager?>(R.id.container)
        mSectionsPagerAdapter = DetailViewPagerAdapter(supportFragmentManager,this)
        mDetailPresenter = DetailPresenter(this,this)
        setSupportActionBar(toolbar)
        activityId = intent.getLongExtra("activityId",0)
        Log.d("activityId",activityId.toString())
        mDetailPresenter.getActivityForId(activityId)
        setupViewPager()


    }


    private fun setupViewPager() {
        mSectionsPagerAdapter?.addFragment(ExpenseListFragment(), "Expenses")
        mSectionsPagerAdapter?.addFragment(ExpenseListFragment(), "Status")
        mSectionsPagerAdapter?.addFragment(ExpenseListFragment(), "History")
        mViewPager!!.adapter = mSectionsPagerAdapter
        tabLayout?.setupWithViewPager(mViewPager)

    }

    inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.slide_from_bottom,0)
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }

    fun showAddExpenseFragment(){
        supportFragmentManager.inTransaction {

            add(android.R.id.content,AddExpenseFragment())
        }
    }

    override fun onFragmentInteraction(uri: Uri) {
        showAddExpenseFragment()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    override fun getcurrentWorkingActivity(): Activities? {

        return currentlyWorkingActivity
    }

    override fun onActivityFetched(activity: Activities) {
        Log.d("detailActivity","currently "+activity?.title)
        currentlyWorkingActivity = activity

    }



    override fun closeFragment() {
        var currentFragment: Fragment =  supportFragmentManager.findFragmentById(android.R.id.content)
        if(currentFragment is AddExpenseFragment &&currentFragment.isVisible){
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.design_bottom_sheet_slide_in,R.anim.design_bottom_sheet_slide_out)
                    .detach(currentFragment)
                  //  .addToBackStack(currentFragment.javaClass.simpleName)
                    .commit()
        }
    }


}
