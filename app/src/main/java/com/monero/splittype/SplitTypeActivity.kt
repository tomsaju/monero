package com.monero.splittype

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
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
    var SPLIT_TYPE = SPLIT_TYPE_PERCENTAGE
    lateinit var adapter:SplitTypeRecyclerAdapter
    lateinit var mPresenter:ISplitTypePresenter
    var activityId:String = ""
    var selecteduserList: ArrayList<User> = ArrayList()
    var totalAmount:Int = 0
    var splitList:ArrayList<SplitItem> = ArrayList()

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
        }else if(SPLIT_TYPE==SPLIT_TYPE_PERCENTAGE){
            split_radio_percentage.isChecked = true
        }else{
           split_radio_amount.isChecked = true
        }

        mPresenter = SplitTypePresenter(this,this)
        mPresenter.getAllUsersForActivity(activityId)

        add_user_split_btn.setOnClickListener{
            showAddDialog()
        }



    }

    private fun showAddDialog() {
        var selectedUserId =""
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.add_split_item_dialog_layout, null)
        val minimalContactList = ArrayList<ContactMinimal>()
        for(user in selecteduserList){

            minimalContactList.add(ContactMinimal(user.user_id,user.user_name,user.user_phone))
        }
        var sortedList = minimalContactList.sortedWith(compareBy({ it.name }))

        val contactsAdapter: ContactListAdapter = ContactListAdapter(this,sortedList,object:IContactSelectedListener{
            override fun onContactSelected(contact: ContactMinimal) {
                
                mDialogView.user_name_autocomplete_tv.setText(contact.name)
                selectedUserId = contact.contact_id
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

            adapter = SplitTypeRecyclerAdapter(splitList,this,SPLIT_TYPE)
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

            adapter = SplitTypeRecyclerAdapter(splitList,this,SPLIT_TYPE)
            split_members_list.adapter = adapter
        }
    }


    override fun onAllusersFetched(userList: ArrayList<User>) {
        selecteduserList = userList
      //  setList()
    }

    override fun onContactSelected(contact: ContactMinimal) {

    }
}
