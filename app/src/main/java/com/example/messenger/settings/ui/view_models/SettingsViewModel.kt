package com.example.messenger.settings.ui.view_models

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messenger.R
import com.example.messenger.contacts.ui.view_models.ContactsViewModel
import com.example.messenger.data.ApiService
import com.example.messenger.data.models.UpdateNameRequest
import com.example.messenger.data.models.UserResponse
import com.example.messenger.libs.HandleOperators
import com.example.messenger.settings.ui.models.SettingsScreenState
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.String

class SettingsViewModel(val apiService: ApiService, val context: Context, val view: View?) : ViewModel() {
    private val _settingsData = MutableLiveData<SettingsScreenState>(SettingsScreenState.Loading)
    val settingsData: LiveData<SettingsScreenState> get() = _settingsData

    fun renderState(state: SettingsScreenState) {
        _settingsData.postValue(state)
    }

    fun processSettings() {
        renderState(SettingsScreenState.Loading)
        viewModelScope.launch {
            val userResponse = apiService.getUser()
            if (userResponse.isSuccessful) {
                val user = userResponse.body()!!
                renderState(SettingsScreenState.Content(userId = user.id,
                    userName = user.name, userLogin = user.login, userAvatar = user.avatar))
            }
        }
    }

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context, view: View?): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    SettingsViewModel(
                        apiService = apiService,
                        context = context,
                        view = view
                    )
                }
            }
    }

    fun getUser() {
        viewModelScope.launch {
            val response = HandleOperators.handleRequest {
                apiService.getUser()
            }
            when (response.code()) {
                200 -> {
                    val user = response.body()!!
                    _settingsData.value = SettingsScreenState.Content(userId = user.id,
                        userName = user.name, userLogin = user.login, userAvatar = user.avatar)
                }
                -1 -> {
                }
            }
        }
    }

    fun rename(result: String) {
        viewModelScope.launch {
            val response = HandleOperators.handleRequest {
                apiService.userUpdateName(UpdateNameRequest(result))
            }
            when (response.code()) {
                200 -> {
                }
                -1 -> {
                }
            }
        }
    }
}