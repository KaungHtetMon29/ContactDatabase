package com.kaunghtetmon.contactdatabase.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kaunghtetmon.contactdatabase.data.Contact
import com.kaunghtetmon.contactdatabase.databinding.ItemContactBinding
import com.kaunghtetmon.contactdatabase.utils.ImageUtils

class ContactAdapter(
    private val onItemClick: (Contact) -> Unit,
    private val onDeleteClick: (Contact) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding, onItemClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ContactViewHolder(
        private val binding: ItemContactBinding,
        private val onItemClick: (Contact) -> Unit,
        private val onDeleteClick: (Contact) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            binding.contactNameTextView.text = "Name: ${contact.name}"
            binding.contactDobTextView.text = "DoB: ${contact.dob}"
            binding.contactEmailTextView.text = "Email: ${contact.email}"

            // Handle profile image
            contact.profileImage?.let { imageBytes ->
                val bitmap = ImageUtils.byteArrayToBitmap(imageBytes)
                if (bitmap != null) {
                    binding.contactImageView.setImageBitmap(bitmap)
                    binding.contactImageView.visibility = View.VISIBLE
                    binding.defaultContactIcon.visibility = View.GONE
                } else {
                    showDefaultIcon()
                }
            } ?: showDefaultIcon()

            // Set click listener for viewing contact details
            binding.root.setOnClickListener {
                onItemClick(contact)
            }

            // Set click listener for delete button
            binding.deleteContactButton.setOnClickListener {
                onDeleteClick(contact)
            }
        }

        private fun showDefaultIcon() {
            binding.contactImageView.visibility = View.GONE
            binding.defaultContactIcon.visibility = View.VISIBLE
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}

