package com.monero.payeeSelector

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.widget.*

import com.monero.R
import com.monero.models.User
import com.monero.payeeSelector.fragment.SelectPayerFragment
import com.monero.payeeSelector.presenter.IPayerSelectorPresenter
import com.monero.payeeSelector.presenter.IPayerSelectorView
import com.monero.payeeSelector.presenter.PayerSelectorPresenter
import com.monero.utility.Utility

class PayerSelectorActivity : AppCompatActivity(),SelectPayerFragment.SelectPayerFragmentInteractionListener,IPayerSelectorView {

    var REQUEST_CODE_PAYER_SELECTION = 3
    lateinit var toolbar:Toolbar
    lateinit var listParent:LinearLayout
    lateinit var addPayerBanner:RelativeLayout
    lateinit var selectPayerFragment:SelectPayerFragment
    lateinit var doneButton:Button
    var payerList:HashMap<User,Int> = HashMap()
    lateinit var mPayerSelectorPresenter:IPayerSelectorPresenter
    lateinit var allUserList:ArrayList<User>
    var activityId:String =""
    var enteredTotal:Int =0
    var addedUpTotal:Int =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payee_selector)
        toolbar = findViewById<Toolbar> (R.id.toolbar) as Toolbar
        listParent = findViewById<LinearLayout>(R.id.userList_linear) as LinearLayout
        addPayerBanner = findViewById(R.id.banner_parent)
        doneButton = findViewById(R.id.done_button_payer_select)
        mPayerSelectorPresenter = PayerSelectorPresenter(this,this)
        allUserList = ArrayList()
        if(intent!=null&&intent.extras!=null){
            activityId = intent.getStringExtra("activity_id")
            enteredTotal = intent.getIntExtra("entered_total",0)
            payerList = intent.getSerializableExtra("PayeeList") as HashMap<User, Int>
        }


         mPayerSelectorPresenter?.getAllUsersForActivity(activityId)

        addPayerBanner.setOnClickListener{v:View ->
            selectPayerFragment = SelectPayerFragment()
            selectPayerFragment.show(supportFragmentManager,"selectContacts")
        }

        doneButton.setOnClickListener{v:View->


            refreshPayerList()
            if(valuesAddUp()) {
                if(enteredTotal==0){
                    intent.putExtra("total",addedUpTotal)
                }
                intent.putExtra("PayeeList", payerList)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }else{

            }

        }

        if(payerList!=null&&payerList.isNotEmpty()){
            for(item in payerList){
               showPayerList(item.key,item.value)
            }
        }

    }

    private fun valuesAddUp():Boolean {
        var sum:Int = 0
        for (user in payerList) {
            sum+=user.value
        }

        if(enteredTotal==0){
            addedUpTotal = sum
            return true
        }else {

            if(enteredTotal==sum){
            return true
        }else{
            Toast.makeText(this, "Entered values should add up to "+enteredTotal, Toast.LENGTH_SHORT).show()
        }
        }
        return false

    }

    private fun refreshPayerList() {

        var childCount = listParent.childCount
        if(childCount>0){
            for (i in 0 until childCount)kotlin.run{
                var child:View = listParent.getChildAt(i) as View
                var amountEdittext: EditText = child.findViewById<EditText>(R.id.edittext_paid_amount) as EditText
                var userId:TextView = child.findViewById<TextView>(R.id.userId) as TextView

                for (user in payerList){
                    if(user.key.user_id==userId.text.toString()){
                        user.setValue((amountEdittext.text.toString().toDouble()*100).toInt())
                    }
                }
            }
        }

    }

    fun addUserToPayerList(user: User,amount:Int){

        listParent.addView(getPayerListView(user,amount))
        if(!payerList.containsKey(user)) {
            payerList.put(user, 0)
        }else{
            payerList.remove(user)
        }
    }

    fun showPayerList(user:User,amount:Int){
        listParent.addView(getPayerListView(user,amount))
    }

    fun getPayerListView(user: User,amount:Int): View {
        var inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view: View = inflater?.inflate(R.layout.user_selection_list_item_layout,null,false)
        var nameAutoCompleteText: TextView = view.findViewById<TextView>(R.id.autocomplete_tv_add_payer) as TextView
        var amountEdittext: EditText = view.findViewById<EditText>(R.id.edittext_paid_amount) as EditText
        var userId:TextView = view.findViewById<TextView>(R.id.userId) as TextView
        var delete:ImageView = view.findViewById(R.id.imageview_delete)
        var edit:ImageView = view.findViewById(R.id.imageview_edit)
        var amountTv:TextView = view.findViewById(R.id.amount_tv_payer_list)
        var save:ImageView = view.findViewById(R.id.imageview_save)

        save.visibility = View.INVISIBLE

        nameAutoCompleteText.text = user.user_name
        userId.text = user.user_id.toString()
        if(amount>0) {
            amountEdittext.setText(Utility.getInHigherDenimonation(amount))
            amountTv.setText(Utility.getInHigherDenimonation(amount))
        }
       /* edit.setOnClickListener {
            amountTv.visibility = View.INVISIBLE
            amountEdittext.visibility = View.VISIBLE
            save.visibility = View.VISIBLE
            edit.visibility = View.INVISIBLE
        }*/
        edit.visibility = View.INVISIBLE
        amountTv.visibility = View.INVISIBLE

      /*  amountTv.setOnClickListener {
            amountTv.visibility = View.VISIBLE
            amountEdittext.visibility = View.INVISIBLE
        }*/

       /* save.setOnClickListener {
            edit.visibility = View.VISIBLE


        }*/

        delete.setOnClickListener { view:View->

        }
        return  view
    }

    override fun onUserSelected(payerList:HashMap<User,Int>) {
        for(user in payerList.keys) {
            addUserToPayerList(user,0)
        }
    }

    override fun getAllusers(): ArrayList<User> {
        return allUserList!!
    }

    override fun onUsersFetched(userList: ArrayList<User>) {
        allUserList = userList
    }
}
