package com.monero.activitydetail.presenter.expense

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.monero.Dao.DBContract
import com.monero.helper.AppDatabase
import com.monero.helper.converters.TagConverter
import com.monero.models.Expense
import com.monero.models.User
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by tom.saju on 7/19/2018.
 */
class ExpenseFragmentPresenter: IExpenseFragmentPresenter {


    var context: Context
    var view: IExpenseFragmentView
    var firestoreDb: FirebaseFirestore? = null

    constructor(context: Context, view: IExpenseFragmentView) {
        this.context = context
        this.view = view
        firestoreDb = FirebaseFirestore.getInstance()
    }

    override fun saveExpense(expense: Expense) {
        var convertor = TagConverter()
        var debitJson = convertor.convertDebitListtoString(expense.debitList)
        var creditJson = convertor.convertCreditListtoString(expense.creditList)

        var newExpense = HashMap<String, Any>()
        newExpense.put(DBContract.EXPENSE_TABLE.EXPENSE_ACTIVITY_ID, expense.activity_id)
        newExpense.put(DBContract.EXPENSE_TABLE.EXPENSE_COMMENTS, expense.comments)
        newExpense.put(DBContract.EXPENSE_TABLE.EXPENSE_TITLE, expense.title)
        newExpense.put(DBContract.EXPENSE_TABLE.EXPENSE_AMOUNT, expense.amount)
        newExpense.put(DBContract.EXPENSE_TABLE.EXPENSE_DEBIT, debitJson)
        newExpense.put(DBContract.EXPENSE_TABLE.EXPENSE_CREDITS, creditJson)

        firestoreDb?.collection("expenses")?.add(newExpense)

                ?.addOnSuccessListener { DocumentReference ->

                    expense.id = DocumentReference.id
                    Single.fromCallable {

                        AppDatabase.db = AppDatabase.getAppDatabase(context)
                        AppDatabase.db?.expenseDao()?.insertIntoAExpensesTable(expense)

                        for (credit in expense.creditList) {
                            AppDatabase.db?.creditDao()?.insertIntoCreditTable(credit)
                        }

                        for (debit in expense.debitList) {
                            AppDatabase.db?.debitDao()?.insertIntoDebitTable(debit)
                        }


                    }.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread()).subscribe()
                }
                ?.addOnFailureListener { e ->
                    //failure
                }


        /*override fun getAllParticipantsForthisActivity(id: Long): ArrayList<User> {
       var allUserList:ArrayList<User> = ArrayList()

        Single.fromCallable {
            var gson = Gson()
            AppDatabase.db = AppDatabase.getAppDatabase(context)
            var users = AppDatabase.db?.activitesDao()?.getAllUsersForActivity(id)
            var list :List<User> =gson.fromJson(users , Array<User>::class.java).toList()
            allUserList = ArrayList(list)


        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe()
        return allUserList
    }*/
    }
}