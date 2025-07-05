package com.example.messenger.data.models

data class UserSearchResponse(
    val id: String,
    val name: String,
    val login: String,
    val avatar: String,
    val isInContacts: Boolean,
    val hasPendingRequest: Boolean,
    val incomeRequestId: String?
)