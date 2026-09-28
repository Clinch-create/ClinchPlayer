package com.example.clinchplayer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.example.clinchplayer.data.FavoritesManager
import com.example.clinchplayer.data.SessionManager
import com.example.clinchplayer.data.SettingsManager
import com.example.clinchplayer.network.models.LiveStream
import com.example.clinchplayer.network.models.SeriesEpisode
import com.example.clinchplayer.network.models.VodStream
import com.example.clinchplayer.ui.continuewatching.ContinueWatchingScreen
import com.example.clinchplayer.ui.favorites.FavoritesScreen
import com.example.clinchplayer.ui.favorites.FavoritesViewModel
import com.example.clinchplayer.ui.home.HomeScreen
import com.example.clinchplayer.ui.livetv.LiveTvScreen
import com.example.clinchplayer.ui.livetv.LiveTvViewModel
import com.example.clinchplayer.ui.livetv.PlayerScreen
import com.example.clinchplayer.ui.login.LoginScreen
import com.example.clinchplayer.ui.login.LoginViewModel
import com.example.clinchplayer.ui.movies.MovieDetailsScreen
import com.example.clinchplayer.ui.movies.MoviePlayerScreen
import com.example.clinchplayer.ui.movies.MoviesScreen
import com.example.clinchplayer.ui.movies.MoviesViewModel
import com.example.clinchplayer.ui.search.SearchScreen
import com.example.clinchplayer.ui.search.SearchViewModel
import com.example.clinchplayer.ui.series.SeriesDetailsScreen
import com.example.clinchplayer.ui.series.SeriesPlayerScreen
import com.example.clinchplayer.ui.series.SeriesScreen
import com.example.clinchplayer.ui.series.SeriesViewModel
import com.example.clinchplayer.ui.settings.SettingsScreen
import com.example.clinchplayer.ui.splash.SplashScreen
import com.example.clinchplayer.ui.theme.ClinchPlayerTheme
import com.example.clinchplayer.update.UpdateManager
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var favoritesManager: FavoritesManager
    private lateinit var updateManager: UpdateManager


    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        sessionManager =
            SessionManager(
                applicationContext
            )

        settingsManager =
            SettingsManager(
                applicationContext
            )

        favoritesManager =
            FavoritesManager(
                applicationContext
            )

        updateManager =
            UpdateManager(
                applicationContext
            )


        setContent {

            ClinchPlayerTheme {

                Surface(
                    modifier =
                        Modifier.fillMaxSize(),

                    shape =
                        RectangleShape
                ) {

                    val navController =
                        rememberNavController()

                    val updateScope =
                        rememberCoroutineScope()


                    // =================================================
                    // SESSION
                    // =================================================

                    val session by
                    sessionManager
                        .sessionFlow
                        .collectAsState(
                            initial = null
                        )


                    // =================================================
                    // SPLASH
                    // =================================================

                    var splashFinished by
                    remember {
                        mutableStateOf(
                            false
                        )
                    }


                    // =================================================
                    // SHARED MOVIES VIEWMODEL
                    // =================================================

                    val moviesViewModel:
                            MoviesViewModel =
                        viewModel(
                            factory =
                                object :
                                    ViewModelProvider.Factory {

                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(
                                        modelClass: Class<T>
                                    ): T {

                                        return MoviesViewModel(
                                            sessionManager
                                        ) as T
                                    }
                                }
                        )


                    // =================================================
                    // SHARED SERIES VIEWMODEL
                    // =================================================

                    val seriesViewModel:
                            SeriesViewModel =
                        viewModel(
                            factory =
                                object :
                                    ViewModelProvider.Factory {

                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(
                                        modelClass: Class<T>
                                    ): T {

                                        return SeriesViewModel(
                                            sessionManager
                                        ) as T
                                    }
                                }
                        )


                    // =================================================
                    // SHARED LIVE TV VIEWMODEL
                    // =================================================

                    val liveTvViewModel:
                            LiveTvViewModel =
                        viewModel(
                            factory =
                                object :
                                    ViewModelProvider.Factory {

                                    @Suppress("UNCHECKED_CAST")
                                    override fun <T : ViewModel> create(
                                        modelClass: Class<T>
                                    ): T {

                                        return LiveTvViewModel(
                                            sessionManager
                                        ) as T
                                    }
                                }
                        )


                    // =================================================
                    // LIVE TV PLAYER STATE
                    // =================================================

                    var fullscreenStream by
                    remember {
                        mutableStateOf<LiveStream?>(
                            null
                        )
                    }

                    var fullscreenUrl by
                    remember {
                        mutableStateOf<String?>(
                            null
                        )
                    }


                    // =================================================
                    // MOVIE STATE
                    // =================================================

                    var selectedMovie by
                    remember {
                        mutableStateOf<VodStream?>(
                            null
                        )
                    }

                    var selectedMovieUrl by
                    remember {
                        mutableStateOf<String?>(
                            null
                        )
                    }


                    // =================================================
                    // SERIES / EPISODE STATE
                    // =================================================

                    var selectedEpisode by
                    remember {
                        mutableStateOf<SeriesEpisode?>(
                            null
                        )
                    }

                    var selectedEpisodeUrl by
                    remember {
                        mutableStateOf<String?>(
                            null
                        )
                    }

                    var selectedEpisodeSeason by
                    remember {
                        mutableStateOf<String?>(
                            null
                        )
                    }

                    var selectedEpisodeNumber by
                    remember {
                        mutableIntStateOf(
                            1
                        )
                    }


                    // =================================================
                    // SESSION NAVIGATION
                    // =================================================

                    LaunchedEffect(
                        session,
                        splashFinished
                    ) {

                        if (!splashFinished) {
                            return@LaunchedEffect
                        }

                        val currentSession =
                            session
                                ?: return@LaunchedEffect

                        val route =
                            navController
                                .currentDestination
                                ?.route


                        if (
                            currentSession.isLoggedIn
                        ) {

                            if (
                                route == "splash" ||
                                route == "login"
                            ) {

                                navController.navigate(
                                    "home"
                                ) {

                                    popUpTo(
                                        "splash"
                                    ) {
                                        inclusive = true
                                    }

                                    launchSingleTop = true
                                }
                            }

                        } else {

                            if (
                                route != "login"
                            ) {

                                navController.navigate(
                                    "login"
                                ) {

                                    popUpTo(
                                        "splash"
                                    ) {
                                        inclusive = true
                                    }

                                    launchSingleTop = true
                                }
                            }
                        }
                    }


                    // =================================================
                    // NAVIGATION
                    // =================================================

                    NavHost(
                        navController =
                            navController,

                        startDestination =
                            "splash"
                    ) {


                        // =============================================
                        // SPLASH
                        // =============================================

                        composable(
                            "splash"
                        ) {

                            SplashScreen(
                                onFinished = {
                                    splashFinished =
                                        true
                                }
                            )
                        }


                        // =============================================
                        // LOGIN
                        // =============================================

                        composable(
                            "login"
                        ) {

                            val loginViewModel:
                                    LoginViewModel =
                                viewModel(
                                    factory =
                                        object :
                                            ViewModelProvider.Factory {

                                            @Suppress("UNCHECKED_CAST")
                                            override fun <T : ViewModel> create(
                                                modelClass: Class<T>
                                            ): T {

                                                return LoginViewModel(
                                                    sessionManager
                                                ) as T
                                            }
                                        }
                                )


                            LoginScreen(
                                viewModel =
                                    loginViewModel,

                                onLoginSuccess = {
                                    // sessionFlow navega al Home.
                                }
                            )
                        }


                        // =============================================
                        // HOME
                        // =============================================

                        composable(
                            "home"
                        ) {

                            HomeScreen(
                                moviesViewModel =
                                    moviesViewModel,

                                seriesViewModel =
                                    seriesViewModel,

                                expirationDate =
                                    session
                                        ?.expirationDate,


                                onLiveTvClick = {

                                    navController.navigate(
                                        "live_tv"
                                    )
                                },


                                onCatchupClick = {

                                    liveTvViewModel
                                        .refreshData()

                                    moviesViewModel
                                        .refreshData()

                                    seriesViewModel
                                        .refreshData()
                                },


                                onMoviesClick = {

                                    navController.navigate(
                                        "movies"
                                    )
                                },


                                onSeriesClick = {

                                    navController.navigate(
                                        "series"
                                    )
                                },


                                onSearchClick = {

                                    navController.navigate(
                                        "search"
                                    )
                                },


                                onFavoritesClick = {

                                    navController.navigate(
                                        "favorites"
                                    )
                                },


                                onContinueWatchingClick = {

                                    navController.navigate(
                                        "continue_watching"
                                    ) {
                                        launchSingleTop = true
                                    }
                                },


                                onSettingsClick = {

                                    navController.navigate(
                                        "settings"
                                    )
                                },


                                onMovieClick = {
                                        movie,
                                        url ->

                                    selectedMovie =
                                        movie

                                    selectedMovieUrl =
                                        url

                                    moviesViewModel
                                        .selectMovie(
                                            movie
                                        )

                                    navController.navigate(
                                        "movie_details"
                                    )
                                },


                                onFeaturedSeriesClick = {
                                        series ->

                                    seriesViewModel
                                        .selectSeries(
                                            series
                                        )

                                    navController.navigate(
                                        "series_details"
                                    )
                                }
                            )
                        }


                        // =============================================
                        // CONTINUE WATCHING
                        // =============================================

                        composable(
                            "continue_watching"
                        ) {

                            ContinueWatchingScreen(
                                onBack = {
                                    navController
                                        .popBackStack()
                                },

                                onMovieClick = {
                                        movie ->

                                    val movieUrl =
                                        moviesViewModel
                                            .getMovieUrl(
                                                movie
                                            )

                                    if (
                                        movieUrl.isNotBlank()
                                    ) {

                                        selectedMovie =
                                            movie

                                        selectedMovieUrl =
                                            movieUrl

                                        moviesViewModel
                                            .selectMovie(
                                                movie
                                            )

                                        navController.navigate(
                                            "movie_details"
                                        )
                                    }
                                },

                                onEpisodeClick = {
                                        entry ->

                                    val savedSeries =
                                        entry.series

                                    val savedEpisode =
                                        entry.episode

                                    if (
                                        savedSeries != null &&
                                        savedEpisode != null &&
                                        entry.url.isNotBlank()
                                    ) {

                                        selectedEpisode =
                                            savedEpisode

                                        selectedEpisodeUrl =
                                            entry.url

                                        selectedEpisodeSeason =
                                            entry.seasonNumber

                                        selectedEpisodeNumber =
                                            entry.episodeNumber
                                                .coerceAtLeast(
                                                    1
                                                )

                                        seriesViewModel
                                            .selectSeries(
                                                item =
                                                    savedSeries,

                                                preferredSeason =
                                                    entry.seasonNumber
                                            )

                                        navController.navigate(
                                            "series_player"
                                        )
                                    }
                                }
                            )
                        }


                        // =============================================
                        // SEARCH
                        // =============================================

                        composable(
                            "search"
                        ) {

                            val searchViewModel:
                                    SearchViewModel =
                                viewModel(
                                    factory =
                                        object :
                                            ViewModelProvider.Factory {

                                            @Suppress("UNCHECKED_CAST")
                                            override fun <T : ViewModel> create(
                                                modelClass: Class<T>
                                            ): T {

                                                return SearchViewModel(
                                                    sessionManager
                                                ) as T
                                            }
                                        }
                                )


                            SearchScreen(
                                viewModel =
                                    searchViewModel,

                                onBack = {
                                    navController
                                        .popBackStack()
                                },

                                onLiveClick = {
                                        stream,
                                        url ->

                                    fullscreenStream =
                                        stream

                                    fullscreenUrl =
                                        url

                                    navController.navigate(
                                        "player"
                                    )
                                },

                                onMovieClick = {
                                        movie,
                                        url ->

                                    selectedMovie =
                                        movie

                                    selectedMovieUrl =
                                        url

                                    moviesViewModel
                                        .selectMovie(
                                            movie
                                        )

                                    navController.navigate(
                                        "movie_details"
                                    )
                                },

                                onSeriesClick = {
                                        series ->

                                    seriesViewModel
                                        .selectSeries(
                                            series
                                        )

                                    navController.navigate(
                                        "series_details"
                                    )
                                }
                            )
                        }


                        // =============================================
                        // FAVORITES
                        // =============================================

                        composable(
                            "favorites"
                        ) {

                            val favoritesViewModel:
                                    FavoritesViewModel =
                                viewModel(
                                    factory =
                                        object :
                                            ViewModelProvider.Factory {

                                            @Suppress("UNCHECKED_CAST")
                                            override fun <T : ViewModel> create(
                                                modelClass: Class<T>
                                            ): T {

                                                return FavoritesViewModel(
                                                    sessionManager =
                                                        sessionManager,

                                                    favoritesManager =
                                                        favoritesManager
                                                ) as T
                                            }
                                        }
                                )


                            FavoritesScreen(
                                viewModel =
                                    favoritesViewModel,

                                onBack = {
                                    navController
                                        .popBackStack()
                                },

                                onLiveClick = {
                                        stream,
                                        url ->

                                    fullscreenStream =
                                        stream

                                    fullscreenUrl =
                                        url

                                    navController.navigate(
                                        "player"
                                    )
                                },

                                onMovieClick = {
                                        movie,
                                        url ->

                                    selectedMovie =
                                        movie

                                    selectedMovieUrl =
                                        url

                                    moviesViewModel
                                        .selectMovie(
                                            movie
                                        )

                                    navController.navigate(
                                        "movie_details"
                                    )
                                },

                                onSeriesClick = {
                                        series ->

                                    seriesViewModel
                                        .selectSeries(
                                            series
                                        )

                                    navController.navigate(
                                        "series_details"
                                    )
                                }
                            )
                        }


                        // =============================================
                        // SETTINGS
                        // =============================================

                        composable(
                            "settings"
                        ) {

                            SettingsScreen(
                                username =
                                    session
                                        ?.username
                                        ?: "",

                                server =
                                    session
                                        ?.server
                                        ?: "",

                                settingsManager =
                                    settingsManager,


                                // =====================================
                                // CHECK FOR APP UPDATE
                                // =====================================

                                onCheckForUpdates = {

                                    updateScope.launch {

                                        // =====================================================
                                        // BUSCAR ACTUALIZACIÓN
                                        // =====================================================

                                        Toast
                                            .makeText(
                                                this@MainActivity,
                                                "Buscando actualizaciones...",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()


                                        val update =
                                            updateManager
                                                .checkForUpdate()


                                        // =====================================================
                                        // NO HAY ACTUALIZACIÓN
                                        // =====================================================

                                        if (
                                            update == null
                                        ) {

                                            Toast
                                                .makeText(
                                                    this@MainActivity,
                                                    "Clinch Player está actualizado.",
                                                    Toast.LENGTH_LONG
                                                )
                                                .show()

                                            return@launch
                                        }


                                        // =====================================================
                                        // NUEVA VERSIÓN
                                        // =====================================================

                                        Toast
                                            .makeText(
                                                this@MainActivity,
                                                "Nueva versión disponible: ${update.versionName}",
                                                Toast.LENGTH_LONG
                                            )
                                            .show()


                                        // =====================================================
                                        // PERMISO PARA INSTALAR APK
                                        // =====================================================

                                        if (
                                            !updateManager
                                                .canInstallPackages()
                                        ) {

                                            Toast
                                                .makeText(
                                                    this@MainActivity,
                                                    "Activa 'Permitir desde esta fuente' para Clinch Player.",
                                                    Toast.LENGTH_LONG
                                                )
                                                .show()

                                            updateManager
                                                .openInstallPermissionSettings()

                                            return@launch
                                        }


                                        // =====================================================
                                        // DESCARGAR ACTUALIZACIÓN
                                        // =====================================================

                                        Toast
                                            .makeText(
                                                this@MainActivity,
                                                "Descargando Clinch Player ${update.versionName}...",
                                                Toast.LENGTH_LONG
                                            )
                                            .show()


                                        var lastShownProgress =
                                            0


                                        val apkFile =
                                            updateManager
                                                .downloadUpdate(
                                                    updateInfo =
                                                        update,

                                                    onProgress = {
                                                            progress ->

                                                        if (
                                                            progress >=
                                                            lastShownProgress + 25 ||
                                                            progress == 100
                                                        ) {

                                                            lastShownProgress =
                                                                progress

                                                            Toast
                                                                .makeText(
                                                                    this@MainActivity,
                                                                    "Descargando: $progress%",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                        }
                                                    }
                                                )


                                        // =====================================================
                                        // ERROR DESCARGANDO
                                        // =====================================================

                                        if (
                                            apkFile == null
                                        ) {

                                            Toast
                                                .makeText(
                                                    this@MainActivity,
                                                    "No se pudo descargar la actualización.",
                                                    Toast.LENGTH_LONG
                                                )
                                                .show()

                                            return@launch
                                        }


                                        // =====================================================
                                        // DESCARGA TERMINADA
                                        // =====================================================

                                        Toast
                                            .makeText(
                                                this@MainActivity,
                                                "Actualización descargada. Preparando instalación...",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()


                                        // =====================================================
                                        // ABRIR INSTALADOR DE ANDROID
                                        // =====================================================

                                        updateManager
                                            .installUpdate(
                                                apkFile
                                            )
                                    }
                                },


                                onBack = {

                                    navController
                                        .popBackStack()
                                }
                            )
                        }


                        // =============================================
                        // LIVE TV
                        // =============================================

                        composable(
                            "live_tv"
                        ) {

                            LiveTvScreen(
                                viewModel =
                                    liveTvViewModel,

                                onBack = {
                                    navController
                                        .popBackStack()
                                },

                                onPlayStream = {
                                        stream,
                                        url ->

                                    fullscreenStream =
                                        stream

                                    fullscreenUrl =
                                        url

                                    navController.navigate(
                                        "player"
                                    )
                                },

                                onFavoritesClick = {
                                    navController.navigate(
                                        "favorites"
                                    )
                                }
                            )
                        }


                        // =============================================
                        // LIVE TV PLAYER
                        // =============================================

                        composable(
                            "player"
                        ) {

                            val stream =
                                fullscreenStream

                            val url =
                                fullscreenUrl


                            if (
                                stream != null &&
                                !url.isNullOrBlank()
                            ) {

                                PlayerScreen(
                                    stream =
                                        stream,

                                    url =
                                        url,

                                    onBack = {
                                        navController
                                            .popBackStack()
                                    }
                                )

                            } else {

                                LaunchedEffect(
                                    Unit
                                ) {
                                    navController
                                        .popBackStack()
                                }
                            }
                        }


                        // =============================================
                        // MOVIES
                        // =============================================

                        composable(
                            "movies"
                        ) {

                            MoviesScreen(
                                viewModel =
                                    moviesViewModel,

                                onBack = {
                                    navController
                                        .popBackStack()
                                },

                                onMovieClick = {
                                        movie,
                                        url ->

                                    selectedMovie =
                                        movie

                                    selectedMovieUrl =
                                        url

                                    moviesViewModel
                                        .selectMovie(
                                            movie
                                        )

                                    navController.navigate(
                                        "movie_details"
                                    )
                                }
                            )
                        }


                        // =============================================
                        // MOVIE DETAILS
                        // =============================================

                        composable(
                            "movie_details"
                        ) {

                            val movie =
                                selectedMovie

                            val url =
                                selectedMovieUrl


                            if (
                                movie != null &&
                                !url.isNullOrBlank()
                            ) {

                                MovieDetailsScreen(
                                    movie =
                                        movie,

                                    movieInfo =
                                        moviesViewModel
                                            .movieInfo,

                                    isLoading =
                                        moviesViewModel
                                            .isLoadingMovieInfo,

                                    onPlay = {
                                        navController.navigate(
                                            "movie_player"
                                        )
                                    },

                                    onBack = {
                                        navController
                                            .popBackStack()
                                    }
                                )

                            } else {

                                LaunchedEffect(
                                    Unit
                                ) {
                                    navController
                                        .popBackStack()
                                }
                            }
                        }


                        // =============================================
                        // MOVIE PLAYER
                        // =============================================

                        composable(
                            "movie_player"
                        ) {

                            val movie =
                                selectedMovie

                            val url =
                                selectedMovieUrl


                            if (
                                movie != null &&
                                !url.isNullOrBlank()
                            ) {

                                MoviePlayerScreen(
                                    movie =
                                        movie,

                                    url =
                                        url,

                                    onBack = {
                                        navController
                                            .popBackStack()
                                    }
                                )

                            } else {

                                LaunchedEffect(
                                    Unit
                                ) {
                                    navController
                                        .popBackStack()
                                }
                            }
                        }


                        // =============================================
                        // SERIES
                        // =============================================

                        composable(
                            "series"
                        ) {

                            SeriesScreen(
                                viewModel =
                                    seriesViewModel,

                                onBack = {

                                    val returnedToHome =
                                        navController
                                            .popBackStack(
                                                route =
                                                    "home",

                                                inclusive =
                                                    false
                                            )

                                    if (
                                        !returnedToHome
                                    ) {

                                        navController.navigate(
                                            "home"
                                        ) {
                                            launchSingleTop =
                                                true
                                        }
                                    }
                                },

                                onSeriesClick = {
                                        series ->

                                    seriesViewModel
                                        .selectSeries(
                                            series
                                        )

                                    navController.navigate(
                                        "series_details"
                                    )
                                }
                            )
                        }


                        // =============================================
                        // SERIES DETAILS
                        // =============================================

                        composable(
                            "series_details"
                        ) {

                            SeriesDetailsScreen(
                                viewModel =
                                    seriesViewModel,

                                onBack = {
                                    navController
                                        .popBackStack()
                                },

                                onEpisodeClick = {
                                        episode,
                                        url ->

                                    val seasonEpisodes =
                                        seriesViewModel
                                            .getSelectedSeasonEpisodes()

                                    val index =
                                        seasonEpisodes
                                            .indexOfFirst {
                                                it.id ==
                                                        episode.id
                                            }

                                    selectedEpisode =
                                        episode

                                    selectedEpisodeUrl =
                                        url

                                    selectedEpisodeSeason =
                                        seriesViewModel
                                            .selectedSeason

                                    selectedEpisodeNumber =
                                        episode.episodeNum
                                            ?: if (
                                                index >= 0
                                            ) {
                                                index + 1
                                            } else {
                                                1
                                            }

                                    navController.navigate(
                                        "series_player"
                                    )
                                }
                            )
                        }


                        // =============================================
                        // SERIES PLAYER
                        // =============================================

                        composable(
                            "series_player"
                        ) {

                            val episode =
                                selectedEpisode

                            val url =
                                selectedEpisodeUrl

                            val series =
                                seriesViewModel
                                    .selectedSeries

                            val currentSeason =
                                seriesViewModel
                                    .selectedSeason
                                    ?: selectedEpisodeSeason
                                    ?: "1"

                            val seasonEpisodes =
                                seriesViewModel
                                    .getSelectedSeasonEpisodes()


                            val currentEpisodeIndex =
                                episode
                                    ?.let {
                                            current ->

                                        seasonEpisodes
                                            .indexOfFirst {
                                                it.id ==
                                                        current.id
                                            }
                                    }
                                    ?: -1


                            val effectiveEpisodeNumber =
                                episode
                                    ?.episodeNum
                                    ?: if (
                                        currentEpisodeIndex >= 0
                                    ) {
                                        currentEpisodeIndex + 1
                                    } else {
                                        selectedEpisodeNumber
                                            .coerceAtLeast(
                                                1
                                            )
                                    }


                            val previousEpisode =
                                if (
                                    currentEpisodeIndex > 0
                                ) {

                                    seasonEpisodes
                                        .getOrNull(
                                            currentEpisodeIndex - 1
                                        )

                                } else {
                                    null
                                }


                            val nextEpisode =
                                if (
                                    currentEpisodeIndex >= 0
                                ) {

                                    seasonEpisodes
                                        .getOrNull(
                                            currentEpisodeIndex + 1
                                        )

                                } else {
                                    null
                                }


                            if (
                                episode != null &&
                                series != null &&
                                !url.isNullOrBlank()
                            ) {

                                SeriesPlayerScreen(
                                    series =
                                        series,

                                    episode =
                                        episode,

                                    url =
                                        url,

                                    seasonNumber =
                                        currentSeason,

                                    episodeNumber =
                                        effectiveEpisodeNumber,

                                    canGoPrevious =
                                        previousEpisode != null,

                                    canGoNext =
                                        nextEpisode != null,

                                    autoPlayNextEpisode =
                                        settingsManager
                                            .autoPlayNextEpisode,


                                    onPreviousEpisode = {

                                        previousEpisode
                                            ?.let {
                                                    newEpisode ->

                                                val newUrl =
                                                    seriesViewModel
                                                        .getEpisodeUrl(
                                                            newEpisode
                                                        )

                                                if (
                                                    newUrl.isNotBlank()
                                                ) {

                                                    selectedEpisode =
                                                        newEpisode

                                                    selectedEpisodeUrl =
                                                        newUrl

                                                    selectedEpisodeSeason =
                                                        currentSeason

                                                    selectedEpisodeNumber =
                                                        newEpisode.episodeNum
                                                            ?: currentEpisodeIndex
                                                                .coerceAtLeast(
                                                                    1
                                                                )
                                                }
                                            }
                                    },


                                    onNextEpisode = {

                                        nextEpisode
                                            ?.let {
                                                    newEpisode ->

                                                val newUrl =
                                                    seriesViewModel
                                                        .getEpisodeUrl(
                                                            newEpisode
                                                        )

                                                if (
                                                    newUrl.isNotBlank()
                                                ) {

                                                    selectedEpisode =
                                                        newEpisode

                                                    selectedEpisodeUrl =
                                                        newUrl

                                                    selectedEpisodeSeason =
                                                        currentSeason

                                                    selectedEpisodeNumber =
                                                        newEpisode.episodeNum
                                                            ?: (
                                                                    currentEpisodeIndex + 2
                                                                    ).coerceAtLeast(
                                                                    1
                                                                )
                                                }
                                            }
                                    },


                                    onBack = {
                                        navController
                                            .popBackStack()
                                    }
                                )

                            } else {

                                LaunchedEffect(
                                    Unit
                                ) {
                                    navController
                                        .popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
