package com.example.messenger.chats.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.messenger.chats.ui.adapters.UserSearchForAddAdapter
import com.example.messenger.chats.ui.view_models.AddUserToChatViewModel
import com.example.messenger.contacts.ui.models.UserSearchScreenState
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentContactsUserSearchBinding

class AddUserToChatDialogFragment : DialogFragment() {
    private var _binding: FragmentContactsUserSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var userSearchAdapter: UserSearchForAddAdapter
    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }
    private val viewModel by viewModels<AddUserToChatViewModel> {
        AddUserToChatViewModel.getViewModelFactory(apiService, requireContext(), view)
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
        _binding = FragmentContactsUserSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userSearchAdapter = UserSearchForAddAdapter(
            onClick = { user ->
                viewModel.addUserToChat(chatId, user.id)
            }
        )

        binding.rvSearchResults.adapter = userSearchAdapter

        viewModel.userSearch.observe(viewLifecycleOwner) { state ->
            render(state)
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

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchUser(newText.toString())
                return true
            }
        })

        binding.toBack.setOnClickListener {
            dismiss()
        }
    }

    private fun render(state: UserSearchScreenState) {
        when (state) {
            is UserSearchScreenState.Loading -> showLoading(state)
            is UserSearchScreenState.Content -> showContent(state)
            is UserSearchScreenState.Error -> showError(state)
        }
    }

    private fun hideAll() {
        binding.progressBar.isVisible = false
        binding.rvSearchResults.isVisible = false
    }

    private fun showLoading(state: UserSearchScreenState.Loading) {
        hideAll()
        binding.progressBar.isVisible = true
    }

    private fun showContent(state: UserSearchScreenState.Content) {
        hideAll()
        userSearchAdapter.setUsers(state.users)
        binding.rvSearchResults.isVisible = true
    }

    private fun showError(state: UserSearchScreenState.Error) {
        hideAll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}