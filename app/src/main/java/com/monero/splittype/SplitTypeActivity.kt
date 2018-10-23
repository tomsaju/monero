package com.monero.splittype

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import kotlinx.android.synthetic.main.activity_split_type.*
import com.monero.R
import com.monero.addActivities.adapter.ContactListAdapter
import com.monero.addActivities.adapter.IContactSelectedListener
import com.monero.models.ContactMinimal
import com.monero.models.SplitItem
import com.monero.models.User
import com.monero.splittype.presenter.ISplitTypePresenter
import com.monero.splittype.presenter.ISplitTypeView
import com.monero.splittype.presenter.SplitTypePresenter
import kotlinx.android.synthetic.main.add_split_item_dialog_layout.view.*


class SplitTypeActivity : AppCompatActivity(),ISplitTypeView,IContactSelectedListener {

    var SPLIT_TYPE_PERCENTAGE = 1
    var SPLIT_TYPE_MONEY = 2
    var SPLIT_TYPE_EQUALLY = 2
    var SPLIT_TYPE = SPLIT_TYPE_EQUALLY
    lateinit var adapter:SplitTypeRecyclerAdapter
    lateinit var mPresenter:ISplitTypePresenter
    var activityId:String = ""
    var selecteduserList: ArrayList<User> = ArrayList()
    var totalAmount:Int = 0
    var splitList:ArrayList<SplitItem> = ArrayList()
    var selectedUsersId:ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_type)

        split_members_list.layoutManager = LinearLayoutManager(this)

        if(intent.extras!=null){
            activityId = intent.getStringExtra("activityId")
            totalAmount = intent.getIntExtra("total",0)
        }

        if(SPLIT_TYPE==SPLIT_TYPE_EQUALLY){
            split_radio_equally.isChecked = true
            //load the equal split arraylist
            //diabel the add user button
            add_user_split_btn.visibility = View.GONE
            selectedUsersId.clear()

        }else if(SPLIT_TYPE==SPLIT_TYPE_PERCENTAGE){
            split_radio_percentage.isChecked = true
            add_user_split_btn.visibility = View.VISIBLE
            selectedUsersId.clear()
        }else{
           split_radio_amount.isChecked = true
            add_user_split_btn.visibility = View.VISIBLE
            selectedUsersId.clear()
        }

        mPresenter = SplitTypePresenter(this,this)
        mPresenter.getAllUsersForActivity(activityId)

        add_user_split_btn.setOnClickListener{
            showAddDialog()
        }

        split_type_radiogroup.setOnCheckedChangeListener(
                RadioGroup.OnCheckedChangeListener { group, checkedId ->
                    val radio: RadioButton = findViewById(checkedId)
                    if(radio.text=="Equally") {
                    add_user_split_btn.visibility = View.GONE
                        showEqualList()
                    }else{
                        add_user_split_btn.visibility = View.VISIBLE
                        hideEqualList()
                    }
    })




    }

    private fun hideEqualList() {
        splitList.clear()
        adapter.notifyDataSetChanged()
    }


    private fun showEqualList(){
        for(user in selecteduserList){

            var splitItem = SplitItem(((totalAmount)/selecteduserList.size),0.0,user)
            splitList.add(splitItem)
                // setList()
        }

        adapter = SplitTypeRecyclerAdapter(splitList,this,SPLIT_TYPE,totalAmount)
        split_members_list.adapter = adapter
    }

    private fun showAddDialog() {
        var selectedUserId =""
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.add_split_item_dialog_layout, null)
        val minimalContactList = ArrayList<ContactMinimal>()
        for(user in selecteduserList){
            if(!selectedUsersId.contains(user.user_id)) {
                minimalContactList.add(ContactMinimal(user.user_id, user.user_name, user.user_phone))
            }
        }
        var sortedList = minimalContactList.sortedWith(compareBy({ it.name }))

        val contactsAdapter: ContactListAdapter = ContactListAdapter(this,sortedList,object:IContactSelectedListener{
            override fun onContactSelected(contact: ContactMinimal) {
                
                mDialogView.user_name_autocomplete_tv.setText(contact.name)
                selectedUserId = contact.contact_id
                selectedUsersId.add(contact.contact_id)
            }

        })




        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Add a split")
        //show dialog
        mDialogView.user_name_autocomplete_tv.setAdapter(contactsAdapter)
        mDialogView.user_name_autocomplete_tv.threshold =1

        val  mAlertDialog = mBuilder.show()
        //login button click of custom layout
        mDialogView.add_btn.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
            //get text from EditTexts of custom layout



            val name = mDialogView.user_name_autocomplete_tv.text.toString()
            val amount = mDialogView.amount_edittext_add_split.text.toString()

            for(user in selecteduserList){
                if(user.user_id ==selectedUserId){
                    var splitItem = SplitItem((amount.toDouble()*100).toInt(),0.0,user)
                    splitList.add(splitItem)
                   // setList()
                }
            }

            adapter = SplitTypeRecyclerAdapter(splitList,this,SPLIT_TYPE,totalAmount)
            split_members_list.adapter = adapter




        }
        //cancel button click of custom layout
        mDialogView.cancel_btn.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }
    }

    private fun setList() {
        if(selecteduserList!=null){
            var splitList:ArrayList<SplitItem> = ArrayList()
            for(user in selecteduserList){

                var item = SplitItem(totalAmount/selecteduserList.size,(100/selecteduserList.size).toDouble(),user)
                splitList.add(item)
            }

            adapter = SplitTypeRecyclerAdapter(splitList,this,SPLIT_TYPE,totalAmount)
            split_members_list.adapter = adapter
        }
    }


    override fun onAllusersFetched(userList: ArrayList<User>) {
        selecteduserList = userList
        if(SPLIT_TYPE==SPLIT_TYPE_EQUALLY){
            showEqualList()
        }
      //  setList()
    }

    override fun onContactSelected(contact: ContactMinimal) {

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.split_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId==R.id.split_done){
            doneSplitting()

        }
        return true
    }

    private fun doneSplitting() {

    }
}
