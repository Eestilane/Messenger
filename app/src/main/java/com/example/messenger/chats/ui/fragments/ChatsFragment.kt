package com.example.messenger.chats.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger.R
import com.example.messenger.chats.ui.adapters.ChatsAdapter
import com.example.messenger.chats.ui.models.Chat
import com.example.messenger.chats.ui.view_models.ChatsViewModel
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentChatsBinding

class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatsAdapter: ChatsAdapter
    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }
    private val viewModel by navGraphViewModels<ChatsViewModel>(R.id.navigation_graph) {
        ChatsViewModel.getViewModelFactory(apiService, requireContext())
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chatCreate.setOnClickListener {
            findNavController().navigate(R.id.action_chatsFragment_to_createChatDialogFragment)
        }

        viewModel.navigateToChat.observe(viewLifecycleOwner) { chat ->
            chat?.let {
                navigateToChat(it)
                viewModel.onChatNavigated()
            }
        }

        chatsAdapter = ChatsAdapter(emptyList()) { chat ->
            navigateToChat(chat)
        }

        binding.chats.layoutManager = LinearLayoutManager(requireContext())
        binding.chats.adapter = chatsAdapter

        viewModel.chats.observe(viewLifecycleOwner) { chats ->
            chatsAdapter.updateList(chats)
            chats.forEach { chat ->
                viewModel.loadLastMessage(chat.id) { message, time ->
                    activity?.runOnUiThread {
                        chatsAdapter.updateLastMessage(chat.id, message, time)
                    }
                }
            }
        }
        viewModel.loadChats()

    }

    private fun navigateToChat(chat: Chat) {
        findNavController().navigate(R.id.action_chatsFragment_to_chatFragment,
            bundleOf(
                "chatId" to chat.id,
                "chatName" to chat.name,
                "avatar" to chat.avatar,
                "ownerId" to chat.ownerId
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
