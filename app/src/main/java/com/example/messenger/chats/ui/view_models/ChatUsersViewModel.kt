package com.example.messenger.chats.ui.view_models

import android.content.Context
import android.net.Uri
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ChatUsersViewModel(private val apiService: ApiService, private val context: Context, private val view: View?) : ViewModel() {

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

    private val _currentChatAvatar = MutableLiveData<String>()
    val currentChatAvatar: LiveData<String> = _currentChatAvatar

    fun loadChatUsers(chatId: String) = viewModelScope.launch {
        try {
            val response = apiService.getChatUsers(chatId)
            if (response.isSuccessful) {
                _chatUsers.postValue(response.body()?.data ?: emptyList())
            } else {
                _errorMessage.postValue("Ошибка загрузки участников чата")
            }
        } catch (e: Exception) {
            _errorMessage.postValue("Ошибка")
        }
    }

    fun removeUserFromChat(chatId: String, userId: String) = viewModelScope.launch {
        try {
            val response = apiService.removeUserFromChat(RemoveUserRequest(chatId, userId))
            if (response.isSuccessful) {
                loadChatUsers(chatId)
                _successMessage.postValue("Пользователь удален")
            } else {
                _errorMessage.postValue("Ошибка удаления пользователя")
            }
        } catch (e: Exception) {
            _errorMessage.postValue("Ошибка")
        }
    }

    fun updateChatAvatar(chatId: String, uri: Uri) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri) ?: run {
                    _errorMessage.postValue("Не удалось определить тип файла")
                    return@launch
                }

                val inputStream = contentResolver.openInputStream(uri) ?: run {
                    _errorMessage.postValue("Не удалось открыть файл")
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
                val chatIdPart = chatId.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.updateChatAvatar(filePart, chatIdPart)
                if (response.isSuccessful) {
                    val newAvatarUrl = response.body() ?: ""
                    _currentChatAvatar.postValue(newAvatarUrl)
                    _successMessage.postValue("Аватар чата обновлен")
                } else {
                    _errorMessage.postValue("Ошибка обновления аватара")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка")
            }
        }
    }

    fun deleteChat(chatId: String) = viewModelScope.launch {
        try {
            apiService.deleteChat(chatId)
        } catch (e: Exception) {
            _errorMessage.postValue("Ошибка")
        }
    }

    fun onSuccessMessageShown() {
        _successMessage.value = null
    }

    fun onErrorMessageShown() {
        _errorMessage.value = null
    }
}