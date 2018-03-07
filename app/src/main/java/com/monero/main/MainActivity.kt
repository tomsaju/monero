package com.monero.main

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.widget.FrameLayout
import com.monero.R
import com.monero.main.fragments.AccountBookFragment
import com.monero.main.fragments.ActivityFragment
import com.monero.main.fragments.NotificationFragment
import com.monero.main.fragments.ProfileFragment
import com.monero.helper.BottomNavigationViewHelper
import com.monero.main.presenter.IMainPresenter
import com.monero.main.presenter.IMainView
import com.monero.main.presenter.MainPresenter
import com.monero.models.Activities

class MainActivity : AppCompatActivity(),IMainView,ActivityFragment.ActivityFragmentListener {
    private var content:FrameLayout? = null
    var mActivityFragmentListener: ActivityFragment.ActivityFragmentListener?=null
    lateinit var  MainPresenter:IMainPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        content = findViewById<FrameLayout>(R.id.container) as FrameLayout
        val bottomNavigationMenu = findViewById<BottomNavigationMenuView>(R.id.bottomNavigation) as BottomNavigationView

        MainPresenter = MainPresenter(baseContext,this)

        BottomNavigationViewHelper.removeShiftMode(bottomNavigationMenu)
        bottomNavigationMenu.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val fragment  = ActivityFragment.newInstance()
        addFragment(fragment)
    }


    override fun getAllActivitiesList() {
        //call from fragment
        MainPresenter.getAllActivitiesList()
    }



    private fun addFragment(fragment:Fragment){

        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.design_bottom_sheet_slide_in,R.anim.design_bottom_sheet_slide_out)
                .replace(R.id.container,fragment,"currentFragment")
                .addToBackStack(fragment.javaClass.simpleName)
                .commit()

    }

    override fun onActivitiesFetched(activityList: List<Activities>) {
        var currentFragment:Fragment =  supportFragmentManager.findFragmentByTag("currentFragment")
        if(currentFragment is ActivityFragment){
            currentFragment.onAllActivitiesFetched(activityList)
        }
    }



    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId){
            R.id.action_activities ->{
                var fragment = ActivityFragment.newInstance()
                addFragment(fragment)
                true
            }
            R.id.action_account_book ->{
                var fragment =  AccountBookFragment()
                addFragment(fragment)
                true
            }
            R.id.action_notification ->{
                var fragment = NotificationFragment()
                addFragment(fragment)
                true
            }
            R.id.action_profile-> {
                var fragment = ProfileFragment()
                addFragment(fragment)
                true
            }

            else -> {
            true
            }
        }

    }
}




