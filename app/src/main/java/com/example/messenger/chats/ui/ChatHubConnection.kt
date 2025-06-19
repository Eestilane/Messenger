package com.example.messenger.chats.ui

import android.util.Log
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubException
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext

class ChatHubConnection(private val authToken: String) {
    private val connection: HubConnection = HubConnectionBuilder.create("wss://api.nogamenolife.pro/chatHub").withAccessTokenProvider( Single.fromCallable { authToken } ).build()

    suspend fun connect() {
        try {
            connection.start().await()
            Log.i("ChatHubConnection", "Успешно подключено к хабу")
        } catch (e: Exception) {
            Log.e("ChatHubConnection", "Ошибка подключения: ${e.message}", e)
        }
    }

    suspend fun sendMessage(chatId: String, content: String) {
        try {
            connection.invoke("SendMessage", chatId, content).await()
            Log.i("ChatHubConnection", "вСЁ ЗАЕБИСЬ")
        } catch (e: Exception) {
            Log.e("ChatHubConnection", "Ошибка отправки сообщения: ${e.message}", e)
        }
    }

    suspend fun editMessage(messageId: String, newContent: String) = withContext(Dispatchers.IO) {

        try {
            connection.invoke("EditMessage", messageId, newContent)
            Log.i("ChatHub", "Редактирование прошло заебись")
        } catch (e: HubException) {
            Log.e("ChatHub", "Хуита: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("ChatHub", "Хуита", e)
            throw e
        }
    }

    suspend fun deleteMessage(messageId: String) = withContext(Dispatchers.IO) {
        connection.invoke("DeleteMessage", messageId).await()
    }

    fun onMessageReceived(callback: (userId: String, content: String) -> Unit) {
        connection.on("ReceiveMessage", callback, String::class.java, String::class.java)
    }

    fun onMessageEdited(callback: (messageId: String, newContent: String) -> Unit) {
        connection.on("EditedMessage", callback, String::class.java, String::class.java)
    }

    fun onMessageDeleted(callback: (messageId: String) -> Unit) {
        connection.on("DeletedMessage", callback, String::class.java)
    }

    fun disconnect() {
        connection.stop()
    }
}