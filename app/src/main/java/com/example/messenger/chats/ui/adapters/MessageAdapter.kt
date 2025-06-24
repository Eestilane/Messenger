package com.example.messenger.chats.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
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
    private val currentUser: UserResponse
): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private val messages = mutableListOf<Message>()
    private val timeFormatter by lazy { DateTimeFormatter.ofPattern("HH:mm") }
    private val utcZone = ZoneId.of("UTC")
    private val moscowZone = ZoneId.of("Europe/Moscow")
    private val userNamesCache = mutableMapOf<String, String>().apply {
        put(currentUser.id, currentUser.name)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemMessageBinding.bind(itemView)


        fun bind(message: Message) {
            binding.apply {

                senderName.text = userNamesCache[message.senderId] ?: "User ${message.senderId.takeLast(4)}"

                if (message.senderId == currentUser.id) {
                    senderName.text = "Вы"
                } else {
                    senderName.text = "Участник ${message.senderId.takeLast(4)}"
                    senderName.visibility = View.VISIBLE
                }

                messageText.text = message.content

                timeText.text = formatTime(message.sentAt)

                if (message.editedAt != null) {
                    editedIndicator.visibility = View.VISIBLE
                } else {
                    editedIndicator.visibility = View.GONE
                }


                root.setOnLongClickListener {
                    showContextMenu(it, message)
                    true
                }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    fun updateUserCache(userId: String, userName: String) {
        userNamesCache[userId] = userName
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
            val updatedMessage = messages[position].copy(
                content = newText,
                editedAt = LocalDateTime.now().toString()
            )
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