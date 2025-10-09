package com.example.contactdatabase.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.contactdatabase.data.Contact
import com.example.contactdatabase.data.DatabaseHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactViewModel(application: Application) : ViewModel() {

    private val dbHelper = DatabaseHelper(application)

    private val _allContacts = MutableStateFlow<List<Contact>>(emptyList())
    val allContacts: StateFlow<List<Contact>> = _allContacts

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

    fun insert(contact: Contact) {
        viewModelScope.launch {
            dbHelper.addContact(contact)
            loadContacts()
        }
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
