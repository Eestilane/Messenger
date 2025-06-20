package com.example.messenger.chats.ui.view_models

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messenger.chats.ui.models.RemoveUserRequest
import com.example.messenger.data.ApiService
import com.example.messenger.data.models.UserResponse
import kotlinx.coroutines.launch

class ChatUsersViewModel (private val apiService: ApiService, private val context: Context, private val view: View?) : ViewModel() {

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context, view: View?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ChatUsersViewModel(apiService, context, view)
            }
        }
    }

    private val _chatUsers = MutableLiveData<List<UserResponse>>()
    val chatUsers: LiveData<List<UserResponse>> = _chatUsers

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadChatUsers(chatId: String) = viewModelScope.launch {
        try {
            val response = apiService.getChatUsers(chatId)
            if (response.isSuccessful) {
                _chatUsers.postValue(response.body()?.data ?: emptyList())
            }
        } catch (e: Exception) {
            _errorMessage.postValue("Ошибка отображения участников чата")
        }
    }

    fun removeUserFromChat(chatId: String, userId: String) = viewModelScope.launch {
        try {
            apiService.removeUserFromChat(RemoveUserRequest(chatId, userId))
            loadChatUsers(chatId)
        } catch (e: Exception) {
            _errorMessage.postValue("Ошибка сети: ${e.localizedMessage}")
        }
    }

    fun onSuccessMessageShown() {
        _successMessage.value = null
    }

    fun onErrorMessageShown() {
        _errorMessage.value = null
    }

}