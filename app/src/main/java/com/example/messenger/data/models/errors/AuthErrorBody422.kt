package com.example.messenger.data.models.errors

data class AuthErrorBody422(val type: String, val title: String, val status: Int, val errors: Errors) {
    data class Errors(val Login: List<String>?, val Password: List<String>?, val Name: List<String>?)
}