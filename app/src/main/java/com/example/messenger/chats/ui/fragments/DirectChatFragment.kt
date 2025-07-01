package com.example.messenger.chats.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger.chats.ui.ChatHubConnection
import com.example.messenger.chats.ui.adapters.MessageAdapter
import com.example.messenger.chats.ui.models.ChatNavigationParameters
import com.example.messenger.chats.ui.models.Message
import com.example.messenger.chats.ui.view_models.ChatViewModel
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentChatsDirectChatBinding
import com.example.messenger.libs.TokenManager
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class DirectChatFragment : Fragment() {
    private var _binding: FragmentChatsDirectChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatHub: ChatHubConnection
    private lateinit var chat: ChatNavigationParameters
    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }
    private val viewModel by viewModels<ChatViewModel> {
        ChatViewModel.getViewModelFactory(apiService, requireContext(), view)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChatsDirectChatBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeChat()
        setupRecyclerView()
        initChatHub()
        setAllBindings()

        viewModel.loadChatUsers(chat.id)

    }

    private fun initializeChat() {
        val chatId = arguments?.getString("chatId") ?: run {
            findNavController().navigateUp()
            return
        }
        val ownerId = arguments?.getString("ownerId") ?: ""
        val chatName = arguments?.getString("chatName") ?: ""
        val avatar = arguments?.getString("avatar") ?: ""
        val isDirect = arguments?.getBoolean("isDirect") ?: true

        chat = ChatNavigationParameters(chatId, ownerId, chatName, avatar, isDirect)

        viewModel.loadChatUsers(chatId)

        viewModel.loadMessages(chat.id)
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageAdapter.setMessages(messages)
            if (messages.isNotEmpty()) {
                binding.recyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun setAllBindings() {
        binding.userName.text = chat.name

        binding.toBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.sendMessage.setOnClickListener {
            sendMessage()
        }

    }

    private fun initChatHub() {
        val token = TokenManager.getToken(requireContext()) ?: throw IllegalStateException("Токен не найден")

        chatHub = ChatHubConnection(token).apply {
            onMessageReceived { messageId, chatId, userId, content ->
                val message = Message(
                    id = messageId,
                    chatId = chatId,
                    senderId = userId,
                    content = content,
                    sentAt = LocalDateTime.now().toString(),
                    editedAt = null,
                    viewed = false
                )
                addMessageToChat(message)
            }

            onMessageEdited { messageId, chatId, content ->
                activity?.runOnUiThread {
                    messageAdapter.updateMessage(messageId, content)
                    binding.recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                }
            }

            onMessageDeleted { messageId ->
                activity?.runOnUiThread {
                    messageAdapter.removeMessage(messageId)
                    viewModel.removeMessage(messageId)
                }
            }

            onUserAdded { chatId, userId ->
                if (chatId == this@DirectChatFragment.chat.id) {
                    activity?.runOnUiThread {
                        Toast.makeText(context,"Пользователь добавлен", Toast.LENGTH_LONG).show()
                        viewModel.loadChatUsers(chatId)
                    }
                }
            }
            onUserRemoved { chatId, userId ->
                if (chatId == this@DirectChatFragment.chat.id) {
                    activity?.runOnUiThread {
                        Toast.makeText(context,"Пользователь удалён", Toast.LENGTH_LONG).show()
                        viewModel.loadChatUsers(chatId)
                    }
                }
            }

            onAvatarUpdated { chatId, userId, link ->
                if (chatId == this@DirectChatFragment.chat.id) {
                    activity?.runOnUiThread {
                        Toast.makeText(context,"Аватар чата обновлён", Toast.LENGTH_LONG).show()
                    }
                }
            }


            lifecycleScope.launch {
                connect()
            }
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(
            onEdit = { messageId, currentText -> showEditDialog(messageId, currentText) },
            onDelete = { messageId -> confirmDelete(messageId) },
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }

        viewModel.chatUsers.observe(viewLifecycleOwner) { users -> messageAdapter.updateUsersCache(users) }
    }

    private fun sendMessage() {
        val text = binding.messageInput.text.toString().trim()
        if (text.isNotEmpty()) {
            lifecycleScope.launch {
                chatHub.sendMessage(chat.id, text)
                binding.messageInput.text.clear()
            }
        }
    }

    private fun addMessageToChat(message: Message) {
        requireActivity().runOnUiThread {
            messageAdapter.addMessage(message)
            binding.recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    private fun showEditDialog(messageId: String, currentText: String) {

        val editText = EditText(requireContext()).apply {
            setText(currentText)
        }

        AlertDialog.Builder(requireContext()).setTitle("Редактировать сообщение").setView(editText).setPositiveButton("Сохранить") { _, _ ->
            val content = editText.text.toString()
            lifecycleScope.launch {
                chatHub.editMessage(messageId, content)
            }
        }.setNegativeButton("Отмена", null).show()
    }

    private fun confirmDelete(messageId: String) {
        AlertDialog.Builder(requireContext()).setTitle("Удаление сообщения").setMessage("Вы уверены, что хотите удалить это сообщение?").setPositiveButton("Удалить") { _, _ ->
            lifecycleScope.launch {
                chatHub.deleteMessage(messageId)
                viewModel.removeMessage(messageId)
            }
        }.setNegativeButton("Отмена", null).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        chatHub.disconnect()
    }
}