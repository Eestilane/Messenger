package com.example.messenger.libs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

const val PREFS_TOKEN = "prefs_token"
const val KEY_TOKEN = "key_token"

object TokenManager {

    private fun getPrefs(context: Context): SharedPreferences{
        return EncryptedSharedPreferences.create(
            context,
            PREFS_TOKEN,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        return getPrefs(context).getString(KEY_TOKEN, null)
    }

    fun clearToken(context: Context) {
        getPrefs(context).edit().remove(KEY_TOKEN).apply()
    }
}