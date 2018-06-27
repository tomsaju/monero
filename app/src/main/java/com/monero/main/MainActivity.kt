package com.monero.main

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.widget.FrameLayout
import android.widget.Toast
import com.monero.Application.ApplicationController
import com.monero.R
import com.monero.addActivities.AddActivity
import com.monero.addActivities.AddActivityFragment
import com.monero.main.fragments.AccountBookFragment
import com.monero.main.fragments.Activities.ActivityFragment
import com.monero.main.fragments.NotificationFragment
import com.monero.main.fragments.ProfileFragment
import com.monero.helper.BottomNavigationViewHelper
import com.monero.main.presenter.IMainPresenter
import com.monero.main.presenter.IMainView
import com.monero.main.presenter.MainPresenter
import com.monero.models.Activities
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity(),IMainView, ActivityFragment.ActivityFragmentListener {
    private var content:FrameLayout? = null
    lateinit var  MainPresenter:IMainPresenter
    val TIME_INTERVAL:Long =2000
    var mBackPressed:Long=0
    lateinit var context:Context
    var toolbar:Toolbar?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        Room.databaseBuilder(baseContext.applicationContext, AppDatabase::class.java, "fair-db").build()
        context = baseContext
        content = findViewById<FrameLayout>(R.id.container) as FrameLayout
        val bottomNavigationMenu = findViewById<BottomNavigationMenuView>(R.id.bottomNavigation) as BottomNavigationView
        toolbar = findViewById<Toolbar>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

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

    override fun onBackPressed() {

        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            "Press back button again to exit".shortToast(context)
        }

        mBackPressed = System.currentTimeMillis()
    }

    fun Any.shortToast(context: Context) {
        Toast.makeText(context, this.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun addNewActivity(activity: Activities) {

        var ft = supportFragmentManager.beginTransaction()

        //Basic "newInstance" constructor to avoid omitting necessary variables
        var frag =  AddActivityFragment()

        //Here is where error occurs
        ft.add(android.R.id.content, frag).commit()

      /*  val intent = Intent(this,AddActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)*/
    }

    //var intent: Intent = Intent(context,AddActivity.class)


}




