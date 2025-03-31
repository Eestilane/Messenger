package com.example.messenger.settings.ui

import com.example.messenger.R
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.messenger.contacts.ui.dialogs.UserSearchDialogFragment
import com.example.messenger.data.ApiService
import com.example.messenger.data.RetrofitClient
import com.example.messenger.data.models.LoginResponse
import com.example.messenger.data.models.RegisterRequest
import com.example.messenger.data.models.UserResponse
import com.example.messenger.databinding.FragmentSettingsBinding
import com.example.messenger.libs.ThemeManager
import com.example.messenger.libs.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        RetrofitClient.create(requireContext(), view).getUser().enqueue(
            object :
                Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { user ->
                            binding.userLogin.text = getString(R.string.user, user.login)
                            binding.userName.text = getString(R.string.name, user.name)
                        }
                    }
                }

                override fun onFailure(p0: Call<UserResponse?>, p1: Throwable) {
                }
            })

        binding.themeSwitch.isChecked = ThemeManager.getTheme(requireContext())
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setDarkTheme()
            } else {
                setLightTheme()
            }
            ThemeManager.saveTheme(requireContext(), isChecked)
        }

//        val languagesArray = arrayOf("English", "Русский", "Українська")
//        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, languagesArray)
//        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
//        binding.languageSpinner.adapter = adapter

//        val prefs = requireActivity().getSharedPreferences("AppSettings", AppCompatActivity.MODE_PRIVATE)
//        val savedLanguage = prefs.getString("My_Lang", "en")
//        setLocale(savedLanguage ?: "en")

//        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                val languageCode = if (position == 0) "en" else if (position == 1) "ru" else "uk"
//                setLocale(languageCode)
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//            }
//        }

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

        binding.rename.setOnClickListener{
            NameChangeDialogFragment().show(childFragmentManager, "ConfirmationDialog")
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

//    private fun setLocale(lang: String) {
//        val locale = Locale(lang)
//        Locale.setDefault(locale)
//        val configuration = Configuration()
//        configuration.setLocale(locale)
//        requireActivity().resources.updateConfiguration(configuration, requireActivity().resources.displayMetrics)
//
//        val editor = requireActivity().getSharedPreferences("AppSettings", AppCompatActivity.MODE_PRIVATE).edit()
//        editor.putString("My_Lang", lang)
//        editor.apply()
//    }
}