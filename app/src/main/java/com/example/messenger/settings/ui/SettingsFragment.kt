package com.example.messenger.settings.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.messenger.data.RetrofitClient
import com.example.messenger.databinding.FragmentSettingsBinding
import com.example.messenger.libs.ThemeManager
import com.example.messenger.settings.ui.models.SettingsScreenState
import com.example.messenger.settings.ui.view_models.SettingsViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }
    private val viewModel by navGraphViewModels<SettingsViewModel>(R.id.navigation_graph) {
        SettingsViewModel.getViewModelFactory(apiService, requireContext(), view)
    }

    val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri == null) {
            return@registerForActivityResult
        }
        val type = requireContext().contentResolver.getType(uri) ?: return@registerForActivityResult

        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type) ?: return@registerForActivityResult

        val requestBody = requireContext().contentResolver.openInputStream(uri).use {
            it?.readBytes()?.toRequestBody(type.toMediaTypeOrNull())
        } ?: return@registerForActivityResult

        val filePart = MultipartBody.Part.createFormData(
            "file", uri.lastPathSegment + "." + extension, requestBody
        );

        viewModel.updateAvatar(filePart)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.settingsData.observe(viewLifecycleOwner) { settingsData ->
            render(settingsData)
        }

        viewModel.navigateToAuth.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                val navOptions = NavOptions.Builder().setPopUpTo(R.id.navigation_graph, true).build()
                findNavController().navigate(R.id.action_settingsFragment_to_authFragment, null, navOptions)
                viewModel.resetNavigation()
            }
        }

        binding.themeSwitch.isChecked = ThemeManager.getTheme(requireContext())
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setDarkTheme()
            } else {
                setLightTheme()
            }
            ThemeManager.saveTheme(requireContext(), isChecked)
        }

        setFragmentResultListener("requestRename") { key, bundle ->
            val result = bundle.getString("resultRename")
            viewModel.rename(result.toString())
        }

//        binding.frameName.setOnTouchListener { view, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    view.setBackgroundColor(getColorFromTheme(R.attr.colorTouchedBackground))
//                    view.isPressed = true
//                    true
//                }
//
//                MotionEvent.ACTION_UP -> {
//                    view.setBackgroundColor(getColorFromTheme(R.attr.colorFragment))
//                    view.isPressed = false
//                    false
//                }
//
//                MotionEvent.ACTION_CANCEL -> {
//                    view.setBackgroundColor(getColorFromTheme(R.attr.colorFragment))
//                    view.isPressed = false
//                    false
//                }
//
//                else -> false
//            }
//        }

        binding.frameName.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_nameChangeDialogFragment)
        }

//        binding.frameLogout.setOnTouchListener { view, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    view.setBackgroundColor(getColorFromTheme(R.attr.colorTouchedBackground))
//                    view.isPressed = true
//                    true
//                }
//
//                MotionEvent.ACTION_UP -> {
//                    view.setBackgroundColor(getColorFromTheme(R.attr.colorFragment))
//                    view.isPressed = false
//                    false
//                }
//
//                MotionEvent.ACTION_CANCEL -> {
//                    view.setBackgroundColor(getColorFromTheme(R.attr.colorFragment))
//                    view.isPressed = false
//                    false
//                }
//
//                else -> false
//            }
//        }

        setFragmentResultListener("requestPassword") { key, bundle ->
            val newPassword = bundle.getString("newPassword").toString()
            viewModel.changePassword(newPassword)
        }

        binding.frameChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_passwordChangeDialogFragment)
        }

        binding.frameLogout.setOnClickListener {
            viewModel.logout(requireContext())
        }

        binding.frameLogoutAll.setOnClickListener {
            viewModel.logoutAll(requireContext())
        }


        binding.userAvatar.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setDarkTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    private fun setLightTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun render(state: SettingsScreenState) {
        when (state) {
            is SettingsScreenState.Loading -> showLoading(state)
            is SettingsScreenState.Content -> showContent(state)
            is SettingsScreenState.Error -> showError(state)
        }
    }

    private fun hideAll() {
        binding.overlay.isVisible = false
        binding.progressBar.isVisible = false
    }

    private fun showLoading(state: SettingsScreenState.Loading) {
        hideAll()
        binding.overlay.isVisible = true
        binding.progressBar.isVisible = true
    }

    private fun showContent(state: SettingsScreenState.Content) {
        hideAll()
        binding.userLogin.text = state.userLogin
        binding.userName.text = state.userName
        Glide.with(requireContext()).load(state.userAvatar).placeholder(R.drawable.avatar).into(binding.userAvatar)
    }

    private fun showError(state: SettingsScreenState.Error) {
        hideAll()
    }
}