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
import com.example.messenger.data.models.contacts.AddContactRequest
import com.example.messenger.data.models.UserSearchResponse
import com.example.messenger.libs.DebounceOperators
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserSearchViewModel(val apiService: ApiService, val context: Context, val view: View?): ViewModel() {
    private val _userSearch = MutableLiveData<List<UserSearchResponse>>()
    val userSearch: LiveData<List<UserSearchResponse>> get() = _userSearch

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context, view: View?): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    UserSearchViewModel(
                        apiService = apiService,
                        context = context,
                        view = view
                    )
                }
            }}

    val searchUser: (String)-> Unit = DebounceOperators.debounce(300L, viewModelScope,
        this::onUserSearch)

    fun onUserSearch(newText: String){
        RetrofitClient.create(context, view).userSearch(newText).enqueue(
            object :
                Callback<List<UserSearchResponse>> {
                override fun onResponse(call: Call<List<UserSearchResponse>>, response: Response<List<UserSearchResponse>>) {
                    if (response.isSuccessful) {
                        if (response.body() != null)
                            _userSearch.value = response.body()
                    }
                }

                override fun onFailure(p0: Call<List<UserSearchResponse>?>, p1: Throwable) {

                }
            })
    }

    fun addFriendRequest(userId: String) {
        RetrofitClient.create(context, view).addContact(AddContactRequest(userId)).enqueue(
            object :
                Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {

                    }
                }

                override fun onFailure(p0: Call<Unit?>, p1: Throwable) {

                }
            })
    }
}