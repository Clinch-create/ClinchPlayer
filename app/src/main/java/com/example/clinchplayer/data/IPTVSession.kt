package com.example.clinchplayer.data


data class IPTVSession(
    val server: String,
    val username: String,
    val password: String,
    val isLoggedIn: Boolean,
    val expirationDate: String? = null
)
