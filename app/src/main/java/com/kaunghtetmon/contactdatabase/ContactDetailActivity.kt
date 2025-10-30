package com.kaunghtetmon.contactdatabase

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.kaunghtetmon.contactdatabase.data.Contact
import com.kaunghtetmon.contactdatabase.databinding.ActivityContactDetailBinding
import com.kaunghtetmon.contactdatabase.databinding.ActivityContactEditBinding
import com.kaunghtetmon.contactdatabase.utils.ImageUtils
import com.kaunghtetmon.contactdatabase.viewModel.ContactViewModel
import com.kaunghtetmon.contactdatabase.viewModel.ContactViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class ContactDetailActivity : AppCompatActivity() {

    private lateinit var detailBinding: ActivityContactDetailBinding
    private lateinit var editBinding: ActivityContactEditBinding
    private lateinit var contactViewModel: ContactViewModel
    private var contactId: Int = 0
    private var contactName: String = ""
    private var contactDob: String = ""
    private var contactEmail: String = ""
    private var contactImage: ByteArray? = null
    private var profileImageByteArray: ByteArray? = null
    private var isEditMode = false

    companion object {
        const val EXTRA_CONTACT_ID = "contact_id"
        const val EXTRA_CONTACT_NAME = "contact_name"
        const val EXTRA_CONTACT_DOB = "contact_dob"
        const val EXTRA_CONTACT_EMAIL = "contact_email"
        const val EXTRA_CONTACT_IMAGE = "contact_image"
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bmp = ImageUtils.uriToBitmap(this, it)
                bmp?.let { bitmap ->
                    // Compress image
                    val compressedBytes = ImageUtils.compressImage(bitmap)
                    
                    // Create a new bitmap from compressed data for display
                    val displayBitmap = ImageUtils.byteArrayToBitmap(compressedBytes)
                    
                    if (displayBitmap != null) {
                        editBinding.editProfileImageView.setImageBitmap(displayBitmap)
                        editBinding.editProfileImageView.visibility = View.VISIBLE
                        editBinding.editDefaultProfileIcon.visibility = View.GONE
                        profileImageByteArray = compressedBytes
                    }
                    
                    // Recycle the original bitmap
                    bitmap.recycle()
                }
            } catch (e: Exception) {
                Snackbar.make(editBinding.root, "Failed to load image", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openImagePicker()
        } else {
            Snackbar.make(editBinding.root, "Permission denied", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize both bindings
        detailBinding = ActivityContactDetailBinding.inflate(layoutInflater)
        editBinding = ActivityContactEditBinding.inflate(layoutInflater)
        
        // Start with detail view
        setContentView(detailBinding.root)

        // Set up action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Contact Details"

        // Initialize ViewModel
        val factory = ContactViewModelFactory(application)
        contactViewModel = ViewModelProvider(this, factory)[ContactViewModel::class.java]

        // Get contact data from intent
        contactId = intent.getIntExtra(EXTRA_CONTACT_ID, 0)
        contactName = intent.getStringExtra(EXTRA_CONTACT_NAME) ?: ""
        contactDob = intent.getStringExtra(EXTRA_CONTACT_DOB) ?: ""
        contactEmail = intent.getStringExtra(EXTRA_CONTACT_EMAIL) ?: ""
        contactImage = intent.getByteArrayExtra(EXTRA_CONTACT_IMAGE)
        profileImageByteArray = contactImage

        setupDetailView()
    }

    private fun setupDetailView() {
        isEditMode = false
        supportActionBar?.title = "Contact Details"
        
        displayContactDetails()
        
        // Set up edit button
        detailBinding.editButton.setOnClickListener {
            switchToEditMode()
        }
        
        // Set up delete button
        detailBinding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun setupEditView() {
        isEditMode = true
        supportActionBar?.title = "Edit Contact"
        
        // Populate edit fields with current data
        editBinding.editNameEditText.setText(contactName)
        editBinding.editDobEditText.setText(contactDob)
        editBinding.editEmailEditText.setText(contactEmail)

        // Set profile image
        profileImageByteArray?.let { bytes ->
            val bitmap = ImageUtils.byteArrayToBitmap(bytes)
            if (bitmap != null) {
                editBinding.editProfileImageView.setImageBitmap(bitmap)
                editBinding.editProfileImageView.visibility = View.VISIBLE
                editBinding.editDefaultProfileIcon.visibility = View.GONE
            } else {
                showEditDefaultIcon()
            }
        } ?: showEditDefaultIcon()

        // Set up image picker
        editBinding.updateImageButton.setOnClickListener {
            checkPermissionAndOpenImagePicker()
        }

        // Set up date picker
        editBinding.editDobEditText.setOnClickListener {
            showDatePicker()
        }
        
        editBinding.editDobInputLayout.setEndIconOnClickListener {
            showDatePicker()
        }

        // Set up save button
        editBinding.saveButton.setOnClickListener {
            saveContact()
        }

        // Set up cancel button
        editBinding.cancelButton.setOnClickListener {
            switchToDetailMode()
        }
    }

    private fun switchToEditMode() {
        setContentView(editBinding.root)
        setupEditView()
    }

    private fun switchToDetailMode() {
        setContentView(detailBinding.root)
        setupDetailView()
    }

    private fun displayContactDetails() {
        // Set name
        detailBinding.detailNameTextView.text = contactName

        // Set email
        detailBinding.detailEmailTextView.text = contactEmail

        // Set date of birth
        detailBinding.detailDobTextView.text = contactDob

        // Set profile image
        profileImageByteArray?.let { bytes ->
            val bitmap = ImageUtils.byteArrayToBitmap(bytes)
            if (bitmap != null) {
                detailBinding.detailProfileImageView.setImageBitmap(bitmap)
                detailBinding.detailProfileImageView.visibility = View.VISIBLE
                detailBinding.detailDefaultProfileIcon.visibility = View.GONE
            } else {
                showDetailDefaultIcon()
            }
        } ?: showDetailDefaultIcon()
    }

    private fun showDetailDefaultIcon() {
        detailBinding.detailProfileImageView.visibility = View.GONE
        detailBinding.detailDefaultProfileIcon.visibility = View.VISIBLE
    }

    private fun showEditDefaultIcon() {
        editBinding.editProfileImageView.visibility = View.GONE
        editBinding.editDefaultProfileIcon.visibility = View.VISIBLE
    }

    private fun checkPermissionAndOpenImagePicker() {
        when {
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ uses READ_MEDIA_IMAGES
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openImagePicker()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else -> {
                // Below Android 13 uses READ_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openImagePicker()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                editBinding.editDobEditText.setText(sdf.format(selectedCalendar.time))
                editBinding.editDobInputLayout.error = null
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun saveContact() {
        val name = editBinding.editNameEditText.text.toString().trim()
        val dob = editBinding.editDobEditText.text.toString().trim()
        val email = editBinding.editEmailEditText.text.toString().trim()

        // Validate inputs
        var isValid = true

        if (name.isEmpty()) {
            editBinding.editNameInputLayout.error = "Name cannot be empty"
            isValid = false
        } else {
            editBinding.editNameInputLayout.error = null
        }

        if (dob.isEmpty()) {
            editBinding.editDobInputLayout.error = "Date of Birth cannot be empty"
            isValid = false
        } else {
            editBinding.editDobInputLayout.error = null
        }

        if (email.isEmpty()) {
            editBinding.editEmailInputLayout.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editBinding.editEmailInputLayout.error = "Invalid email format"
            isValid = false
        } else {
            // Check if email exists for other contacts (excluding current contact)
            if (contactViewModel.isEmailExistsExcludingContact(email, contactId)) {
                editBinding.editEmailInputLayout.error = "Email already exists"
                isValid = false
            } else {
                editBinding.editEmailInputLayout.error = null
            }
        }

        if (!isValid) return

        // Update contact
        val contact = Contact(
            id = contactId,
            name = name,
            dob = dob,
            email = email,
            profileImage = profileImageByteArray
        )

        val success = contactViewModel.update(contact)
        if (success) {
            // Update local data
            contactName = name
            contactDob = dob
            contactEmail = email
            contactImage = profileImageByteArray

            Snackbar.make(editBinding.root, "Contact updated successfully", Snackbar.LENGTH_SHORT).show()
            
            // Switch back to detail view
            switchToDetailMode()
        } else {
            Snackbar.make(editBinding.root, "Failed to update contact", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete this contact? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteContact()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteContact() {
        val success = contactViewModel.delete(contactId)
        if (success) {
            Snackbar.make(detailBinding.root, "Contact deleted successfully", Snackbar.LENGTH_SHORT).show()
            finish() // Close the activity and return to list
        } else {
            Snackbar.make(detailBinding.root, "Failed to delete contact", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        if (!isEditMode) return false

        val currentName = editBinding.editNameEditText.text.toString().trim()
        val currentDob = editBinding.editDobEditText.text.toString().trim()
        val currentEmail = editBinding.editEmailEditText.text.toString().trim()

        // Check if any field has changed
        if (currentName != contactName) return true
        if (currentDob != contactDob) return true
        if (currentEmail != contactEmail) return true

        // Check if image has changed
        if (profileImageByteArray != contactImage) {
            // Compare byte arrays
            if (profileImageByteArray == null && contactImage != null) return true
            if (profileImageByteArray != null && contactImage == null) return true
            if (profileImageByteArray != null && contactImage != null) {
                if (!profileImageByteArray.contentEquals(contactImage)) return true
            }
        }

        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (isEditMode) {
                    // Only ask confirmation if there are unsaved changes
                    if (hasUnsavedChanges()) {
                        AlertDialog.Builder(this)
                            .setTitle("Discard Changes?")
                            .setMessage("Are you sure you want to discard your changes?")
                            .setPositiveButton("Discard") { _, _ ->
                                switchToDetailMode()
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    } else {
                        switchToDetailMode()
                    }
                } else {
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (isEditMode) {
            // Only ask confirmation if there are unsaved changes
            if (hasUnsavedChanges()) {
                AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("Are you sure you want to discard your changes?")
                    .setPositiveButton("Discard") { _, _ ->
                        switchToDetailMode()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                switchToDetailMode()
            }
        } else {
            super.onBackPressed()
        }
    }
}
