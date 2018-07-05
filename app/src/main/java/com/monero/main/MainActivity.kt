package com.monero.main

import android.arch.core.util.Function
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
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
import com.monero.Dao.ActivitiesDao
import com.monero.R
import com.monero.addActivities.AddActivityFragment
import com.monero.helper.AppDatabase
import com.monero.main.fragments.AccountBookFragment
import com.monero.main.fragments.Activities.ActivityFragment
import com.monero.main.fragments.NotificationFragment
import com.monero.main.fragments.ProfileFragment
import com.monero.helper.BottomNavigationViewHelper
import com.monero.main.presenter.IMainPresenter
import com.monero.main.presenter.IMainView
import com.monero.main.presenter.MainPresenter
import com.monero.models.Activities

class MainActivity : AppCompatActivity(),IMainView, ActivityFragment.ActivityFragmentListener,AddActivityFragment.IAddActivityFragmentListener {
    private var content:FrameLayout? = null
    lateinit var mMainPresenter:IMainPresenter
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

        mMainPresenter = MainPresenter(baseContext,this)

        BottomNavigationViewHelper.removeShiftMode(bottomNavigationMenu)
        bottomNavigationMenu.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val fragment  = ActivityFragment.newInstance()
        addFragment(fragment)



    }


    override fun getAllActivitiesList() {
        //call from fragment
        mMainPresenter.getAllActivitiesList()
    }


    override fun saveActivity(activity: Activities) {
        mMainPresenter.saveActivity(activity)
        supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentByTag("activity_add_fragment")).commit()

        var currentFragment:Fragment =  supportFragmentManager.findFragmentByTag("currentFragment")
        if(currentFragment is ActivityFragment){
            currentFragment.refreshList()
        }
    }

    override fun getActivity(id: String): Activities {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun addFragment(fragment:Fragment){

        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.design_bottom_sheet_slide_in,R.anim.design_bottom_sheet_slide_out)
                .replace(R.id.container,fragment,"currentFragment")
                .addToBackStack(fragment.javaClass.simpleName)
                .commit()

    }

    override fun onActivitiesFetched(activityList: LiveData<List<Activities>>?) {
        var currentFragment:Fragment =  supportFragmentManager.findFragmentByTag("currentFragment")

                activityList?.observe(this, object : Observer<List<Activities>> {
                    override fun onChanged(allList: List<Activities>?) {

                        if(currentFragment is ActivityFragment){
                            currentFragment.onAllActivitiesFetched(allList)
                        }

                    }

                });





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

        var currentFragment:Fragment =  supportFragmentManager.findFragmentByTag("activity_add_fragment")
        if(currentFragment is AddActivityFragment&&currentFragment.isVisible){
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.design_bottom_sheet_slide_in,R.anim.design_bottom_sheet_slide_out)
                    .detach(currentFragment)
                    .addToBackStack(currentFragment.javaClass.simpleName)
                    .commit()
        }else {

            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed()
                finish()
            } else {
                "Press back button again to exit".shortToast(context)
                mBackPressed = System.currentTimeMillis()
            }

        }


    }

    fun Any.shortToast(context: Context) {
        Toast.makeText(context, this.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun addNewActivity(activity: Activities) {

        var ft = supportFragmentManager.beginTransaction()
        var frag =  AddActivityFragment()
        ft.add(android.R.id.content, frag,"activity_add_fragment").commit()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }


}




