package com.example.clinchplayer.network

import com.example.clinchplayer.network.models.LiveCategory
import com.example.clinchplayer.network.models.LiveStream
import com.example.clinchplayer.network.models.SeriesCategory
import com.example.clinchplayer.network.models.SeriesInfoResponse
import com.example.clinchplayer.network.models.SeriesStream
import com.example.clinchplayer.network.models.VodCategory
import com.example.clinchplayer.network.models.VodInfoResponse
import com.example.clinchplayer.network.models.VodStream
import com.example.clinchplayer.network.models.XtreamResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface XtreamCodesApi {

    // ================================================================
    // LOGIN
    // ================================================================

    @GET("player_api.php")
    suspend fun login(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<XtreamResponse>


    // ================================================================
    // LIVE TV - CATEGORIES
    // ================================================================

    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
    ): Response<List<LiveCategory>>


    // ================================================================
    // LIVE TV - STREAMS
    // ================================================================

    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams",
        @Query("category_id") categoryId: String? = null
    ): Response<List<LiveStream>>


    // ================================================================
    // MOVIES / VOD - CATEGORIES
    // ================================================================

    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories"
    ): Response<List<VodCategory>>


    // ================================================================
    // MOVIES / VOD - STREAMS
    // ================================================================

    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams",
        @Query("category_id") categoryId: String? = null
    ): Response<List<VodStream>>


    // ================================================================
    // MOVIE / VOD INFO
    // ================================================================

    @GET("player_api.php")
    suspend fun getVodInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_info",
        @Query("vod_id") vodId: Int
    ): Response<VodInfoResponse>


    // ================================================================
    // SERIES - CATEGORIES
    // ================================================================

    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_categories"
    ): Response<List<SeriesCategory>>


    // ================================================================
    // SERIES
    // ================================================================

    @GET("player_api.php")
    suspend fun getSeries(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series",
        @Query("category_id") categoryId: String? = null
    ): Response<List<SeriesStream>>


    // ================================================================
    // SERIES INFO
    // TEMPORADAS + EPISODIOS
    // ================================================================

    @GET("player_api.php")
    suspend fun getSeriesInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_info",
        @Query("series_id") seriesId: Int
    ): Response<SeriesInfoResponse>
}