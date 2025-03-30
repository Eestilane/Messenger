package com.example.messenger.contacts.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.messenger.R
import com.example.messenger.contacts.ui.adapters.IncomingRequestsAdapter
import com.example.messenger.contacts.ui.view_models.IncomingRequestsViewModel
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentIncomingRequestsDialogBinding
import kotlin.getValue

class IncomingRequestsDialogFragment : DialogFragment() {
    private var _binding: FragmentIncomingRequestsDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomingRequestsAdapter: IncomingRequestsAdapter
    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }
    private val viewModel by viewModels<IncomingRequestsViewModel> {
        IncomingRequestsViewModel.getViewModelFactory(apiService, requireContext(), view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIncomingRequestsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        incomingRequestsAdapter = IncomingRequestsAdapter(
            accept = { request -> viewModel.acceptRequest(request.requestId) },
            decline = { request -> viewModel.declineRequest(request.requestId) }
        )
        binding.rvSearchResults.adapter = incomingRequestsAdapter
        viewModel.incomingRequests.observe(viewLifecycleOwner) { requests ->
            incomingRequestsAdapter.setIncomingRequests(requests)
        }

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        viewModel.inRequestResponse()
    }
}
