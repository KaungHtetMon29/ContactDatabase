package com.kaunghtetmon.contactdatabase.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaunghtetmon.contactdatabase.data.Contact
import com.kaunghtetmon.contactdatabase.data.DatabaseHelper
import kotlinx.coroutines.launch

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)

    private val _allContacts = MutableLiveData<List<Contact>>(emptyList())
    val allContacts: LiveData<List<Contact>> = _allContacts

    init {
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            _allContacts.value = dbHelper.getAllContacts()
        }
    }

    fun refreshContacts() {
        loadContacts()
    }

    fun isEmailExists(email: String): Boolean {
        return dbHelper.isEmailExists(email)
    }

    fun isEmailExistsExcludingContact(email: String, contactId: Int): Boolean {
        return dbHelper.isEmailExistsExcludingContact(email, contactId)
    }

    fun insert(contact: Contact) {
        viewModelScope.launch {
            dbHelper.addContact(contact)
            loadContacts()
        }
    }

    fun update(contact: Contact): Boolean {
        val result = dbHelper.updateContact(contact)
        if (result) {
            loadContacts()
        }
        return result
    }

    fun delete(contactId: Int): Boolean {
        val result = dbHelper.deleteContact(contactId)
        if (result) {
            loadContacts()
        }
        return result
    }
}

class ContactViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
