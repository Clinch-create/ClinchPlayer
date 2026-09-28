package com.example.clinchplayer.network.models

import com.google.gson.annotations.SerializedName


// ====================================================================
// SERIES CATEGORY
// ====================================================================

data class SeriesCategory(

    @SerializedName("category_id")
    val categoryId: String = "",

    @SerializedName("category_name")
    val categoryName: String = "",

    @SerializedName("parent_id")
    val parentId: Int? = null
)


// ====================================================================
// SERIES STREAM
// ====================================================================

data class SeriesStream(

    @SerializedName("num")
    val num: Int? = null,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("series_id")
    val seriesId: Int = 0,

    @SerializedName("cover")
    val cover: String? = null,

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

    @SerializedName("last_modified")
    val lastModified: String? = null,

    @SerializedName("rating")
    val rating: String? = null,

    @SerializedName("rating_5based")
    val rating5Based: Double? = null,

    @SerializedName("backdrop_path")
    val backdropPath: List<String>? = null,

    @SerializedName("youtube_trailer")
    val youtubeTrailer: String? = null,

    @SerializedName("episode_run_time")
    val episodeRunTime: String? = null,

    @SerializedName("category_id")
    val categoryId: String? = null
)


// ====================================================================
// SERIES INFO RESPONSE
// ====================================================================

data class SeriesInfoResponse(

    @SerializedName("seasons")
    val seasons: List<SeriesSeason>? = null,

    @SerializedName("info")
    val info: SeriesInfo? = null,

    @SerializedName("episodes")
    val episodes: Map<String, List<SeriesEpisode>>? = null
)


// ====================================================================
// SERIES INFO
// ====================================================================

data class SeriesInfo(

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("cover")
    val cover: String? = null,

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

    @SerializedName("last_modified")
    val lastModified: String? = null,

    @SerializedName("rating")
    val rating: String? = null,

    @SerializedName("rating_5based")
    val rating5Based: Double? = null,

    @SerializedName("backdrop_path")
    val backdropPath: List<String>? = null,

    @SerializedName("youtube_trailer")
    val youtubeTrailer: String? = null,

    @SerializedName("episode_run_time")
    val episodeRunTime: String? = null,

    @SerializedName("category_id")
    val categoryId: String? = null
)


// ====================================================================
// SEASON
// ====================================================================

data class SeriesSeason(

    @SerializedName("air_date")
    val airDate: String? = null,

    @SerializedName("episode_count")
    val episodeCount: Int? = null,

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("overview")
    val overview: String? = null,

    @SerializedName("season_number")
    val seasonNumber: Int? = null,

    @SerializedName("cover")
    val cover: String? = null,

    @SerializedName("cover_big")
    val coverBig: String? = null
)


// ====================================================================
// EPISODE
// ====================================================================

data class SeriesEpisode(

    @SerializedName("id")
    val id: String = "",

    @SerializedName("episode_num")
    val episodeNum: Int? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("container_extension")
    val containerExtension: String? = null,

    @SerializedName("info")
    val info: EpisodeInfo? = null,

    @SerializedName("custom_sid")
    val customSid: String? = null,

    @SerializedName("added")
    val added: String? = null,

    @SerializedName("season")
    val season: Int? = null,

    @SerializedName("direct_source")
    val directSource: String? = null
)


// ====================================================================
// EPISODE INFO
// ====================================================================

data class EpisodeInfo(

    @SerializedName("movie_image")
    val movieImage: String? = null,

    @SerializedName("releaseDate")
    val releaseDate: String? = null,

    @SerializedName("plot")
    val plot: String? = null,

    @SerializedName("duration_secs")
    val durationSecs: Int? = null,

    @SerializedName("duration")
    val duration: String? = null,

    @SerializedName("rating")
    val rating: String? = null
)