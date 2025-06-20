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
import com.example.messenger.chats.ui.models.AddUserRequest
import com.example.messenger.contacts.ui.models.UserSearchScreenState
import com.example.messenger.data.ApiService
import com.example.messenger.data.models.UserSearchResponse
import com.example.messenger.libs.DebounceOperators
import com.example.messenger.libs.HandleOperators
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AddUserToChatViewModel(private val apiService: ApiService, private val context: Context, private val view: View?) : ViewModel() {

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context, view: View?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AddUserToChatViewModel(apiService, context, view)
            }
        }
    }
    var lastExpression: String = ""

    val searchUser: (String) -> Unit = DebounceOperators.debounce(
        300L, viewModelScope,
        this::onUserSearch
    )

    init{
        searchUser("")
    }

    private val _userSearch = MutableLiveData<UserSearchScreenState>(UserSearchScreenState.Loading)
    val userSearch: LiveData<UserSearchScreenState> get() = _userSearch

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun renderState(state: UserSearchScreenState) {
        _userSearch.value = state
    }

    fun onUserSearch(newText: String) {
        viewModelScope.launch {
            lastExpression = newText
            val response = HandleOperators.handleRequest {
                apiService.userSearch(newText)
            }
            when (response.code()) {
                200 -> {
                    val users = response.body()
                    renderState(
                        UserSearchScreenState.Content(
                            users = users!!
                        )
                    )
                }

                999 -> {
                }
            }
        }
    }

    fun addUserToChat(chatId: String, userId: String) = viewModelScope.launch {
        try {
            apiService.addUserToChat(AddUserRequest(chatId, userId))
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