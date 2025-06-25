package com.example.messenger.chats.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.chats.ui.adapters.ChatUsersAdapter
import com.example.messenger.chats.ui.models.ChatNavigationParameters
import com.example.messenger.chats.ui.view_models.ChatUsersViewModel
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentChatsChatUsersBinding

class ChatUsersDialogFragment : DialogFragment() {
    private var _binding: FragmentChatsChatUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatUsersAdapter: ChatUsersAdapter
    private lateinit var chat: ChatNavigationParameters

    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }
    private val viewModel by viewModels<ChatUsersViewModel> {
        ChatUsersViewModel.getViewModelFactory(apiService, requireContext(), view)
    }

    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        uri?.let { selectedUri ->
            viewModel.updateChatAvatar(chat.id, selectedUri)
        }
    }

    companion object {
        fun newInstance(chat: ChatNavigationParameters): ChatUsersDialogFragment {
            return ChatUsersDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("chat_id", chat.id)
                    putString("chat_name", chat.name)
                    putString("owner_id", chat.ownerId)
                    putString("avatar", chat.avatar)
                    putBoolean("isDirect", chat.isDirect)
                }
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChatsChatUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chat = ChatNavigationParameters(
            id = requireArguments().getString("chat_id") ?: return dismiss(),
            ownerId = requireArguments().getString("owner_id") ?: return dismiss(),
            name = requireArguments().getString("chat_name") ?: return dismiss(),
            avatar = requireArguments().getString("avatar"),
            isDirect = requireArguments().getBoolean("isDirect")
        )

        setupUI()
        setupObservers()
        viewModel.loadChatUsers(chat.id)
    }

    private fun setupUI() {
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        chat.avatar?.takeIf { it.isNotEmpty() }?.let { url ->
            Glide.with(requireContext())
                .load(url)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .circleCrop()
                .into(binding.chatAvatar)
        }

        binding.chatName.text = chat.name

        chatUsersAdapter = ChatUsersAdapter(
            onDeleteClick = { user -> viewModel.removeUserFromChat(chat.id, user.id) }
        )

        binding.chatUsers.apply {
            adapter = chatUsersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.toBack.setOnClickListener { dismiss() }

        binding.chatAvatar.setOnClickListener { pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }

        binding.addUsersInFrame.setOnClickListener {
            AddUserToChatDialogFragment.newInstance(chat.id).show(childFragmentManager, "AddUserToChatDialog")
        }

        binding.deleteChat.setOnClickListener {
            viewModel.deleteChat(chat.id)
        }
    }

    private fun setupObservers() {
        viewModel.chatUsers.observe(viewLifecycleOwner) { users ->
            users?.let { chatUsersAdapter.setChatUsers(it) }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
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

        viewModel.currentChatAvatar.observe(viewLifecycleOwner) { newAvatarUrl ->
            newAvatarUrl?.takeIf { it.isNotEmpty() }?.let { url ->
                Glide.with(requireContext())
                    .load(url)
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .circleCrop()
                    .into(binding.chatAvatar)
                (parentFragment as? OnAvatarUpdatedListener)?.onAvatarUpdated(url)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnAvatarUpdatedListener {
        fun onAvatarUpdated(newAvatarUrl: String)
    }
}