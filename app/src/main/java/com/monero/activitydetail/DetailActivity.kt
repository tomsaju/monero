package com.monero.activitydetail

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import com.monero.R
import com.monero.activitydetail.fragments.ExpenseListFragment

class DetailActivity : AppCompatActivity()  {
    var toolbar:Toolbar?=null
    var tabLayout:TabLayout?=null

    private var mViewPager: ViewPager? = null
    private var mSectionsPagerAdapter:DetailViewPagerAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        toolbar = findViewById<Toolbar>(R.id.my_toolbar) as Toolbar
        tabLayout = findViewById<TabLayout>(R.id.detailPageTab) as TabLayout
        mViewPager = findViewById<ViewPager?>(R.id.container)

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
}
