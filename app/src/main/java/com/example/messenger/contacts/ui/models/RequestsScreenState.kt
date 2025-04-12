package com.example.messenger.contacts.ui.models

sealed class RequestsScreenState {
    data object Base : RequestsScreenState()
    data object Loading : RequestsScreenState()
    data class Error(val loginError: String?, val nameError: String?, val passwordError: String?) : RequestsScreenState()
}