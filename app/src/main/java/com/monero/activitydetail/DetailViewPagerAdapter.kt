package com.monero.activitydetail

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.monero.activitydetail.fragments.AddExpenseFragment
import com.monero.activitydetail.fragments.ExpenseListFragment
import com.monero.activitydetail.fragments.HistoryFragment
import com.monero.activitydetail.fragments.StatsFragment

/**
 * Created by Dreamz on 01-05-2018.
 */
class DetailViewPagerAdapter(fm: FragmentManager?,private val context: Context) : FragmentPagerAdapter(fm) {
    private var mFragmentList:ArrayList<Fragment> = ArrayList()
    private var mFragmentTitleList:ArrayList<String> = ArrayList()



    override fun getItem(position: Int): Fragment {
        var returnFragment:Fragment = ExpenseListFragment.newInstance()
        when(position){
            0 ->{
               returnFragment = mFragmentList[position]
            }
            1->{
               returnFragment = mFragmentList[position]
            }
            2->{
                returnFragment =  mFragmentList[position]
            }
        }
        return returnFragment
    }

    override fun getCount(): Int {
        return mFragmentList.size
         }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return mFragmentTitleList.get(position)
    }

}