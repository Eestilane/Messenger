package com.example.messenger.contacts.ui.models

import com.example.messenger.data.models.contacts.RequestResponse

sealed class RequestsScreenState {
    data object Loading : RequestsScreenState()
    data class Content(val requests: List<RequestResponse>) : RequestsScreenState()
    data class Error(val loginError: String?, val nameError: String?, val passwordError: String?) : RequestsScreenState()
}