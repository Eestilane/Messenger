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
import com.example.messenger.chats.ui.models.CreateDirectChatRequest
import com.example.messenger.contacts.ui.models.ContactsScreenState
import com.example.messenger.data.ApiService
import com.example.messenger.data.models.contacts.ContactRequest
import com.example.messenger.libs.HandleOperators
import kotlinx.coroutines.launch

class ContactsViewModel(val apiService: ApiService, val context: Context, val view: View?) : ViewModel() {
    private val _contacts = MutableLiveData<ContactsScreenState>(ContactsScreenState.Loading)
    val contacts: LiveData<ContactsScreenState> get() = _contacts

    private val _navigateToChat = MutableLiveData<String?>()
    val navigateToChat: LiveData<String?> = _navigateToChat

    init {
        getContacts()
    }

    private fun renderState(state: ContactsScreenState) {
        _contacts.value = state
    }

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context, view: View?): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ContactsViewModel(apiService = apiService, context = context, view = view)
                }
            }
    }

    fun getContacts() {
        viewModelScope.launch {
            renderState(ContactsScreenState.Loading)
            val response = HandleOperators.handleRequest {
                apiService.getContacts()
            }
            when (response.code()) {
                200 -> {
                    val users = response.body()!!
                    renderState(ContactsScreenState.Content(contacts = users))
                }

                999 -> {
                }
            }
        }
    }

    fun deleteContact(userId: String) {
        viewModelScope.launch {
            renderState(ContactsScreenState.Loading)
            val response = HandleOperators.handleRequest {
                apiService.deleteContact(ContactRequest(userId))
            }
            if (response.isSuccessful) {
                Toast.makeText(context,"Пользователь удалён", Toast.LENGTH_SHORT).show()
            }
            when (response.code()) {
                200 -> {
                    getContacts()
                }
                999 -> {
                }
            }
        }
    }

    fun getOrCreateDirectChat(contactId: String) = viewModelScope.launch {
        try {

            apiService.getDirectChat(contactId).let { response ->
                if (response.isSuccessful && response.body()?.data != null) {
                    _navigateToChat.postValue(response.body()?.data)
                    return@launch
                }
            }

            apiService.createDirectChat(CreateDirectChatRequest(contactId)).let { response ->
                if (response.isSuccessful) {
                    _navigateToChat.postValue(response.body())
                } else {
                    Toast.makeText(context,"Ошибка создания чата", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context,"Ошибка создания чата", Toast.LENGTH_SHORT).show()
        }
    }

    fun onDirectChatNavigated() {
        _navigateToChat.value = null
    }
}