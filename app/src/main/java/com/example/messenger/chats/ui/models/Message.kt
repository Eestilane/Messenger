package com.example.messenger.chats.ui.models

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val sentAt: String,
    val editedAt: String?,
    val viewed: Boolean
)