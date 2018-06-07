package com.monero.activitydetail

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.widget.FrameLayout
import com.monero.R
import com.monero.activitydetail.fragments.AddExpenseFragment
import com.monero.activitydetail.fragments.ExpenseListFragment

class DetailActivity : AppCompatActivity(),ExpenseListFragment.OnFragmentInteractionListener,AddExpenseFragment.OnFragmentInteractionListener  {
    var toolbar:Toolbar?=null
    var tabLayout:TabLayout?=null
    var fragmentContainer:FrameLayout?=null
    private var mViewPager: ViewPager? = null
    private var mSectionsPagerAdapter:DetailViewPagerAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        toolbar = findViewById<Toolbar>(R.id.my_toolbar) as Toolbar
        tabLayout = findViewById<TabLayout>(R.id.detailPageTab) as TabLayout
        mViewPager = findViewById<ViewPager?>(R.id.container)
        fragmentContainer = findViewById<FrameLayout>(R.id.container_fragment) as FrameLayout
        mSectionsPagerAdapter = DetailViewPagerAdapter(supportFragmentManager,this)

        setSupportActionBar(toolbar)
        setupViewPager()
    }

    private fun setupViewPager() {
        mSectionsPagerAdapter?.addFragment(ExpenseListFragment(), "Expenses")
        mSectionsPagerAdapter?.addFragment(ExpenseListFragment(), "Statistics")
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
            add(R.id.container_fragment,AddExpenseFragment())
        }
    }

    override fun onFragmentInteraction(uri: Uri) {
        showAddExpenseFragment()
    }
}
