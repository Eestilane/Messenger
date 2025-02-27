package com.example.messenger.libs

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.res.Configuration

const val PREFS_THEME = "prefs_theme"
const val KEY_THEME = "key_theme"

object ThemeManager {

    private fun getPrefs(context: Context): SharedPreferences{
        val getPrefs = context.getSharedPreferences(PREFS_THEME, MODE_PRIVATE)
        return getPrefs
    }

    fun saveTheme(context: Context, theme: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_THEME, theme).apply()
    }

    fun getTheme(context: Context): Boolean {
        val exists = getPrefs(context).contains(KEY_THEME)

        if (!exists){
            val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val isSystemInDarkTheme = currentNightMode == Configuration.UI_MODE_NIGHT_YES
            saveTheme(context, isSystemInDarkTheme)
            return isSystemInDarkTheme
        }

        return  getPrefs(context).getBoolean(KEY_THEME, true)
    }
}