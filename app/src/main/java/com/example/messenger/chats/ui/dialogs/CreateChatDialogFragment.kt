
package com.example.messenger.chats.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.messenger.R
import com.example.messenger.chats.ui.fragments.ChatsFragment
import com.example.messenger.chats.ui.view_models.ChatsViewModel
import com.example.messenger.databinding.FragmentChatsCreateChatBinding

class CreateChatDialogFragment : DialogFragment() {
    private var _binding: FragmentChatsCreateChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel by navGraphViewModels<ChatsViewModel>(R.id.navigation_graph)

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

        binding.saveButton.setOnClickListener {
            val chatName = binding.enterChatName.text.toString().trim()
            if (chatName.isNotEmpty()) {
                viewModel.createChat(chatName)
                dismiss()
            }
        }

        viewModel.navigateToChat.observe(viewLifecycleOwner) { chat ->
            chat?.let {
                dismiss()

                (parentFragment as? ChatsFragment)?.navigateToChat(chat)

                viewModel.onChatNavigated()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
