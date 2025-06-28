package com.example.messenger.chats.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.chats.ui.models.Message
import com.example.messenger.data.models.UserResponse
import com.example.messenger.databinding.ItemMessageBinding
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MessageAdapter (
    private val onEdit: (messageId: String, currentText: String) -> Unit,
    private val onDelete: (messageId: String) -> Unit,
    ): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMessageBinding): RecyclerView.ViewHolder(binding.root)

    private val messages = mutableListOf<Message>()
    private val usersCache = mutableMapOf<String, UserResponse>()
    private val timeFormatter by lazy { DateTimeFormatter.ofPattern("HH:mm") }
    private val utcZone = ZoneId.of("UTC")
    private val moscowZone = ZoneId.of("Europe/Moscow")
    private lateinit var currentUserId: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.ViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageAdapter.ViewHolder, position: Int) {
        with(holder.binding) {
            val message = messages[position]
            val senderId = message.senderId
            val user = usersCache[senderId]
            senderName.text = user?.name
            messageText.text = message.content
            timeText.text = formatTime(message.sentAt)
            editedIndicator.visibility = message.editedAt?.let { View.VISIBLE } ?: View.GONE
            user?.avatar?.let { url ->
                Glide.with(holder.itemView.context).load(url).placeholder(R.drawable.avatar).error(R.drawable.avatar).circleCrop().into(userAvatar)
            }
            root.setOnLongClickListener {
                showContextMenu(it, message)
                true
            }
        }
    }

    override fun getItemCount() = messages.size

    fun updateUsersCache(users: List<UserResponse>) {
        val oldSize = usersCache.size
        usersCache.clear()
        usersCache.putAll(users.associateBy { it.id })
        if (oldSize != usersCache.size) {
            notifyDataSetChanged()
        }
    }

    private fun formatTime(isoTime: String): String {
        return try {
            LocalDateTime.parse(isoTime).atZone(utcZone).withZoneSameInstant(moscowZone).format(timeFormatter)
        } catch (e: Exception) {
            isoTime
        }
    }

    private fun showContextMenu(view: View, message: Message) {
        if (message.senderId != currentUserId) return
        PopupMenu(view.context, view).apply {
            menuInflater.inflate(R.menu.message_menu, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.edit -> onEdit(message.id, message.content)
                    R.id.delete -> onDelete(message.id)
                }
                true
            }
            show()
        }
    }

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }

    fun setMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages.sortedBy { it.sentAt })
        notifyDataSetChanged()
    }

    fun addMessage(message: Message) {
        val insertPos = messages.indexOfFirst { LocalDateTime.parse(it.sentAt) > LocalDateTime.parse(message.sentAt) }.takeIf { it != -1 } ?: messages.size
        messages.add(insertPos, message)
        notifyItemInserted(insertPos)
    }

    fun updateMessage(messageId: String, newText: String) {
        val position = messages.indexOfFirst { it.id == messageId }
        if (position != -1) {
            val updatedMessage = messages[position].copy(content = newText, editedAt = LocalDateTime.now().toString())
            messages[position] = updatedMessage
            notifyItemChanged(position)
        }
    }

    fun removeMessage(messageId: String) {
        val position = messages.indexOfFirst { it.id == messageId }
        if (position != -1) {
            messages.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}