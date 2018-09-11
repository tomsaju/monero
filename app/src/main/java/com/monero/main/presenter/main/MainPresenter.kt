package com.monero.main.presenter.main

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.monero.Dao.DBContract
import com.monero.helper.AppDatabase
import com.monero.helper.AppDatabase.Companion.db
import com.monero.helper.AppDatabase.Companion.getAppDatabase
import com.monero.helper.converters.TagConverter
import com.monero.models.Activities
import com.monero.models.User
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import java.nio.file.Files.exists
import com.monero.Application.ApplicationController
import com.monero.helper.PreferenceManager
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.reflect.TypeToken
import com.monero.models.Tag
import io.reactivex.Observable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


/**
 * Created by tom.saju on 3/7/2018.
 */
class MainPresenter: IMainPresenter {


    var context: Context
    var view: IMainView
    var firestoreDb: FirebaseFirestore? = null
    constructor(context: Context, view: IMainView) {
        this.context = context
        this.view = view
        firestoreDb = FirebaseFirestore.getInstance()
    }
    var PendingDownloadItems:ArrayList<String>?=null



    override fun getAllActivitiesList() {
        db= getAppDatabase(context)
        var allActivities = db?.activitesDao()?.getAllActivities()
        view.onActivitiesFetched(allActivities)
    }


    override fun saveActivity(activity: Activities) {

//////////////////////////

        Observable.fromCallable {
            db= getAppDatabase(context)
            db?.activitesDao()?.insertIntoActivitiesTable(activity) // .database?.personDao()?.insert(person)
            for(tag in activity.tags){
                AppDatabase.db?.tagDao()?.insertIntoTagTable(tag)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ orderItem ->
            // set values to UI
            Log.d("tag","done")
            ///
            var gson =Gson()
            var convertor = TagConverter()
            var membersJson:String =convertor.convertUserListtoString(activity.members)
            var tagsJson:String = convertor.convertTagListtoString(activity.tags)
            var author:String = gson.toJson(activity.author,User::class.java)

            var permittedUserArrayList = arrayListOf<String>()
            for(user in activity.members){
                permittedUserArrayList.add(user.user_phone)
            }



            var newActivity = HashMap<String, Any>()
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_TITLE,activity.title)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_DESCRIPTION,activity.description)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_MODE,activity.mode)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_TAGS,tagsJson)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_USERS,membersJson)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_AUTHOR,author)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_ALLOWED_READ_PERMISSION_USERS,permittedUserArrayList)
            newActivity.put(DBContract.ACTIVITY_TABLE.ACTIVITY_CREATED_DATE,activity.createdDate)


            firestoreDb?.collection("activities")?.add(newActivity)

                    ?.addOnSuccessListener {DocumentReference ->

                        //success
                       // activity.id = DocumentReference.id
                        activity.syncStatus = true
                        Single.fromCallable {
                            db= getAppDatabase(context)
                            db?.activitesDao()?.updateActivityId(activity.id,DocumentReference.id) // .database?.personDao()?.insert(person)
                            /*for(tag in activity.tags){
                                AppDatabase.db?.tagDao()?.insertIntoTagTable(tag)
                            }*/
                        }.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread()).subscribe()
                    }

                    ?.addOnFailureListener { e ->
                        //failure
                    }

            ///
        }, { error ->
            // handle exception if any
            Log.d("tag","exception")
        }, {
            // on complete
            Log.d("tag","completed")
        })




    }

    override fun getAllActivitiesFromServer() {

        getActivityIdList()

       /* */
    }


    fun downloadAllActivities(activityIdList: ArrayList<String>){
        for(id in activityIdList){
            downloadActivity(id)
        }
    }

    fun downloadActivity(activityId: String) {
        var gson = Gson()
        FirebaseFirestore.getInstance()
                .collection("activities").document(activityId).get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
                val document = task.result



                val tagType = object : TypeToken<List<Tag>>() {}.type
                val tagsList = Gson().fromJson<List<Tag>>(document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_TAGS).toString(), tagType)

                val userListType = object : TypeToken<List<User>>() {}.type
                val usersList = Gson().fromJson<List<User>>(document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_USERS).toString(), userListType)

                var activityAuthor = TagConverter().convertJsonToUserObject(document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_AUTHOR).toString())

                var id: String=activityId
                var title:String=document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_TITLE).toString()
                var description:String=document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_DESCRIPTION).toString()
                var tags:List<Tag> = tagsList
                var mode:Int = Integer.parseInt(document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_MODE).toString())
                var members:List<User> =usersList
                var author:User= activityAuthor
                var syncStatus:Boolean=true //syncstatus is true
                var createdDate:Long=0
                //var simpledateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                var lastModifiedTime = "";
                if(document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_MODIFIED_TIME)==null){
                    lastModifiedTime = "";
                }else{


                    var dateObject = document.get(DBContract.ACTIVITY_TABLE.ACTIVITY_MODIFIED_TIME)
                    lastModifiedTime =  dateObject.toString();
                }

                var downloadedActivity = Activities(id,title,description,tags,mode,members,author,syncStatus,createdDate,lastModifiedTime)


                saveActivityToLocal(downloadedActivity)

            } else {
                Log.d("tasklist", "unsuccessfull")
            }

        })
    }

    private fun saveActivityToLocal(downloadedActivity: Activities?) {
        Observable.fromCallable {
            db= getAppDatabase(context)
            db?.activitesDao()?.insertIntoActivitiesTable(downloadedActivity!!) // .database?.personDao()?.insert(person)
            for(tag in downloadedActivity!!.tags){
                AppDatabase.db?.tagDao()?.insertIntoTagTable(tag)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ orderItem ->
            // set values to UI
          //  PendingDownloadItems?.remove(downloadedActivity?.id)

        }, { error ->
            // handle exception if any
            Log.d("tag","exception")
        }, {
            // on complete
            Log.d("tag","completed")
        })

    }


    fun getActivityIdList(){

       
        var userId = ApplicationController.preferenceManager!!.myCredential
        var myActivityIds:ArrayList<String> = ArrayList()
        var stringlist:String =""
        FirebaseFirestore.getInstance()
                   .collection("pending_reg_users").document(userId).get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
               if (task.isSuccessful) {
                   try {
                       val document = task.result
                       if (document.exists()) {
                            var finalList :HashMap<String,String> = HashMap();

                           val activitiesData:Map<String,Any>? = document.data!!
                            val activityiesDetails = activitiesData?.get("activities_details") as HashMap<String,Any>;
                           for ((key, value) in activityiesDetails) {
                               println("$key = $value")
                               try {
                                   var last_modified_time =(value as HashMap<String,Any>).get("last_modified_time").toString();
                                   finalList[key]= last_modified_time
                               } catch (e: Exception) {

                               }
                           }
                           downloadUpdatedActivities(finalList)
                           downloadAllActivities(myActivityIds)
                       }
                   } catch (e: Exception) {
                   }
               }
       })
   }

    private fun downloadUpdatedActivities(finalList: HashMap<String, String>) {
        var localList: HashMap<String,String> = HashMap()
        for((key,value) in finalList){
            localList[key]=(db?.activitesDao()?.getActivityForId(key) as Activities).lastModifiedTime
        }
        println("done man")
    }

    fun printAllIds(list:ArrayList<String>){
       for(id in list){
           Log.d("Print",id);
       }
   }


}
