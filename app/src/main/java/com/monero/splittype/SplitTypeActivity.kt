package com.monero.splittype

import android.app.Activity
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
import android.widget.Toast
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
import kotlinx.android.synthetic.main.user_selection_list_item_layout.view.*


class SplitTypeActivity : AppCompatActivity(),ISplitTypeView,IContactSelectedListener {


    override fun onSingleContactSelected(contactMinimal: ContactMinimal) {

    }

    var SPLIT_TYPE_PERCENTAGE = 1
    var SPLIT_TYPE_MONEY = 2
    var SPLIT_TYPE_EQUALLY = 0
    var SPLIT_TYPE = SPLIT_TYPE_EQUALLY
    lateinit var adapter:SplitTypeRecyclerAdapter
    lateinit var mPresenter:ISplitTypePresenter
    var activityId:String = ""
    var selecteduserList: ArrayList<User> = ArrayList()
    var totalAmount:Int = 0
    var splitList:ArrayList<SplitItem> = ArrayList()
    var selectedUsersId:ArrayList<String> = ArrayList()
    var splitPaymentList:HashMap<User,Int>  = HashMap()//<each user,amount owed>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_type)

        split_members_list.layoutManager = LinearLayoutManager(this)

        if(intent.extras!=null){
            activityId = intent.getStringExtra("activityId")
            totalAmount = intent.getIntExtra("total",0)
            try {
                splitPaymentList= intent.getSerializableExtra("owedList") as HashMap<User, Int>
                SPLIT_TYPE = intent.getIntExtra("splitType",0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if(SPLIT_TYPE==SPLIT_TYPE_EQUALLY){
            split_radio_equally.isChecked = true
            //load the equal split arraylist
            //disable the add user button
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
                        SPLIT_TYPE = SPLIT_TYPE_EQUALLY
                        add_user_split_btn.visibility = View.GONE
                        showEqualList()
                    }else if(radio.text=="Percentage"){
                        SPLIT_TYPE = SPLIT_TYPE_PERCENTAGE
                        add_user_split_btn.visibility = View.VISIBLE
                        hideEqualList()
                    }else if(radio.text=="Amount"){
                        SPLIT_TYPE = SPLIT_TYPE_MONEY
                        add_user_split_btn.visibility = View.VISIBLE
                        hideEqualList()
                    }
    })


        if(splitPaymentList!=null&&splitPaymentList.isNotEmpty()){
            splitList.clear()
            for ((key, value) in splitPaymentList) {

                if(SPLIT_TYPE==SPLIT_TYPE_EQUALLY){


                }else if(SPLIT_TYPE==SPLIT_TYPE_PERCENTAGE){
                    var percentValue =(100* value)/totalAmount
                    var splitItem = SplitItem(value,percentValue.toDouble(),key)
                    splitList.add(splitItem)
                }else{
                    var splitItem = SplitItem(value,0.0,key)
                    splitList.add(splitItem)
                }

            }

            adapter = SplitTypeRecyclerAdapter(splitList,this,SPLIT_TYPE,totalAmount)
            split_members_list.adapter = adapter
        }

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
        var remaining=0.0
        var currentSum=0.0
        if(splitList!=null&&!splitList.isEmpty()){
            for (item in splitList) {
                if (SPLIT_TYPE == SPLIT_TYPE_MONEY) {
                    currentSum += item.amount
                } else if (SPLIT_TYPE == SPLIT_TYPE_PERCENTAGE) {
                    currentSum +=item.percentage
                }
            }

            if (SPLIT_TYPE == SPLIT_TYPE_MONEY) {
                remaining = totalAmount-currentSum
            } else if (SPLIT_TYPE == SPLIT_TYPE_PERCENTAGE) {
                remaining = 100-currentSum
            }

        }else{
            if (SPLIT_TYPE == SPLIT_TYPE_MONEY) {
                remaining = totalAmount.toDouble()
            } else if (SPLIT_TYPE == SPLIT_TYPE_PERCENTAGE) {
                remaining = 100.0
            }
        }

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.add_split_item_dialog_layout, null)
        val minimalContactList = ArrayList<ContactMinimal>()
        for(user in selecteduserList){
            if(!selectedUsersId.contains(user.user_id)) {
                minimalContactList.add(ContactMinimal(user.user_id, user.user_name, user.user_phone,user.user_email))
            }
        }
        var sortedList = minimalContactList.sortedWith(compareBy({ it.name }))

        val contactsAdapter: ContactListAdapter = ContactListAdapter(this,sortedList,object:IContactSelectedListener{
            override fun onContactSelected(contactList: ArrayList<ContactMinimal>) {
                if(contactList.isNotEmpty()) {
                    var contact = contactList[0]
                    mDialogView.user_name_autocomplete_tv.setText(contact.name)
                    selectedUserId = contact.contact_id
                    selectedUsersId.add(contact.contact_id)
                }
            }

            override fun onSingleContactSelected(contactMinimal: ContactMinimal) {
                mDialogView.user_name_autocomplete_tv.setText(contactMinimal.name)
                selectedUserId = contactMinimal.contact_id
                selectedUsersId.add(contactMinimal.contact_id)
            }
        })




        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Add a split")
        //show dialog
        mDialogView.user_name_autocomplete_tv.setAdapter(contactsAdapter)
        mDialogView.user_name_autocomplete_tv.threshold =1
        if (SPLIT_TYPE == SPLIT_TYPE_MONEY) {
            var amountInHigherDenomination = "%.2f".format((remaining/100))
            mDialogView.remaining_indicator_tv.text = "Remaining:"+"$"+amountInHigherDenomination
        } else if (SPLIT_TYPE == SPLIT_TYPE_PERCENTAGE) {
            mDialogView.remaining_indicator_tv.text = "Remaining:"+remaining+"%"
        }

        mDialogView.remaining_indicator_tv.setOnClickListener {
            mDialogView.amount_edittext_add_split.setText(remaining.toString())
        }

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
                    if (SPLIT_TYPE == SPLIT_TYPE_MONEY) {
                        var splitItem = SplitItem((amount.toDouble()*100).toInt(),0.0,user)
                        splitList.add(splitItem)
                    } else if (SPLIT_TYPE == SPLIT_TYPE_PERCENTAGE) {
                        var amountOfPercentage = (totalAmount*amount.toDouble())/100
                        var splitItem = SplitItem(amountOfPercentage.toInt(),amount.toDouble(),user)
                        splitList.add(splitItem)
                    }

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


    override fun onContactSelected(contactList: ArrayList<ContactMinimal>) {

    }

    override fun onAllusersFetched(userList: ArrayList<User>) {
        selecteduserList = userList
        if(SPLIT_TYPE==SPLIT_TYPE_EQUALLY){
            showEqualList()
        }
      //  setList()
    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.split_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId==R.id.split_done){
            doSplitting()

        }
        return true
    }

    private fun doSplitting() {

        splitList = adapter.getCurrentList()

        splitPaymentList = HashMap()
        if(SPLIT_TYPE==SPLIT_TYPE_PERCENTAGE){
            for (item in splitList) {
                var amountOwed = (totalAmount*item.percentage)/100
                splitPaymentList.put(item.user, amountOwed.toInt())
            }

        }else {
            for (item in splitList) {
                splitPaymentList.put(item.user, item.amount)
            }
        }

        if(valuesAddUp()) {
            intent.putExtra("owedList", splitPaymentList)
            intent.putExtra("splitType",SPLIT_TYPE)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun valuesAddUp(): Boolean {
        var sum=0
        if(SPLIT_TYPE==SPLIT_TYPE_PERCENTAGE){
            for (item in splitList) {
                var amountOwed = (totalAmount*item.percentage)/100
                sum+=amountOwed.toInt()
            }

        }else {
            for (item in splitList) {
                sum+=item.amount
            }
        }

        if(sum>=totalAmount-1){
            return true
        }else{
            Toast.makeText(this,"Values remaining",Toast.LENGTH_SHORT).show()
            return false
        }
    }


}
