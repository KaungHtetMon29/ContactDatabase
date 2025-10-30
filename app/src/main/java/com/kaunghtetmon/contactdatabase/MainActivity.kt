package com.kaunghtetmon.contactdatabase

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.kaunghtetmon.contactdatabase.data.Contact
import com.kaunghtetmon.contactdatabase.databinding.ActivityMainBinding
import com.kaunghtetmon.contactdatabase.utils.ImageUtils
import com.kaunghtetmon.contactdatabase.viewModel.ContactViewModel
import com.kaunghtetmon.contactdatabase.viewModel.ContactViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var contactViewModel: ContactViewModel
    private var profileImageByteArray: ByteArray? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = ImageUtils.uriToBitmap(this, it)
                bitmap?.let { bmp ->
                    // Compress and save the image
                    profileImageByteArray = ImageUtils.compressImage(bmp)
                    
                    // Create a new bitmap from the compressed data to display
                    val displayBitmap = ImageUtils.byteArrayToBitmap(profileImageByteArray)
                    displayBitmap?.let { displayBmp ->
                        binding.profileImageView.setImageBitmap(displayBmp)
                        binding.profileImageView.visibility = View.VISIBLE
                        binding.defaultProfileIcon.visibility = View.GONE
                    }
                    
                    // Now safe to recycle the original bitmap
                    bmp.recycle()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Snackbar.make(binding.root, "Failed to load image", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Snackbar.make(binding.root, "Permission denied", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up title
        title = "Add Contact"

        // Initialize ViewModel
        val factory = ContactViewModelFactory(application)
        contactViewModel = ViewModelProvider(this, factory)[ContactViewModel::class.java]

        setupViews()
    }

    private fun setupViews() {
        // Profile image click listener
        binding.profileImageCard.setOnClickListener {
            checkPermissionAndOpenPicker()
        }

        // Date picker
        binding.dobEditText.setOnClickListener {
            showDatePicker()
        }
        binding.dobInputLayout.setEndIconOnClickListener {
            showDatePicker()
        }

        // Save button
        binding.saveButton.setOnClickListener {
            saveContact()
        }

        // View button
        binding.viewButton.setOnClickListener {
            val intent = Intent(this, ContactListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkPermissionAndOpenPicker() {
        // For Android 13+ (API 33+), we use READ_MEDIA_IMAGES
        // For Android 10-12, no permission needed for ACTION_GET_CONTENT
        // For below Android 10, we use READ_EXTERNAL_STORAGE
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openImagePicker()
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10 and above - no permission needed for GetContent
            openImagePicker()
        } else {
            // Below Android 10
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openImagePicker()
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
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
                binding.dobEditText.setText(sdf.format(selectedCalendar.time))
                binding.dobInputLayout.error = null
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveContact() {
        val name = binding.nameEditText.text.toString().trim()
        val dob = binding.dobEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()

        var isValid = true

        // Validate name
        if (name.isEmpty()) {
            binding.nameInputLayout.error = "Name cannot be empty"
            isValid = false
        } else {
            binding.nameInputLayout.error = null
        }

        // Validate DOB
        if (dob.isEmpty()) {
            binding.dobInputLayout.error = "Date of Birth must be selected"
            isValid = false
        } else {
            binding.dobInputLayout.error = null
        }

        // Validate email
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Invalid email format"
            isValid = false
        } else if (contactViewModel.isEmailExists(email)) {
            binding.emailInputLayout.error = "Email already exists"
            isValid = false
        } else {
            binding.emailInputLayout.error = null
        }

        if (isValid) {
            val contact = Contact(
                name = name,
                dob = dob,
                email = email,
                profileImage = profileImageByteArray
            )
            contactViewModel.insert(contact)

            // Clear form
            binding.nameEditText.setText("")
            binding.dobEditText.setText("")
            binding.emailEditText.setText("")
            binding.profileImageView.setImageDrawable(null)
            binding.profileImageView.visibility = View.GONE
            binding.defaultProfileIcon.visibility = View.VISIBLE
            profileImageByteArray = null

            Snackbar.make(binding.root, "Contact saved successfully", Snackbar.LENGTH_SHORT).show()
        }
    }
}
