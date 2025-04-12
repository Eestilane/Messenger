package com.example.messenger.contacts.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.messenger.contacts.ui.adapters.OutgoingRequestsAdapter
import com.example.messenger.contacts.ui.view_models.RequestsViewModel
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentContactsOutgoingRequestsBinding
import kotlin.getValue

class OutgoingRequestsFragment : Fragment() {
    private var _binding: FragmentContactsOutgoingRequestsBinding? = null
    private val binding get() = _binding!!
    private lateinit var outgoingRequestsAdapter: OutgoingRequestsAdapter
    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }
    private val viewModel by viewModels<RequestsViewModel> {
        RequestsViewModel.getViewModelFactory(apiService, requireContext(), view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactsOutgoingRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        outgoingRequestsAdapter = OutgoingRequestsAdapter(
            decline = { request -> viewModel.declineRequest(request.requestId) }
        )
        binding.rvSearchResults.adapter = outgoingRequestsAdapter
        viewModel.outgoingRequests.observe(viewLifecycleOwner) { requests ->
            outgoingRequestsAdapter.setIncomingRequests(requests)
        }
        viewModel.outRequestResponse()
    }
}