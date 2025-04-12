package com.example.messenger.contacts.ui.models

sealed class ContactsScreenState {
    data object Base : ContactsScreenState()
    data object Loading : ContactsScreenState()
    data class Error(val loginError: String?, val nameError: String?, val passwordError: String?) : ContactsScreenState()
}