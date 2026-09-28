package com.example.clinchplayer.ui.movies

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinchplayer.data.IPTVSession
import com.example.clinchplayer.data.SessionManager
import com.example.clinchplayer.network.RetrofitClient
import com.example.clinchplayer.network.models.VodCategory
import com.example.clinchplayer.network.models.VodInfo
import com.example.clinchplayer.network.models.VodStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class MoviesViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    // ================================================================
    // SESSION
    // ================================================================

    private var session: IPTVSession? = null


    // ================================================================
    // JOBS
    // ================================================================

    private var moviesJob: Job? = null
    private var detailsJob: Job? = null


    // ================================================================
    // CATEGORIES
    // ================================================================

    var categories by mutableStateOf<List<VodCategory>>(emptyList())
        private set


    var selectedCategory by mutableStateOf<VodCategory?>(null)
        private set


    // ================================================================
    // MOVIES
    // ================================================================

    var movies by mutableStateOf<List<VodStream>>(emptyList())
        private set


    var selectedMovie by mutableStateOf<VodStream?>(null)
        private set


    // ================================================================
    // MOVIE INFO
    // ================================================================

    var movieInfo by mutableStateOf<VodInfo?>(null)
        private set


    // ================================================================
    // LOADING
    // ================================================================

    var isLoadingCategories by mutableStateOf(false)
        private set


    var isLoadingMovies by mutableStateOf(false)
        private set


    var isLoadingMovieInfo by mutableStateOf(false)
        private set


    // ================================================================
    // ERRORS
    // ================================================================

    var errorMessage by mutableStateOf<String?>(null)
        private set


    var movieInfoError by mutableStateOf<String?>(null)
        private set


    // ================================================================
    // INIT
    // ================================================================

    init {
        loadData()
    }


    // ================================================================
    // LOAD DATA
    // ================================================================

    fun loadData() {

        viewModelScope.launch {

            try {

                errorMessage = null


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

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                Log.e(
                    "MoviesViewModel",
                    "Error obteniendo sesión",
                    e
                )


                errorMessage =
                    "Error obteniendo sesión"
            }
        }
    }


    // ================================================================
    // LOAD CATEGORIES
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
                "MoviesViewModel",
                "Requesting get_vod_categories"
            )


            val response =
                api.getVodCategories(
                    username = s.username,
                    password = s.password
                )


            Log.d(
                "MoviesViewModel",
                "Vod categories HTTP: ${response.code()}"
            )


            if (response.isSuccessful) {

                val receivedCategories =
                    response.body()
                        ?: emptyList()


                categories =
                    receivedCategories


                Log.d(
                    "MoviesViewModel",
                    "VOD categories received: ${receivedCategories.size}"
                )


                // ====================================================
                // CARGAR PRIMERA CATEGORÍA AUTOMÁTICAMENTE
                // ====================================================

                if (
                    receivedCategories.isNotEmpty()
                ) {

                    val firstCategory =
                        receivedCategories.first()


                    /*
                     * No asignamos selectedCategory aquí.
                     *
                     * selectCategory() se encarga de:
                     *
                     * 1. Seleccionarla.
                     * 2. Crear el Job.
                     * 3. Cargar sus películas.
                     */

                    selectCategory(
                        firstCategory
                    )
                }

            } else {

                categories =
                    emptyList()


                errorMessage =
                    "Error cargando categorías (${response.code()})"
            }

        } catch (e: CancellationException) {

            Log.d(
                "MoviesViewModel",
                "Category request cancelled"
            )


            throw e

        } catch (e: Exception) {

            Log.e(
                "MoviesViewModel",
                "Error cargando categorías",
                e
            )


            categories =
                emptyList()


            errorMessage =
                "Error de conexión al cargar categorías"

        } finally {

            isLoadingCategories =
                false
        }
    }


    // ================================================================
    // SELECT CATEGORY
    // ================================================================

    fun selectCategory(
        category: VodCategory
    ) {

        // ============================================================
        // YA TENEMOS ESTA CATEGORÍA CARGADA
        // ============================================================

        if (
            selectedCategory?.categoryId ==
            category.categoryId &&
            movies.isNotEmpty()
        ) {

            return
        }


        // ============================================================
        // CANCELAR CARGA ANTERIOR
        // ============================================================

        moviesJob?.cancel()


        // ============================================================
        // NUEVA CATEGORÍA
        // ============================================================

        selectedCategory =
            category


        selectedMovie =
            null


        movieInfo =
            null


        errorMessage =
            null


        // ============================================================
        // LIMPIAR PELÍCULAS ANTERIORES
        // ============================================================

        movies =
            emptyList()


        // ============================================================
        // INICIAR NUEVA PETICIÓN
        // ============================================================

        moviesJob =
            viewModelScope.launch {

                loadMovies(
                    category.categoryId
                )
            }
    }


    // ================================================================
    // LOAD MOVIES
    // ================================================================

    private suspend fun loadMovies(
        categoryId: String
    ) {

        val s =
            session


        if (s == null) {

            Log.e(
                "MoviesViewModel",
                "Session is null while loading movies"
            )


            if (
                selectedCategory?.categoryId ==
                categoryId
            ) {

                isLoadingMovies =
                    false


                errorMessage =
                    "Sesión no disponible"
            }


            return
        }


        // ============================================================
        // SOLO LA CATEGORÍA ACTUAL CONTROLA EL LOADING
        // ============================================================

        if (
            selectedCategory?.categoryId ==
            categoryId
        ) {

            isLoadingMovies =
                true


            errorMessage =
                null
        }


        Log.d(
            "MoviesViewModel",
            "START loadMovies category=$categoryId"
        )


        try {

            val api =
                RetrofitClient.createApi(
                    s.server
                )


            Log.d(
                "MoviesViewModel",
                "Requesting get_vod_streams category=$categoryId"
            )


            val response =
                api.getVodStreams(
                    username = s.username,
                    password = s.password,
                    categoryId = categoryId
                )


            Log.d(
                "MoviesViewModel",
                "Vod streams HTTP: ${response.code()}"
            )


            // ========================================================
            // PETICIÓN EXITOSA
            // ========================================================

            if (response.isSuccessful) {

                val receivedMovies =
                    response.body()
                        ?: emptyList()


                Log.d(
                    "MoviesViewModel",
                    "Movies received category=$categoryId: ${receivedMovies.size}"
                )


                /*
                 * Solo actualizamos la pantalla si esta petición
                 * todavía corresponde a la categoría seleccionada.
                 */

                if (
                    selectedCategory?.categoryId ==
                    categoryId
                ) {

                    movies =
                        receivedMovies


                    errorMessage =
                        null
                }

            } else {

                Log.e(
                    "MoviesViewModel",
                    "HTTP error category=$categoryId code=${response.code()}"
                )


                if (
                    selectedCategory?.categoryId ==
                    categoryId
                ) {

                    movies =
                        emptyList()


                    errorMessage =
                        "Error cargando películas (${response.code()})"
                }
            }

        } catch (e: CancellationException) {

            // ========================================================
            // CANCELACIÓN NORMAL
            // ========================================================

            Log.d(
                "MoviesViewModel",
                "Request cancelled category=$categoryId"
            )


            throw e

        } catch (e: Exception) {

            Log.e(
                "MoviesViewModel",
                "Exception loading category=$categoryId",
                e
            )


            if (
                selectedCategory?.categoryId ==
                categoryId
            ) {

                movies =
                    emptyList()


                errorMessage =
                    "Error cargando películas"
            }

        } finally {

            /*
             * IMPORTANTE:
             *
             * Una petición vieja que fue cancelada NO puede cambiar
             * el estado de loading de una categoría nueva.
             */

            if (
                selectedCategory?.categoryId ==
                categoryId
            ) {

                isLoadingMovies =
                    false
            }


            Log.d(
                "MoviesViewModel",
                "END loadMovies category=$categoryId"
            )
        }
    }


    // ================================================================
    // SELECT MOVIE
    // ================================================================

    fun selectMovie(
        movie: VodStream
    ) {

        selectedMovie =
            movie


        movieInfo =
            null


        movieInfoError =
            null


        detailsJob?.cancel()


        detailsJob =
            viewModelScope.launch {

                loadMovieInfo(
                    movie.streamId
                )
            }
    }


    // ================================================================
    // LOAD MOVIE INFO
    // ================================================================

    private suspend fun loadMovieInfo(
        vodId: Int
    ) {

        val s =
            session ?: return


        isLoadingMovieInfo =
            true


        movieInfoError =
            null


        try {

            val api =
                RetrofitClient.createApi(
                    s.server
                )


            Log.d(
                "MoviesViewModel",
                "Requesting get_vod_info vod_id=$vodId"
            )


            val response =
                api.getVodInfo(
                    username = s.username,
                    password = s.password,
                    vodId = vodId
                )


            Log.d(
                "MoviesViewModel",
                "get_vod_info HTTP: ${response.code()}"
            )


            if (response.isSuccessful) {

                movieInfo =
                    response.body()
                        ?.info


                Log.d(
                    "MoviesViewModel",
                    "Movie info loaded: ${movieInfo?.name}"
                )


                Log.d(
                    "MoviesViewModel",
                    "Movie image: ${movieInfo?.movieImage}"
                )


                Log.d(
                    "MoviesViewModel",
                    "Cover big: ${movieInfo?.coverBig}"
                )


                Log.d(
                    "MoviesViewModel",
                    "Backdrop: ${movieInfo?.backdropPath}"
                )


                Log.d(
                    "MoviesViewModel",
                    "Plot: ${movieInfo?.plot}"
                )

            } else {

                movieInfoError =
                    "No se pudieron cargar los detalles"
            }

        } catch (e: CancellationException) {

            Log.d(
                "MoviesViewModel",
                "Movie info request cancelled vodId=$vodId"
            )


            throw e

        } catch (e: Exception) {

            Log.e(
                "MoviesViewModel",
                "Exception loading movie info",
                e
            )


            movieInfoError =
                "Error cargando información"

        } finally {

            isLoadingMovieInfo =
                false
        }
    }


    // ================================================================
    // GET MOVIE URL
    // ================================================================

    fun getMovieUrl(
        movie: VodStream
    ): String {

        val s =
            session ?: return ""


        val baseUrl =
            s.server
                .trim()
                .removeSuffix("/")


        val extension =

            if (
                !movie.containerExtension
                    .isNullOrBlank()
            ) {

                ".${movie.containerExtension}"

            } else {

                ".mp4"
            }


        val url =
            "$baseUrl/movie/${s.username}/${s.password}/${movie.streamId}$extension"


        Log.d(
            "MoviesViewModel",
            "Movie URL created for ${movie.name}"
        )


        return url
    }


    // ================================================================
    // CLEAR ERROR
    // ================================================================

    fun clearError() {

        errorMessage =
            null
    }


    // ================================================================
    // ON CLEARED
    // ================================================================

    override fun onCleared() {

        moviesJob?.cancel()

        detailsJob?.cancel()

        super.onCleared()
    }


    // ================================================================
    // CATCH UP / REFRESH
    // ================================================================

    fun refreshData() {
        moviesJob?.cancel()
        detailsJob?.cancel()

        selectedCategory = null
        selectedMovie = null
        movieInfo = null
        movieInfoError = null
        errorMessage = null
        movies = emptyList()

        loadData()
    }

}
