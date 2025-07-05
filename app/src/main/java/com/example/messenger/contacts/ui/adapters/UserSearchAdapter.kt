package com.example.messenger.contacts.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.data.models.UserSearchResponse
import com.example.messenger.databinding.ItemUserSearchBinding

class UserSearchAdapter(private val onClick: (UserSearchResponse) -> Unit) : RecyclerView.Adapter<UserSearchAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemUserSearchBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var contacts = mutableListOf<UserSearchResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSearchAdapter.ViewHolder {
        val binding = ItemUserSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserSearchAdapter.ViewHolder, position: Int) {
        val contact = contacts[position]
        with(holder.binding) {
            inContacts.visibility = if (contact.isInContacts) View.VISIBLE else View.GONE
            pendingrequest.visibility = if (contact.hasPendingRequest) View.VISIBLE else View.GONE
            add.visibility = if (contact.hasPendingRequest || contact.isInContacts) View.GONE else View.VISIBLE
            userName.text = contact.name
            userLogin.text = contact.login
            contact?.avatar.let {
                Glide.with(holder.itemView).load(contact.avatar).placeholder(R.drawable.avatar).into(userAvatar)
            }
            add.setOnClickListener {
                onClick(contact)
            }
        }
    }

    override fun getItemCount() = contacts.size

    fun setContacts(newContacts: List<UserSearchResponse>) {
        contacts = newContacts.toMutableList()
        notifyDataSetChanged()
    }
}