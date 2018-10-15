package com.monero.activitydetail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import com.monero.R
import com.monero.activitydetail.fragments.AddExpenseFragment
import com.monero.activitydetail.fragments.ExpenseListFragment
import com.monero.activitydetail.fragments.HistoryFragment
import com.monero.activitydetail.fragments.StatsFragment
import com.monero.activitydetail.presenter.detail.DetailPresenter
import com.monero.activitydetail.presenter.detail.IDetailPresenter
import com.monero.activitydetail.presenter.detail.IDetailView
import android.view.WindowManager
import android.os.Build
import android.preference.Preference
import android.support.design.widget.CollapsingToolbarLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.monero.activitydetail.banner.BannerViewPager
import com.monero.models.*


class DetailActivity : AppCompatActivity(),AddExpenseFragment.OnFragmentInteractionListener,IDetailView ,ExpenseListFragment.OnExpenseListFragmentInteractionListener,StatsFragment.StatsFragmentListener {
    val auth = FirebaseAuth.getInstance()!!
    var REQUEST_CODE_PAYER_SELECTION = 3
    var toolbar:Toolbar?=null
    var tabLayout:TabLayout?=null
    private var mViewPager: ViewPager? = null
    private var mSectionsPagerAdapter:DetailViewPagerAdapter?=null
    var activityId:String =""
    private var currentlyWorkingActivity:Activities?=null
    lateinit var mDetailPresenter:IDetailPresenter
    //lateinit var totalCost:TextView
    lateinit var addExpenseFab:FloatingActionButton
    lateinit var collapsingToolbarLayout:CollapsingToolbarLayout
    lateinit var desc_tv:TextView
    lateinit var bannerViewPager:ViewPager
    lateinit var bannerItem:BannerItem
    lateinit var bannerAdapter:BannerViewPager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        toolbar = findViewById<Toolbar>(R.id.toolbar) as Toolbar
        tabLayout = findViewById<TabLayout>(R.id.detailPageTab) as TabLayout
        mViewPager = findViewById<ViewPager?>(R.id.container)
        collapsingToolbarLayout = findViewById(R.id.collapsibletoolbar)
        //totalCost = findViewById(R.id.totalCost)
        collapsingToolbarLayout.setTitleEnabled(true);
        desc_tv = findViewById(R.id.description_text)
        bannerViewPager = findViewById(R.id.banner_viewpager)

        bannerItem = BannerItem("0.00","0.00","0.00","0.00")
        bannerAdapter = BannerViewPager(this,bannerItem)
        bannerViewPager.adapter = bannerAdapter
        addExpenseFab = findViewById(R.id.addexpenseButton)
        mSectionsPagerAdapter = DetailViewPagerAdapter(supportFragmentManager,this)
        mDetailPresenter = DetailPresenter(this,this)
        setSupportActionBar(toolbar)
        activityId = intent.getStringExtra("activityId")
        Log.d("activityId",activityId.toString())
        mDetailPresenter!!.getActivityForId(activityId)
        setupViewPager()

        //change status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor("#CC536DFE")
        }

    }


    private fun setupViewPager() {
        mSectionsPagerAdapter?.addFragment(ExpenseListFragment(), "Expenses")
        mSectionsPagerAdapter?.addFragment(StatsFragment(), "Status")
        mSectionsPagerAdapter?.addFragment(HistoryFragment(), "History")
        mViewPager!!.adapter = mSectionsPagerAdapter
        tabLayout?.setupWithViewPager(mViewPager)
        mViewPager?.addOnPageChangeListener(object:ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
               if(position==0){
                   addExpenseFab.show()
               }else{
                   addExpenseFab.hide()
               }
            }
        })

        addExpenseFab?.setOnClickListener { _:View ->
            //Show the add expense fragment

            (mSectionsPagerAdapter?.getItem(0) as ExpenseListFragment).addNewExpense()
        }

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
        collapsingToolbarLayout.title = activity.title
        desc_tv.text = activity.description
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

    override fun onTotal(total: String) {
      //  totalCost.setText("$"+total)
        bannerItem.total = total
        bannerAdapter.notifyDataSetChanged()
    }

    override fun setBannerData(expenseList: LiveData<List<Expense>>) {
        expenseList?.observe(this, object : Observer<List<Expense>> {
            override fun onChanged(allList: List<Expense>?) {
                calculateBannerValues(allList)
            }

        });
    }

    fun calculateBannerValues(allList: List<Expense>?){
        var debitList = ArrayList<Debit>()
        var creditList = ArrayList<Credit>()
        var sum =0
        var myActualShare=0
        var ispent=0
        var theyowe=0
        var iowe=0
        if(allList!=null){
            for(expense in allList){
                sum+=expense.amount
                debitList.addAll(ArrayList(expense.debitList))
                creditList.addAll(ArrayList(expense.creditList))
            }
            val myId = auth.currentUser?.uid
            if(myId!=null) {
                for (debit in debitList) {
                    if (debit.user_id == myId) {
                        ispent += debit.amount
                    }
                }

                for(credit in creditList){
                    if(credit.user_id == myId){
                        myActualShare+=credit.amount
                    }
                }

               var myRemainingShare = ispent-myActualShare
                if(myRemainingShare==0){
                    theyowe=0
                    iowe=0
                }else if(myRemainingShare>0){
                    theyowe = Math.abs(myRemainingShare)
                    iowe =0
                }else if(myRemainingShare<0){
                    theyowe=0
                    iowe = Math.abs(myRemainingShare)
                }

            }

            bannerItem = BannerItem(sum.toString(),ispent.toString(),iowe.toString(),theyowe.toString())
            bannerAdapter = BannerViewPager(this,bannerItem)
            bannerViewPager.adapter = bannerAdapter

        }

    }
}
