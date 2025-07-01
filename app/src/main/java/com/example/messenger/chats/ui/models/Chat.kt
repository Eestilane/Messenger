package com.example.messenger.chats.ui.models

data class Chat(
    val id: String,
    val ownerId: String,
    val name: String?,
    val avatar: String?,
    val isDirect: Boolean,
    val lastMessage: Message?
)
