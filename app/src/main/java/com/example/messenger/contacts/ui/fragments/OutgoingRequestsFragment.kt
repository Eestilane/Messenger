package com.example.messenger.contacts.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.example.messenger.contacts.ui.adapters.OutgoingRequestsAdapter
import com.example.messenger.contacts.ui.models.RequestsScreenState
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

        viewModel.incomingRequest = false

        outgoingRequestsAdapter = OutgoingRequestsAdapter(
            decline = { request -> viewModel.decline(request.requestId) }
        )
        binding.rvSearchResults.adapter = outgoingRequestsAdapter
        viewModel.requestsState.observe(viewLifecycleOwner) { state ->
            render(state)
        }
        viewModel.outRequestResponse()
    }

    private fun render(state: RequestsScreenState) {
        when (state) {
            is RequestsScreenState.Loading -> showLoading(state)
            is RequestsScreenState.Content -> showContent(state)
            is RequestsScreenState.Error -> showError(state)
        }
    }

    private fun hideAll() {
        setFragmentResult("request", bundleOf("progressBar" to false))
        binding.rvSearchResults.isVisible = false
    }

    private fun showLoading(state: RequestsScreenState.Loading) {
        hideAll()
        setFragmentResult("request", bundleOf("progressBar" to true))
    }

    private fun showContent(state: RequestsScreenState.Content) {
        hideAll()
        outgoingRequestsAdapter.setIncomingRequests(state.requests)
        binding.rvSearchResults.isVisible = true
    }

    private fun showError(state: RequestsScreenState.Error) {
        hideAll()
    }
}