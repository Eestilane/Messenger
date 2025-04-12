package com.example.messenger.contacts.ui.view_models

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
import com.example.messenger.data.models.UserSearchResponse
import com.example.messenger.data.models.contacts.ContactRequest
import com.example.messenger.libs.DebounceOperators
import com.example.messenger.libs.HandleOperators
import kotlinx.coroutines.launch

class UserSearchViewModel(val apiService: ApiService, val context: Context, val view: View?) : ViewModel() {
    private val _userSearch = MutableLiveData<List<UserSearchResponse>>()
    val userSearch: LiveData<List<UserSearchResponse>> get() = _userSearch

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

    val searchUser: (String) -> Unit = DebounceOperators.debounce(
        300L, viewModelScope,
        this::onUserSearch
    )

    fun onUserSearch(newText: String) {
        viewModelScope.launch {
            val response = HandleOperators.handleRequest {
                apiService.userSearch(newText)
            }
            when (response.code()) {
                200 -> {
                    _userSearch.value = response.body()
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
            when (response.code()) {
                200 -> {
                }

                999 -> {
                }
            }
        }
    }
}