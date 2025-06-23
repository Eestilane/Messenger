package com.example.messenger.chats.ui.view_models

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messenger.chats.ui.models.Chat
import com.example.messenger.chats.ui.models.CreateChatRequest
import com.example.messenger.data.ApiService
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ChatsViewModel(
    private val apiService: ApiService,
    private val context: Context
) : ViewModel() {

    companion object {
        fun getViewModelFactory(
            apiService: ApiService,
            context: Context
        ): ViewModelProvider.Factory = viewModelFactory {
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

    private val _navigateToChat = MutableLiveData<Chat?>()
    val navigateToChat: LiveData<Chat?> = _navigateToChat

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _avatarUri = MutableLiveData<Uri?>(null)
    val avatarUri: LiveData<Uri?> = _avatarUri

    fun loadChats() = viewModelScope.launch {
        try {
            val response = apiService.getChats()
            if (response.isSuccessful) {
                _chats.value = response.body()?.data ?: emptyList()
            } else {
                _errorMessage.value = "Ошибка загрузки чатов: ${response.message()}"
            }
        } catch (e: Exception) {
            _errorMessage.value = "Ошибка загрузки чатов: ${e.message}"
        }
    }

    fun createChat(name: String) = viewModelScope.launch {
        try {
            val response = apiService.createChat(CreateChatRequest(name))
            if (response.isSuccessful) {
                loadChats() // Обновляем список чатов после создания
            } else {
                _errorMessage.value = "Ошибка создания чата: ${response.message()}"
            }
        } catch (e: Exception) {
            _errorMessage.value = "Ошибка создания чата: ${e.message}"
        }
    }

    fun loadLastMessage(chatId: String, callback: (String, String) -> Unit) = viewModelScope.launch {
        try {
            val response = apiService.getChatMessages(chatId, limit = 1)
            if (response.isSuccessful) {
                val lastMessage = response.body()?.data?.firstOrNull()
                lastMessage?.let {
                    val time = formatTime(it.sentAt)
                    callback(it.content, time)
                } ?: callback("Нет сообщений", "")
            } else {
                callback("Ошибка загрузки", "")
            }
        } catch (e: Exception) {
            callback("Ошибка: ${e.message}", "")
        }
    }

    fun requestNavigationToChat(chat: Chat) {
        _navigateToChat.value = chat
    }

    fun onChatNavigated() {
        _navigateToChat.value = null
    }

    fun setAvatarUri(uri: Uri?) {
        _avatarUri.value = uri
    }

    private fun formatTime(isoTime: String): String {
        return try {
            val utcTime = LocalDateTime.parse(isoTime).atZone(ZoneId.of("UTC"))
            val moscowTime = utcTime.withZoneSameInstant(ZoneId.of("Europe/Moscow"))
            moscowTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            isoTime
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}