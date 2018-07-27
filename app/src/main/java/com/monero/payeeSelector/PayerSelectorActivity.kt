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

class PayerSelectorActivity : AppCompatActivity(),SelectPayerFragment.SelectPayerFragmentInteractionListener,IPayerSelectorView {

    var REQUEST_CODE_PAYER_SELECTION = 3
    lateinit var toolbar:Toolbar
    lateinit var listParent:LinearLayout
    lateinit var addPayerBanner:RelativeLayout
    lateinit var selectPayerFragment:SelectPayerFragment
    lateinit var doneButton:Button
    var payerList:HashMap<User,Double> = HashMap()
    lateinit var mPayerSelectorPresenter:IPayerSelectorPresenter
    lateinit var allUserList:ArrayList<User>
    var activityId:Long =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payee_selector)
        toolbar = findViewById<Toolbar> (R.id.toolbar) as Toolbar
        listParent = findViewById<LinearLayout>(R.id.userList_linear) as LinearLayout
        addPayerBanner = findViewById(R.id.banner_parent)
        doneButton = findViewById(R.id.done_button_payer_select)
        mPayerSelectorPresenter = PayerSelectorPresenter(this,this)

        if(intent!=null&&intent.extras!=null){
            activityId = intent.getLongExtra("activity_id",0)
        }

         mPayerSelectorPresenter?.getAllUsersForActivity(activityId)

        addPayerBanner.setOnClickListener{v:View ->
           selectPayerFragment = SelectPayerFragment()
            selectPayerFragment.show(supportFragmentManager,"selectContacts")
        }

        doneButton.setOnClickListener{v:View->


            refreshPayerList()

            intent.putExtra("PayeeList",payerList)
            setResult(Activity.RESULT_OK,intent)
            finish()

        }

    }

    private fun refreshPayerList() {

        var childCount = listParent.childCount
        if(childCount>0){
            for (i in 0 until childCount)kotlin.run{
                var child:View = listParent.getChildAt(i) as View
                var amountEdittext: EditText = child.findViewById<EditText>(R.id.edittext_paid_amount) as EditText
                var userId:TextView = child.findViewById<TextView>(R.id.userId) as TextView

                for (user in payerList){
                    if(user.key.user_id==userId.text.toString().toLong()){
                        user.setValue(amountEdittext.text.toString().toDouble())
                    }
                }
            }
        }

    }

    fun addUserToPayerList(user: User){

        listParent.addView(getPayerListView(user))
        if(!payerList.containsKey(user)) {
            payerList.put(user, 0.0)
        }
    }

    fun getPayerListView(user: User): View {
        var inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view: View = inflater?.inflate(R.layout.user_selection_list_item_layout,null,false)
        var nameAutoCompleteText: TextView = view.findViewById<TextView>(R.id.autocomplete_tv_add_payer) as TextView
        var amountEdittext: EditText = view.findViewById<EditText>(R.id.edittext_paid_amount) as EditText
        var userId:TextView = view.findViewById<TextView>(R.id.userId) as TextView

        nameAutoCompleteText.text = user.user_name
        userId.text = user.user_id.toString()
        return  view
    }

    override fun onUserSelected(user: User) {
        addUserToPayerList(user)
    }

    override fun getAllusers(): ArrayList<User> {
        return allUserList
    }

    override fun onUsersFetched(userList: ArrayList<User>) {
        allUserList = userList
    }
}
