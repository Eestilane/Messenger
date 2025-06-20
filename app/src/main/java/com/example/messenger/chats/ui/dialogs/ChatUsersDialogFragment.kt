package com.example.messenger.chats.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger.chats.ui.adapters.ChatUsersAdapter
import com.example.messenger.chats.ui.view_models.ChatUsersViewModel
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentChatUsersBinding

class ChatUsersDialogFragment: DialogFragment() {
    private var _binding: FragmentChatUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatUsersAdapter: ChatUsersAdapter
    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }
    private val viewModel by viewModels<ChatUsersViewModel> {
        ChatUsersViewModel.getViewModelFactory(apiService, requireContext(), view)
    }

    companion object {
        private const val ARG_CHAT_ID = "chat_id"

        fun newInstance(chatId: String): ChatUsersDialogFragment {
            return ChatUsersDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CHAT_ID, chatId)
                }
            }
        }
    }

    private val chatId by lazy { arguments?.getString(ARG_CHAT_ID) ?: "" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChatUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        chatUsersAdapter = ChatUsersAdapter(
            onClick = { user ->
                viewModel.removeUserFromChat(chatId, user.id)
            })

        binding.chatUsers.apply {
            adapter = chatUsersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.chatUsers.observe(viewLifecycleOwner) { users ->
            users?.let {
                chatUsersAdapter.setChatUsers(it)
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { users ->
            users?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.onSuccessMessageShown()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.onErrorMessageShown()
            }
        }

        viewModel.loadChatUsers(chatId)

        binding.toBack.setOnClickListener {
            dismiss()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}