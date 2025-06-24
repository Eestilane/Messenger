package com.example.messenger.chats.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.chats.ui.models.Chat
import com.example.messenger.databinding.ItemChatBinding
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ChatsAdapter(private var chats: List<Chat>, private val onClick: (Chat) -> Unit) :
    RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    private val lastMessages = mutableMapOf<String, Pair<String, LocalDateTime?>>()
    private val moscowZone = ZoneId.of("Europe/Moscow")

    inner class ViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val chat = chats[position]
            chatName.text = chat.name
            Glide.with(holder.itemView)
                .load(chat.avatar)
                .placeholder(R.drawable.chat_placeholder)
                .circleCrop()
                .into(chatAvatar)

            val (message, date) = lastMessages[chat.id] ?: Pair("Нет сообщений", null)

            lastMessage.text = message
            timeLastMessage.text = date?.let { formatMessageTime(it) } ?: ""
            root.setOnClickListener { onClick(chat) }
        }
    }

    private fun formatMessageTime(dateTime: LocalDateTime): String {
        if (dateTime.isBefore(LocalDateTime.of(2000, 1, 1, 0, 0))) {
            return ""
        }
        val now = LocalDateTime.now(moscowZone)
        return when {
            dateTime.isAfter(now.minusDays(1)) -> dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            dateTime.isAfter(now.minusDays(7)) -> dateTime.format(DateTimeFormatter.ofPattern("E"))
            else -> dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yy"))
        }
    }

    override fun getItemCount() = chats.size

    fun updateList(newChats: List<Chat>) {
        chats = newChats.sortedByDescending { lastMessages[it.id]?.second ?: LocalDateTime.MIN }
        notifyDataSetChanged()
    }

    fun updateLastMessage(chatId: String, message: String, dateTime: LocalDateTime) {
        val moscowTime = dateTime?.atZone(ZoneId.systemDefault()) ?.withZoneSameInstant(moscowZone) ?.toLocalDateTime()
        lastMessages[chatId] = Pair(message, moscowTime)
        updateList(chats)
    }
}