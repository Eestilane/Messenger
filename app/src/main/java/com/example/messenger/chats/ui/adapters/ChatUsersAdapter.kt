package com.example.messenger.chats.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.data.models.UserSearchResponse
import com.example.messenger.databinding.ItemContactBinding

class ChatUsersAdapter (private val onClick: (UserSearchResponse) -> Unit) : RecyclerView.Adapter<ChatUsersAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root)


    private var chatUsers = mutableListOf<UserSearchResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUsersAdapter.ViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatUsersAdapter.ViewHolder, position: Int) {
        with(holder.binding) {
            userName.text = chatUsers[position].name
            userLogin.text = chatUsers[position].login
            Glide.with(holder.itemView).load(chatUsers[position].avatar).placeholder(R.drawable.avatar).into(userAvatar)
            delete.setOnClickListener {
                onClick(chatUsers[position])
            }
        }
    }

    override fun getItemCount() = chatUsers.size

    fun setChatUsers(newUsers: List<UserSearchResponse>) {
        chatUsers = newUsers.toMutableList()
        notifyDataSetChanged()
    }
}