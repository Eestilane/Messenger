package com.example.messenger.chats.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.chats.ui.fragments.ChatFragment
import com.example.messenger.chats.ui.models.Chat
import com.example.messenger.databinding.ItemChatBinding

class ChatsAdapter(private var chats: List<Chat>, private val onClick: (chatId: String) -> Unit) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemChatBinding): RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            chatName.text = chats[position].name
            root.setOnClickListener {
                ChatFragment.currentChatName = chats[position].name
                onClick(chats[position].id)
            }
        }
    }

    override fun getItemCount() = chats.size

    fun updateList(newChats: List<Chat>) {
        chats = newChats
        notifyDataSetChanged()
    }
}