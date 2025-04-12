package com.example.messenger.data

import com.example.messenger.data.models.LoginRequest
import com.example.messenger.data.models.LoginResponse
import com.example.messenger.data.models.RegisterRequest
import com.example.messenger.data.models.UpdateNameRequest
import com.example.messenger.data.models.UserResponse
import com.example.messenger.data.models.UserSearchResponse
import com.example.messenger.data.models.contacts.AcceptDeclineRequest
import com.example.messenger.data.models.contacts.ContactRequest
import com.example.messenger.data.models.contacts.ContactsResponse
import com.example.messenger.data.models.contacts.RequestResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
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
    suspend fun getUser(): Response<UserResponse>

    @GET("/users/search")
    suspend fun userSearch(@Query("search") search : String): Response<List<UserSearchResponse>>

    @PATCH("/user/update/name")
    suspend fun userUpdateName(@Body request: UpdateNameRequest): Response<Unit>

    @GET("/contacts")
    suspend fun getContacts(): Response<List<ContactsResponse>>

    @POST("/contacts/add")
    suspend fun addContact(@Body request: ContactRequest): Response<Unit>

    @POST("/contacts/accept")
    suspend fun acceptRequest(@Body request: AcceptDeclineRequest): Response<Unit>

    @POST("/contacts/decline")
    suspend fun declineRequest(@Body request: AcceptDeclineRequest): Response<Unit>

    @POST("/contacts/delete")
    suspend fun deleteContact(@Body request: ContactRequest): Response<Unit>

    @GET("contacts/in_requests")
    suspend fun getContactsInRequest(): Response<List<RequestResponse>>

    @GET("contacts/out_requests")
    suspend fun getContactsOutRequest(): Response<List<RequestResponse>>
}