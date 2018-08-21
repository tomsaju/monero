package com.monero.main.presenter.main

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
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
}
