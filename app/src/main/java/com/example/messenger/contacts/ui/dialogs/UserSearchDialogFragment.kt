package com.example.messenger.contacts.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.example.messenger.contacts.ui.adapters.UserSearchAdapter
import com.example.messenger.contacts.ui.models.RequestsScreenState
import com.example.messenger.contacts.ui.models.UserSearchScreenState
import com.example.messenger.contacts.ui.view_models.UserSearchViewModel
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentContactsUserSearchBinding

class UserSearchDialogFragment : DialogFragment() {
    private var _binding: FragmentContactsUserSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var userSearchAdapter: UserSearchAdapter
    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }
    private val viewModel by viewModels<UserSearchViewModel> {
        UserSearchViewModel.getViewModelFactory(apiService, requireContext(), view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactsUserSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userSearchAdapter = UserSearchAdapter(
            onClick = { contact -> viewModel.addFriendRequest(contact.id) }
        )
        binding.rvSearchResults.adapter = userSearchAdapter
        viewModel.userSearch.observe(viewLifecycleOwner) { state ->
            render(state)
        }

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchUser(newText.toString())
                return true
            }
        })

        binding.toBack.setOnClickListener{
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
        userSearchAdapter.setContacts(state.users)
        binding.rvSearchResults.isVisible = true
    }

    private fun showError(state: UserSearchScreenState.Error) {
        hideAll()
    }
}