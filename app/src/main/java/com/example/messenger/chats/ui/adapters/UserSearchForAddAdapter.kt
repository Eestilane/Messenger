package com.example.messenger.chats.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.data.models.UserSearchResponse
import com.example.messenger.databinding.ItemUserSearchBinding

class UserSearchForAddAdapter(private val onClick: (UserSearchResponse) -> Unit) : RecyclerView.Adapter<UserSearchForAddAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemUserSearchBinding) :
        RecyclerView.ViewHolder(binding.root)


    private var users = mutableListOf<UserSearchResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSearchForAddAdapter.ViewHolder {
        val binding = ItemUserSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserSearchForAddAdapter.ViewHolder, position: Int) {
        with(holder.binding) {
            userName.text = users[position].name
            userLogin.text = users[position].login
            Glide.with(holder.itemView).load(users[position].avatar).placeholder(R.drawable.avatar).into(userAvatar)
            add.setOnClickListener {
                onClick(users[position])
            }
        }
    }

    override fun getItemCount() = users.size

    fun setUsers(newUsers: List<UserSearchResponse>) {
        users = newUsers.toMutableList()
        notifyDataSetChanged()
    }
}