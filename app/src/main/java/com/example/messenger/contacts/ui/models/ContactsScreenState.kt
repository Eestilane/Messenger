package com.example.messenger.contacts.ui.models

import com.example.messenger.data.models.contacts.ContactsResponse

sealed class ContactsScreenState {
    data object Loading : ContactsScreenState()
    data class Content(val contacts: List<ContactsResponse>) : ContactsScreenState()
    data class Error(val loginError: String?, val nameError: String?, val passwordError: String?) : ContactsScreenState()
}