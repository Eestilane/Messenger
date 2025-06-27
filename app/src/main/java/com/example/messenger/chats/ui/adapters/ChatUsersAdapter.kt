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
            val user = chatUsers[position]
            userName.text = if (user.id == ownerId) {
                deleteInFrame.visibility = View.GONE
                "${user.name} (админ)"
            } else {
                user.name
            }
            userLogin.text = user.login
            user.avatar?.takeIf{ it.isNotEmpty() }?.let { url ->
                Glide.with(holder.itemView).load(url).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(userAvatar)
            }
            deleteInFrame.setOnClickListener {
                onDeleteClick(user)
            }
        }
    }

    override fun getItemCount() = chatUsers.size

    fun setChatUsers(newUsers: List<UserResponse>) {
        chatUsers = newUsers.toMutableList()
        notifyDataSetChanged()
    }
}