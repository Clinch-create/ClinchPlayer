package com.example.clinchplayer.ui.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinchplayer.data.FavoritesManager
import com.example.clinchplayer.data.SessionManager
import com.example.clinchplayer.network.RetrofitClient
import com.example.clinchplayer.network.models.LiveStream
import com.example.clinchplayer.network.models.SeriesStream
import com.example.clinchplayer.network.models.VodStream
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val sessionManager: SessionManager,
    private val favoritesManager: FavoritesManager
) : ViewModel() {


    // ============================================================
    // FAVORITOS
    // ============================================================

    var favoriteLiveStreams by
    mutableStateOf<List<LiveStream>>(
        emptyList()
    )
        private set


    var favoriteMovies by
    mutableStateOf<List<VodStream>>(
        emptyList()
    )
        private set


    var favoriteSeries by
    mutableStateOf<List<SeriesStream>>(
        emptyList()
    )
        private set


    // ============================================================
    // ESTADO
    // ============================================================

    var isLoading by
    mutableStateOf(false)
        private set


    var errorMessage by
    mutableStateOf<String?>(
        null
    )
        private set


    /*
     * Datos de la sesión.
     *
     * Los guardamos para crear
     * las URLs cuando seleccionemos
     * un favorito.
     */

    private var username: String =
        ""

    private var password: String =
        ""

    private var server: String =
        ""


    // ============================================================
    // INIT
    // ============================================================

    init {

        refreshFavorites()
    }


    // ============================================================
    // REFRESH
    // ============================================================

    fun refreshFavorites() {

        viewModelScope.launch {

            loadFavorites()
        }
    }


    // ============================================================
    // LOAD FAVORITES
    // ============================================================

    private suspend fun loadFavorites() {

        isLoading =
            true

        errorMessage =
            null


        try {

            /*
             * SESIÓN
             */
            val session =
                sessionManager
                    .sessionFlow
                    .first()


            if (session == null) {

                favoriteLiveStreams =
                    emptyList()

                favoriteMovies =
                    emptyList()

                favoriteSeries =
                    emptyList()

                errorMessage =
                    "No hay sesión activa."

                return
            }


            username =
                session.username

            password =
                session.password

            server =
                session.server


            /*
             * IDS GUARDADOS
             */
            val liveIds =
                favoritesManager
                    .getLiveFavorites()


            val movieIds =
                favoritesManager
                    .getMovieFavorites()


            val seriesIds =
                favoritesManager
                    .getSeriesFavorites()


            /*
             * SI NO HAY FAVORITOS
             */
            if (
                liveIds.isEmpty() &&
                movieIds.isEmpty() &&
                seriesIds.isEmpty()
            ) {

                favoriteLiveStreams =
                    emptyList()

                favoriteMovies =
                    emptyList()

                favoriteSeries =
                    emptyList()

                return
            }


            /*
             * API XTREAM
             */
            val api =
                RetrofitClient
                    .createApi(
                        server
                    )


            /*
             * Descargamos las tres listas
             * al mismo tiempo.
             */
            coroutineScope {

                val liveDeferred =
                    async {

                        if (
                            liveIds.isNotEmpty()
                        ) {

                            api.getLiveStreams(
                                username =
                                    username,

                                password =
                                    password,

                                categoryId =
                                    null
                            )

                        } else {

                            null
                        }
                    }


                val moviesDeferred =
                    async {

                        if (
                            movieIds.isNotEmpty()
                        ) {

                            api.getVodStreams(
                                username =
                                    username,

                                password =
                                    password,

                                categoryId =
                                    null
                            )

                        } else {

                            null
                        }
                    }


                val seriesDeferred =
                    async {

                        if (
                            seriesIds.isNotEmpty()
                        ) {

                            api.getSeries(
                                username =
                                    username,

                                password =
                                    password,

                                categoryId =
                                    null
                            )

                        } else {

                            null
                        }
                    }


                /*
                 * TV
                 */
                val liveResponse =
                    liveDeferred.await()


                favoriteLiveStreams =
                    liveResponse
                        ?.body()
                        ?.filter { stream ->

                            liveIds.contains(
                                stream.streamId
                                    .toString()
                            )
                        }
                        ?: emptyList()


                /*
                 * MOVIES
                 */
                val movieResponse =
                    moviesDeferred.await()


                favoriteMovies =
                    movieResponse
                        ?.body()
                        ?.filter { movie ->

                            movieIds.contains(
                                movie.streamId
                                    .toString()
                            )
                        }
                        ?: emptyList()


                /*
                 * SERIES
                 */
                val seriesResponse =
                    seriesDeferred.await()


                favoriteSeries =
                    seriesResponse
                        ?.body()
                        ?.filter { series ->

                            seriesIds.contains(
                                series.seriesId
                                    .toString()
                            )
                        }
                        ?: emptyList()
            }

        } catch (
            exception: Exception
        ) {

            errorMessage =
                exception.message
                    ?: "Error cargando favoritos."

        } finally {

            isLoading =
                false
        }
    }


    // ============================================================
    // REMOVE MOVIE
    // ============================================================

    fun removeMovie(
        movie: VodStream
    ) {

        favoritesManager
            .removeMovieFavorite(
                movie.streamId
                    .toString()
            )


        favoriteMovies =
            favoriteMovies.filterNot {

                it.streamId ==
                        movie.streamId
            }
    }


    // ============================================================
    // REMOVE SERIES
    // ============================================================

    fun removeSeries(
        series: SeriesStream
    ) {

        favoritesManager
            .removeSeriesFavorite(
                series.seriesId
                    .toString()
            )


        favoriteSeries =
            favoriteSeries.filterNot {

                it.seriesId ==
                        series.seriesId
            }
    }


    // ============================================================
    // REMOVE LIVE
    // ============================================================

    fun removeLive(
        stream: LiveStream
    ) {

        favoritesManager
            .removeLiveFavorite(
                stream.streamId
                    .toString()
            )


        favoriteLiveStreams =
            favoriteLiveStreams.filterNot {

                it.streamId ==
                        stream.streamId
            }
    }


    // ============================================================
    // LIVE URL
    // ============================================================

    fun getLiveUrl(
        stream: LiveStream
    ): String {

        val baseUrl =
            server.trimEnd('/')


        val extension =
            stream.containerExtension
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "ts"


        return "$baseUrl/live/$username/$password/${stream.streamId}.$extension"
    }


    // ============================================================
    // MOVIE URL
    // ============================================================

    fun getMovieUrl(
        movie: VodStream
    ): String {

        val baseUrl =
            server.trimEnd('/')


        val extension =
            movie.containerExtension
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "mp4"


        return "$baseUrl/movie/$username/$password/${movie.streamId}.$extension"
    }


    // ============================================================
    // COUNTS
    // ============================================================

    fun totalFavorites(): Int {

        return favoriteLiveStreams.size +
                favoriteMovies.size +
                favoriteSeries.size
    }
}