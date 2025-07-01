package com.example.messenger.chats.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
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

    private val diffUtilCallback: DiffUtil.ItemCallback<Message> = object: DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean = (oldItem.id == newItem.id)

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean = (oldItem.content == newItem.content
                && oldItem.sentAt == newItem.sentAt
                && oldItem.editedAt == newItem.editedAt)
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtilCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.ViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageAdapter.ViewHolder, position: Int) {
        with(holder.binding) {
            Glide.with(holder.itemView).clear(userAvatar)
            val message = asyncListDiffer.currentList[position]
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
                if (message.senderId == currentUserId) {
                    showContextMenu(it, message)
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun getItemCount() = asyncListDiffer.currentList.size

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }

    fun setMessages(newMessages: List<Message>) {
        asyncListDiffer.submitList(newMessages.sortedBy { it.sentAt })
    }

    fun addMessage(message: Message) {
        val newList = asyncListDiffer.currentList.toMutableList()
        newList.add(message)
        asyncListDiffer.submitList(newList)
    }

    fun updateMessage(messageId: String, newText: String) {
        val newList = asyncListDiffer.currentList.toMutableList()
        val position = newList.indexOfFirst { it.id == messageId }
        if (position != -1) {
            newList[position] = newList[position].copy(content = newText, editedAt = LocalDateTime.now().toString())
            asyncListDiffer.submitList(newList)
        }
    }

    fun removeMessage(messageId: String) {
        val newList = asyncListDiffer.currentList.toMutableList()
        val position = messages.indexOfFirst { it.id == messageId }
        if (position != -1) {
            newList.removeAt(position)
            asyncListDiffer.submitList(newList)
        }
    }

    fun updateUsersCache(users: List<UserResponse>) {
        usersCache.clear()
        usersCache.putAll(users.associateBy { it.id })
        val positions = asyncListDiffer.currentList.mapIndexedNotNull { index, message ->
            index.takeIf { usersCache.containsKey(message.senderId) }
        }
        positions.forEach { notifyItemChanged(it) }
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
}