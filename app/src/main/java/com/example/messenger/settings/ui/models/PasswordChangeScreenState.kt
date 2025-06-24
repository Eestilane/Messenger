package com.example.messenger.settings.ui.models

sealed class PasswordChangeScreenState {
    data object Null : PasswordChangeScreenState()
    data class Error(val passwordChangeError: String?) : PasswordChangeScreenState()
}