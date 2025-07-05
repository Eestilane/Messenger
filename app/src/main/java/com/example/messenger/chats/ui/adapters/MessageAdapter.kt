package com.example.messenger.chats.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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

    private val usersCache = mutableMapOf<String, UserResponse>()
    private lateinit var currentUserId: String
    private val timeCache = mutableMapOf<String, String>()
    companion object {
        private val UTC_ZONE = ZoneId.of("UTC")
        private val MOSCOW_ZONE = ZoneId.of("Europe/Moscow")
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
    }

    private val diffUtilCallback: DiffUtil.ItemCallback<Message> = object: DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean = (oldItem.id == newItem.id)

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean = (
                oldItem.content == newItem.content &&
                oldItem.sentAt == newItem.sentAt &&
                oldItem.editedAt == newItem.editedAt &&
                usersCache[oldItem.senderId]?.name == usersCache[newItem.senderId]?.name &&
                usersCache[oldItem.senderId]?.avatar == usersCache[newItem.senderId]?.avatar
                )
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtilCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.ViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageAdapter.ViewHolder, position: Int) {
        val message = asyncListDiffer.currentList[position]
        val senderId = message.senderId
        val user = usersCache[senderId]
        val isMyMessage = asyncListDiffer.currentList.getOrNull(position)?.senderId == currentUserId
        with(holder.binding) {
            messageFrame.visibility = if (isMyMessage) View.GONE else View.VISIBLE
            myMessageFrame.visibility = if (isMyMessage) View.VISIBLE else View.GONE
            userAvatar.visibility = if (isMyMessage) View.GONE else View.VISIBLE
            if (isMyMessage) {
                myMessageText.text = message.content
                myTimeText.text = formatTime(message.sentAt)
                myEditedIndicator.visibility = message.editedAt?.let { View.VISIBLE } ?: View.GONE
            } else {
                messageText.text = message.content
                timeText.text = formatTime(message.sentAt)
                editedIndicator.visibility = message.editedAt?.let { View.VISIBLE } ?: View.GONE
                senderName.text = user?.name
                user?.avatar?.let {
                    Glide.with(holder.itemView.context).load(user.avatar).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.avatar).error(R.drawable.avatar).circleCrop().into(userAvatar)
                }
            }

            root.setOnLongClickListener {
                if (isMyMessage) {
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
        val position = newList.indexOfFirst { it.id == messageId }
        if (position != -1) {
            newList.removeAt(position)
            asyncListDiffer.submitList(newList)
        }
    }

    fun updateUsersCache(users: List<UserResponse>) {
        if (users.isEmpty()) return

        val usersIds = HashSet<String>(users.size)
        val changedAvatarUserIds = HashSet<String>(users.size)

        users.forEach { newUser ->
            usersIds.add(newUser.id)
            usersCache[newUser.id]?.let { oldUser ->
                if (oldUser.avatar != newUser.avatar) {
                    changedAvatarUserIds.add(newUser.id)
                }
            } ?: run {
                changedAvatarUserIds.add(newUser.id)
            }
            usersCache[newUser.id] = newUser
        }
        val positions = ArrayList<Int>()
        asyncListDiffer.currentList.forEachIndexed { index, message ->
            if (message.senderId in usersIds) {
                if (message.senderId in changedAvatarUserIds) {
                    positions.add(index)
                }
            }
        }

        if (positions.isNotEmpty()) {
            positions.forEach { notifyItemChanged(it) }
        }
    }

    private fun formatTime(isoTime: String): String {
        return timeCache.getOrPut(isoTime) {
            try {
                LocalDateTime.parse(isoTime).atZone(UTC_ZONE).withZoneSameInstant(MOSCOW_ZONE).format(TIME_FORMATTER)
            } catch (e: Exception) {
                isoTime
            }
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

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemView.context).clear(holder.binding.userAvatar)
    }
}