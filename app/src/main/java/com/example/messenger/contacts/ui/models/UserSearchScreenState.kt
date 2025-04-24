package com.example.messenger.contacts.ui.models

sealed class UserSearchScreenState {
    data object Loading : UserSearchScreenState()
    data object Content : UserSearchScreenState()
    data class Error(val loginError: String?, val nameError: String?, val passwordError: String?): UserSearchScreenState()
}