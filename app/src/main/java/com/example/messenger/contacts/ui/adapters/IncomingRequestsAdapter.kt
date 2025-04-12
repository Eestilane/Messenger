package com.example.messenger.contacts.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.data.models.contacts.RequestResponse
import com.example.messenger.databinding.ItemIncomingRequestBinding

class IncomingRequestsAdapter(val accept: (RequestResponse) -> Unit, val decline: (RequestResponse) -> Unit): RecyclerView.Adapter<IncomingRequestsAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemIncomingRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var requests = mutableListOf<RequestResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomingRequestsAdapter.ViewHolder {
        val binding = ItemIncomingRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncomingRequestsAdapter.ViewHolder, position: Int) {
        with(holder.binding) {
            userName.text = requests[position].name
            userLogin.text = requests[position].login
            Glide.with(holder.itemView).load(requests[position].avatar).placeholder(R.drawable.avatar).into(userAvatar)
            accept.setOnClickListener {
                accept(requests[position])
            }
            decline.setOnClickListener {
                decline(requests[position])
            }
        }
    }

    override fun getItemCount() = requests.size

    fun setIncomingRequests(newContacts: List<RequestResponse>) {
        requests = newContacts.toMutableList()
        notifyDataSetChanged()
    }
}