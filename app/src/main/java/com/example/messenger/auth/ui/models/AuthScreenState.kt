package com.example.messenger.auth.ui.models;

sealed class AuthScreenState {
    data object Loading : AuthScreenState()
    data object Content : AuthScreenState()
    data class Error(val loginError: String? = null, val passwordError: String? = null, val nameError: String? = null, val errorMessage: String? = null) : AuthScreenState()
}