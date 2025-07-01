package com.example.messenger.chats.ui.models

sealed class ChatState {
    data class Success(val chatId: String) : ChatState()
    data class Error(val message: String) : ChatState()
}
