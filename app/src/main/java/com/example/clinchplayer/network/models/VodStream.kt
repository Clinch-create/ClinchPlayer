package com.example.clinchplayer.network.models

import com.google.gson.annotations.SerializedName

data class VodStream(

    @SerializedName("num")
    val num: Int? = null,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("stream_type")
    val streamType: String? = null,

    @SerializedName("stream_id")
    val streamId: Int = 0,

    @SerializedName("stream_icon")
    val streamIcon: String? = null,

    @SerializedName("rating")
    val rating: String? = null,

    @SerializedName("rating_5based")
    val rating5Based: Double? = null,

    @SerializedName("added")
    val added: String? = null,

    @SerializedName("category_id")
    val categoryId: String? = null,

    @SerializedName("container_extension")
    val containerExtension: String? = null,

    @SerializedName("plot")
    val plot: String? = null,

    @SerializedName("cast")
    val cast: String? = null,

    @SerializedName("director")
    val director: String? = null,

    @SerializedName("genre")
    val genre: String? = null,

    @SerializedName("releaseDate")
    val releaseDate: String? = null,

    @SerializedName("duration")
    val duration: String? = null,

    @SerializedName("youtube_trailer")
    val youtubeTrailer: String? = null
)