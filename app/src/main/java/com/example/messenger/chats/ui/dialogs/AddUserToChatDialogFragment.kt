package com.example.messenger.chats.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.messenger.chats.ui.adapters.UserSearchForAddAdapter
import com.example.messenger.chats.ui.view_models.AddUserToChatViewModel
import com.example.messenger.contacts.ui.models.ContactsScreenState
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentChatsAddContactsToChatBinding

class AddUserToChatDialogFragment : DialogFragment() {
    private var _binding: FragmentChatsAddContactsToChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var userSearchAdapter: UserSearchForAddAdapter
    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }
    private val viewModel by viewModels<AddUserToChatViewModel> {
        AddUserToChatViewModel.getViewModelFactory(apiService, requireContext())
    }

    companion object {
        private const val ARG_CHAT_ID = "chat_id"

        fun newInstance(chatId: String): AddUserToChatDialogFragment {
            return AddUserToChatDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CHAT_ID, chatId)
                }
            }
        }
    }

    private val chatId by lazy { arguments?.getString(ARG_CHAT_ID) ?: "" }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChatsAddContactsToChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.contacts.observe(viewLifecycleOwner) { contacts ->
            render(contacts)
        }

        userSearchAdapter = UserSearchForAddAdapter(
            onClick = { contact -> viewModel.addUserToChat(chatId, contact.id)
            }
        )

        binding.rvSearchResults.adapter = userSearchAdapter

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                userSearchAdapter.filter(newText)
                return true
            }
        })

        binding.toBack.setOnClickListener {
            dismiss()
        }
    }

    private fun render(state: ContactsScreenState) {
        when (state) {
            is ContactsScreenState.Loading -> showLoading(state)
            is ContactsScreenState.Content -> showContent(state)
            is ContactsScreenState.Error -> showError(state)
        }
    }

    private fun hideAll() {
        binding.progressBar.isVisible = false
        binding.rvSearchResults.isVisible = false
    }

    private fun showLoading(state: ContactsScreenState.Loading) {
        hideAll()
        binding.progressBar.isVisible = true
    }

    private fun showContent(state: ContactsScreenState.Content) {
        hideAll()
        binding.rvSearchResults.isVisible = true
        userSearchAdapter.setContacts(state.contacts)
    }

    private fun showError(state: ContactsScreenState.Error) {
        hideAll()
    }

    override fun onResume() {
        super.onResume()
        userSearchAdapter.updateFilter()
    }

    override fun onPause() {
        super.onPause()
        viewModel.getContacts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}