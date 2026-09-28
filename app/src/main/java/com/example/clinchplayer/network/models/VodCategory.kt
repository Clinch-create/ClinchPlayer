package com.example.clinchplayer.network.models

import com.google.gson.annotations.SerializedName

data class VodCategory(

    @SerializedName("category_id")
    val categoryId: String = "",

    @SerializedName("category_name")
    val categoryName: String = "",

    @SerializedName("parent_id")
    val parentId: Int? = null
)