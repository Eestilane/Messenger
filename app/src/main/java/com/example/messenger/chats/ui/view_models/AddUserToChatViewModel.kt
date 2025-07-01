package com.example.messenger.chats.ui.view_models

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messenger.chats.ui.models.AddUserRequest
import com.example.messenger.contacts.ui.models.ContactsScreenState
import com.example.messenger.data.ApiService
import com.example.messenger.libs.HandleOperators
import kotlinx.coroutines.launch

class AddUserToChatViewModel(private val apiService: ApiService, private val context: Context) : ViewModel() {

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AddUserToChatViewModel(apiService = apiService, context = context)
            }
        }
    }

    private val _contacts = MutableLiveData<ContactsScreenState>(ContactsScreenState.Loading)
    val contacts: LiveData<ContactsScreenState> get() = _contacts

    init{
        getContacts()
    }

    fun renderState(state: ContactsScreenState) {
        _contacts.value = state
    }

    fun getContacts() {
        viewModelScope.launch {
            renderState(ContactsScreenState.Loading)
            val response = HandleOperators.handleRequest {
                apiService.getContacts()
            }
            when (response.code()) {
                200 -> {
                    val contacts = response.body()!!
                    renderState(
                        ContactsScreenState.Content(
                            contacts = contacts
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
            Toast.makeText(context, "Ошибка добавления пользователя", Toast.LENGTH_SHORT).show()
        }
    }
}