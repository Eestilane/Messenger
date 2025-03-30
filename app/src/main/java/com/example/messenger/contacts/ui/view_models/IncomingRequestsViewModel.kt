package com.example.messenger.contacts.ui.view_models

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messenger.data.ApiService
import com.example.messenger.data.RetrofitClient
import com.example.messenger.data.models.UserSearchResponse
import com.example.messenger.data.models.contacts.AcceptDeclineRequest
import com.example.messenger.data.models.contacts.AddContactRequest
import com.example.messenger.data.models.contacts.RequestResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IncomingRequestsViewModel(val apiService: ApiService, val context: Context, val view: View?) : ViewModel() {
    private val _incomingRequests = MutableLiveData<List<RequestResponse>>()
    val incomingRequests: LiveData<List<RequestResponse>> get() = _incomingRequests

    companion object {
        fun getViewModelFactory(apiService: ApiService, context: Context, view: View?): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    IncomingRequestsViewModel(
                        apiService = apiService,
                        context = context,
                        view = view
                    )
                }
            }
    }

    fun inRequestResponse(){
        RetrofitClient.create(context, view).getContactsInRequest().enqueue(
            object :
                Callback<List<RequestResponse>> {
                override fun onResponse(call: Call<List<RequestResponse>>, response: Response<List<RequestResponse>>) {
                    if (response.isSuccessful) {
                        if (response.body() != null)
                            _incomingRequests.value = response.body()
                    }
                }

                override fun onFailure(p0: Call<List<RequestResponse>?>, p1: Throwable) {

                }
            })
    }

    fun acceptRequest(userId: String) {
        RetrofitClient.create(context, view).acceptRequest(AcceptDeclineRequest(userId)).enqueue(
            object :
                Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        inRequestResponse()
                    }
                }

                override fun onFailure(p0: Call<Unit?>, p1: Throwable) {

                }
            })
    }

    fun declineRequest(userId: String) {
        RetrofitClient.create(context, view).acceptRequest(AcceptDeclineRequest(userId)).enqueue(
            object :
                Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        inRequestResponse()
                    }
                }

                override fun onFailure(p0: Call<Unit?>, p1: Throwable) {

                }
            })
    }
}