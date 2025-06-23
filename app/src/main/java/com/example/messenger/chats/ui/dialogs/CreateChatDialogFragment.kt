package com.example.messenger.chats.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.messenger.R
import com.example.messenger.databinding.FragmentChatsCreateChatBinding
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.bumptech.glide.Glide
import com.example.messenger.chats.ui.fragments.ChatsFragment
import com.example.messenger.chats.ui.view_models.ChatsViewModel

class CreateChatDialogFragment : DialogFragment() {
    private var _binding: FragmentChatsCreateChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel by navGraphViewModels<ChatsViewModel>(R.id.navigation_graph)

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@registerForActivityResult

        viewModel.setAvatarUri(uri)
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(binding.chatAvatar)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatsCreateChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.okButton.setOnClickListener {
            val chatName = binding.enterChatName.text.toString().trim()
            if (chatName.isNotEmpty()) {
                viewModel.createChat(chatName)
            }
        }

        viewModel.navigateToChat.observe(viewLifecycleOwner) { chat ->
            chat?.let {
                dismiss()

                (parentFragment as? ChatsFragment)?.navigateToChat(chat)

                viewModel.onChatNavigated()
            }
        }

        viewModel.currentChatAvatar.observe(viewLifecycleOwner) { avatarUrl ->
            avatarUrl?.let {
                Glide.with(this)
                    .load(it)
                    .circleCrop()
                    .into(binding.chatAvatar)
            }
        }

        binding.chatAvatar.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}