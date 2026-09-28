package com.example.clinchplayer.ui.search

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinchplayer.data.IPTVSession
import com.example.clinchplayer.data.SessionManager
import com.example.clinchplayer.network.RetrofitClient
import com.example.clinchplayer.network.models.LiveStream
import com.example.clinchplayer.network.models.SeriesStream
import com.example.clinchplayer.network.models.VodCategory
import com.example.clinchplayer.network.models.VodStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.util.Locale


// ====================================================================
// REGEX
// ====================================================================

private val SearchDiacriticsRegex =
    "\\p{Mn}+".toRegex()

private val SearchSpacesRegex =
    "\\s+".toRegex()


// ====================================================================
// SEARCH INDEX
// ====================================================================

private data class SearchIndex<T>(
    val item: T,
    val searchableText: String,
    val title: String
)


// ====================================================================
// VIEW MODEL
// ====================================================================

class SearchViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {


    // ================================================================
    // SESSION
    // ================================================================

    private var session: IPTVSession? =
        null


    // ================================================================
    // SEARCH JOB
    // ================================================================

    private var searchJob: Job? =
        null


    // ================================================================
    // QUERY
    // ================================================================

    private var internalQuery by
    mutableStateOf("")


    var query: String
        get() = internalQuery

        set(value) {

            internalQuery =
                value

            scheduleSearch(
                value
            )
        }


    // ================================================================
    // FULL CATALOG
    // ================================================================

    var allLiveStreams by
    mutableStateOf<List<LiveStream>>(
        emptyList()
    )
        private set


    var allMovies by
    mutableStateOf<List<VodStream>>(
        emptyList()
    )
        private set


    var allSeries by
    mutableStateOf<List<SeriesStream>>(
        emptyList()
    )
        private set


    // ================================================================
    // MOVIE CATEGORIES
    // ================================================================

    var movieCategories by
    mutableStateOf<List<VodCategory>>(
        emptyList()
    )
        private set


    // ================================================================
    // SEARCH RESULTS
    // ================================================================

    var liveResults by
    mutableStateOf<List<LiveStream>>(
        emptyList()
    )
        private set


    var movieResults by
    mutableStateOf<List<VodStream>>(
        emptyList()
    )
        private set


    var seriesResults by
    mutableStateOf<List<SeriesStream>>(
        emptyList()
    )
        private set


    // ================================================================
    // SEARCH INDEXES
    // ================================================================

    private var liveSearchIndex:
            List<SearchIndex<LiveStream>> =
        emptyList()


    private var movieSearchIndex:
            List<SearchIndex<VodStream>> =
        emptyList()


    private var seriesSearchIndex:
            List<SearchIndex<SeriesStream>> =
        emptyList()


    // ================================================================
    // STATES
    // ================================================================

    var isLoading by
    mutableStateOf(false)
        private set


    var errorMessage by
    mutableStateOf<String?>(
        null
    )
        private set


    // ================================================================
    // INIT
    // ================================================================

    init {

        loadAllContent()
    }


    // ================================================================
    // LOAD ALL CONTENT
    // ================================================================

    fun loadAllContent() {

        viewModelScope.launch {

            isLoading =
                true

            errorMessage =
                null


            try {

                // ====================================================
                // SESSION
                // ====================================================

                session =
                    sessionManager
                        .sessionFlow
                        .first()


                val currentSession =
                    session


                if (
                    currentSession?.isLoggedIn != true
                ) {

                    errorMessage =
                        "Sesión no válida"

                    return@launch
                }


                // ====================================================
                // API
                // ====================================================

                val api =
                    RetrofitClient.createApi(
                        currentSession.server
                    )


                // ====================================================
                // LOAD IN PARALLEL
                // ====================================================

                coroutineScope {

                    // =================================================
                    // LIVE TV
                    // =================================================

                    val liveDeferred =
                        async {

                            try {

                                api.getLiveStreams(
                                    username =
                                        currentSession.username,

                                    password =
                                        currentSession.password,

                                    categoryId =
                                        null
                                )

                            } catch (
                                e: CancellationException
                            ) {

                                throw e

                            } catch (
                                e: Exception
                            ) {

                                Log.e(
                                    "SearchViewModel",
                                    "Error cargando Live TV",
                                    e
                                )

                                null
                            }
                        }


                    // =================================================
                    // MOVIES
                    // =================================================

                    val moviesDeferred =
                        async {

                            try {

                                api.getVodStreams(
                                    username =
                                        currentSession.username,

                                    password =
                                        currentSession.password,

                                    categoryId =
                                        null
                                )

                            } catch (
                                e: CancellationException
                            ) {

                                throw e

                            } catch (
                                e: Exception
                            ) {

                                Log.e(
                                    "SearchViewModel",
                                    "Error cargando películas",
                                    e
                                )

                                null
                            }
                        }


                    // =================================================
                    // SERIES
                    // =================================================

                    val seriesDeferred =
                        async {

                            try {

                                api.getSeries(
                                    username =
                                        currentSession.username,

                                    password =
                                        currentSession.password,

                                    categoryId =
                                        null
                                )

                            } catch (
                                e: CancellationException
                            ) {

                                throw e

                            } catch (
                                e: Exception
                            ) {

                                Log.e(
                                    "SearchViewModel",
                                    "Error cargando series",
                                    e
                                )

                                null
                            }
                        }


                    // =================================================
                    // VOD CATEGORIES
                    // =================================================

                    val categoriesDeferred =
                        async {

                            try {

                                api.getVodCategories(
                                    username =
                                        currentSession.username,

                                    password =
                                        currentSession.password
                                )

                            } catch (
                                e: CancellationException
                            ) {

                                throw e

                            } catch (
                                e: Exception
                            ) {

                                Log.e(
                                    "SearchViewModel",
                                    "Error cargando categorías",
                                    e
                                )

                                null
                            }
                        }


                    // =================================================
                    // WAIT
                    // =================================================

                    val liveResponse =
                        liveDeferred.await()

                    val movieResponse =
                        moviesDeferred.await()

                    val seriesResponse =
                        seriesDeferred.await()

                    val categoriesResponse =
                        categoriesDeferred.await()


                    // =================================================
                    // LIVE
                    // =================================================

                    if (
                        liveResponse?.isSuccessful == true
                    ) {

                        allLiveStreams =
                            liveResponse
                                .body()
                                ?: emptyList()
                    }


                    // =================================================
                    // MOVIES
                    // =================================================

                    if (
                        movieResponse?.isSuccessful == true
                    ) {

                        allMovies =
                            movieResponse
                                .body()
                                ?: emptyList()
                    }


                    // =================================================
                    // SERIES
                    // =================================================

                    if (
                        seriesResponse?.isSuccessful == true
                    ) {

                        allSeries =
                            seriesResponse
                                .body()
                                ?: emptyList()
                    }


                    // =================================================
                    // CATEGORIES
                    // =================================================

                    if (
                        categoriesResponse
                            ?.isSuccessful == true
                    ) {

                        movieCategories =
                            categoriesResponse
                                .body()
                                ?: emptyList()
                    }


                    // =================================================
                    // ALL FAILED
                    // =================================================

                    val liveFailed =
                        liveResponse == null ||
                                !liveResponse.isSuccessful


                    val moviesFailed =
                        movieResponse == null ||
                                !movieResponse.isSuccessful


                    val seriesFailed =
                        seriesResponse == null ||
                                !seriesResponse.isSuccessful


                    if (
                        liveFailed &&
                        moviesFailed &&
                        seriesFailed
                    ) {

                        errorMessage =
                            "No se pudo cargar el contenido para buscar"
                    }
                }


                // ====================================================
                // BUILD INDEX
                //
                // IMPORTANTE:
                // Se ejecuta fuera del hilo principal.
                // ====================================================

                buildSearchIndexes()


                Log.d(
                    "SearchViewModel",

                    "Catalog loaded " +
                            "live=${allLiveStreams.size}, " +
                            "movies=${allMovies.size}, " +
                            "series=${allSeries.size}, " +
                            "categories=${movieCategories.size}"
                )


            } catch (
                e: CancellationException
            ) {

                throw e


            } catch (
                e: Exception
            ) {

                Log.e(
                    "SearchViewModel",
                    "Error cargando búsqueda",
                    e
                )


                errorMessage =
                    "Error de conexión al cargar la búsqueda"

            } finally {

                isLoading =
                    false
            }
        }
    }


    // ================================================================
    // BUILD SEARCH INDEXES
    // ================================================================

    private suspend fun buildSearchIndexes() {

        // Copias para trabajar en background.
        val liveSnapshot =
            allLiveStreams

        val movieSnapshot =
            allMovies

        val seriesSnapshot =
            allSeries

        val categorySnapshot =
            movieCategories


        val indexes =
            withContext(
                Dispatchers.Default
            ) {

                // ====================================================
                // CATEGORY MAP
                // ====================================================

                val categoryNames =
                    categorySnapshot
                        .associate { category ->

                            category.categoryId
                                .toString() to
                                    category.categoryName
                                        .orEmpty()
                        }


                // ====================================================
                // LIVE INDEX
                // ====================================================

                val liveIndex =
                    liveSnapshot.map { stream ->

                        val title =
                            normalizeSearchText(
                                stream.name
                            )


                        SearchIndex(
                            item =
                                stream,

                            searchableText =
                                title,

                            title =
                                title
                        )
                    }


                // ====================================================
                // MOVIE INDEX
                // ====================================================

                val movieIndex =
                    movieSnapshot.map { movie ->


                        /*
                         * Esta reflexión ya NO ocurre cada vez
                         * que escribes.
                         *
                         * Se ejecuta solamente UNA VEZ aquí
                         * y además en Dispatchers.Default.
                         */
                        val categoryId =
                            getCategoryIdSafely(
                                movie
                            )


                        val categoryName =
                            categoryNames[
                                categoryId
                            ]
                                .orEmpty()


                        val fullText =
                            buildString {

                                // TITLE
                                append(
                                    movie.name
                                        .orEmpty()
                                )

                                append(" ")


                                // YEAR / RELEASE DATE
                                append(
                                    movie.releaseDate
                                        .orEmpty()
                                )

                                append(" ")


                                // GENRE
                                append(
                                    movie.genre
                                        .orEmpty()
                                )

                                append(" ")


                                // ACTORS / CAST
                                append(
                                    movie.cast
                                        .orEmpty()
                                )

                                append(" ")


                                // DIRECTOR
                                append(
                                    movie.director
                                        .orEmpty()
                                )

                                append(" ")


                                // CATEGORY
                                append(
                                    categoryName
                                )
                            }


                        val normalizedTitle =
                            normalizeSearchText(
                                movie.name
                            )


                        SearchIndex(
                            item =
                                movie,

                            searchableText =
                                normalizeSearchText(
                                    fullText
                                ),

                            title =
                                normalizedTitle
                        )
                    }


                // ====================================================
                // SERIES INDEX
                // ====================================================

                val seriesIndex =
                    seriesSnapshot.map { series ->

                        val title =
                            normalizeSearchText(
                                series.name
                            )


                        SearchIndex(
                            item =
                                series,

                            searchableText =
                                title,

                            title =
                                title
                        )
                    }


                Triple(
                    liveIndex,
                    movieIndex,
                    seriesIndex
                )
            }


        liveSearchIndex =
            indexes.first

        movieSearchIndex =
            indexes.second

        seriesSearchIndex =
            indexes.third


        // Si había texto escrito mientras cargaba,
        // ejecutar búsqueda ahora.
        if (
            internalQuery
                .trim()
                .length >= 2
        ) {

            scheduleSearch(
                internalQuery
            )
        }
    }


    // ================================================================
    // SCHEDULE SEARCH
    // ================================================================

    private fun scheduleSearch(
        value: String
    ) {

        searchJob
            ?.cancel()


        val normalizedQuery =
            normalizeSearchText(
                value
            )


        // ============================================================
        // LESS THAN 2 CHARACTERS
        // ============================================================

        if (
            normalizedQuery.length < 2
        ) {

            liveResults =
                emptyList()

            movieResults =
                emptyList()

            seriesResults =
                emptyList()

            return
        }


        // ============================================================
        // DEBOUNCE
        // ============================================================

        searchJob =
            viewModelScope.launch {

                /*
                 * Evita buscar con cada tecla instantáneamente.
                 */
                delay(
                    180
                )


                val searchText =
                    normalizeSearchText(
                        internalQuery
                    )


                if (
                    searchText.length < 2
                ) {

                    liveResults =
                        emptyList()

                    movieResults =
                        emptyList()

                    seriesResults =
                        emptyList()

                    return@launch
                }


                // ====================================================
                // SEARCH IN BACKGROUND
                // ====================================================

                val result =
                    withContext(
                        Dispatchers.Default
                    ) {

                        val words =
                            searchText
                                .split(
                                    SearchSpacesRegex
                                )
                                .filter {
                                    it.isNotBlank()
                                }


                        // =============================================
                        // LIVE
                        // =============================================

                        val foundLive =
                            liveSearchIndex
                                .asSequence()
                                .filter { entry ->

                                    matchesWords(
                                        entry.searchableText,
                                        words
                                    )
                                }
                                .take(
                                    30
                                )
                                .map {
                                    it.item
                                }
                                .toList()


                        // =============================================
                        // MOVIES
                        // =============================================

                        val foundMovies =
                            movieSearchIndex
                                .asSequence()

                                .filter { entry ->

                                    matchesWords(
                                        entry.searchableText,
                                        words
                                    )
                                }

                                /*
                                 * Título primero.
                                 */
                                .sortedWith(
                                    compareBy<SearchIndex<VodStream>> { entry ->

                                        when {

                                            entry.title.startsWith(
                                                searchText
                                            ) ->
                                                0

                                            entry.title.contains(
                                                searchText
                                            ) ->
                                                1

                                            else ->
                                                2
                                        }
                                    }
                                )

                                .take(
                                    50
                                )

                                .map {
                                    it.item
                                }

                                .toList()


                        // =============================================
                        // SERIES
                        // =============================================

                        val foundSeries =
                            seriesSearchIndex
                                .asSequence()
                                .filter { entry ->

                                    matchesWords(
                                        entry.searchableText,
                                        words
                                    )
                                }
                                .take(
                                    30
                                )
                                .map {
                                    it.item
                                }
                                .toList()


                        Triple(
                            foundLive,
                            foundMovies,
                            foundSeries
                        )
                    }


                // ====================================================
                // QUERY MAY HAVE CHANGED
                // ====================================================

                if (
                    normalizeSearchText(
                        internalQuery
                    ) != searchText
                ) {

                    return@launch
                }


                // ====================================================
                // UPDATE UI
                // ====================================================

                liveResults =
                    result.first

                movieResults =
                    result.second

                seriesResults =
                    result.third
            }
    }


    // ================================================================
    // MATCH WORDS
    // ================================================================

    private fun matchesWords(
        content: String,
        words: List<String>
    ): Boolean {

        if (
            words.isEmpty()
        ) {
            return false
        }


        return words.all { word ->

            content.contains(
                word
            )
        }
    }


    // ================================================================
    // NORMALIZE
    //
    // Acción -> accion
    // ACCIÓN -> accion
    // ================================================================

    private fun normalizeSearchText(
        value: String?
    ): String {

        val lowered =
            value
                .orEmpty()
                .lowercase(
                    Locale.ROOT
                )


        return Normalizer
            .normalize(
                lowered,
                Normalizer.Form.NFD
            )
            .replace(
                SearchDiacriticsRegex,
                ""
            )
            .replace(
                SearchSpacesRegex,
                " "
            )
            .trim()
    }


    // ================================================================
    // GET CATEGORY ID
    //
    // Solo se ejecuta al crear el índice,
    // NO mientras estás escribiendo.
    // ================================================================

    private fun getCategoryIdSafely(
        movie: VodStream
    ): String {

        return try {

            // ========================================================
            // GETTER
            // ========================================================

            val getter =
                movie.javaClass
                    .methods
                    .firstOrNull { method ->

                        method.parameterCount == 0 &&
                                (
                                        method.name.equals(
                                            "getCategoryId",
                                            ignoreCase = true
                                        ) ||

                                                method.name.equals(
                                                    "getCategory_id",
                                                    ignoreCase = true
                                                )
                                        )
                    }


            val getterValue =
                getter
                    ?.invoke(
                        movie
                    )
                    ?.toString()


            if (
                !getterValue.isNullOrBlank()
            ) {

                return getterValue
            }


            // ========================================================
            // FIELD FALLBACK
            // ========================================================

            val field =
                movie.javaClass
                    .declaredFields
                    .firstOrNull { field ->

                        field.name.equals(
                            "categoryId",
                            ignoreCase = true
                        ) ||

                                field.name.equals(
                                    "category_id",
                                    ignoreCase = true
                                )
                    }


            if (
                field != null
            ) {

                field.isAccessible =
                    true


                field
                    .get(
                        movie
                    )
                    ?.toString()
                    .orEmpty()

            } else {

                ""
            }


        } catch (
            e: Exception
        ) {

            ""
        }
    }


    // ================================================================
    // LIVE URL
    // ================================================================

    fun getLiveUrl(
        stream: LiveStream
    ): String {

        val currentSession =
            session
                ?: return ""


        val baseUrl =
            currentSession.server
                .trim()
                .removeSuffix(
                    "/"
                )


        val extension =

            if (
                !stream.containerExtension
                    .isNullOrBlank()
            ) {

                ".${stream.containerExtension}"

            } else {

                ".ts"
            }


        return "$baseUrl/live/" +
                "${currentSession.username}/" +
                "${currentSession.password}/" +
                "${stream.streamId}" +
                extension
    }


    // ================================================================
    // MOVIE URL
    // ================================================================

    fun getMovieUrl(
        movie: VodStream
    ): String {

        val currentSession =
            session
                ?: return ""


        val baseUrl =
            currentSession.server
                .trim()
                .removeSuffix(
                    "/"
                )


        val extension =

            if (
                !movie.containerExtension
                    .isNullOrBlank()
            ) {

                ".${movie.containerExtension}"

            } else {

                ".mp4"
            }


        return "$baseUrl/movie/" +
                "${currentSession.username}/" +
                "${currentSession.password}/" +
                "${movie.streamId}" +
                extension
    }


    // ================================================================
    // CLEANUP
    // ================================================================

    override fun onCleared() {

        searchJob
            ?.cancel()

        super.onCleared()
    }
}