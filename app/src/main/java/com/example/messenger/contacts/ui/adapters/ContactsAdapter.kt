package com.example.messenger.contacts.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.data.models.contacts.ContactsResponse
import com.example.messenger.databinding.ItemContactBinding

class ContactsAdapter(val onClick: (ContactsResponse) -> Unit) : RecyclerView.Adapter<ContactsAdapter.ViewHolder> (), Filterable {
    inner class ViewHolder(val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var contacts = mutableListOf<ContactsResponse>()
    private var filteredContacts = mutableListOf<ContactsResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactsAdapter.ViewHolder, position: Int) {
        with(holder.binding) {
            userName.text = filteredContacts[position].name
            userLogin.text = filteredContacts[position].login
            Glide.with(holder.itemView).load(filteredContacts[position].avatar).into(userAvatar)
            delete.setOnClickListener {
                onClick(filteredContacts[position])
            }
        }
    }

    fun setContacts(newContacts: List<ContactsResponse>) {
        contacts = newContacts.toMutableList()
        filteredContacts = newContacts.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount() = filteredContacts.size

    override fun getFilter() = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            val filtered: MutableList<ContactsResponse> = mutableListOf()

            if (constraint.isNullOrEmpty()) {
                filtered.addAll(contacts)
            } else {
                for (user in contacts) {
                    if (user.name.contains(constraint, true) || user.login.contains(constraint, true)) {
                        filtered.add(user)
                    }
                }
            }

            results.values = filtered
            results.count = filtered.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredContacts = results?.values as? MutableList<ContactsResponse> ?: mutableListOf()
            notifyDataSetChanged()
        }
    }
}
