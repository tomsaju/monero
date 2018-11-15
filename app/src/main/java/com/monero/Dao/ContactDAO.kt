package com.monero.Dao

import android.arch.persistence.room.*
import com.monero.models.Contact
import com.monero.models.ContactMinimal
import com.monero.models.Credit
import io.reactivex.Single

/**
 * Created by Dreamz on 23-09-2018.
 */

@Dao interface ContactDAO {

    @Query("SELECT * FROM "+DBContract.CONTACTS_TABLE.TABLE_NAME+" WHERE "+DBContract.CONTACTS_TABLE.CONTACT_ID+" = :id")
    fun getContactForId(id:Long): Contact

    @Query("SELECT * FROM "+DBContract.CONTACTS_TABLE.TABLE_NAME+" WHERE "+DBContract.CONTACTS_TABLE.CONTACT_PHONE+" IS NOT NULL AND TRIM("+DBContract.CONTACTS_TABLE.CONTACT_PHONE+") <> ''"+" ORDER BY "+DBContract.CONTACTS_TABLE.CONTACT_NAME_LOCAL+" ASC")
    fun getAllContacts():List<Contact>

    @Query("SELECT * FROM "+DBContract.CONTACTS_TABLE.TABLE_NAME+" ORDER BY "+DBContract.CONTACTS_TABLE.CONTACT_NAME_LOCAL+" ASC")
    fun getAllTypeContacts(): Single<List<Contact>>

    @Query("SELECT * FROM "+DBContract.CONTACTS_TABLE.TABLE_NAME+" WHERE "+DBContract.CONTACTS_TABLE.CONTACT_PHONE+" IS NOT NULL AND TRIM("+DBContract.CONTACTS_TABLE.CONTACT_PHONE+") <> ''")
    fun getAllContactsMinimal(): Single<List<Contact>>

    @Query("SELECT * FROM "+DBContract.CONTACTS_TABLE.TABLE_NAME+" WHERE "+DBContract.CONTACTS_TABLE.CONTACT_EMAIL+" IS NOT NULL AND TRIM("+DBContract.CONTACTS_TABLE.CONTACT_EMAIL+") <> ''")
    fun getAllEmailContactsMinimal(): Single<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIntoContactTable(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllContactIntoContactTable(contacts: List<Contact>)

    @Delete
    fun deleteContact(contact: Contact)

    @Query("DELETE FROM "+DBContract.CONTACTS_TABLE.TABLE_NAME)
    fun deleteTable()
}