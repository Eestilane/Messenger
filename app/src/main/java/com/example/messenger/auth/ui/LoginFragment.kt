package com.example.messenger.auth.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.messenger.R
import com.example.messenger.data.RetrofitClient
import com.example.messenger.data.models.LoginRequest
import com.example.messenger.data.models.LoginResponse
import com.example.messenger.data.models.errors.AuthErrorBody422
import com.example.messenger.databinding.FragmentAuthLoginBinding
import com.example.messenger.libs.TokenManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {
    private var _binding: FragmentAuthLoginBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginError.isVisible = false
        binding.passwordError.isVisible = false
        binding.loginOrPasswordError.isVisible = false

        binding.loginButton.setOnClickListener {
            binding.loginError.isVisible = false
            binding.passwordError.isVisible = false
            binding.loginOrPasswordError.isVisible = false
            RetrofitClient.create(requireContext(), view).login(LoginRequest(binding.enterLogin.text.toString(), binding.enterPassword.text.toString())).enqueue(
                object :
                    Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.token?.let { token ->
                                TokenManager.saveToken(requireContext(), token)
                                findNavController().navigate(R.id.action_authFragment_to_chatsFragment)
                            }
                        } else if (response.code() == 422) {
                            val gson = Gson()
                            val type = object : TypeToken<AuthErrorBody422>() {}.type
                            var errorResponse: AuthErrorBody422? = gson.fromJson(response.errorBody()!!.charStream(), type)
                            if (errorResponse == null)
                                return

                            if (errorResponse.errors.Login != null) {
                                binding.loginError.text = errorResponse.errors.Login!!.first()
                                binding.loginError.isVisible = true
                            }
                            if (errorResponse.errors.Password != null) {
                                binding.passwordError.text = errorResponse.errors.Password!!.first()
                                binding.passwordError.isVisible = true
                            }
                        } else if (response.code() == 400) {
                            binding.loginOrPasswordError.text = response.errorBody()?.charStream()?.readText()
                            binding.loginOrPasswordError.isVisible = true
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    }
                })
        }
    }
}