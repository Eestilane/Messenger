package com.example.messenger.data

import com.example.messenger.data.models.contacts.AddContactRequest
import com.example.messenger.data.models.contacts.ContactsResponse
import com.example.messenger.data.models.contacts.AcceptDeclineRequest
import com.example.messenger.data.models.LoginRequest
import com.example.messenger.data.models.LoginResponse
import com.example.messenger.data.models.RegisterRequest
import com.example.messenger.data.models.UserResponse
import com.example.messenger.data.models.UserSearchResponse
import com.example.messenger.data.models.contacts.RequestResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/auth/register")
    fun register(@Body request: RegisterRequest): Call<LoginResponse>

    @POST("/auth/logout")
    fun logout(): Call<Unit>

    @GET("/users/user")
    fun getUser(): Call<UserResponse>

    @GET("/users/search")
    fun userSearch(@Query("search") search : String): Call<List<UserSearchResponse>>

    @GET("/contacts")
    fun getContacts(): Call<List<ContactsResponse>>

    @POST("/contacts/add")
    fun addContact(@Body request: AddContactRequest): Call<Unit>

    @POST("/contacts/accept")
    fun acceptRequest(@Body request: AcceptDeclineRequest): Call<Unit>

    @POST("/contacts/decline")
    fun declineRequest(@Body request: AcceptDeclineRequest): Call<Unit>

    @GET("contacts/in_requests")
    fun getContactsInRequest(): Call<List<RequestResponse>>

    @GET("contacts/out_requests")
    fun getContactsOutRequest(): Call<List<RequestResponse>>
}