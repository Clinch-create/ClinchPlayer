package com.example.clinchplayer.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.example.clinchplayer.R
import com.example.clinchplayer.data.WeatherInfo
import com.example.clinchplayer.data.WeatherRepository
import com.example.clinchplayer.network.models.SeriesStream
import com.example.clinchplayer.network.models.VodStream
import com.example.clinchplayer.ui.components.ClinchLogo
import com.example.clinchplayer.ui.movies.MoviesViewModel
import com.example.clinchplayer.ui.series.SeriesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val ClinchYellow = Color(0xFFFFD600)
private val ClinchBlack = Color.Black
private val SoftBlack = Color(0xFF101114)
private val PanelBlack = Color(0xFF090A0C)


@Composable
private fun homeText(es: String, en: String): String {
    val language = LocalConfiguration.current.locales[0]?.language ?: "es"
    return if (language == "en") en else es
}

private enum class HeroType {
    WEATHER,
    MOVIE,
    SERIES
}

@Composable
fun HomeScreen(
    moviesViewModel: MoviesViewModel,
    seriesViewModel: SeriesViewModel,
    expirationDate: String? = null,
    onLiveTvClick: () -> Unit,
    onCatchupClick: () -> Unit = {},
    onContinueWatchingClick: () -> Unit,
    onMoviesClick: () -> Unit,
    onSeriesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onMovieClick: (VodStream, String) -> Unit,
    onFeaturedSeriesClick: (SeriesStream) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var catchupLoading by remember {
        mutableStateOf(false)
    }

    // ============================================================
    // WEATHER
    // ============================================================

    val weatherRepository = remember {
        WeatherRepository()
    }

    var weatherInfo by remember {
        mutableStateOf(
            weatherRepository.getCached(context)
        )
    }

    var weatherLoading by remember {
        mutableStateOf(false)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            weatherRepository.hasLocationPermission(context)
        )
    }

    var currentTimeMillis by remember {
        mutableLongStateOf(
            System.currentTimeMillis()
        )
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            hasLocationPermission =
                granted ||
                        weatherRepository.hasLocationPermission(context)

            if (hasLocationPermission) {
                scope.launch {
                    weatherLoading = true
                    weatherInfo =
                        weatherRepository.refresh(context)
                            ?: weatherInfo
                    weatherLoading = false
                }
            }
        }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis =
                System.currentTimeMillis()

            delay(30_000)
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            while (true) {
                weatherLoading = true

                weatherInfo =
                    weatherRepository.refresh(context)
                        ?: weatherInfo

                weatherLoading = false

                delay(
                    45 * 60 * 1000L
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    // ============================================================
    // HOME CONTENT
    // ============================================================

    val movies =
        moviesViewModel.movies

    val recentMovies =
        movies
            .asReversed()
            .take(15)

    val featuredMovies =
        movies.take(15)

    val featuredSeries =
        seriesViewModel
            .series
            .take(15)

    // ============================================================
    // HERO
    //
    // WEATHER = while navigating Side Menu.
    // MOVIE / SERIES = only while navigating carousels.
    // ============================================================

    var heroType by remember {
        mutableStateOf(
            HeroType.WEATHER
        )
    }

    var heroMovie by remember {
        mutableStateOf<VodStream?>(
            null
        )
    }

    var heroSeries by remember {
        mutableStateOf<SeriesStream?>(
            null
        )
    }

    val movieInfo =
        moviesViewModel.movieInfo

    val heroTitle =
        when (heroType) {
            HeroType.WEATHER ->
                "Clinch Player"

            HeroType.MOVIE ->
                movieInfo
                    ?.name
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: heroMovie?.name
                    ?: "Clinch Player"

            HeroType.SERIES ->
                heroSeries?.name
                    ?: "Clinch Player"
        }

    val heroBackdrop =
        when (heroType) {
            HeroType.WEATHER ->
                null

            HeroType.MOVIE ->
                movieInfo
                    ?.backdropPath
                    ?.firstOrNull()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: movieInfo
                        ?.coverBig
                        ?.takeIf {
                            !it.isNullOrBlank()
                        }
                    ?: movieInfo
                        ?.movieImage
                        ?.takeIf {
                            !it.isNullOrBlank()
                        }
                    ?: heroMovie?.streamIcon

            HeroType.SERIES ->
                heroSeries?.cover
        }

    val heroRating =
        when (heroType) {
            HeroType.WEATHER ->
                null

            HeroType.MOVIE ->
                movieInfo?.rating
                    ?: heroMovie?.rating

            HeroType.SERIES ->
                heroSeries?.rating
        }

    val heroReleaseDate =
        if (heroType == HeroType.MOVIE) {
            movieInfo?.releaseDate
                ?: movieInfo?.releaseDateAlt
                ?: heroMovie?.releaseDate
        } else {
            null
        }

    val heroGenre =
        if (heroType == HeroType.MOVIE) {
            movieInfo?.genre
                ?: heroMovie?.genre
        } else {
            null
        }

    val heroDuration =
        if (heroType == HeroType.MOVIE) {
            movieInfo?.duration
                ?: heroMovie?.duration
        } else {
            null
        }

    val heroPlot =
        if (heroType == HeroType.MOVIE) {
            movieInfo
                ?.plot
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: heroMovie?.plot
        } else {
            null
        }

    // ============================================================
    // HOME VERTICAL CAROUSEL
    //
    // 0 = RECENTLY ADDED
    // 1 = FEATURED MOVIES
    // 2 = FEATURED SERIES
    // ============================================================

    val verticalCarouselState =
        rememberLazyListState()

    var activeSection by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(activeSection) {
        verticalCarouselState.animateScrollToItem(
            activeSection
        )
    }

    // ============================================================
    // SCREEN
    // ============================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.10f),
                        Color.Black.copy(alpha = 0.48f),
                        Color.Black.copy(alpha = 0.82f),
                        Color.Black
                    )
                )
            )
            .background(ClinchBlack)
    ) {

        // Dynamic Hero image is shown only while navigating content.
        if (
            heroType != HeroType.WEATHER &&
            !heroBackdrop.isNullOrBlank()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {

                AsyncImage(
                    model = heroBackdrop,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                ClinchBlack,
                                ClinchBlack.copy(alpha = 0.93f),
                                ClinchBlack.copy(alpha = 0.56f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                ClinchBlack
                            )
                        )
                    )
            )
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            // ====================================================
            // SIDE MENU
            // ====================================================

            SideMenu(
                expirationDate = expirationDate,

                onMenuFocused = {
                    heroType =
                        HeroType.WEATHER

                    heroMovie =
                        null

                    heroSeries =
                        null
                },

                onLiveTvClick =
                    onLiveTvClick,

                catchupLoading =
                    catchupLoading,

                onCatchupClick = {
                    if (!catchupLoading) {
                        scope.launch {
                            catchupLoading = true

                            try {
                                onCatchupClick()
                                delay(1800)
                            } finally {
                                catchupLoading = false
                            }
                        }
                    }
                },

                onContinueWatchingClick =
                    onContinueWatchingClick,

                onMoviesClick =
                    onMoviesClick,

                onSeriesClick =
                    onSeriesClick,

                onSearchClick =
                    onSearchClick,

                onFavoritesClick =
                    onFavoritesClick,

                onSettingsClick =
                    onSettingsClick
            )

            // ====================================================
            // MAIN HOME AREA
            // ====================================================

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.Transparent)
            ) {

                // =================================================
                // TOP AREA
                // WEATHER when Side Menu has focus.
                // DYNAMIC HERO when carousel has focus.
                // =================================================

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {

                    if (heroType == HeroType.WEATHER) {

                        WeatherDashboardHero(
                            weatherInfo =
                                weatherInfo,

                            currentTimeMillis =
                                currentTimeMillis,

                            loading =
                                weatherLoading,

                            hasLocationPermission =
                                hasLocationPermission
                        )

                    } else {

                        DynamicContentHero(
                            heroType =
                                heroType,

                            heroTitle =
                                heroTitle,

                            heroRating =
                                heroRating,

                            heroReleaseDate =
                                heroReleaseDate,

                            heroGenre =
                                heroGenre,

                            heroDuration =
                                heroDuration,

                            heroPlot =
                                heroPlot,

                            onPlayClick = {
                                when (heroType) {
                                    HeroType.WEATHER ->
                                        Unit

                                    HeroType.MOVIE ->
                                        heroMovie
                                            ?.let { movie ->
                                                onMovieClick(
                                                    movie,
                                                    moviesViewModel.getMovieUrl(movie)
                                                )
                                            }

                                    HeroType.SERIES ->
                                        heroSeries
                                            ?.let {
                                                onFeaturedSeriesClick(it)
                                            }
                                }
                            },

                            onFavoritesClick =
                                onFavoritesClick
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Color.White.copy(
                                alpha = 0.08f
                            )
                        )
                )

                // =================================================
                // CONTENT CAROUSELS
                // =================================================

                LazyColumn(
                    state =
                        verticalCarouselState,

                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),

                    contentPadding =
                        PaddingValues(
                            top = 12.dp,
                            bottom = 34.dp
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            11.dp
                        )
                ) {

                    // =============================================
                    // 0. RECENTLY ADDED
                    // =============================================

                    item(
                        key = "recent_movies"
                    ) {
                        MovieSection(
                            title = homeText("RECIÉN", "RECENTLY"),
                            highlight = homeText("AGREGADAS", "ADDED"),
                            movies = recentMovies,

                            onSectionFocused = {
                                if (activeSection != 0) {
                                    activeSection = 0
                                }
                            },

                            onFocused = { movie ->
                                heroType =
                                    HeroType.MOVIE

                                heroSeries =
                                    null

                                if (
                                    heroMovie?.streamId !=
                                    movie.streamId
                                ) {
                                    heroMovie =
                                        movie

                                    moviesViewModel.selectMovie(
                                        movie
                                    )
                                }
                            },

                            onMovieClick = { movie ->
                                onMovieClick(
                                    movie,
                                    moviesViewModel.getMovieUrl(
                                        movie
                                    )
                                )
                            }
                        )
                    }

                    // =============================================
                    // 1. FEATURED MOVIES
                    // =============================================

                    item(
                        key = "featured_movies"
                    ) {
                        MovieSection(
                            title = homeText("PELÍCULAS", "FEATURED"),
                            highlight = homeText("DESTACADAS", "MOVIES"),
                            movies = featuredMovies,

                            onSectionFocused = {
                                if (activeSection != 1) {
                                    activeSection = 1
                                }
                            },

                            onFocused = { movie ->
                                heroType =
                                    HeroType.MOVIE

                                heroSeries =
                                    null

                                if (
                                    heroMovie?.streamId !=
                                    movie.streamId
                                ) {
                                    heroMovie =
                                        movie

                                    moviesViewModel.selectMovie(
                                        movie
                                    )
                                }
                            },

                            onMovieClick = { movie ->
                                onMovieClick(
                                    movie,
                                    moviesViewModel.getMovieUrl(
                                        movie
                                    )
                                )
                            }
                        )
                    }

                    // =============================================
                    // 2. FEATURED SERIES
                    // =============================================

                    item(
                        key = "featured_series"
                    ) {
                        SeriesSection(
                            title = homeText("SERIES", "FEATURED"),
                            highlight = homeText("DESTACADAS", "SERIES"),
                            series = featuredSeries,

                            onSectionFocused = {
                                if (activeSection != 2) {
                                    activeSection = 2
                                }
                            },

                            onFocused = { item ->
                                heroType =
                                    HeroType.SERIES

                                heroMovie =
                                    null

                                heroSeries =
                                    item
                            },

                            onSeriesClick = { item ->
                                onFeaturedSeriesClick(
                                    item
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

// =================================================================
// WEATHER DASHBOARD HERO
// =================================================================

@Composable
private fun WeatherDashboardHero(
    weatherInfo: WeatherInfo?,
    currentTimeMillis: Long,
    loading: Boolean,
    hasLocationPermission: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 26.dp,
                end = 30.dp,
                top = 18.dp,
                bottom = 15.dp
            )
            .clip(
                RoundedCornerShape(
                    10.dp
                )
            )
            .background(
                Color(0xFF0D1118)
            )
    ) {

        val weatherImage =
            weatherInfo?.heroImageUrl

        if (
            !weatherImage.isNullOrBlank()
        ) {
            AsyncImage(
                model = weatherImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF080B10).copy(
                                    alpha = 0.96f
                                ),
                                Color(0xFF111722).copy(
                                    alpha = 0.86f
                                ),
                                Color.Black.copy(
                                    alpha = 0.76f
                                )
                            )
                        )
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 22.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text =
                        weatherInfo
                            ?.locationName
                            ?: "CLINCH PLAYER",
                    color =
                        Color.White.copy(
                            alpha = 0.86f
                        ),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(
                    Modifier.height(
                        4.dp
                    )
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            when {
                                weatherInfo != null ->
                                    weatherInfo.icon

                                loading ->
                                    "↻"

                                else ->
                                    "☁"
                            },
                        color = ClinchYellow,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        Modifier.width(
                            10.dp
                        )
                    )

                    Text(
                        text =
                            if (weatherInfo != null) {
                                "${weatherInfo.temperatureF.toInt()}°F"
                            } else {
                                "--°F"
                            },
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    Modifier.height(
                        1.dp
                    )
                )

                Text(
                    text =
                        when {
                            weatherInfo != null ->
                                weatherInfo.conditionText

                            loading ->
                                homeText("Actualizando clima...", "Updating weather...")

                            !hasLocationPermission ->
                                homeText("Permite ubicación para mostrar el clima", "Allow location to show weather")

                            else ->
                                homeText("Clima no disponible", "Weather unavailable")
                        },
                    color =
                        Color.White.copy(
                            alpha = 0.72f
                        ),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(76.dp)
                    .background(
                        Color.White.copy(
                            alpha = 0.18f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .width(215.dp)
                    .padding(
                        start = 25.dp
                    ),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(
                    text =
                        formatHeroTime(
                            currentTimeMillis
                        ),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    Modifier.height(
                        3.dp
                    )
                )

                Text(
                    text =
                        formatHeroDate(
                            currentTimeMillis
                        ),
                    color =
                        Color.White.copy(
                            alpha = 0.72f
                        ),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(
                    Modifier.height(
                        5.dp
                    )
                )

                Text(
                    text = "CLINCH PLAYER",
                    color = ClinchYellow,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

// =================================================================
// DYNAMIC MOVIE / SERIES HERO
// =================================================================

@Composable
private fun DynamicContentHero(
    heroType: HeroType,
    heroTitle: String,
    heroRating: String?,
    heroReleaseDate: String?,
    heroGenre: String?,
    heroDuration: String?,
    heroPlot: String?,
    onPlayClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 35.dp,
                end = 40.dp,
                top = 12.dp,
                bottom = 12.dp
            ),
        verticalArrangement = Arrangement.Bottom
    ) {

        Text(
            text =
                if (
                    heroType ==
                    HeroType.MOVIE
                ) {
                    "CLINCH PLAYER  •  ${stringResource(R.string.movies).uppercase()}"
                } else {
                    "CLINCH PLAYER  •  ${stringResource(R.string.series).uppercase()}"
                },
            color = ClinchYellow,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(
            Modifier.height(
                4.dp
            )
        )

        Text(
            text = heroTitle,
            color = Color.White,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(
            Modifier.height(
                4.dp
            )
        )

        HeroMetadata(
            rating = heroRating,
            releaseDate = heroReleaseDate,
            genre = heroGenre,
            duration = heroDuration
        )

        Spacer(
            Modifier.height(
                4.dp
            )
        )

        Text(
            text =
                when {
                    heroType ==
                            HeroType.SERIES ->
                        homeText("Serie destacada de Clinch Player", "Featured series on Clinch Player")

                    !heroPlot.isNullOrBlank() ->
                        heroPlot

                    else ->
                        ""
                },
            color =
                Color.White.copy(
                    alpha = 0.78f
                ),
            fontSize = 12.sp,
            lineHeight = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

    }
}

// =================================================================
// SIDE MENU
// =================================================================

@Composable
private fun SideMenu(
    expirationDate: String?,
    onMenuFocused: () -> Unit,
    onLiveTvClick: () -> Unit,
    catchupLoading: Boolean,
    onCatchupClick: () -> Unit,
    onContinueWatchingClick: () -> Unit,
    onMoviesClick: () -> Unit,
    onSeriesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // ============================================================
    // QUICK SIDE MENU CONTROLS
    // Edit only these values to reposition the menu.
    // ============================================================

    // Move the entire Side Menu.
    val menuX = 0.dp
    val menuY = 0.dp

    // Move only the logo.
    val logoX = 0.dp
    val logoY = 0.dp

    // Move only the buttons.
    val buttonsX = 30.dp
    val buttonsY = 0.dp

    // Move only expiration.
    val expirationX = 30.dp
    val expirationY = 0.dp

    // Sizes.
    val menuWidth = 205.dp
    val logoHeight = 150.dp

    val menuTextSize = 12.sp
    val menuIconSize = 16.sp

    val expirationLabelSize = 12.sp
    val expirationDateSize = 12.sp

    // Spacing.
    val iconTextSpacing = 12.dp
    val buttonSpacing = 7.dp
    val buttonHeight = 36.dp

    Column(
        modifier = Modifier
            .width(
                menuWidth
            )
            .fillMaxHeight()
            .offset(
                x = menuX,
                y = menuY
            )
            .background(
                Color.Transparent
            )
            .padding(
                start = 10.dp,
                end = 10.dp,
                top = 5.dp,
                bottom = 25.dp
            )
    ) {

        // ========================================================
        // LOGO
        // ========================================================

        Box(
            modifier =
                Modifier.offset(
                    x = logoX,
                    y = logoY
                )
        ) {

            ClinchLogo(
                modifier =
                    Modifier.fillMaxWidth(),
                height =
                    logoHeight
            )
        }

        // ========================================================
        // BUTTONS
        // ========================================================

        Column(
            modifier =
                Modifier.offset(
                    x = buttonsX,
                    y = buttonsY
                )
        ) {

            CatchupMenuItem(
                isLoading =
                    catchupLoading,
                onClick =
                    onCatchupClick,
                onFocused =
                    onMenuFocused,
                textSize =
                    menuTextSize,
                iconSize =
                    menuIconSize,
                iconTextSpacing =
                    iconTextSpacing,
                buttonHeight =
                    buttonHeight
            )

            Spacer(
                Modifier.height(
                    buttonSpacing
                )
            )

            MenuItem(
                icon = "▶",
                text = stringResource(R.string.continue_watching).uppercase(),
                onClick = onContinueWatchingClick,
                onFocused = onMenuFocused,
                textSize = menuTextSize,
                iconSize = menuIconSize,
                iconTextSpacing = iconTextSpacing,
                buttonHeight = buttonHeight
            )

            Spacer(
                Modifier.height(
                    buttonSpacing
                )
            )

            MenuItem(
                icon = "⌕",
                text = stringResource(R.string.search).uppercase(),
                onClick = onSearchClick,
                onFocused = onMenuFocused,
                textSize = menuTextSize,
                iconSize = menuIconSize,
                iconTextSpacing = iconTextSpacing,
                buttonHeight = buttonHeight
            )

            Spacer(
                Modifier.height(
                    buttonSpacing
                )
            )

            MenuItem(
                icon = "♡",
                text = stringResource(R.string.favorites).uppercase(),
                onClick = onFavoritesClick,
                onFocused = onMenuFocused,
                textSize = menuTextSize,
                iconSize = menuIconSize,
                iconTextSpacing = iconTextSpacing,
                buttonHeight = buttonHeight
            )

            Spacer(
                Modifier.height(
                    buttonSpacing
                )
            )

            MenuItem(
                icon = "●",
                text = stringResource(R.string.movies).uppercase(),
                onClick = onMoviesClick,
                onFocused = onMenuFocused,
                textSize = menuTextSize,
                iconSize = menuIconSize,
                iconTextSpacing = iconTextSpacing,
                buttonHeight = buttonHeight
            )

            Spacer(
                Modifier.height(
                    buttonSpacing
                )
            )

            MenuItem(
                icon = "▶",
                text = stringResource(R.string.series).uppercase(),
                onClick = onSeriesClick,
                onFocused = onMenuFocused,
                textSize = menuTextSize,
                iconSize = menuIconSize,
                iconTextSpacing = iconTextSpacing,
                buttonHeight = buttonHeight
            )

            Spacer(
                Modifier.height(
                    buttonSpacing
                )
            )

            MenuItem(
                icon = "▣",
                text = stringResource(R.string.live_tv).uppercase(),
                onClick = onLiveTvClick,
                onFocused = onMenuFocused,
                textSize = menuTextSize,
                iconSize = menuIconSize,
                iconTextSpacing = iconTextSpacing,
                buttonHeight = buttonHeight
            )

            Spacer(
                Modifier.height(
                    buttonSpacing
                )
            )

            MenuItem(
                icon = "⚙",
                text = stringResource(R.string.settings).uppercase(),
                onClick = onSettingsClick,
                onFocused = onMenuFocused,
                textSize = menuTextSize,
                iconSize = menuIconSize,
                iconTextSpacing = iconTextSpacing,
                buttonHeight = buttonHeight
            )
        }

        Spacer(
            modifier =
                Modifier.weight(
                    1f
                )
        )

        AccountExpiration(
            expirationDate =
                expirationDate,

            modifier =
                Modifier.offset(
                    x = expirationX,
                    y = expirationY
                ),

            labelSize =
                expirationLabelSize,

            dateSize =
                expirationDateSize
        )
    }
}

// =================================================================
// CATCH UP MENU ITEM
// =================================================================

@Composable
private fun CatchupMenuItem(
    isLoading: Boolean,
    onClick: () -> Unit,
    onFocused: () -> Unit = {},
    textSize: TextUnit = 12.sp,
    iconSize: TextUnit = 16.sp,
    iconTextSpacing: Dp = 12.dp,
    buttonHeight: Dp = 36.dp
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                1.02f
            } else {
                1f
            },
        animationSpec =
            tween(120),
        label =
            "catchupScale"
    )

    val infiniteTransition =
        rememberInfiniteTransition(
            label =
                "catchupRotation"
        )

    val rotation by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,

        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 750,
                        easing = LinearEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),

        label =
            "catchupIconRotation"
    )

    Surface(
        onClick =
            onClick,

        modifier = Modifier
            .fillMaxWidth()
            .height(
                buttonHeight
            )
            .scale(
                scale
            )
            .onFocusChanged {

                val gainedFocus =
                    it.isFocused &&
                            !focused

                focused =
                    it.isFocused

                if (gainedFocus) {
                    onFocused()
                }
            },

        shape =
            ClickableSurfaceDefaults.shape(
                RoundedCornerShape(
                    4.dp
                )
            ),

        colors =
            ClickableSurfaceDefaults.colors(
                containerColor =
                    Color.Transparent,

                focusedContainerColor =
                    Color.Transparent,

                pressedContainerColor =
                    Color.Transparent
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = "↻",

                color =
                    when {
                        focused ->
                            ClinchYellow

                        isLoading ->
                            ClinchYellow

                        else ->
                            Color.White.copy(
                                alpha = 0.74f
                            )
                    },

                fontSize =
                    iconSize,

                fontWeight =
                    FontWeight.Bold,

                modifier =
                    Modifier.rotate(
                        if (isLoading) {
                            rotation
                        } else {
                            0f
                        }
                    )
            )

            Spacer(
                Modifier.width(
                    iconTextSpacing
                )
            )

            Text(
                text =
                    if (isLoading) {
                        homeText("ACTUALIZANDO...", "CATCHING UP...")
                    } else {
                        "CATCH UP"
                    },

                color =
                    when {
                        focused ->
                            ClinchYellow

                        isLoading ->
                            ClinchYellow

                        else ->
                            Color.White.copy(
                                alpha = 0.86f
                            )
                    },

                fontSize =
                    textSize,

                fontWeight =
                    if (
                        focused ||
                        isLoading
                    ) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },

                maxLines = 1
            )
        }
    }
}

// =================================================================
// NORMAL MENU ITEM
// =================================================================

@Composable
private fun MenuItem(
    icon: String,
    text: String,
    onClick: () -> Unit,
    onFocused: () -> Unit = {},
    textSize: TextUnit = 12.sp,
    iconSize: TextUnit = 16.sp,
    iconTextSpacing: Dp = 12.dp,
    buttonHeight: Dp = 36.dp
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                1.02f
            } else {
                1f
            },

        animationSpec =
            tween(120),

        label =
            "menuScale"
    )

    Surface(
        onClick =
            onClick,

        modifier = Modifier
            .fillMaxWidth()
            .height(
                buttonHeight
            )
            .scale(
                scale
            )
            .onFocusChanged {

                val gainedFocus =
                    it.isFocused &&
                            !focused

                focused =
                    it.isFocused

                if (gainedFocus) {
                    onFocused()
                }
            },

        shape =
            ClickableSurfaceDefaults.shape(
                RoundedCornerShape(
                    6.dp
                )
            ),

        colors =
            ClickableSurfaceDefaults.colors(
                containerColor =
                    Color.Transparent,

                focusedContainerColor =
                    Color.Transparent,

                pressedContainerColor =
                    Color.Transparent
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    icon,

                color =
                    if (focused) {
                        ClinchYellow
                    } else {
                        Color.White.copy(
                            alpha = 0.74f
                        )
                    },

                fontSize =
                    iconSize,

                fontWeight =
                    if (focused) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
            )

            Spacer(
                Modifier.width(
                    iconTextSpacing
                )
            )

            Text(
                text =
                    text,

                color =
                    if (focused) {
                        ClinchYellow
                    } else {
                        Color.White.copy(
                            alpha = 0.86f
                        )
                    },

                fontSize =
                    textSize,

                fontWeight =
                    if (focused) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

// =================================================================
// ACCOUNT EXPIRATION
// =================================================================

@Composable
private fun AccountExpiration(
    expirationDate: String?,
    modifier: Modifier = Modifier,
    labelSize: TextUnit = 9.sp,
    dateSize: TextUnit = 9.sp
) {
    val formattedDate =
        remember(
            expirationDate
        ) {
            formatExpirationDate(
                expirationDate
            )
        }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 5.dp,
                end = 5.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = stringResource(R.string.expiration).uppercase(),
            color =
                Color.White.copy(
                    alpha = 0.52f
                ),
            fontSize = labelSize,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(
            Modifier.width(
                6.dp
            )
        )

        Text(
            text =
                "•  $formattedDate",
            color =
                ClinchYellow,
            fontSize =
                dateSize,
            fontWeight =
                FontWeight.Bold,
            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis
        )
    }
}

private fun formatExpirationDate(
    expirationDate: String?
): String {
    val raw =
        expirationDate
            ?.trim()
            .orEmpty()

    if (
        raw.isBlank() ||
        raw == "0" ||
        raw.equals(
            "null",
            ignoreCase = true
        )
    ) {
        return "NO DISPONIBLE"
    }

    val timestamp =
        raw.toLongOrNull()
            ?: return raw

    return try {

        val milliseconds =
            if (
                timestamp >
                10_000_000_000L
            ) {
                timestamp
            } else {
                timestamp * 1000L
            }

        val calendar =
            Calendar.getInstance()
                .apply {
                    timeInMillis =
                        milliseconds
                }

        val months =
            arrayOf(
                "ENE",
                "FEB",
                "MAR",
                "ABR",
                "MAY",
                "JUN",
                "JUL",
                "AGO",
                "SEP",
                "OCT",
                "NOV",
                "DIC"
            )

        val day =
            calendar.get(
                Calendar.DAY_OF_MONTH
            )

        val month =
            months[
                calendar.get(
                    Calendar.MONTH
                )
            ]

        val year =
            calendar.get(
                Calendar.YEAR
            )

        String.format(
            Locale.US,
            "%02d/%02d/%02d",
            day,
            calendar.get(Calendar.MONTH) + 1,
            year % 100
        )

    } catch (
        _: Exception
    ) {
        "NO DISPONIBLE"
    }
}

// =================================================================
// HERO METADATA
// =================================================================

@Composable
private fun HeroMetadata(
    rating: String?,
    releaseDate: String?,
    genre: String?,
    duration: String?
) {
    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        if (
            !rating.isNullOrBlank()
        ) {
            Text(
                text = "★ $rating",
                color = ClinchYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (
            !releaseDate.isNullOrBlank()
        ) {
            MetadataSeparator()

            Text(
                text = releaseDate,
                color =
                    Color.White.copy(
                        alpha = 0.80f
                    ),
                fontSize = 11.sp
            )
        }

        if (
            !genre.isNullOrBlank()
        ) {
            MetadataSeparator()

            Text(
                text = genre,
                color =
                    Color.White.copy(
                        alpha = 0.80f
                    ),
                fontSize = 11.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        if (
            !duration.isNullOrBlank()
        ) {
            MetadataSeparator()

            Text(
                text = duration,
                color =
                    Color.White.copy(
                        alpha = 0.80f
                    ),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun MetadataSeparator() {
    Text(
        text = "  •  ",
        color =
            Color.White.copy(
                alpha = 0.45f
            ),
        fontSize = 10.sp
    )
}

// =================================================================
// HERO BUTTON
// =================================================================

@Composable
private fun HeroButton(
    text: String,
    primary: Boolean,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                1.05f
            } else {
                1f
            },
        animationSpec =
            tween(120),
        label =
            "heroButtonScale"
    )

    Surface(
        onClick =
            onClick,

        modifier = Modifier
            .height(
                34.dp
            )
            .scale(
                scale
            )
            .onFocusChanged {
                focused =
                    it.isFocused
            },

        shape =
            ClickableSurfaceDefaults.shape(
                RoundedCornerShape(
                    6.dp
                )
            ),

        colors =
            ClickableSurfaceDefaults.colors(
                containerColor =
                    if (primary) {
                        Color.White.copy(
                            alpha = 0.05f
                        )
                    } else {
                        Color.Transparent
                    },

                focusedContainerColor =
                    ClinchYellow,

                pressedContainerColor =
                    ClinchYellow,

                contentColor =
                    Color.White,

                focusedContentColor =
                    Color.Black
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(
                    horizontal = 10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = text,

                color =
                    if (focused) {
                        ClinchYellow
                    } else {
                        Color.White.copy(
                            alpha = 0.88f
                        )
                    },

                fontSize = 11.sp,

                fontWeight =
                    if (focused) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },

                maxLines = 1
            )
        }
    }
}

// =================================================================
// MOVIE SECTION
// =================================================================

@Composable
private fun MovieSection(
    title: String,
    highlight: String,
    movies: List<VodStream>,
    onSectionFocused: () -> Unit,
    onFocused: (VodStream) -> Unit,
    onMovieClick: (VodStream) -> Unit
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        SectionTitle(
            title = title,
            highlight = highlight
        )

        Spacer(
            Modifier.height(
                7.dp
            )
        )

        if (
            movies.isEmpty()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        175.dp
                    ),
                contentAlignment =
                    Alignment.CenterStart
            ) {

                Text(
                    text =
                        stringResource(R.string.loading),
                    color =
                        Color.White.copy(
                            alpha = 0.50f
                        ),
                    modifier =
                        Modifier.padding(
                            start = 35.dp
                        )
                )
            }

        } else {

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        180.dp
                    ),

                contentPadding =
                    PaddingValues(
                        start = 35.dp,
                        end = 60.dp,
                        top = 4.dp,
                        bottom = 4.dp
                    ),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        13.dp
                    )
            ) {

                items(
                    items = movies,
                    key = {
                        "${title}_${it.streamId}"
                    }
                ) { movie ->

                    MovieCard(
                        movie = movie,

                        onFocused = {
                            onSectionFocused()
                            onFocused(movie)
                        },

                        onClick = {
                            onMovieClick(movie)
                        }
                    )
                }
            }
        }
    }
}

// =================================================================
// SERIES SECTION
// =================================================================

@Composable
private fun SeriesSection(
    title: String,
    highlight: String,
    series: List<SeriesStream>,
    onSectionFocused: () -> Unit,
    onFocused: (SeriesStream) -> Unit,
    onSeriesClick: (SeriesStream) -> Unit
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        SectionTitle(
            title = title,
            highlight = highlight
        )

        Spacer(
            Modifier.height(
                7.dp
            )
        )

        if (
            series.isEmpty()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        175.dp
                    ),
                contentAlignment =
                    Alignment.CenterStart
            ) {

                Text(
                    text =
                        stringResource(R.string.loading),
                    color =
                        Color.White.copy(
                            alpha = 0.50f
                        ),
                    modifier =
                        Modifier.padding(
                            start = 35.dp
                        )
                )
            }

        } else {

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        180.dp
                    ),

                contentPadding =
                    PaddingValues(
                        start = 35.dp,
                        end = 60.dp,
                        top = 4.dp,
                        bottom = 4.dp
                    ),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        13.dp
                    )
            ) {

                items(
                    items = series,
                    key = {
                        "${title}_${it.seriesId}"
                    }
                ) { item ->

                    SeriesCard(
                        series = item,

                        onFocused = {
                            onSectionFocused()
                            onFocused(item)
                        },

                        onClick = {
                            onSeriesClick(item)
                        }
                    )
                }
            }
        }
    }
}

// =================================================================
// SECTION TITLE
// =================================================================

@Composable
private fun SectionTitle(
    title: String,
    highlight: String
) {
    Row(
        modifier =
            Modifier.padding(
                start = 35.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            Modifier.width(
                7.dp
            )
        )

        Text(
            text = highlight,
            color = ClinchYellow,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
    }
}

// =================================================================
// MOVIE CARD
// =================================================================

@Composable
private fun MovieCard(
    movie: VodStream,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                1.08f
            } else {
                1f
            },
        animationSpec =
            tween(130),
        label =
            "movieScale"
    )

    Column(
        modifier = Modifier
            .width(
                112.dp
            )
            .scale(
                scale
            )
    ) {

        Surface(
            onClick =
                onClick,

            modifier = Modifier
                .width(
                    112.dp
                )
                .height(
                    152.dp
                )
                .onFocusChanged {

                    val gainedFocus =
                        it.isFocused &&
                                !focused

                    focused =
                        it.isFocused

                    if (gainedFocus) {
                        onFocused()
                    }
                },

            shape =
                ClickableSurfaceDefaults.shape(
                    RoundedCornerShape(
                        8.dp
                    )
                ),

            colors =
                ClickableSurfaceDefaults.colors(
                    containerColor =
                        SoftBlack,

                    focusedContainerColor =
                        SoftBlack,

                    pressedContainerColor =
                        SoftBlack
                )
        ) {

            Box(
                Modifier.fillMaxSize()
            ) {

                if (
                    !movie.streamIcon.isNullOrBlank()
                ) {

                    AsyncImage(
                        model =
                            movie.streamIcon,
                        contentDescription =
                            movie.name,
                        contentScale =
                            ContentScale.Crop,
                        modifier =
                            Modifier.fillMaxSize()
                    )

                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                SoftBlack
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = "▶",
                            color =
                                ClinchYellow,
                            fontSize =
                                27.sp
                        )
                    }
                }

                if (focused) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(
                                    alpha = 0.17f
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .align(
                                Alignment.Center
                            )
                            .size(
                                40.dp
                            )
                            .background(
                                Color.Black.copy(
                                    alpha = 0.72f
                                ),
                                RoundedCornerShape(
                                    50
                                )
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = "▶",
                            color =
                                ClinchYellow,
                            fontSize =
                                17.sp
                        )
                    }
                }
            }
        }

        Spacer(
            Modifier.height(
                4.dp
            )
        )

        Text(
            text =
                movie.name,

            color =
                if (focused) {
                    ClinchYellow
                } else {
                    Color.White.copy(
                        alpha = 0.88f
                    )
                },

            fontSize =
                10.sp,

            fontWeight =
                if (focused) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}

// =================================================================
// SERIES CARD
// =================================================================

@Composable
private fun SeriesCard(
    series: SeriesStream,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                1.08f
            } else {
                1f
            },
        animationSpec =
            tween(130),
        label =
            "seriesScale"
    )

    Column(
        modifier = Modifier
            .width(
                112.dp
            )
            .scale(
                scale
            )
    ) {

        Surface(
            onClick =
                onClick,

            modifier = Modifier
                .width(
                    112.dp
                )
                .height(
                    152.dp
                )
                .onFocusChanged {

                    val gainedFocus =
                        it.isFocused &&
                                !focused

                    focused =
                        it.isFocused

                    if (gainedFocus) {
                        onFocused()
                    }
                },

            shape =
                ClickableSurfaceDefaults.shape(
                    RoundedCornerShape(
                        8.dp
                    )
                ),

            colors =
                ClickableSurfaceDefaults.colors(
                    containerColor =
                        SoftBlack,

                    focusedContainerColor =
                        SoftBlack,

                    pressedContainerColor =
                        SoftBlack
                )
        ) {

            Box(
                Modifier.fillMaxSize()
            ) {

                if (
                    !series.cover.isNullOrBlank()
                ) {

                    AsyncImage(
                        model =
                            series.cover,
                        contentDescription =
                            series.name,
                        contentScale =
                            ContentScale.Crop,
                        modifier =
                            Modifier.fillMaxSize()
                    )

                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                SoftBlack
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = "▶",
                            color =
                                ClinchYellow,
                            fontSize =
                                27.sp
                        )
                    }
                }

                if (focused) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(
                                    alpha = 0.17f
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .align(
                                Alignment.Center
                            )
                            .size(
                                40.dp
                            )
                            .background(
                                Color.Black.copy(
                                    alpha = 0.72f
                                ),
                                RoundedCornerShape(
                                    50
                                )
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = "▶",
                            color =
                                ClinchYellow,
                            fontSize =
                                17.sp
                        )
                    }
                }
            }
        }

        Spacer(
            Modifier.height(
                4.dp
            )
        )

        Text(
            text =
                series.name,

            color =
                if (focused) {
                    ClinchYellow
                } else {
                    Color.White.copy(
                        alpha = 0.88f
                    )
                },

            fontSize =
                10.sp,

            fontWeight =
                if (focused) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}

// =================================================================
// DATE / TIME
// =================================================================

private fun formatHeroTime(
    timeMillis: Long
): String =
    SimpleDateFormat(
        "h:mm a",
        Locale.getDefault()
    )
        .format(
            Date(
                timeMillis
            )
        )
        .uppercase(
            Locale.getDefault()
        )

private fun formatHeroDate(
    timeMillis: Long
): String =
    SimpleDateFormat(
        "EEE, d MMM yyyy",
        Locale.getDefault()
    )
        .format(
            Date(
                timeMillis
            )
        )
        .replaceFirstChar {
            if (
                it.isLowerCase()
            ) {
                it.titlecase(
                    Locale.getDefault()
                )
            } else {
                it.toString()
            }
        }

// =================================================================
// PREVIEW
// =================================================================

@Preview(
    name = "Clinch Player - Android TV 1080p",
    device = "spec:width=1920px,height=1080px,dpi=320",
    showBackground = true,
    backgroundColor = 0xFF000000,
    fontScale = 1f
)
@Composable
private fun HomeScreenTvPreview() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                ClinchBlack
            )
    ) {

        Row(
            modifier =
                Modifier.fillMaxSize()
        ) {

            SideMenu(
                expirationDate =
                    "1790006400",

                onMenuFocused =
                    {},

                onLiveTvClick =
                    {},

                catchupLoading =
                    false,

                onCatchupClick =
                    {},

                onContinueWatchingClick =
                    {},

                onMoviesClick =
                    {},

                onSeriesClick =
                    {},

                onSearchClick =
                    {},

                onFavoritesClick =
                    {},

                onSettingsClick =
                    {}
            )

            Column(
                modifier = Modifier
                    .weight(
                        1f
                    )
                    .fillMaxHeight()
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            190.dp
                        )
                ) {

                    WeatherDashboardHero(
                        weatherInfo =
                            WeatherInfo(
                                temperatureF =
                                    84.0,
                                apparentTemperatureF =
                                    88.0,
                                weatherCode =
                                    2,
                                isDay =
                                    true,
                                windSpeedMph =
                                    7.0,
                                locationName =
                                    "Ponce, Puerto Rico",
                                latitude =
                                    18.01,
                                longitude =
                                    -66.61,
                                updatedAt =
                                    System.currentTimeMillis()
                            ),

                        currentTimeMillis =
                            System.currentTimeMillis(),

                        loading =
                            false,

                        hasLocationPermission =
                            true
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            1.dp
                        )
                        .background(
                            Color.White.copy(
                                alpha = 0.08f
                            )
                        )
                )

                PreviewContentSection(
                    title =
                        homeText("RECIÉN", "RECENTLY"),
                    highlight =
                        homeText("AGREGADAS", "ADDED")
                )

                PreviewContentSection(
                    title =
                        homeText("PELÍCULAS", "FEATURED"),
                    highlight =
                        homeText("DESTACADAS", "MOVIES")
                )

                PreviewContentSection(
                    title =
                        homeText("SERIES", "FEATURED"),
                    highlight =
                        homeText("DESTACADAS", "SERIES")
                )
            }
        }
    }
}

@Composable
private fun PreviewContentSection(
    title: String,
    highlight: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 10.dp
            )
    ) {

        SectionTitle(
            title =
                title,
            highlight =
                highlight
        )

        Spacer(
            Modifier.height(
                7.dp
            )
        )

        Row(
            modifier =
                Modifier.padding(
                    start = 35.dp
                ),
            horizontalArrangement =
                Arrangement.spacedBy(
                    13.dp
                )
        ) {

            repeat(
                7
            ) { index ->

                Column(
                    modifier =
                        Modifier.width(
                            112.dp
                        )
                ) {

                    Box(
                        modifier = Modifier
                            .width(
                                112.dp
                            )
                            .height(
                                152.dp
                            )
                            .background(
                                color =
                                    if (
                                        index ==
                                        0
                                    ) {
                                        Color(
                                            0xFF252525
                                        )
                                    } else {
                                        SoftBlack
                                    },
                                shape =
                                    RoundedCornerShape(
                                        8.dp
                                    )
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                if (
                                    index ==
                                    0
                                ) {
                                    "▶"
                                } else {
                                    "CLINCH"
                                },
                            color =
                                if (
                                    index ==
                                    0
                                ) {
                                    ClinchYellow
                                } else {
                                    Color.White.copy(
                                        alpha = 0.35f
                                    )
                                },
                            fontSize =
                                if (
                                    index ==
                                    0
                                ) {
                                    27.sp
                                } else {
                                    10.sp
                                },
                            fontWeight =
                                FontWeight.Bold
                        )
                    }

                    Spacer(
                        Modifier.height(
                            4.dp
                        )
                    )

                    Text(
                        text =
                            homeText("Contenido ${index + 1}", "Content ${index + 1}"),
                        color =
                            Color.White.copy(
                                alpha = 0.88f
                            ),
                        fontSize =
                            10.sp,
                        maxLines =
                            1,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
