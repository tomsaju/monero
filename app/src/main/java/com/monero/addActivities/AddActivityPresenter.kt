package com.monero.addActivities

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.monero.models.ContactMinimal

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

                    var newCOntact: ContactMinimal = ContactMinimal(System.currentTimeMillis().toString(),name, number)
                    contactsList.add(newCOntact)
                }
            } finally {
                cursor.close();
            }

        }
            return contactsList
        }


}