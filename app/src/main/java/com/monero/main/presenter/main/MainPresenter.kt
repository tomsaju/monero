package com.monero.main.presenter.main

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.monero.Dao.DBContract
import com.monero.helper.AppDatabase
import com.monero.helper.AppDatabase.Companion.db
import com.monero.helper.AppDatabase.Companion.getAppDatabase
import com.monero.helper.converters.TagConverter
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.*
import com.monero.Application.ApplicationController
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.reflect.TypeToken
import com.monero.models.*
import com.monero.network.RestService
import com.monero.network.ServiceRest
import io.reactivex.Observable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import io.reactivex.disposables.Disposable
import io.reactivex.SingleObserver
import org.json.JSONArray
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley




/**
 * Created by tom.saju on 3/7/2018.
 */
class MainPresenter : IMainPresenter {

    var TAG: String = "MainPresenter"
    var context: Context
    var view: IMainView
    var firestoreDb: FirebaseFirestore? = null

    constructor(context: Context, view: IMainView) {
        this.context = context
        this.view = view
        firestoreDb = FirebaseFirestore.getInstance()

    }

    var PendingDownloadItems: ArrayList<String>? = null


    override fun getAllActivitiesList() {
        db = getAppDatabase(context)
        var allActivities = db?.activitesDao()?.getAllActivities()
        view.onActivitiesFetched(allActivities)
    }


    val RestAPIService by lazy {
        RestService.create()
    }

    var disposable: Disposable? = null

    override fun saveActivity(activity: Activities) {

//////////////////////////

        Observable.fromCallable {
            db = getAppDatabase(context)
            db?.activitesDao()?.insertIntoActivitiesTable(activity) // .database?.personDao()?.insert(person)
            for (tag in activity.tags) {
                AppDatabase.db?.tagDao()?.insertIntoTagTable(tag)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ orderItem ->
            //save the log to history
            var historyItem = HistoryLogItem(UUID.randomUUID().toString(),
                                activity.author.user_id,
                                DBContract.HISTORY_LOG_ITEM_TABLE.TYPE_ADDED_NEW_ACTIVITY,
                                activity.createdDate.toString(),
                                activity.title,
                                "",
                                activity.id,
                                activity.id)

            saveLog(historyItem)

            // set values to UI
            Log.d("tag", "done")
            ///
            var gson = Gson()
            var convertor = TagConverter()
            var membersJson: String = convertor.convertUserListtoString(activity.members)
            var tagsJson: String = convertor.convertTagListtoString(activity.tags)
            var author: String = gson.toJson(activity.author, User::class.java)

            var permittedUserArrayList = arrayListOf<String>()
            for (user in activity.members) {
                permittedUserArrayList.add(user.user_phone)
            }


            var newActivity = HashMap<String, Any>()
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_TITLE, activity.title)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_DESCRIPTION, activity.description)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_MODE, activity.mode)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_TAGS, tagsJson)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_USERS, membersJson)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_AUTHOR, author)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_ALLOWED_READ_PERMISSION_USERS, permittedUserArrayList)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_CREATED_DATE, activity.createdDate)


            firestoreDb?.collection("activities")?.document(activity.id)?.set(newActivity)

                    ?.addOnSuccessListener { DocumentReference ->

                        //success
                        // activity.id = DocumentReference.id
                        activity.syncStatus = true
                        /*Single.fromCallable {
                          //  db = getAppDatabase(context)
                         //   db?.activitesDao()?.updateActivityId(activity.id, DocumentReference.id) // .database?.personDao()?.insert(person)
                            *//*for(tag in activity.tags){
                                AppDatabase.db?.tagDao()?.insertIntoTagTable(tag)
                            }*//*
                        }.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread()).subscribe()*/
                    }

                    ?.addOnFailureListener { e ->
                        //failure
                    }

            ///
        }, { error ->
            // handle exception if any
            Log.d("tag", "exception")
        }, {
            // on complete
            Log.d("tag", "completed")
        })


    }

    private fun saveLog(historyItem: HistoryLogItem) {
        Observable.fromCallable {
            db = getAppDatabase(context)
            db?.historyDao()?.insertIntoHistoryTable(historyItem) // .database?.personDao()?.insert(person)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ orderItem ->

           Log.d(TAG,"log saving complete")
        })

    }

    override fun getAllActivitiesFromServer() {
        Log.d(TAG,"getAllActivitiesFromServer")
        view.showLoader()
        getActivityIdList()

        /* */
    }


    fun downloadAllActivities(activityIdList: ArrayList<String>) {
        Log.d(TAG,"downloadActivitiesin list")
        view.showLoader()
        for (id in activityIdList) {
            downloadActivity(id)
        }
    }

    fun downloadActivity(activityId: String) {
        Log.d(TAG,"getAllActivity : "+activityId)
        var gson = Gson()
        FirebaseFirestore.getInstance()
                .collection("activities").document(activityId).get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {

                val document = task.result
                var timestampWithoutNanoseconds = "";
                var timestampinSeconds = "";


                val tagType = object : TypeToken<List<Tag>>() {}.type
                val tagsList = Gson().fromJson<List<Tag>>(document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_TAGS).toString(), tagType)

                val userListType = object : TypeToken<List<User>>() {}.type
                val usersList = Gson().fromJson<List<User>>(document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_USERS).toString(), userListType)

                var activityAuthor = TagConverter().convertJsonToUserObject(document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_AUTHOR).toString())

                var id: String = activityId
                var title: String = document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_TITLE).toString()
                var description: String = document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_DESCRIPTION).toString()
                var tags: List<Tag> = tagsList
                var mode: Int = Integer.parseInt(document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_MODE).toString())
                var members: List<User> = usersList
                var author: User = activityAuthor
                var syncStatus: Boolean = true //syncstatus is true
                var createdDate: Long = document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_CREATED_DATE).toString().toLong()

                var lastModifiedTime = "";
                if (document.get("last_modified_time") == null) {
                    lastModifiedTime = "";
                } else {


                    var dateObject = document.get("last_modified_time")
                    lastModifiedTime = dateObject.toString();
                    timestampWithoutNanoseconds = lastModifiedTime.substringBefore(",")
                    timestampinSeconds = timestampWithoutNanoseconds.substringAfter("=")
                }
                var transactionIds = ""
                var historyLogIds = ""
                var expenseListId = document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_EXPENSE_LIST).toString()
                var downloadedActivity = Activities(id, title, description, tags, mode, members, author, syncStatus, createdDate, expenseListId, historyLogIds, transactionIds, timestampinSeconds)


                saveActivityToLocal(downloadedActivity)

            } else {
                Log.d("tasklist", "unsuccessfull")
            }

        })
    }

    private fun saveActivityToLocal(downloadedActivity: Activities?) {
        Log.d(TAG,"saveActivityToLocal")
        Observable.fromCallable {
            db = getAppDatabase(context)
            db?.activitesDao()?.insertIntoActivitiesTable(downloadedActivity!!) // .database?.personDao()?.insert(person)
            for (tag in downloadedActivity!!.tags) {
                AppDatabase.db?.tagDao()?.insertIntoTagTable(tag)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ orderItem ->
            // set values to UI
            //  PendingDownloadItems?.remove(downloadedActivity?.id)

        }, { error ->
            // handle exception if any
            Log.d("tag", "exception")
            view.hideLoader()
        }, {
            // on complete
            Log.d("tag", "completed")
            view.hideLoader()
            saveExpensesForActivity(downloadedActivity);


        })

    }

    private fun saveExpensesForActivity(downloadedActivity: Activities?) {
        view.showLoader()
        Log.d(TAG,"saveExpensesForAvtivity : "+downloadedActivity?.id)
        if (downloadedActivity != null) {
            var expenseListFromServer: ArrayList<String> = ArrayList()
            if (downloadedActivity.expenseIdList != null) {
                if (downloadedActivity.expenseIdList.contains(",")) {
                    //more than one item
                    expenseListFromServer = ArrayList(downloadedActivity.expenseIdList.split(","))
                } else {
                    //single item
                    expenseListFromServer.add(downloadedActivity.expenseIdList)
                }
            }

            if (expenseListFromServer != null && expenseListFromServer.isNotEmpty()) {
                Observable.fromCallable {
                    db?.expenseDao()?.getAllExpenseIdListForActivity(downloadedActivity.id)
                }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe({ localList ->

                    downLoadExpensesFromServer(expenseListFromServer, localList)

                }, { error ->
                    // handle exception if any
                    Log.d("tag", "exception")
                    view.hideLoader()
                }, {
                    // on complete
                    Log.d("tag", "completed")
                    view.hideLoader()


                })
            }else{
                Log.d(TAG,"Nothing to download")
                view.hideLoader()
            }

        }

    }

    private fun downLoadExpensesFromServer(expenseListFromServer: ArrayList<String>, localList: List<String>) {
        view.showLoader()
        Log.d(TAG,"downLoadExpensesFromServer")
        if (expenseListFromServer != null && expenseListFromServer.isNotEmpty()) {

            if(localList!=null&&localList.isNotEmpty()) {

                var tempExpenseIdList = expenseListFromServer

                //check with each element
                expenseListFromServer.removeAll(localList)
                //find deleted expenses
                ArrayList(localList).removeAll(tempExpenseIdList)
                //now local list contains id's which are deleted from server --1
                //expenseListFromServer contains new id's which are not present in local --2
                //delete 1 and save 2
            }
            for (expenseId in expenseListFromServer) {
                downloadExpense(expenseId)
            }

            Log.d("new", expenseListFromServer.size.toString())

        }

    }

    private fun downloadExpense(expenseId: String) {
        Log.d(TAG,"downloadExpense")
        FirebaseFirestore.getInstance()
                .collection("expenses").document(expenseId).get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
                view.hideLoader()
                val document = task.result

                var title: String = document.get(DBContract.EXPENSE_TABLE.EXPENSE_TITLE).toString()
                var Comments: String = document.get(DBContract.EXPENSE_TABLE.EXPENSE_COMMENTS).toString()
                var activityId: String = document.get(DBContract.EXPENSE_TABLE.EXPENSE_ACTIVITY_ID).toString()
                var creditList: String = document.get(DBContract.EXPENSE_TABLE.EXPENSE_CREDITS).toString()
                var debitList: String = document.get(DBContract.EXPENSE_TABLE.EXPENSE_DEBIT).toString()
                var createdDate :String = document.get(DBContract.EXPENSE_TABLE.EXPENSE_CREATED_DATE).toString()
                var amount: Int = 0
                try {
                    amount = document.get(DBContract.EXPENSE_TABLE.EXPENSE_AMOUNT).toString().toInt()
                }catch (e:Exception){

                }
                var convertor = TagConverter()
               // var debitArrayList = convertor.convertJSONtoDebitList(debitList)
               // var creditArrayList = convertor.convertJSONtoCreditList(creditList)

                var debitJson:JSONArray
                var creditJson :JSONArray
                var debitArrayList = ArrayList<Debit>()
                var creditArrayList = ArrayList<Credit>()

                if(debitList!=null&&creditList!=null) {
                    try {
                        debitJson = JSONArray(debitList)
                        creditJson = JSONArray(creditList)



                    for (i in 0..(debitJson.length() - 1)) {
                        var debit = convertor.convertStringToDebit(debitJson[i].toString())
                        debitArrayList.add(debit)
                    }


                    for (i in 0..(creditJson.length() - 1)) {
                        var credit = convertor.convertStringToCredit(creditJson[i].toString())
                        creditArrayList.add(credit)
                    }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }


                var expense = Expense(expenseId, title, Comments, activityId, amount, creditArrayList, debitArrayList,createdDate)

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
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe()


            }else{
                view.hideLoader()
            }
        })

    }

    private fun shouldRemove(it: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun getActivityIdList() {

        Log.d(TAG,"getActivityIdLIst")
        var userId = ApplicationController.preferenceManager!!.myCredential
        var myActivityIds: ArrayList<String> = ArrayList()
        var stringlist: String = ""
        FirebaseFirestore.getInstance()
                .collection("pending_reg_users").document(userId).get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
                try {
                    view.hideLoader()
                    val document = task.result
                    if (document.exists()) {
                        var finalList: HashMap<String, String> = HashMap();

                        val activitiesData: Map<String, Any>? = document.data!!
                        val activityiesDetails = activitiesData?.get("activities_details") as HashMap<String, Any>;
                        for ((key, value) in activityiesDetails) {
                            println("$key = $value")
                            try {
                                var last_modified_time = (value as HashMap<String, Any>).get("last_modified_time").toString();
                                var lastModifiedTimeWithoutNanoseconds = last_modified_time.substringBefore(",")
                                var lastModifiedSeconds = lastModifiedTimeWithoutNanoseconds.substringAfter("=")
                                finalList[key] = lastModifiedSeconds
                            } catch (e: Exception) {

                            }
                        }
                        downloadUpdatedActivities(finalList)

                    }
                } catch (e: Exception) {
                    view.hideLoader()
                }
            }
        })
    }

    private fun downloadUpdatedActivities(finalList: HashMap<String, String>) {
        Log.d(TAG,"downloadUpdatedActivities")
        view.showLoader()
        var updatedActivityIdList: ArrayList<String> = ArrayList()


        var single: Single<List<ActivitiesMinimal>>? = db?.activitesDao()?.getAllActivitiesModifiedDate()
        if (single != null) {
            single.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<List<ActivitiesMinimal>> {
                        override fun onSubscribe(d: Disposable) {
                            // add it to a CompositeDisposable
                        }

                        override fun onSuccess(users: List<ActivitiesMinimal>) {
                            // update the UI
                            view.hideLoader()
                            var allActivitiesModifiedTime = users
                            if (allActivitiesModifiedTime != null) {
                                for (activity in allActivitiesModifiedTime) {
                                    if (finalList.containsKey(activity.id) &&
                                            areModifiedTImeSame(finalList.get(activity.id), activity.lastModifiedTime)) {
                                        finalList.remove(activity.id)
                                    } else {
                                        updatedActivityIdList.add(activity.id)
                                    }
                                }
                            }

                            if (!finalList.isEmpty()) {
                                for ((key, value) in finalList) {
                                    updatedActivityIdList.add(key)
                                }
                            }

                            if (updatedActivityIdList != null && updatedActivityIdList.isNotEmpty()) {
                                Log.d(TAG, "found new items")
                                downloadAllActivities(updatedActivityIdList)
                            } else {
                                Log.d(TAG, "No new items")
                                view.hideLoader()
                            }

                        }

                        override fun onError(e: Throwable) {
                            // show an error message
                            Log.d("download", "Error")
                            view.hideLoader()
                        }
                    })
        }


    }

    private fun areModifiedTImeSame(serverModifiedTime: String?, localModifiedTime: String): Boolean {
        var isEqual = false
        //Compare the modified timestamps
        //Firebase timestamp when fetched as milliseconds and nanseconds... shows difference in value for same timestamp
        //ex September 5 2018 12:00:00 timestamp fetched from two different sections give different values for milliseconds(sometimes) and nanoseconds(always)
        //so we are converting them to standard format for comparison
        if(localModifiedTime==""){
            return  false;
        }

        var serverTime = serverModifiedTime?.toLong()
        var localTime = localModifiedTime?.toLong()

        val pattern = "yyyy-MM-dd HH:mm:ss"
        val simpleDateFormat = SimpleDateFormat(pattern)
        if (serverTime != null && localTime != null) {
            var servertimeFormatted = simpleDateFormat.format(Date(serverTime))
            var localtimeFormatted = simpleDateFormat.format(Date(localTime))
            if (servertimeFormatted.equals(localtimeFormatted)) {
                isEqual = true
            }
        } else {
            isEqual = false
        }
        return isEqual
    }

    fun printAllIds(list: ArrayList<String>) {
        for (id in list) {
            Log.d("Print", id);
        }
    }


    override fun syncContactsWithServer(contactList: ArrayList<Contact>) {

      /*  var params="[";
        for( i in 0 .. contactList.size-1){
            var number:String=contactList[i].Contact_phone.replace("+","");
            number = number.replace("\\s".toRegex(), "")
            params+=number
          if(i!=contactList.size-1){
              params+=","
          }
        }
        params+="]"


*/
       // var array=Array<String>(contactList.size){"it = $it"}
        var numberLIst:ArrayList<String> = ArrayList()
        for(contact in contactList){
            var number:String=contact.Contact_phone.replace("+","");
            number = number.replace("\\s".toRegex(), "")
            numberLIst.add(number)
        }
        val array = arrayOfNulls<String>(numberLIst.size)
        numberLIst.toArray(array)

      //  println(Arrays.toString(array))

      //  var params = Array

        syncContactWithServer(Arrays.toString(array))

        /*if(contactArray!=null&&contactArray.length()>0){
            sendContactsJSON(contactArray)
        }*/
    }


    private fun sendAndRequestResponse(localContacts: String) {

        //RequestQueue initialized
        var mRequestQueue = Volley.newRequestQueue(context)

        var url ="https://us-central1-monero-efbcb.cloudfunctions.net/webApi/api/v1/getRegisteredUsers";

        //String Request initialized
        var mStringRequest = object : StringRequest(Request.Method.POST, url,  Response.Listener<String> {
            response ->
            Log.d("Vol Tag",response)

        },  Response.ErrorListener { error ->
            Log.d("Vol Tag",error.toString())

        }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                var gson = Gson()
                val objectList = gson.fromJson(localContacts, Array<String>::class.java).asList()
                params.put("localContacts", "[]")
                return params
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return HashMap()
            }
        }

        mRequestQueue.add(mStringRequest)
    }


    fun syncContactWithServer(localContacts: String){

      var restService = ServiceRest()
        var params = HashMap<String,String>()
        params.put("localContacts",localContacts)
        sendAndRequestResponse(localContacts)
        /*restService.getRegisteredContacts(context,"api/v1/getRegisteredUsers",params,{response ->
          Log.d("result",response)
      })*/

     /* var map = HashMap<String,String>()
        map.put("localContacts",localContacts)
      disposable = RestAPIService.getRegisteredContactForNumber(map)
                .subscribeOn(Schedulers.io())
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

    private fun showError(message: String?) {
        //do nothing
        //set sync status false
        Log.d("tag",message)
    }

    private fun showResult(result: String) {
        //if contact not null, search that number and get details
        Log.d("result",result);
    }


    private fun sendContactsJSON(contactArray: JSONArray) {
       /* disposable = RestAPIService.getAllRegisteredContacts()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { result -> showResult(result) },
                                { error -> showError(error.message) }
                        )*/
    }


}
