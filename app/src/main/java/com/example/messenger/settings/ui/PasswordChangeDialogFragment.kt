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
import com.example.messenger.settings.ui.models.PasswordChangeScreenState
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

        viewModel.passwordChangeData.observe(viewLifecycleOwner) { passwordChangeData ->
            render(passwordChangeData)
        }

        viewModel.navigateToSettings.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigateUp()
            }
        }

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding.cancelButton.setOnClickListener {
            viewModel.resetPasswordChangeError()
            binding.root.post {
                findNavController().navigateUp()
            }
        }

        binding.okButton.setOnClickListener {
            setFragmentResult("requestPassword", bundleOf("newPassword" to binding.enterPassword.text.toString()))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        render(PasswordChangeScreenState.Null)
    }

    private fun render(state: PasswordChangeScreenState) {
        when (state) {
            is PasswordChangeScreenState.Null -> hideAll()
            is PasswordChangeScreenState.Error -> showError(state)
        }
    }

    private fun hideAll() {
        binding.passwordChangeError.isVisible = false
    }

    private fun showError(state: PasswordChangeScreenState.Error) {
        hideAll()
        binding.passwordChangeError.isVisible = true
        binding.passwordChangeError.text = state.passwordChangeError
    }
}
