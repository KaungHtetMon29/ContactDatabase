package com.kaunghtetmon.contactdatabase

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kaunghtetmon.contactdatabase.adapter.ContactAdapter
import com.kaunghtetmon.contactdatabase.data.Contact
import com.kaunghtetmon.contactdatabase.databinding.ActivityContactListBinding
import com.kaunghtetmon.contactdatabase.viewModel.ContactViewModel
import com.kaunghtetmon.contactdatabase.viewModel.ContactViewModelFactory

class ContactListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactListBinding
    private lateinit var contactViewModel: ContactViewModel
    private lateinit var contactAdapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Contact List"

        // Initialize ViewModel
        val factory = ContactViewModelFactory(application)
        contactViewModel = ViewModelProvider(this, factory)[ContactViewModel::class.java]

        setupRecyclerView()
        observeContacts()

        // Refresh contacts
        contactViewModel.refreshContacts()
    }

    private fun setupRecyclerView() {
        contactAdapter = ContactAdapter(
            onItemClick = { contact ->
                openContactDetail(contact)
            },
            onDeleteClick = { contact ->
                showDeleteConfirmationDialog(contact)
            }
        )
        binding.contactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ContactListActivity)
            adapter = contactAdapter
        }
    }

    private fun openContactDetail(contact: Contact) {
        val intent = Intent(this, ContactDetailActivity::class.java).apply {
            putExtra(ContactDetailActivity.EXTRA_CONTACT_ID, contact.id)
            putExtra(ContactDetailActivity.EXTRA_CONTACT_NAME, contact.name)
            putExtra(ContactDetailActivity.EXTRA_CONTACT_DOB, contact.dob)
            putExtra(ContactDetailActivity.EXTRA_CONTACT_EMAIL, contact.email)
            putExtra(ContactDetailActivity.EXTRA_CONTACT_IMAGE, contact.profileImage)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(contact: Contact) {
        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete ${contact.name}? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteContact(contact)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteContact(contact: Contact) {
        val success = contactViewModel.delete(contact.id)
        if (success) {
            Snackbar.make(binding.root, "${contact.name} deleted successfully", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, "Failed to delete contact", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeContacts() {
        contactViewModel.allContacts.observe(this) { contacts ->
            if (contacts.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.contactsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.contactsRecyclerView.visibility = View.VISIBLE
                contactAdapter.submitList(contacts)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the contact list when returning from detail view
        contactViewModel.refreshContacts()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

