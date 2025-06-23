package com.example.messenger.chats.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger.chats.ui.ChatHubConnection
import com.example.messenger.chats.ui.adapters.MessageAdapter
import com.example.messenger.chats.ui.models.Message
import com.example.messenger.chats.ui.view_models.ChatViewModel
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentChatsPersonalChatBinding
import com.example.messenger.libs.TokenManager
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class PersonalChatFragment : Fragment() {
    private var _binding: FragmentChatsPersonalChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var messageAdapter: MessageAdapter
    private val chatId by lazy { arguments?.getString("chatId") ?: "" }
    private val chatName = arguments?.getString("chatName") ?: "Чат"
    private lateinit var chatHub: ChatHubConnection
    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }
    private val viewModel by viewModels<ChatViewModel> {
        ChatViewModel.getViewModelFactory(apiService, requireContext(), view)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChatsPersonalChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initChatHub()
        setupRecyclerView()
        loadInitialMessages()

        binding.personalChatText.text = chatName

        binding.toBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.personalChatText.setOnClickListener {
            //ChatUsersDialogFragment.newInstance(chatId).show(childFragmentManager, "ChatUsersDialogFragment")
        }

        binding.sendMessage.setOnClickListener {
            sendMessage()
        }


    }

    private fun initChatHub() {
        val token = TokenManager.getToken(requireContext())
            ?: throw IllegalStateException("Токен не найден")

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
                }
            }


            lifecycleScope.launch {
                connect()
            }
        }
    }

    private fun loadInitialMessages() {
        viewModel.loadMessages(chatId)
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageAdapter.setMessages(messages)
            if (messages.isNotEmpty()) {
                binding.recyclerView.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(
            onEdit = { messageId, currentText -> showEditDialog(messageId, currentText) },
            onDelete = { messageId -> confirmDelete(messageId) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun sendMessage() {
        val text = binding.messageInput.text.toString().trim()
        if (text.isNotEmpty()) {
            lifecycleScope.launch {
                chatHub.sendMessage(chatId, text)
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
        AlertDialog.Builder(requireContext()).setTitle("Удаление сообщения").setMessage("Вы уверены, что хотите удалить это сообщение?")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    chatHub.deleteMessage(messageId)
                }
            }.setNegativeButton("Отмена", null).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        chatHub.disconnect()
    }
}