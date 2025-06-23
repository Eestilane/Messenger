package com.example.messenger.chats.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.chats.ui.models.Chat
import com.example.messenger.databinding.ItemChatBinding

class ChatsAdapter(private var chats: List<Chat>, private val onClick: (Chat) -> Unit) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    private val lastMessages = mutableMapOf<String, Pair<String, String>>()

    inner class ViewHolder(val binding: ItemChatBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val chat = chats[position]
            chatName.text = chats[position].name
            Glide.with(holder.itemView).load(chats[position].avatar).placeholder(R.drawable.chat_placeholder).circleCrop().into(chatAvatar)
            val (message, time) = lastMessages[chats[position].id] ?: Pair("Создан чат ${chats[position].name}", "")
            lastMessage.text = message
            timeLastMessage.text = time
            root.setOnClickListener { onClick(chat) }
        }
    }

    override fun getItemCount() = chats.size

    fun updateList(newChats: List<Chat>) {
        chats = newChats
        notifyDataSetChanged()
    }

    fun updateLastMessage(chatId: String, message: String, time: String) {
        lastMessages[chatId] = Pair(message, time)
        chats = chats.sortedByDescending { lastMessages[it.id]?.second ?: "" }
        notifyDataSetChanged()
    }
}