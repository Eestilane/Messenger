package com.example.messenger.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentSettingsNameChangeBinding
import com.example.messenger.settings.ui.models.NameChangeScreenState
import com.example.messenger.settings.ui.models.SettingsScreenState
import com.example.messenger.settings.ui.view_models.SettingsViewModel
import kotlin.getValue

class NameChangeDialogFragment : DialogFragment() {
    private var _binding: FragmentSettingsNameChangeBinding? = null
    private val binding get() = _binding!!
    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }

    val viewModel by navGraphViewModels<SettingsViewModel>(R.id.navigation_graph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsNameChangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.renameData.observe(viewLifecycleOwner) { renameData ->
            render(renameData)
        }

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        binding.cancelButton.setOnClickListener {
            render(NameChangeScreenState.NavigateToSettings)
        }

        binding.okButton.setOnClickListener {
            setFragmentResult("requestRename", bundleOf("resultRename" to binding.enterName.text.toString()))
        }
    }

    private fun render(state: NameChangeScreenState) {
        when (state) {
            is NameChangeScreenState.Null -> Unit
            is NameChangeScreenState.Error -> showError(state)
            is NameChangeScreenState.NavigateToSettings -> findNavController().navigateUp()
        }
    }

    private fun hideAll() {

    }

    private fun showError(state: NameChangeScreenState.Error) {
        hideAll()
        binding.renameError.text = state.renameError
    }
}