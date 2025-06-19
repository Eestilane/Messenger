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
import com.example.messenger.data.ApiService
import com.example.messenger.data.models.UserResponse
import kotlinx.coroutines.launch

class ChatViewModel(private val apiService: ApiService, val context: Context, val view: View?): ViewModel() {

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context, view: View?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ChatViewModel(
                    apiService = apiService,
                    context = context,
                    view = view
                )
            }
        }
    }

    private val _messages = MutableLiveData<MutableList<String>>(mutableListOf())
    val messages: LiveData<MutableList<String>> = _messages

    private val _chatUsers = MutableLiveData<List<UserResponse>>()
    val chatUsers: LiveData<List<UserResponse>> = _chatUsers

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadChatUsers(chatId: String) = viewModelScope.launch {
        try {
            val response = apiService.getChatUsers(chatId)
            if (response.isSuccessful) {
                _chatUsers.postValue(response.body() ?: emptyList())
            } else {
                _errorMessage.postValue("Ошибка загрузки пользователей")
            }
        } catch (e: Exception) {
            _errorMessage.postValue("Ошибка сети")
        }
    }

}
