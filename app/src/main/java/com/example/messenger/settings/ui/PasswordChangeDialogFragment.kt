package com.example.messenger.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.messenger.R
import com.example.messenger.databinding.FragmentSettingsPasswordChangeBinding
import com.example.messenger.settings.ui.models.NameChangeScreenState
import com.example.messenger.settings.ui.view_models.SettingsViewModel

class PasswordChangeDialogFragment : DialogFragment() {
    private var _binding: FragmentSettingsPasswordChangeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by navGraphViewModels<SettingsViewModel>(R.id.navigation_graph)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSettingsPasswordChangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.renameData.observe(viewLifecycleOwner) { renameData ->
            render(renameData)
        }

        viewModel.navigateToSettings.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigateUp()
            }
        }

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding.cancelButton.setOnClickListener {
            viewModel.resetRenameError()
            binding.root.post {
                findNavController().navigateUp()
            }
        }

        binding.okButton.setOnClickListener {
            setFragmentResult("requestPassword", bundleOf("newPassword" to binding.enterName.text.toString()))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        render(NameChangeScreenState.Null)
    }

    private fun render(state: NameChangeScreenState) {
        when (state) {
            is NameChangeScreenState.Null -> hideAll()
            is NameChangeScreenState.Error -> showError(state)
        }
    }

    private fun hideAll() {
        binding.renameError.isVisible = false
    }

    private fun showError(state: NameChangeScreenState.Error) {
        hideAll()
        binding.renameError.isVisible = true
        binding.renameError.text = state.renameError
    }
}
