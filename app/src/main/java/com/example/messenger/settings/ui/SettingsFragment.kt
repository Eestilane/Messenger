package com.example.messenger.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.messenger.R
import com.example.messenger.data.RetrofitClient
import com.example.messenger.data.models.UserResponse
import com.example.messenger.databinding.FragmentSettingsBinding
import com.example.messenger.libs.ThemeManager
import com.example.messenger.libs.TokenManager
import com.example.messenger.settings.ui.models.SettingsScreenState
import com.example.messenger.settings.ui.view_models.SettingsViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val apiService by lazy {
        RetrofitClient.create(requireContext(), view)
    }
    private val viewModel by viewModels<SettingsViewModel> {
        SettingsViewModel.getViewModelFactory(apiService, requireContext(), view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.settingsData.observe(viewLifecycleOwner) {
            settingsData -> render(settingsData)
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

        binding.logout.setOnClickListener {
            RetrofitClient.create(requireContext(), view).logout().enqueue(
                object :
                    Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        if (response.isSuccessful) {
                            TokenManager.clearToken(requireContext())
                            findNavController().navigate(com.example.messenger.R.id.action_settingsFragment_to_authFragment)
                        } else {
                        }
                    }

                    override fun onFailure(p0: Call<Unit?>, p1: Throwable) {
                    }
                })
        }

        setFragmentResultListener("requestKey") { key, bundle ->
            val result = bundle.getString("resultKey")
            viewModel.rename(result.toString())
        }

        binding.rename.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_nameChangeDialogFragment)
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
            is SettingsScreenState.Content-> showContent(state)
            is SettingsScreenState.Error -> return
            is SettingsScreenState.Loading -> showLoading(state)
        }
    }

    private fun showContent(state: SettingsScreenState.Content) {
        binding.userLogin.text = getString(R.string.user, state.userLogin)
        binding.userName.text = getString(R.string.name, state.userName)
    }

    private fun showLoading(state: SettingsScreenState.Loading) {
        viewModel.getUser()
        viewModel.processSettings()
    }
}