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
import com.example.messenger.data.models.contacts.AcceptDeclineRequest
import com.example.messenger.data.models.contacts.RequestResponse
import com.example.messenger.libs.HandleOperators
import kotlinx.coroutines.launch

class RequestsViewModel(val apiService: ApiService, val context: Context, val view: View?) : ViewModel() {
    private val _incomingRequests = MutableLiveData<List<RequestResponse>>()
    private val _outgoingRequests = MutableLiveData<List<RequestResponse>>()
    val incomingRequests: LiveData<List<RequestResponse>> get() = _incomingRequests
    val outgoingRequests: LiveData<List<RequestResponse>> get() = _outgoingRequests

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context, view: View?): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    RequestsViewModel(
                        apiService = apiService,
                        context = context,
                        view = view
                    )
                }
            }
    }

    fun inRequestResponse() {
        viewModelScope.launch {
            val response = HandleOperators.handleRequest {
                apiService.getContactsInRequest()
            }
            when (response.code()) {
                200 -> {
                    _incomingRequests.value = response.body()
                }

                999 -> {
                }
            }
        }
    }

    fun outRequestResponse() {
        viewModelScope.launch {
            val response = HandleOperators.handleRequest {
                apiService.getContactsOutRequest()
            }
            when (response.code()) {
                200 -> {
                    _outgoingRequests.value = response.body()
                }

                999 -> {
                }
            }
        }
    }

    fun acceptRequest(userId: String) {
        viewModelScope.launch {
            val response = HandleOperators.handleRequest {
                apiService.acceptRequest(AcceptDeclineRequest(userId))
            }
            when (response.code()) {
                200 -> {
                    inRequestResponse()
                }

                999 -> {
                }
            }
        }
    }

    fun declineRequest(userId: String) {
        viewModelScope.launch {
            val response = HandleOperators.handleRequest {
                apiService.declineRequest(AcceptDeclineRequest(userId))
            }
            when (response.code()) {
                200 -> {
                    inRequestResponse()
                    outRequestResponse()
                }

                999 -> {
                }
            }
        }
    }
}