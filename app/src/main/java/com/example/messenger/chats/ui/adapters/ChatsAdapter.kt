package com.example.messenger.chats.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.chats.ui.models.Chat
import com.example.messenger.chats.ui.models.ChatNavigationParameters
import com.example.messenger.databinding.ItemChatBinding
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ChatsAdapter(private var chats: List<Chat>, private val onClick: (ChatNavigationParameters) -> Unit): RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    private val moscowZone = ZoneId.of("Europe/Moscow")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy")
    private val utcZone = ZoneId.of("UTC")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val chat = chats[position]
            chatName.text = chat.name
            chat.avatar?.takeIf { it.isNotEmpty() }?.let { url ->
                Glide.with(holder.itemView).load(url).placeholder(R.drawable.avatar).error(R.drawable.avatar).circleCrop().into(chatAvatar)
            }
            lastMessage.text = chat.lastMessage?.content ?: "Нет сообщений"
            timeLastMessage.text = formatTime(chat.lastMessage?.sentAt)
            root.setOnClickListener {
                onClick.invoke(ChatNavigationParameters(chat.id, chat.ownerId, chat.name, chat.avatar, chat.isDirect))
            }
        }
    }

    private fun formatTime(isoTime: String?): String? {
        return try {
            val messageTime = LocalDateTime.parse(isoTime).atZone(utcZone).withZoneSameInstant(moscowZone)
            val now = ZonedDateTime.now(moscowZone)
            when {
                messageTime.toLocalDate() == now.toLocalDate() -> messageTime.format(timeFormatter)
                messageTime.toLocalDate() == now.minusDays(1).toLocalDate() -> "Вчера, " + messageTime.format(timeFormatter)
                else -> messageTime.format(dateFormatter)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getItemCount() = chats.size

    fun updateList(newChats: List<Chat>) {
        chats = newChats.sortedByDescending { chat -> chat.lastMessage?.sentAt?.let { LocalDateTime.parse(it) } ?: LocalDateTime.MIN }
        notifyDataSetChanged()
    }
}