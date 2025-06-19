package com.example.messenger.chats.ui.view_models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messenger.chats.ui.models.AddUserRequest
import com.example.messenger.chats.ui.models.Chat
import com.example.messenger.chats.ui.models.CreateChatRequest
import com.example.messenger.data.ApiService
import kotlinx.coroutines.launch

class ChatsViewModel(private val apiService: ApiService, val context: Context): ViewModel() {

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context): ViewModelProvider.Factory = viewModelFactory {
                initializer {
                    ChatsViewModel(
                        apiService = apiService,
                        context = context,
                    )
                }
            }
    }

    private val _chats = MutableLiveData<List<Chat>>(emptyList())
    val chats: LiveData<List<Chat>> = _chats

    private val _navigateToChat = MutableLiveData<String?>()
    val navigateToChat: LiveData<String?> = _navigateToChat

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadChats() = viewModelScope.launch {
        try {
            val response = apiService.getChats()
            _chats.value = response.body()?.data ?: emptyList()
        } catch (e: Exception) {
            _errorMessage.postValue("Ошибка загрузки чатов")
        }
    }

    fun createChat(name: String) = viewModelScope.launch {
        try {
            val response = apiService.createChat(CreateChatRequest(name))
            if (response.isSuccessful) {
                response.body()?.let { chatId ->
                    if (chatId.isNotEmpty()) {
                        _navigateToChat.postValue(chatId)
                        loadChats()
                    }
                }
            } else {
                _errorMessage.postValue("Ошибка создания чата")
            }
        } catch (e: Exception) {
            _errorMessage.postValue("Ошибка создания чата")
        }
    }

    fun onChatNavigated() {
        _navigateToChat.value = null
    }

}