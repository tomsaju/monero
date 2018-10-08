package com.monero.Dao

/**
 * Created by tom.saju on 3/8/2018.
 */
class DBContract {
    public class ACTIVITY_TABLE {

        companion object {
            const val TABLE_NAME = "Activities_Table"
            const val ACTIVITY_ID = "activity_id"
            const val ACTIVITY_TITLE = "title"
            const val ACTIVITY_DESCRIPTION = "description"
            const val ACTIVITY_USERS = "users"
            const val ACTIVITY_MODE ="mode"
            const val ACTIVITY_TAGS = "tags"
            const val ACTIVITY_AUTHOR = "author"
            const val ACTIVITY_SYNC_STATUS = "syncStatus"
            const val ACTIVITY_ALLOWED_READ_PERMISSION_USERS = "allowed_read_permission"
            const val ACTIVITY_CREATED_DATE = "createdDate"
            const val ACTIVITY_MODIFIED_TIME = "lastModifiedTime"

            //not a local db field. But present in firestore document
            const val ACTIVITY_EXPENSE_LIST = "expense_id_list"

        }

    }
    public class TAG_TABLE {

        companion object {
            const val TABLE_NAME = "Tag_Table"
            const val TAG_ID = "Tag_Id"
            const val TAG_NAME = "Tag_Name"
        }
    }

    public class EXPENSE_TABLE {

        companion object {
            const val TABLE_NAME = "Expense_Table"
            const val EXPENSE_ID = "expense_id"
            const val EXPENSE_TITLE = "title"
            const val EXPENSE_COMMENTS = "comments"
            const val EXPENSE_PAYMENTS = "payments"
            const val EXPENSE_CREDITS = "credits"
            const val EXPENSE_DEBIT = "debits"
            const val EXPENSE_ACTIVITY_ID = "activity_id"
            const val EXPENSE_AMOUNT = "amount"
            const val EXPENSE_CREATED_DATE = "created_date"

        }

    }

    public class CREDIT_TABLE {

        companion object {
            const val TABLE_NAME = "Credit_Table"
            const val CREDIT_ID = "credit_id"
            const val ACTIVITY_ID = "activity_id"
            const val USER_ID = "user_id"
            const val USER_NAME = "user_name"
            const val EXPENSE_ID = "expense_id"
            const val AMOUNT = "amount"
        }

    }

    public class DEBIT_TABLE {

        companion object {
            const val TABLE_NAME = "Debit_Table"
            const val DEBIT_ID = "debit_id"
            const val ACTIVITY_ID = "activity_id"
            const val USER_ID = "user_id"
            const val USER_NAME = "user_name"
            const val EXPENSE_ID = "expense_id"
             const val AMOUNT = "amount"

        }

    }



     public class USER_TABLE {

         companion object {
             const val TABLE_NAME = "User_Table"
             const val USER_ID = "user_id"
             const val USER_NAME = "user_name"
             const val USER_PHONE ="user_phone"
             const val USER_EMAIL ="user_email"
         }
     }

    public class CONTACTS_TABLE {
        companion object {
            const val TABLE_NAME = "Contacts_Table"
            const val CONTACT_ID = "Contact_id";
            const val CONTACT_NAME_LOCAL = "Contact_name_local"
            const val CONTACT_NAME_PUBLIC = "Contact_name_public"
            const val CONTACT_PHONE ="Contact_phone"
            const val CONTACT_EMAIL ="Contact_email"
            const val CONTACT_UUID = "Contact_uuid"
            const val CONTACT_PROFILE_IMAGE ="Contact_profile_image"
        }
    }

}