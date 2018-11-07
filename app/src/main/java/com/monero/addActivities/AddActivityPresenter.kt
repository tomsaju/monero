package com.monero.addActivities

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.monero.helper.AppDatabase.Companion.db
import com.monero.helper.AppDatabase.Companion.getAppDatabase
import com.monero.models.Activities
import com.monero.models.ContactMinimal
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by tom.saju on 3/14/2018.
 */
class AddActivityPresenter :IAddActivityPresenter {

    var context:Context
    var view:IAddActivityView

    constructor(context: Context,view: IAddActivityView){
        this.context = context
        this.view = view
    }

    override fun getAllContactsList() {
        val contactsList:MutableList<ContactMinimal> = getContacts()
        view.onContactsfetched(contactsList)
    }

    override fun getActivityForId(id:String){
        Observable.fromCallable {
            db = getAppDatabase(context)
            db?.activitesDao()?.getActivityForId(id) // .database?.personDao()?.insert(person)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ activity:Activities ->

            view.onActivityFetched(activity)

        }, { error ->
            // handle exception if any
            view.onActivityFetchError()
        }, {
            // on complete
            Log.d("tag", "completed")


        })
    }

    private fun getContacts(): MutableList<ContactMinimal> {
        val PROJECTION = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER)

        val contactsList: MutableList<ContactMinimal> = mutableListOf()
        val builder = StringBuilder()
        val resolver: ContentResolver = context?.contentResolver


        val cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);
        if (cursor != null) {
            try {
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                var name: String
                var number: String
                while (cursor.moveToNext()) {
                    name = cursor.getString(nameIndex)
                    number = cursor.getString(numberIndex)

                    var newCOntact: ContactMinimal = ContactMinimal(System.currentTimeMillis().toString(),name, number,"")
                    contactsList.add(newCOntact)
                }
            } finally {
                cursor.close();
            }

        }
            return contactsList
        }


}