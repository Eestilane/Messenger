package com.example.messenger.contacts.ui.models

import com.example.messenger.data.models.UserSearchResponse

sealed class UserSearchScreenState {
    data object Loading : UserSearchScreenState()
    data class Content(val users: List<UserSearchResponse>) : UserSearchScreenState()
    data class Error(val loginError: String?, val nameError: String?, val passwordError: String?): UserSearchScreenState()
}