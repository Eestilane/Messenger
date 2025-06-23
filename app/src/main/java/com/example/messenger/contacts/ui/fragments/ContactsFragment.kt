package com.example.messenger.contacts.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.messenger.contacts.ui.adapters.ContactsAdapter
import com.example.messenger.contacts.ui.dialogs.RequestsDialogFragment
import com.example.messenger.contacts.ui.dialogs.UserSearchDialogFragment
import com.example.messenger.contacts.ui.models.ContactsScreenState
import com.example.messenger.contacts.ui.view_models.ContactsViewModel
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentContactsBinding

class ContactsFragment : Fragment() {
    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private lateinit var contactsAdapter: ContactsAdapter
    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }
    private val viewModel by viewModels<ContactsViewModel> {
        ContactsViewModel.getViewModelFactory(apiService, requireContext(), view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.contacts.observe(viewLifecycleOwner) { contacts ->
            render(contacts)
        }

        contactsAdapter = ContactsAdapter(
            onClick = { contact -> viewModel.deleteContact(contact.id) }
        )
        binding.rvSearchResults.adapter = contactsAdapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                contactsAdapter.filter(newText)
                return true
            }
        })

        binding.addContact.setOnClickListener {
            UserSearchDialogFragment().show(childFragmentManager, "ConfirmationDialog")
        }

        binding.fabRequestContact.setOnClickListener {
            RequestsDialogFragment().show(childFragmentManager, "ConfirmationDialog")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        contactsAdapter.setContacts(state.contacts)
    }

    private fun showError(state: ContactsScreenState.Error) {
        hideAll()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getContacts()
    }
}