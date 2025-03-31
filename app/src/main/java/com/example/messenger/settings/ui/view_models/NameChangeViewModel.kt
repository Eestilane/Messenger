package com.example.messenger.settings.ui.view_models

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.messenger.data.ApiService
import com.example.messenger.data.models.contacts.ContactsResponse

class NameChangeViewModel(val apiService: ApiService, val context: Context, val view: View?): ViewModel() {
    private val _renameData = MutableLiveData<List<ContactsResponse>>()
    val renameData: LiveData<List<ContactsResponse>> get() = _renameData

}