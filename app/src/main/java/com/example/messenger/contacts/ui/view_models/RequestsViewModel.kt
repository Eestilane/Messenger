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
import com.example.messenger.contacts.ui.models.RequestsScreenState
import com.example.messenger.data.ApiService
import com.example.messenger.data.models.contacts.AcceptDeclineRequest
import com.example.messenger.libs.HandleOperators
import com.example.messenger.libs.ThrottleOperators
import kotlinx.coroutines.launch

class RequestsViewModel(val apiService: ApiService, val context: Context, val view: View?) : ViewModel() {
    private val _requestsState = MutableLiveData<RequestsScreenState>(RequestsScreenState.Loading)
    val requestsState: LiveData<RequestsScreenState> get() = _requestsState
    var incomingRequest = false

    fun renderState(state: RequestsScreenState) {
        _requestsState.value = state
    }

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
            renderState(RequestsScreenState.Loading)
            val response = HandleOperators.handleRequest {
                apiService.getContactsInRequest()
            }
            when (response.code()) {
                200 -> {
                    val requests = response.body()
                    renderState(
                        RequestsScreenState.Content(
                            requests = requests!!
                        )
                    )
                }

                999 -> {
                }
            }
        }
    }

    fun outRequestResponse() {
        viewModelScope.launch {
            renderState(RequestsScreenState.Loading)
            val response = HandleOperators.handleRequest {
                apiService.getContactsOutRequest()
            }
            when (response.code()) {
                200 -> {
                    val requests = response.body()
                    renderState(
                        RequestsScreenState.Content(
                            requests = requests!!
                        )
                    )
                }

                999 -> {
                }
            }
        }
    }

    val accept: (String) -> Unit = ThrottleOperators.throttleLatest(
        300L, viewModelScope,
        this::acceptRequest
    )

    fun acceptRequest(userId: String) {
        viewModelScope.launch {
            renderState(RequestsScreenState.Loading)
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

    val decline: (String) -> Unit = ThrottleOperators.throttleLatest(
        300L, viewModelScope,
        this::declineRequest
    )

    fun declineRequest(userId: String) {
        viewModelScope.launch {
            renderState(RequestsScreenState.Loading)
            val response = HandleOperators.handleRequest {
                apiService.declineRequest(AcceptDeclineRequest(userId))
            }
            when (response.code()) {
                200 -> {
                    if (incomingRequest) {
                        inRequestResponse()
                    } else {
                        outRequestResponse()
                    }
                }

                999 -> {
                }
            }
        }
    }
}