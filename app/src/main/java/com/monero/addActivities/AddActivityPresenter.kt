package com.monero.addActivities

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.monero.models.Contact

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
        val contactsList:MutableList<Contact> = getContacts()
        view.onContactsfetched(contactsList)
    }

    private fun getContacts(): MutableList<Contact> {
        val contactsList:MutableList<Contact> = mutableListOf()
        val builder = StringBuilder()
        val resolver: ContentResolver = context?.contentResolver;
        val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
                null)

        if (cursor.count > 0) {
            while (cursor.moveToNext()) {

                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phoneNumber = (cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))).toInt()

                if (phoneNumber > 0) {
                    val cursorPhone = context?.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), null)

                    if(cursorPhone.count > 0) {
                        while (cursorPhone.moveToNext()) {
                            val phoneNumValue = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            builder.append("Contact: ").append(name).append(", Phone Number: ").append(
                                    phoneNumValue).append("\n\n")
                            Log.e("Name ===>",phoneNumValue)
                            var newCOntact:Contact = Contact(name,phoneNumValue)
                            contactsList.add(newCOntact)
                        }

                    }
                    cursorPhone.close()
                }
            }
        } else {
            //   toast("No contacts available!")
        }
        cursor.close()
        return contactsList
    }
}