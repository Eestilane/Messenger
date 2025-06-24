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
import com.example.messenger.chats.ui.models.ChatsState
import com.example.messenger.chats.ui.models.CreateChatRequest
import com.example.messenger.data.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDateTime

class ChatsViewModel(private val apiService: ApiService, private val context: Context) : ViewModel() {

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer { ChatsViewModel(apiService, context) }
        }
    }

    private val _state = MutableLiveData<ChatsState>(ChatsState.Loading)
    val state: LiveData<ChatsState> = _state

    private val _navigateToChat = MutableLiveData<Chat?>()
    val navigateToChat: LiveData<Chat?> = _navigateToChat

    private val lastMessagesMap = mutableMapOf<String, Pair<String, LocalDateTime>>()

    fun loadChats() = viewModelScope.launch {
        _state.value = ChatsState.Loading
        try {
            val response = withContext(Dispatchers.IO) { apiService.getChats() }
            if (response.isSuccessful) {
                val chats = response.body()?.data ?: emptyList()
                _state.value = ChatsState.Success(chats)
                loadLastMessages(chats)
            } else {
                _state.value = ChatsState.Error("Ошибка загрузки чатов: ${response.message()}")
            }
        } catch (e: Exception) {
            _state.value = ChatsState.Error("Ошибка загрузки чатов: ${e.message}")
        }
    }

    private fun loadLastMessages(chats: List<Chat>) = viewModelScope.launch {
        chats.forEach { chat -> loadLastMessage(chat.id) { message, dateTime -> lastMessagesMap[chat.id] = message to dateTime }
        }
    }

    fun loadLastMessage(chatId: String, callback: (String, LocalDateTime) -> Unit) = viewModelScope.launch {
        try {
            val response = withContext(Dispatchers.IO) { apiService.getChatMessages(chatId, limit = 1) }
            if (response.isSuccessful) {
                val lastMessage = response.body()?.data?.firstOrNull()
                lastMessage?.let {
                    val dateTime = LocalDateTime.parse(it.sentAt)
                    callback(it.content, dateTime) } ?: callback("Нет сообщений", LocalDateTime.MIN)
            } else {
                callback("Ошибка загрузки", LocalDateTime.MIN)
            }
        } catch (e: Exception) {
            callback("Ошибка: ${e.message}", LocalDateTime.MIN)
        }
    }

    fun createChat(name: String, avatarUri: Uri? = null) = viewModelScope.launch {
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.createChat(CreateChatRequest(name))
            }
            if (!response.isSuccessful) {
                _state.value = ChatsState.Error("Ошибка создания чата")
                return@launch
            }
            val chatId = response.body() ?: return@launch
            avatarUri?.let { uri ->
                updateChatAvatar(chatId, uri)
            } ?: run {
                _navigateToChat.postValue(Chat(chatId, "", name, null))
            }
        } catch (e: Exception) {
            _state.value = ChatsState.Error("Ошибка: ${e.message}")
        }
    }

    private fun updateChatAvatar(chatId: String, uri: Uri) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri) ?: run {
                    _state.value = ChatsState.Error("Не удалось определить тип файла")
                    return@launch
                }

                val inputStream = contentResolver.openInputStream(uri) ?: run {
                    _state.value = ChatsState.Error("Не удалось открыть файл")
                    return@launch
                }

                val fileBody = inputStream.use {
                    it.readBytes().toRequestBody(mimeType.toMediaTypeOrNull())
                }

                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    "avatar_${System.currentTimeMillis()}.${mimeType.split("/").last()}",
                    fileBody
                )

                withContext(Dispatchers.IO) {
                    apiService.updateChatAvatar(filePart, chatId.toRequestBody("text/plain".toMediaTypeOrNull()))
                }

                _navigateToChat.postValue(Chat(chatId, "", "", null))

            } catch (e: Exception) {
                _state.value = ChatsState.Error("Ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun onChatNavigated() {
        _navigateToChat.value = null
    }
}