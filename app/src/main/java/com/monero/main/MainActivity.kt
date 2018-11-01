package com.monero.main

import android.Manifest
import android.app.job.JobService
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.CoordinatorLayout.Behavior.setTag
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Job
import com.google.firebase.FirebaseApp
import com.monero.R
import com.monero.addActivities.AddActivityFragment
import com.monero.addActivities.fragments.SelectContactsFragment
import com.monero.auth.SignInActivity
import com.monero.main.fragments.AccountBookFragment
import com.monero.main.fragments.Activities.ActivityFragment
import com.monero.main.fragments.NotificationFragment
import com.monero.main.fragments.ProfileFragment
import com.monero.helper.BottomNavigationViewHelper
import com.monero.main.presenter.main.IMainPresenter
import com.monero.main.presenter.main.IMainView
import com.monero.main.presenter.main.MainPresenter
import com.monero.models.Activities
import com.monero.models.ContactMinimal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.monero.Application.ApplicationController
import com.monero.models.Contact
import com.monero.models.User
import com.monero.network.RestService
import com.monero.service.ContactsSyncService
import com.monero.signup.SignUpActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper




class MainActivity : AppCompatActivity(), IMainView, ActivityFragment.ActivityFragmentListener,AddActivityFragment.IAddActivityFragmentListener, SelectContactsFragment.OnCotactSelectedListener {


    private var content:FrameLayout? = null
    lateinit var mMainPresenter: IMainPresenter
    val TIME_INTERVAL:Long =2000
    var mBackPressed:Long=0
    lateinit var context:Context
    var toolbar:Toolbar?=null
    lateinit var selectContactsFragment:SelectContactsFragment
    private val READ_CONTACTS_REQUEST_CODE: Int = 3
    lateinit var currentActivityContactList:ArrayList<User>
    lateinit var auth:FirebaseAuth
    lateinit var activityFragment:ActivityFragment
    lateinit var accountBookFragment:AccountBookFragment
    lateinit var notificationFragment:NotificationFragment
    lateinit var profileFragment:ProfileFragment
    lateinit var editMenuItem:MenuItem
    lateinit var deleteMenuItem:MenuItem

    val RestAPIService by lazy {
        RestService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(applicationContext)
        val firestore = FirebaseFirestore.getInstance()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/RobotoCondensed-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build())


        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore.firestoreSettings = settings
         auth = FirebaseAuth.getInstance()!!

//        Room.databaseBuilder(baseContext.applicationContext, AppDatabase::class.java, "fair-db").build()
        val user = FirebaseAuth.getInstance().currentUser

        context = baseContext
        content = findViewById<FrameLayout>(R.id.container) as FrameLayout
        val bottomNavigationMenu = findViewById<BottomNavigationMenuView>(R.id.bottomNavigation) as BottomNavigationView
        toolbar = findViewById<Toolbar>(R.id.toolbar) as Toolbar

        setSupportActionBar(toolbar)

        mMainPresenter = MainPresenter(baseContext, this)

        BottomNavigationViewHelper.removeShiftMode(bottomNavigationMenu)
        bottomNavigationMenu.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        //init fragment
        activityFragment = ActivityFragment()
        accountBookFragment = AccountBookFragment()
        profileFragment = ProfileFragment()
        notificationFragment = NotificationFragment()

        addFragment(activityFragment)

        /*var dispatcher =  FirebaseJobDispatcher(GooglePlayDriver(context));
        if(dispatcher!=null) {
            var myJob = dispatcher ?. newJobBuilder ()
                    .setService() // the JobService that will be called
                            .setTag("my-unique-tag")        // uniquely identifies the job
                            .build()
            dispatcher.mustSchedule (myJob);
        }*/
    }


    override fun getAllActivitiesList() {
        //call from fragment
        mMainPresenter.getAllActivitiesList()
    }


    override fun saveActivity(activity: Activities) {
        mMainPresenter.saveActivity(activity)
        supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentByTag("activity_add_fragment")).commit()

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity_menu, menu)

        editMenuItem = menu!!.findItem(R.id.action_edit)
        deleteMenuItem = menu.findItem(R.id.action_delete)

        editMenuItem.isVisible = false
        deleteMenuItem.isVisible = false

        return super.onCreateOptionsMenu(menu)
    }


    override fun toggleEditIcon(show: Boolean) {
       editMenuItem.isVisible = show
    }

    override fun toggleDeleteIcon(show: Boolean) {
        deleteMenuItem.isVisible = show
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_sync -> {
                mMainPresenter.getAllActivitiesFromServer();
            }
            R.id.action_sign_out->{
                signout();
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun signout() {
        FirebaseAuth.getInstance().signOut();
    }


    override fun onActivitiesFetched(activityList: LiveData<List<Activities>>?) {
      //  hideProgressBar()
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
                if(activityFragment==null){
                    activityFragment = ActivityFragment()
                }
                addFragment(activityFragment)
                true
            }
            R.id.action_account_book ->{
                if(accountBookFragment==null){
                    accountBookFragment = AccountBookFragment()
                }
                addFragment(accountBookFragment)
                true
            }
            R.id.action_notification ->{
                if(notificationFragment==null){
                    notificationFragment = NotificationFragment()
                }
                addFragment(notificationFragment)
                true
            }
            R.id.action_profile-> {
                if(profileFragment==null){
                    profileFragment = ProfileFragment()
                }
                addFragment(profileFragment)
                true
            }

            else -> {
            true
            }
        }

    }

    override fun onBackPressed() {

        val count = fragmentManager.backStackEntryCount

        if (count == 0) {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed()
                finish()
            } else {
                "Press back button again to exit".shortToast(context)
                mBackPressed = System.currentTimeMillis()

            }

        }else {
            getFragmentManager().popBackStack();
        }


    }

    fun Any.shortToast(context: Context) {
        Toast.makeText(context, this.toString(), Toast.LENGTH_SHORT).show()
    }




    override fun addNewActivity() {

        var ft = supportFragmentManager.beginTransaction()
        var frag =  AddActivityFragment()
        ft.setCustomAnimations(R.anim.design_bottom_sheet_slide_in,R.anim.design_bottom_sheet_slide_out)
        ft.add(android.R.id.content, frag,"activity_add_fragment").commit()

    }

    override fun cancelAddActivityFragment(){
        var currentFragment: Fragment = supportFragmentManager.findFragmentByTag("activity_add_fragment")

        if (currentFragment is AddActivityFragment && currentFragment.isVisible) {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.design_bottom_sheet_slide_in, R.anim.design_bottom_sheet_slide_out)
                    .detach(currentFragment)
                    .addToBackStack(currentFragment.javaClass.simpleName)
                    .commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun showAddContactsPage() {

        var ft = supportFragmentManager.beginTransaction()
        var frag =  SelectContactsFragment()
        ft.setCustomAnimations(R.anim.design_bottom_sheet_slide_in,R.anim.design_bottom_sheet_slide_out)
        ft.add(android.R.id.content, frag,"select_contacts").commit()


    }

    override fun hideAddContactsPage() {
    //   selectContactsFragment.dismiss()
    }

    override fun onContactSelected(contactList: MutableList<ContactMinimal>?) {
        //show contacts in add page
        contactList?.let {
            for(contact in contactList){
                var currentFragment:Fragment =  supportFragmentManager.findFragmentByTag("activity_add_fragment")
                if(currentFragment is AddActivityFragment&&currentFragment.isVisible){

                    currentFragment.setSelectedContacts(contactList)
                   // currentActivityContactList.add(User())
                }
            }
        }

    }

    override fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)

        if (permission != PackageManager.PERMISSION_GRANTED) {
           //permission not granted
            makeRequest()
        }else{
            var currentFragment:Fragment =  supportFragmentManager.findFragmentByTag("activity_add_fragment")
            if(currentFragment is AddActivityFragment&&currentFragment.isVisible){

                currentFragment.onContactPermissionGranted()
            }
        }
    }




    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                READ_CONTACTS_REQUEST_CODE)
    }



     override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            READ_CONTACTS_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                   // "Permission has been denied by user")

                } else {
                    var currentFragment:Fragment =  supportFragmentManager.findFragmentByTag("activity_add_fragment")
                    if(currentFragment is AddActivityFragment&&currentFragment.isVisible){

                        currentFragment.onContactPermissionGranted()
                    }
                }
            }
        }
    }

    override fun getAllContactList() {

    }

    override fun closeContactSelectFragment() {
        supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentByTag("select_contacts")).commit()
    }



    override fun onResume() {
        super.onResume()
        if(auth.currentUser==null){
            ApplicationController.preferenceManager!!.myCredential =""
            //go to login page
            var loginIntent = Intent(this,SignUpActivity::class.java)
            startActivity(loginIntent)
            finish()
        }else{

            ApplicationController.preferenceManager!!.myCredential = auth.currentUser!!.phoneNumber!!
            ApplicationController.preferenceManager!!.myUid = auth.currentUser!!.uid
          // Log.d("url full",RestAPIService.getRegisteredContactForNumber("+919048576020").request().url().toString())
         //  Log.d("url body",RestAPIService.getRegisteredContactForNumber("+919048576020").request().body().toString())

                  /*  .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                result ->
                                showResult(result)
                            },
                            { error ->
                                showError(error.message) }
                    )*/


        }
    }


    private fun showError(message: String?) {
        //do nothing
        //set sync status false
        Log.d("tag",message)
    }

    private fun showResult(result: String) {
        //if contact not null, search that number and get details
        Log.d("result",result);
    }

    override fun setCurrentActivityUserList(userList: java.util.ArrayList<User>) {
        this.currentActivityContactList = userList
    }

    override fun getCurrentActivityUserList(): java.util.ArrayList<User> {
        return this.currentActivityContactList
    }


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun showLoader() {
        var currentFragment:Fragment =  supportFragmentManager.findFragmentByTag("currentFragment")
        if(currentFragment is ActivityFragment&&currentFragment.isVisible){

            currentFragment.showProgressBar()
        }
    }

    override fun hideLoader() {
        var currentFragment:Fragment =  supportFragmentManager.findFragmentByTag("currentFragment")
        if(currentFragment is ActivityFragment&&currentFragment.isVisible){

            currentFragment.hideProgressBar()
        }
    }

    fun clearSelectedActivitiesList(){
        var currentFragment:Fragment =  supportFragmentManager.findFragmentByTag("currentFragment")
        if(currentFragment is ActivityFragment&&currentFragment.isVisible){

            currentFragment.adapter?.selectedActivitieslist?.clear()
        }
    }

    override fun syncContactsWithServer(contactList: ArrayList<Contact>) {
        if(contactList!=null){
            mMainPresenter.syncContactsWithServer(contactList)
        }
    }

    override fun editActivity(id: String) {
        var ft = supportFragmentManager.beginTransaction()
        var frag =  AddActivityFragment()
        val args = Bundle()
        args.putString("activity_id", id)
        frag.arguments = args
        ft.setCustomAnimations(R.anim.design_bottom_sheet_slide_in,R.anim.design_bottom_sheet_slide_out)
        ft.add(android.R.id.content, frag,"activity_add_fragment").commit()
    }
}




