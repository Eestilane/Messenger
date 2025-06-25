package com.example.messenger.chats.ui.view_models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messenger.chats.ui.models.ChatNavigationParameters
import com.example.messenger.chats.ui.models.ChatsState
import com.example.messenger.chats.ui.models.CreateChatRequest
import com.example.messenger.data.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatsViewModel(private val apiService: ApiService, private val context: Context) : ViewModel() {

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer { ChatsViewModel(apiService, context) }
        }
    }

    private val _state = MutableLiveData<ChatsState>(ChatsState.Loading)
    val state: LiveData<ChatsState> = _state

    private val _navigateToChat = MutableLiveData<ChatNavigationParameters?>()
    val navigateToChat: LiveData<ChatNavigationParameters?> = _navigateToChat

    private val _ownerId = MutableLiveData<String?>()
    val ownerId: LiveData<String?> = _ownerId

    fun loadChats() = viewModelScope.launch {
        _state.value = ChatsState.Loading
        try {
            val response = withContext(Dispatchers.IO) { apiService.getChats() }
            if (response.isSuccessful) {
                val chats = response.body()?.data ?: emptyList()
                _state.value = ChatsState.Success(chats)
            } else {
                _state.value = ChatsState.Error("Ошибка загрузки чатов")
            }
        } catch (e: Exception) {
            _state.value = ChatsState.Error("Ошибка загрузки чатов")
        }
    }

    fun createChat(name: String) = viewModelScope.launch {
        try {
            _state.value = ChatsState.Loading
            val response = withContext(Dispatchers.IO) { apiService.createChat(CreateChatRequest(name)) }
            if (!response.isSuccessful) {
                _state.value = ChatsState.Error("Ошибка создания чата")
                return@launch
            }
            val chatId = response.body() ?: return@launch
            val ownerId = getOwnerId().toString()
            val newChat = ChatNavigationParameters(chatId, ownerId ,name.trim(), null, false)
            _navigateToChat.postValue(newChat)
        } catch (e: Exception) {
            _state.value = ChatsState.Error("Ошибка создания чата")
        }
    }

    private fun getOwnerId() {
        viewModelScope.launch {
            _ownerId.value = try {
                val response = apiService.getUser()
                if (response.isSuccessful) {
                    response.body()?.id
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun onChatNavigated() {
        _navigateToChat.value = null
    }
}