package com.example.messenger.settings.ui.models

sealed class SettingsScreenState {
    data object Loading : SettingsScreenState()
    data class Content(val userId: String, val userName: String, val userLogin: String, val userAvatar: String? = null) : SettingsScreenState()
    data class Error(val nameError: String?, val loginError: String?, val avatarError: String?) : SettingsScreenState()
    data object Navigate : SettingsScreenState()
}