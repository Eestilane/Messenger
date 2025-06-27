package com.example.messenger.chats.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.data.models.contacts.ContactsResponse
import com.example.messenger.databinding.ItemUserSearchBinding

class UserSearchForAddAdapter(val onClick: (ContactsResponse) -> Unit) : RecyclerView.Adapter<UserSearchForAddAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemUserSearchBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val contacts = mutableListOf<ContactsResponse>()
    private val filteredContacts = mutableListOf<ContactsResponse>()
    var lastExpression: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSearchForAddAdapter.ViewHolder {
        val binding = ItemUserSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserSearchForAddAdapter.ViewHolder, position: Int) {
        with(holder.binding) {
            val contact = contacts[position]
            val filteredContact = filteredContacts[position]
            userName.text = contact.name
            userLogin.text = contact.login
            contact.avatar?.takeIf { it.isNotEmpty() }?.let { url ->
                Glide.with(holder.itemView).load(url).placeholder(R.drawable.avatar).error(R.drawable.avatar).circleCrop().into(userAvatar)
            }
            add.setOnClickListener {
                onClick(filteredContact)
            }
        }
    }

    override fun getItemCount() = filteredContacts.size

    fun setContacts (newContacts: List<ContactsResponse>) {
        contacts.clear()
        contacts.addAll(newContacts)
        filteredContacts.clear()
        filteredContacts.addAll(newContacts)
        notifyDataSetChanged()
    }

    fun filter(expression: String?){
        lastExpression = expression.toString()
        filteredContacts.clear()
        if (expression.isNullOrEmpty()){
            filteredContacts.addAll(contacts)
            notifyDataSetChanged()
            return
        }

        for (contact in contacts) {
            if (contact.name.contains(expression, true) || contact.login.contains(expression, true)) {
                filteredContacts.add(contact)
            }
        }
        notifyDataSetChanged()
    }

    fun updateFilter() {
        filter(lastExpression)
    }
}