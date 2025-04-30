package com.example.messenger.settings.ui.models

sealed class NameChangeScreenState {
    data object Null : NameChangeScreenState()
    data class Error(val renameError: String?) : NameChangeScreenState()
    data object NavigateToSettings : NameChangeScreenState()
}