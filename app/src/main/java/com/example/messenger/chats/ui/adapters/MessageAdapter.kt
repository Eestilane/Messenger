package com.example.messenger.chats.ui.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.databinding.ItemMessageBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import com.example.messenger.R
import com.example.messenger.chats.ui.models.Message

class MessageAdapter (private val onEdit: (messageId: String, currentText: String) -> Unit, private val onDelete: (messageId: String) -> Unit): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private val messages = mutableListOf<Message>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemMessageBinding.bind(itemView)

        fun bind(message: Message) {
            binding.messageText.text = message.content
            binding.root.setOnLongClickListener {
                showContextMenu(it, message)
                true
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

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun updateMessage(messageId: String, newText: String) {
        val position = messages.indexOfFirst { it.id == messageId }
        if (position != -1) {
            messages[position] = messages[position].copy(content = newText)
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