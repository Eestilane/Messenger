package com.example.messenger.chats.ui.models

data class Message(
    val id: String,
    val content: String,
    val senderId: String,
    val timestamp: Long = System.currentTimeMillis()
)