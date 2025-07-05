package com.example.messenger.contacts.ui.view_models

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messenger.contacts.ui.models.UserSearchScreenState
import com.example.messenger.data.ApiService
import com.example.messenger.data.models.contacts.ContactRequest
import com.example.messenger.libs.DebounceOperators
import com.example.messenger.libs.HandleOperators
import kotlinx.coroutines.launch

class UserSearchViewModel(val apiService: ApiService, val context: Context, val view: View?) : ViewModel() {
    private val _userSearch = MutableLiveData<UserSearchScreenState>(UserSearchScreenState.Loading)
    val userSearch: LiveData<UserSearchScreenState> get() = _userSearch
    var lastExpression: String = ""

    val searchUser: (String) -> Unit = DebounceOperators.debounce(
        300L, viewModelScope,
        this::onUserSearch
    )

    init{
        searchUser("")
    }

    fun renderState(state: UserSearchScreenState) {
        _userSearch.value = state
    }

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context, view: View?): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    UserSearchViewModel(
                        apiService = apiService,
                        context = context,
                        view = view
                    )
                }
            }
    }

    fun onUserSearch(newText: String) {
        viewModelScope.launch {
            lastExpression = newText
            renderState(UserSearchScreenState.Loading)
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

    fun addFriendRequest(userId: String) {
        viewModelScope.launch {
            val response = HandleOperators.handleRequest {
                apiService.addContact(ContactRequest(userId))
            }
            if (response.isSuccessful) {
                Toast.makeText(context,"Запрос отправлен", Toast.LENGTH_SHORT).show()
            }
            when (response.code()) {
                200 -> {
                    searchUser(lastExpression)
                }
                409 -> {
                    Toast.makeText(context,"Пользователь уже добавлен в контакты", Toast.LENGTH_SHORT).show()
                }
                999 -> {
                }
            }
        }
    }
}