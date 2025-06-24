package com.example.messenger.chats.ui.models

sealed class ChatsState {
    object Loading : ChatsState()
    data class Success(val chats: List<Chat>) : ChatsState()
    data class Error(val message: String) : ChatsState()
}