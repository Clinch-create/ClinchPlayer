package com.example.clinchplayer.network.models

import com.google.gson.annotations.SerializedName

data class VodInfoResponse(

    @SerializedName("info")
    val info: VodInfo? = null,

    @SerializedName("movie_data")
    val movieData: VodMovieData? = null
)


data class VodInfo(

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("o_name")
    val originalName: String? = null,

    @SerializedName("movie_image")
    val movieImage: String? = null,

    @SerializedName("cover_big")
    val coverBig: String? = null,

    @SerializedName("backdrop_path")
    val backdropPath: List<String>? = null,

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

    @SerializedName("releasedate")
    val releaseDateAlt: String? = null,

    @SerializedName("duration")
    val duration: String? = null,

    @SerializedName("duration_secs")
    val durationSecs: Int? = null,

    @SerializedName("rating")
    val rating: String? = null,

    @SerializedName("rating_5based")
    val rating5Based: Double? = null,

    @SerializedName("youtube_trailer")
    val youtubeTrailer: String? = null,

    @SerializedName("country")
    val country: String? = null,

    @SerializedName("age")
    val age: String? = null
)


data class VodMovieData(

    @SerializedName("stream_id")
    val streamId: Int? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("added")
    val added: String? = null,

    @SerializedName("category_id")
    val categoryId: String? = null,

    @SerializedName("container_extension")
    val containerExtension: String? = null,

    @SerializedName("custom_sid")
    val customSid: String? = null,

    @SerializedName("direct_source")
    val directSource: String? = null
)