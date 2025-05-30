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
import com.example.messenger.data.ApiService
import com.example.messenger.data.models.UpdateNameRequest
import com.example.messenger.data.models.UserResponse
import com.example.messenger.data.models.errors.AuthErrorBody422
import com.example.messenger.libs.HandleOperators
import com.example.messenger.libs.SingleLiveEvent
import com.example.messenger.libs.TokenManager
import com.example.messenger.settings.ui.models.NameChangeScreenState
import com.example.messenger.settings.ui.models.SettingsScreenState
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import kotlin.String

class SettingsViewModel(val apiService: ApiService, val context: Context, val view: View?) : ViewModel() {
    private val _settingsData = MutableLiveData<SettingsScreenState>(SettingsScreenState.Loading)
    val settingsData: LiveData<SettingsScreenState> get() = _settingsData

    private val _renameData = MutableLiveData<NameChangeScreenState>(NameChangeScreenState.Null)
    val renameData: LiveData<NameChangeScreenState> get() = _renameData

    private val _navigateToAuth = SingleLiveEvent<Boolean>()
    val navigateToAuth: LiveData<Boolean> get() = _navigateToAuth

    private val _navigateToSettings = SingleLiveEvent<Boolean>()
    val navigateToSettings: LiveData<Boolean> get() = _navigateToSettings

    private lateinit var user: UserResponse

    init {
        getUser()
    }

    fun renderSettingsState(state: SettingsScreenState) {
        _settingsData.postValue(state)
    }

    fun renderNameChangeState(state: NameChangeScreenState) {
        _renameData.postValue(state)
    }

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context, view: View?): ViewModelProvider.Factory = viewModelFactory {
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
        renderSettingsState(SettingsScreenState.Loading)
        viewModelScope.launch {
            val response = HandleOperators.handleRequest {
                apiService.getUser()
            }
            when (response.code()) {
                200 -> {
                    user = response.body()!!
                    renderSettingsState(
                        SettingsScreenState.Content(
                            userId = user.id, userName = user.name, userLogin = user.login, userAvatar = user.avatar
                        )
                    )
                }

                999 -> {

                }
            }
        }
    }

    fun logout(context: Context) {
        renderSettingsState(SettingsScreenState.Loading)
        viewModelScope.launch {
            val response = HandleOperators.handleRequest {
                apiService.logout()
            }
            when (response.code()) {
                200 -> {
                    TokenManager.clearToken(context)
                    _navigateToAuth.postValue(true)
                }

                999 -> {

                }
            }
        }
    }

    fun rename(result: String) {
        renderSettingsState(SettingsScreenState.Loading)
        renderNameChangeState(NameChangeScreenState.Null)
        viewModelScope.launch {
            val response = HandleOperators.handleRequest {
                apiService.userUpdateName(UpdateNameRequest(result))
            }
            when (response.code()) {
                200 -> {
                    user = user.copy(name = result)
                    renderSettingsState(
                        SettingsScreenState.Content(
                            userId = user.id, userName = user.name, userLogin = user.login, userAvatar = user.avatar
                        )
                    )
                    _navigateToSettings.postValue(true)
                }

                422 -> {
                    getUser()
                    val error = Gson().fromJson(response.errorBody()?.string(), AuthErrorBody422::class.java)
                    renderNameChangeState(
                        NameChangeScreenState.Error(
                            renameError = error.errors.Name?.firstOrNull(),
                        )
                    )
                }

                999 -> {
                }
            }
        }
    }

    fun updateAvatar(filePart: MultipartBody.Part) {
        renderSettingsState(SettingsScreenState.Loading)
        viewModelScope.launch {
            val response = HandleOperators.handleRequest {
                apiService.updateAvatar(filePart)
            }
            when (response.code()) {
                200 -> {
                    user = user.copy(avatar = response.body()!!)
                    renderSettingsState(
                        SettingsScreenState.Content(
                            userId = user.id, userName = user.name, userLogin = user.login, userAvatar = user.avatar
                        )
                    )
                }

                999 -> {
                }
            }
        }
    }
}