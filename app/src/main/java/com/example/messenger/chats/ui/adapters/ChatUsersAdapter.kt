package com.example.messenger.chats.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.data.models.UserResponse
import com.example.messenger.databinding.ItemChatUsersBinding

class ChatUsersAdapter (
    private val ownerId: String,
    private val onDeleteClick: (UserResponse) -> Unit) :RecyclerView.Adapter<ChatUsersAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemChatUsersBinding): RecyclerView.ViewHolder(binding.root)

    private var chatUsers = mutableListOf<UserResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUsersAdapter.ViewHolder {
        val binding = ItemChatUsersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatUsersAdapter.ViewHolder, position: Int) {
        with(holder.binding) {
            if (chatUsers[position].id == ownerId) {
                userName.text = chatUsers[position].name + " (админ)"
                deleteInFrame.visibility = View.GONE
            } else {
                userName.text = chatUsers[position].name
            }
            userLogin.text = chatUsers[position].login
            Glide.with(holder.itemView).load(chatUsers[position].avatar).placeholder(R.drawable.avatar).into(userAvatar)
            deleteInFrame.setOnClickListener {
                onDeleteClick(chatUsers[position])
            }
        }
    }

    override fun getItemCount() = chatUsers.size

    fun setChatUsers(newUsers: List<UserResponse>) {
        chatUsers = newUsers.toMutableList()
        notifyDataSetChanged()
    }
}