package com.example.clinchplayer.network.models

import com.google.gson.annotations.SerializedName


data class XtreamResponse(

    @SerializedName("user_info")
    val userInfo: UserInfo? = null
)


data class UserInfo(

    @SerializedName("auth")
    val auth: Int = 0,

    @SerializedName("status")
    val status: String = "",

    @SerializedName("username")
    val username: String = "",

    @SerializedName("exp_date")
    val expDate: String? = null
)
