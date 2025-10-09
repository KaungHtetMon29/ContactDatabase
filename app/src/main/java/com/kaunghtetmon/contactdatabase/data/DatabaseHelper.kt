package com.kaunghtetmon.contactdatabase.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "ContactDatabase.db"
        private const val TABLE_CONTACTS = "contacts"

        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_DOB = "dob"
        private const val KEY_EMAIL = "email"
        private const val KEY_PROFILE_IMAGE = "profile_image"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME + " TEXT,"
                + KEY_DOB + " TEXT," + KEY_EMAIL + " TEXT,"
                + KEY_PROFILE_IMAGE + " BLOB" + ")")
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        onCreate(db)
    }

    fun addContact(contact: Contact) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_NAME, contact.name)
        contentValues.put(KEY_DOB, contact.dob)
        contentValues.put(KEY_EMAIL, contact.email)
        contentValues.put(KEY_PROFILE_IMAGE, contact.profileImage)

        db.insert(TABLE_CONTACTS, null, contentValues)
        db.close()
    }

    fun getAllContacts(): List<Contact> {
        val contactList = ArrayList<Contact>()
        val selectQuery = "SELECT * FROM $TABLE_CONTACTS"

        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val contact = Contact(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),
                    dob = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DOB)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)),
                    profileImage = cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_PROFILE_IMAGE))
                )
                contactList.add(contact)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return contactList
    }

    fun isEmailExists(email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_CONTACTS WHERE $KEY_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }
}
