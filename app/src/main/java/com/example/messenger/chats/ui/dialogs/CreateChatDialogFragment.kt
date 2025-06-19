package com.example.messenger.chats.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.messenger.R
import com.example.messenger.databinding.FragmentChatnameBinding
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.messenger.chats.ui.view_models.ChatsViewModel

class CreateChatDialogFragment : DialogFragment() {
    private var _binding: FragmentChatnameBinding? = null
    private val binding get() = _binding!!
    private val viewModel by navGraphViewModels<ChatsViewModel>(R.id.navigation_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatnameBinding.inflate(inflater, container, false)
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
            viewModel.createChat(chatName)
            dismiss()
        }

        viewModel.navigateToChat.observe(viewLifecycleOwner) {
            it?.let { dismiss() }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}