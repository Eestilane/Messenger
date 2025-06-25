package com.example.messenger.chats.ui.models

data class ChatNavigationParameters (
    val id: String,
    val ownerId: String,
    val name: String,
    val avatar: String?,
    val isDirect: Boolean
)