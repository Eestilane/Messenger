package com.example.messenger.contacts.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.data.models.contacts.ContactsResponse
import com.example.messenger.databinding.ItemContactBinding

class ContactsAdapter(val onClick: (ContactsResponse) -> Unit):RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemContactBinding): RecyclerView.ViewHolder(binding.root)

    private val contacts = mutableListOf<ContactsResponse>()
    private val filteredContacts = mutableListOf<ContactsResponse>()
    var lastExpression: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactsAdapter.ViewHolder, position: Int) {
        with(holder.binding) {
            userName.text = filteredContacts[position].name
            userLogin.text = filteredContacts[position].login
            Glide.with(holder.itemView).load(filteredContacts[position].avatar).placeholder(R.drawable.avatar).into(userAvatar)
            delete.setOnClickListener {
                onClick(filteredContacts[position])
            }
        }
    }

    fun setContacts(newContacts: List<ContactsResponse>) {
        contacts.clear()
        contacts.addAll(newContacts)
        filteredContacts.clear()
        filteredContacts.addAll(newContacts)
        notifyDataSetChanged()
    }

    override fun getItemCount() = filteredContacts.size

    fun filter(expression: String?){
        lastExpression = expression.toString()
        filteredContacts.clear()
        if (expression.isNullOrEmpty()){
            filteredContacts.addAll(contacts)
            notifyDataSetChanged()
            return
        }

        for (user in contacts) {
            if (user.name.contains(expression, true) || user.login.contains(expression, true)) {
                filteredContacts.add(user)
            }
        }

        notifyDataSetChanged()
    }

    fun updateFilter() {
        filter(lastExpression)
    }
}