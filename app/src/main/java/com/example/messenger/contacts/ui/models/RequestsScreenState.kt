package com.example.messenger.contacts.ui.models

sealed class RequestsScreenState {
    data object Loading : RequestsScreenState()
    data object Content : RequestsScreenState()
    data class Error(val loginError: String?, val nameError: String?, val passwordError: String?) : RequestsScreenState()
}