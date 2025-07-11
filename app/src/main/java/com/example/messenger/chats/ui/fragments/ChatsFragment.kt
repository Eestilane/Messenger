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
import com.example.messenger.chats.ui.models.ChatNavigationParameters
import com.example.messenger.chats.ui.models.ChatsState
import com.example.messenger.chats.ui.view_models.ChatsViewModel
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentChatsBinding

class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatsAdapter: ChatsAdapter
    private lateinit var currentUserId: String
    private val apiService by lazy { RetrofitClient.create(requireContext(), view) }

    private val viewModel by navGraphViewModels<ChatsViewModel>(R.id.navigation_graph) {
        ChatsViewModel.getViewModelFactory(apiService, requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        setupObservers()

        viewModel.loadChats()
    }

    private fun setupRecyclerView() {
        chatsAdapter = ChatsAdapter(emptyList()) { chat ->
            navigateToChat(chat, userId = currentUserId)
        }
        binding.chats.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        binding.chatCreate.setOnClickListener {
            findNavController().navigate(R.id.action_chatsFragment_to_createChatDialogFragment)
        }
    }

    private fun setupObservers() {
        viewModel.navigateToChat.observe(viewLifecycleOwner) { chat ->
            chat?.let {
                navigateToChat(it, userId = currentUserId)
                viewModel.onChatNavigated()
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ChatsState.Loading -> showLoading()
                is ChatsState.Success -> {
                    hideLoading()
                    chatsAdapter.updateList(state.chats)
                }
                is ChatsState.Error -> hideLoading()
            }
        }

        viewModel.currentUserId.observe(viewLifecycleOwner) { userId ->
            currentUserId = userId
        }
    }

    fun navigateToChat(parameters: ChatNavigationParameters, userId: String?) {
        findNavController().navigate(R.id.action_chatsFragment_to_chatFragment,
            bundleOf(
                "chatId" to parameters.id,
                "ownerId" to parameters.ownerId,
                "chatName" to parameters.name,
                "avatar" to parameters.avatar,
                "isDirect" to parameters.isDirect,
                "userId" to userId
            )
        )
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.chats.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.chats.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}