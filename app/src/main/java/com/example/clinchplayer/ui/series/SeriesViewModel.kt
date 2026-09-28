package com.example.clinchplayer.ui.series

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinchplayer.data.IPTVSession
import com.example.clinchplayer.data.SessionManager
import com.example.clinchplayer.network.RetrofitClient
import com.example.clinchplayer.network.models.SeriesCategory
import com.example.clinchplayer.network.models.SeriesEpisode
import com.example.clinchplayer.network.models.SeriesInfo
import com.example.clinchplayer.network.models.SeriesSeason
import com.example.clinchplayer.network.models.SeriesStream
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class SeriesViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    // ================================================================
    // SESSION
    // ================================================================

    private var session: IPTVSession? = null


    // ================================================================
    // JOBS
    // ================================================================

    private var seriesJob: Job? = null
    private var detailsJob: Job? = null

    // Temporada que debe quedar seleccionada después de cargar
    // get_series_info. Se usa al entrar desde Continue Watching.
    private var preferredSeasonAfterLoad: String? = null


    // ================================================================
    // CATEGORIES
    // ================================================================

    var categories by mutableStateOf<List<SeriesCategory>>(
        emptyList()
    )
        private set


    // ================================================================
    // SERIES
    // ================================================================

    var series by mutableStateOf<List<SeriesStream>>(
        emptyList()
    )
        private set


    // ================================================================
    // SELECTED CATEGORY
    // ================================================================

    var selectedCategory by mutableStateOf<SeriesCategory?>(
        null
    )
        private set


    // ================================================================
    // SELECTED SERIES
    // ================================================================

    var selectedSeries by mutableStateOf<SeriesStream?>(
        null
    )
        private set


    // ================================================================
    // SERIES INFORMATION
    // ================================================================

    var seriesInfo by mutableStateOf<SeriesInfo?>(
        null
    )
        private set


    // ================================================================
    // SEASONS
    // ================================================================

    var seasons by mutableStateOf<List<SeriesSeason>>(
        emptyList()
    )
        private set


    // ================================================================
    // EPISODES
    // ================================================================

    var episodes by mutableStateOf<Map<String, List<SeriesEpisode>>>(
        emptyMap()
    )
        private set


    // ================================================================
    // SELECTED SEASON
    // ================================================================

    var selectedSeason by mutableStateOf<String?>(
        null
    )
        private set


    // ================================================================
    // LOADING
    // ================================================================

    var isLoadingCategories by mutableStateOf(false)
        private set

    var isLoadingSeries by mutableStateOf(false)
        private set

    var isLoadingSeriesInfo by mutableStateOf(false)
        private set


    // ================================================================
    // ERRORS
    // ================================================================

    var errorMessage by mutableStateOf<String?>(
        null
    )
        private set

    var seriesInfoError by mutableStateOf<String?>(
        null
    )
        private set


    // ================================================================
    // INIT
    // ================================================================

    init {
        loadData()
    }


    // ================================================================
    // LOAD SESSION
    // ================================================================

    fun loadData() {

        viewModelScope.launch {

            try {

                session =
                    sessionManager
                        .sessionFlow
                        .first()

                if (session?.isLoggedIn == true) {

                    loadCategories()

                } else {

                    errorMessage =
                        "Sesión no válida"
                }

            } catch (e: Exception) {

                Log.e(
                    "SeriesViewModel",
                    "Error obteniendo sesión",
                    e
                )

                errorMessage =
                    "Error obteniendo sesión"
            }
        }
    }


    // ================================================================
    // LOAD SERIES CATEGORIES
    // ================================================================

    private suspend fun loadCategories() {

        val s =
            session ?: return

        isLoadingCategories = true

        errorMessage = null

        try {

            val api =
                RetrofitClient.createApi(
                    s.server
                )

            Log.d(
                "SeriesViewModel",
                "Requesting get_series_categories"
            )

            val response =
                api.getSeriesCategories(
                    username = s.username,
                    password = s.password
                )

            Log.d(
                "SeriesViewModel",
                "Series categories HTTP: ${response.code()}"
            )

            if (response.isSuccessful) {

                val list =
                    response.body()
                        ?: emptyList()

                categories =
                    list

                Log.d(
                    "SeriesViewModel",
                    "Series categories received: ${list.size}"
                )

                if (
                    categories.isNotEmpty() &&
                    selectedCategory == null
                ) {

                    selectCategory(
                        categories.first()
                    )
                }

            } else {

                errorMessage =
                    "Error cargando categorías (${response.code()})"
            }

        } catch (e: Exception) {

            Log.e(
                "SeriesViewModel",
                "Error cargando categorías",
                e
            )

            errorMessage =
                "Error de conexión al cargar series"

        } finally {

            isLoadingCategories =
                false
        }
    }


    // ================================================================
    // SELECT CATEGORY
    // ================================================================

    fun selectCategory(
        category: SeriesCategory
    ) {

        if (
            selectedCategory?.categoryId ==
            category.categoryId
        ) {
            return
        }

        selectedCategory =
            category

        selectedSeries =
            null

        seriesInfo =
            null

        seasons =
            emptyList()

        episodes =
            emptyMap()

        selectedSeason =
            null

        seriesJob?.cancel()

        seriesJob =
            viewModelScope.launch {

                delay(200)

                loadSeries(
                    category.categoryId
                )
            }
    }


    // ================================================================
    // LOAD SERIES
    // ================================================================

    private suspend fun loadSeries(
        categoryId: String
    ) {

        val s =
            session ?: return

        isLoadingSeries =
            true

        errorMessage =
            null

        try {

            val api =
                RetrofitClient.createApi(
                    s.server
                )

            Log.d(
                "SeriesViewModel",
                "Requesting get_series category=$categoryId"
            )

            val response =
                api.getSeries(
                    username = s.username,
                    password = s.password,
                    categoryId = categoryId
                )

            Log.d(
                "SeriesViewModel",
                "Series HTTP: ${response.code()}"
            )

            if (response.isSuccessful) {

                series =
                    response.body()
                        ?: emptyList()

                Log.d(
                    "SeriesViewModel",
                    "Series received: ${series.size}"
                )

            } else {

                errorMessage =
                    "Error cargando series (${response.code()})"

                Log.e(
                    "SeriesViewModel",
                    "Error loading series: ${response.code()}"
                )
            }

        } catch (e: Exception) {

            Log.e(
                "SeriesViewModel",
                "Exception loading series",
                e
            )

            errorMessage =
                "Error de conexión al cargar series"

        } finally {

            isLoadingSeries =
                false
        }
    }


    // ================================================================
    // SELECT SERIES
    // ================================================================

    fun selectSeries(
        item: SeriesStream,
        preferredSeason: String? = null
    ) {

        selectedSeries =
            item

        seriesInfo =
            null

        seasons =
            emptyList()

        episodes =
            emptyMap()

        selectedSeason =
            null

        seriesInfoError =
            null

        preferredSeasonAfterLoad =
            preferredSeason

        detailsJob?.cancel()

        detailsJob =
            viewModelScope.launch {

                loadSeriesInfo(
                    item.seriesId
                )
            }
    }


    // ================================================================
    // LOAD SERIES INFORMATION
    // ================================================================

    private suspend fun loadSeriesInfo(
        seriesId: Int
    ) {

        val s =
            session ?: return

        isLoadingSeriesInfo =
            true

        seriesInfoError =
            null

        try {

            val api =
                RetrofitClient.createApi(
                    s.server
                )

            Log.d(
                "SeriesViewModel",
                "Requesting get_series_info series_id=$seriesId"
            )

            val response =
                api.getSeriesInfo(
                    username = s.username,
                    password = s.password,
                    seriesId = seriesId
                )

            Log.d(
                "SeriesViewModel",
                "get_series_info HTTP: ${response.code()}"
            )

            if (response.isSuccessful) {

                val body =
                    response.body()

                seriesInfo =
                    body?.info

                seasons =
                    body?.seasons
                        ?: emptyList()

                episodes =
                    body?.episodes
                        ?: emptyMap()

                // Desde Continue Watching intentamos conservar la
                // temporada guardada. Si no existe, usamos la primera.
                val requestedSeason =
                    preferredSeasonAfterLoad
                        ?.takeIf { season ->
                            episodes.containsKey(season)
                        }

                selectedSeason =
                    requestedSeason
                        ?: episodes.keys
                            .sortedWith(
                                compareBy {
                                    it.toIntOrNull()
                                        ?: Int.MAX_VALUE
                                }
                            )
                            .firstOrNull()

                preferredSeasonAfterLoad =
                    null

                Log.d(
                    "SeriesViewModel",
                    "Series info loaded: ${seriesInfo?.name}"
                )

                Log.d(
                    "SeriesViewModel",
                    "Seasons received: ${seasons.size}"
                )

                Log.d(
                    "SeriesViewModel",
                    "Episode seasons received: ${episodes.size}"
                )

            } else {

                preferredSeasonAfterLoad =
                    null

                seriesInfoError =
                    "No se pudieron cargar los detalles"

                Log.e(
                    "SeriesViewModel",
                    "get_series_info error: ${response.code()}"
                )
            }

        } catch (e: Exception) {

            preferredSeasonAfterLoad =
                null

            Log.e(
                "SeriesViewModel",
                "Exception loading series info",
                e
            )

            seriesInfoError =
                "Error cargando información de la serie"

        } finally {

            isLoadingSeriesInfo =
                false
        }
    }


    // ================================================================
    // SELECT SEASON
    // ================================================================

    fun selectSeason(
        season: String
    ) {

        selectedSeason =
            season
    }


    // ================================================================
    // GET EPISODES FOR SELECTED SEASON
    // ================================================================

    fun getSelectedSeasonEpisodes():
            List<SeriesEpisode> {

        val season =
            selectedSeason
                ?: return emptyList()

        return episodes[season]
            ?: emptyList()
    }


    // ================================================================
    // EPISODE URL
    // ================================================================

    fun getEpisodeUrl(
        episode: SeriesEpisode
    ): String {

        val s =
            session ?: return ""

        val baseUrl =
            s.server
                .trim()
                .removeSuffix("/")

        val extension =
            if (
                !episode.containerExtension
                    .isNullOrBlank()
            ) {

                ".${episode.containerExtension}"

            } else {

                ".mp4"
            }

        val url =
            "$baseUrl/series/" +
                    "${s.username}/" +
                    "${s.password}/" +
                    "${episode.id}" +
                    extension

        Log.d(
            "SeriesViewModel",
            "Episode URL created: ${episode.title}"
        )

        return url
    }


    // ================================================================
    // CATCH UP / REFRESH
    // ================================================================

    fun refreshData() {
        seriesJob?.cancel()
        detailsJob?.cancel()

        selectedCategory = null
        selectedSeries = null
        seriesInfo = null
        seasons = emptyList()
        episodes = emptyMap()
        selectedSeason = null
        preferredSeasonAfterLoad = null
        seriesInfoError = null
        errorMessage = null
        series = emptyList()

        loadData()
    }

}
