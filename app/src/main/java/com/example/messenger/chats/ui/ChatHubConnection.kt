package com.example.messenger.chats.ui

import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.rx3.await

class ChatHubConnection(private val authToken: String) {
    private val connection: HubConnection = HubConnectionBuilder.create("wss://api.nogamenolife.pro/chatHub").withAccessTokenProvider( Single.fromCallable { authToken } ).build()

    suspend fun connect() {
        connection.start().await()
    }

    suspend fun sendMessage(chatId: String, content: String) {
        connection.invoke("SendMessage", chatId, content).await()
    }

    suspend fun editMessage(messageId: String, content: String) {
        connection.invoke("EditMessage", messageId, content).await()
    }

    suspend fun deleteMessage(messageId: String) {
        connection.invoke("DeleteMessage", messageId).await()
    }

    fun onMessageReceived(callback: (messageId: String, chatId: String, userId: String, content: String) -> Unit) {
        connection.on("ReceiveMessage", callback, String::class.java, String::class.java, String::class.java, String::class.java)
    }

    fun onMessageEdited(callback: (messageId: String, chatId: String, content: String) -> Unit) {
        connection.on("EditedMessage", callback, String::class.java, String::class.java, String::class.java)
    }

    fun onMessageDeleted(callback: (messageId: String) -> Unit) {
        connection.on("DeletedMessage", callback, String::class.java)
    }

    fun onUserAdded(callback: (chatId: String, userId: String) -> Unit) {
        connection.on("AddedToChat", callback, String::class.java, String::class.java)
    }

    fun onUserRemoved(callback: (chatId: String, userId: String) -> Unit) {
        connection.on("RemovedFromChat", callback, String::class.java, String::class.java)
    }

    fun onAvatarUpdated(callback: (chatId: String, userId: String, link: String) -> Unit) {
        connection.on("ChatAvatarUpdated", callback, String::class.java, String::class.java, String::class.java)
    }

    fun disconnect() {
        connection.stop()
    }
}