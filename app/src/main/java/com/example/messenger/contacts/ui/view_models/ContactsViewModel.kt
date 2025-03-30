package com.example.messenger.contacts.ui.view_models

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messenger.data.ApiService
import com.example.messenger.data.RetrofitClient
import com.example.messenger.data.models.contacts.ContactsResponse
import com.example.messenger.libs.DebounceOperators
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContactsViewModel(val apiService: ApiService, val context: Context, val view: View?): ViewModel() {
    private val _contacts = MutableLiveData<List<ContactsResponse>>()
    val contacts: LiveData<List<ContactsResponse>> get() = _contacts

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context, view: View?): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ContactsViewModel(
                        apiService = apiService,
                        context = context,
                        view = view
                    )
                }
            }}

    fun getContacts(){
        RetrofitClient.create(context, view).getContacts().enqueue(
            object :
                Callback<List<ContactsResponse>> {
                override fun onResponse(call: Call<List<ContactsResponse>>, response: Response<List<ContactsResponse>>) {
                    if (response.isSuccessful) {
                        if (response.body() != null)
                            _contacts.value = response.body()
                    }
                }

                override fun onFailure(p0: Call<List<ContactsResponse>?>, p1: Throwable) {

                }
            })
    }
}