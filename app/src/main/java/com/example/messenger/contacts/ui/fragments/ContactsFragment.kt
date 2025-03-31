package com.example.messenger.contacts.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.messenger.contacts.ui.adapters.ContactsAdapter
import com.example.messenger.contacts.ui.dialogs.RequestsDialogFragment
import com.example.messenger.contacts.ui.dialogs.UserSearchDialogFragment
import com.example.messenger.contacts.ui.view_models.ContactsViewModel
import com.example.messenger.data.RetrofitClient
import com.example.messenger.data.models.contacts.ContactsResponse
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
        contactsAdapter = ContactsAdapter(
            onClick = { contact -> viewModel.deleteContact() }
        )
        binding.rvSearchResults.adapter = contactsAdapter
        viewModel.contacts.observe(viewLifecycleOwner) { contacts ->
            contactsAdapter.setContacts(contacts)
        }
        val contacts = listOf<ContactsResponse>()
        contactsAdapter.setContacts(contacts)

        viewModel.getContacts()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.getContacts()
                return true
            }
        })

        binding.fabAddContact.setOnClickListener{
            UserSearchDialogFragment().show(childFragmentManager, "ConfirmationDialog")
        }

        binding.fabRequestContact.setOnClickListener{
            RequestsDialogFragment().show(childFragmentManager, "ConfirmationDialog")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}