package com.example.clinchplayer.ui.series

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.example.clinchplayer.data.FavoritesManager
import com.example.clinchplayer.network.models.SeriesEpisode


private val ClinchYellow =
    Color(0xFFFFD600)

private val ClinchBlack =
    Color(0xFF050607)

private val SoftBlack =
    Color(0xFF111317)


// ====================================================================
// SERIES DETAILS SCREEN
// ====================================================================

@Composable
fun SeriesDetailsScreen(
    viewModel: SeriesViewModel,
    onBack: () -> Unit,
    onEpisodeClick: (
        SeriesEpisode,
        String
    ) -> Unit
) {

    val selectedSeries =
        viewModel.selectedSeries

    val info =
        viewModel.seriesInfo

    val episodes =
        viewModel.episodes

    val selectedSeason =
        viewModel.selectedSeason


    // ================================================================
    // FAVORITOS
    // ================================================================

    val context =
        LocalContext.current


    val favoritesManager =
        remember {

            FavoritesManager(
                context.applicationContext
            )
        }


    var isFavorite by
    remember(
        selectedSeries?.seriesId
    ) {

        mutableStateOf(
            selectedSeries
                ?.let {
                    favoritesManager
                        .isFavorite(
                            it
                        )
                }
                ?: false
        )
    }


    // ================================================================
    // BACKDROP
    // ================================================================

    val backdrop =
        info
            ?.backdropPath
            ?.firstOrNull()
            ?.takeIf {
                it.isNotBlank()
            }
            ?: selectedSeries
                ?.backdropPath
                ?.firstOrNull()
                ?.takeIf {
                    it.isNotBlank()
                }
            ?: info
                ?.cover
                ?.takeIf {
                    it.isNotBlank()
                }
            ?: selectedSeries
                ?.cover


    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    ClinchBlack
                )
    ) {

        // ============================================================
        // BACKGROUND IMAGE
        // ============================================================

        if (
            !backdrop.isNullOrBlank()
        ) {

            AsyncImage(
                model =
                    backdrop,

                contentDescription =
                    null,

                contentScale =
                    ContentScale.Crop,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(
                            0.70f
                        )
            )


            // ========================================================
            // LEFT GRADIENT
            // ========================================================

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(
                            0.70f
                        )
                        .background(
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        ClinchBlack,

                                        ClinchBlack.copy(
                                            alpha =
                                                0.90f
                                        ),

                                        ClinchBlack.copy(
                                            alpha =
                                                0.50f
                                        ),

                                        Color.Transparent
                                    )
                            )
                        )
            )


            // ========================================================
            // BOTTOM GRADIENT
            // ========================================================

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(
                            0.72f
                        )
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        ClinchBlack
                                    )
                            )
                        )
            )
        }


        // ============================================================
        // MAIN CONTENT
        // ============================================================

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = 42.dp,
                        top = 28.dp,
                        end = 42.dp,
                        bottom = 20.dp
                    )
        ) {

            // ========================================================
            // BACK
            // ========================================================

            BackButton(
                onClick =
                    onBack
            )


            Spacer(
                modifier =
                    Modifier.height(
                        18.dp
                    )
            )


            // ========================================================
            // SERIES INFORMATION
            // ========================================================

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.Bottom
            ) {

                val cover =
                    info
                        ?.cover
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: selectedSeries
                            ?.cover


                // ====================================================
                // COVER
                // ====================================================

                if (
                    !cover.isNullOrBlank()
                ) {

                    AsyncImage(
                        model =
                            cover,

                        contentDescription =
                            info?.name
                                ?: selectedSeries
                                    ?.name,

                        contentScale =
                            ContentScale.Crop,

                        modifier =
                            Modifier
                                .width(
                                    105.dp
                                )
                                .height(
                                    150.dp
                                )
                    )


                    Spacer(
                        modifier =
                            Modifier.width(
                                22.dp
                            )
                    )
                }


                // ====================================================
                // SERIES INFO
                // ====================================================

                Column(
                    modifier =
                        Modifier.widthIn(
                            max =
                                760.dp
                        )
                ) {

                    // =================================================
                    // TITLE
                    // =================================================

                    Text(
                        text =
                            info?.name
                                ?: selectedSeries
                                    ?.name
                                ?: "Serie",

                        color =
                            Color.White,

                        fontSize =
                            30.sp,

                        fontWeight =
                            FontWeight.Bold,

                        maxLines =
                            2,

                        overflow =
                            TextOverflow.Ellipsis
                    )


                    Spacer(
                        modifier =
                            Modifier.height(
                                7.dp
                            )
                    )


                    // =================================================
                    // METADATA
                    // =================================================

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        val rating =
                            info?.rating
                                ?: selectedSeries
                                    ?.rating


                        if (
                            !rating.isNullOrBlank()
                        ) {

                            Text(
                                text =
                                    "★ $rating",

                                color =
                                    ClinchYellow,

                                fontSize =
                                    12.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }


                        val releaseDate =
                            info?.releaseDate
                                ?: info
                                    ?.releaseDateAlt
                                ?: selectedSeries
                                    ?.releaseDate


                        val year =
                            releaseDate
                                ?.take(
                                    4
                                )
                                ?.takeIf {

                                    it.all(
                                        Char::isDigit
                                    )
                                }


                        if (
                            !year.isNullOrBlank()
                        ) {

                            MetadataSeparator()


                            Text(
                                text =
                                    year,

                                color =
                                    Color.White,

                                fontSize =
                                    12.sp
                            )
                        }


                        val genre =
                            info?.genre
                                ?: selectedSeries
                                    ?.genre


                        if (
                            !genre.isNullOrBlank()
                        ) {

                            MetadataSeparator()


                            Text(
                                text =
                                    genre,

                                color =
                                    Color.White.copy(
                                        alpha =
                                            0.82f
                                    ),

                                fontSize =
                                    12.sp,

                                maxLines =
                                    1,

                                overflow =
                                    TextOverflow.Ellipsis
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(
                                9.dp
                            )
                    )


                    // =================================================
                    // PLOT
                    // =================================================

                    val plot =
                        info?.plot
                            ?: selectedSeries
                                ?.plot


                    if (
                        !plot.isNullOrBlank()
                    ) {

                        Text(
                            text =
                                plot,

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.78f
                                ),

                            fontSize =
                                12.sp,

                            lineHeight =
                                17.sp,

                            maxLines =
                                3,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }


                    // =================================================
                    // FAVORITOS
                    // =================================================

                    if (
                        selectedSeries != null
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(
                                    12.dp
                                )
                        )


                        FavoriteSeriesButton(

                            isFavorite =
                                isFavorite,

                            onClick = {

                                isFavorite =
                                    favoritesManager
                                        .toggleFavorite(
                                            selectedSeries
                                        )
                            }
                        )
                    }


                    // =================================================
                    // LOADING
                    // =================================================

                    if (
                        viewModel
                            .isLoadingSeriesInfo
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(
                                    7.dp
                                )
                        )


                        Text(
                            text =
                                "Cargando temporadas y episodios...",

                            color =
                                ClinchYellow,

                            fontSize =
                                10.sp
                        )
                    }


                    // =================================================
                    // ERROR
                    // =================================================

                    if (
                        !viewModel
                            .seriesInfoError
                            .isNullOrBlank()
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(
                                    7.dp
                                )
                        )


                        Text(
                            text =
                                viewModel
                                    .seriesInfoError
                                    ?: "",

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.60f
                                ),

                            fontSize =
                                10.sp
                        )
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )


            // ========================================================
            // SEASONS + EPISODES
            // ========================================================

            if (
                episodes.isNotEmpty()
            ) {

                // ====================================================
                // SEASONS HEADER
                // ====================================================

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            "TEMPORADAS",

                        color =
                            Color.White,

                        fontSize =
                            17.sp,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Spacer(
                        modifier =
                            Modifier.width(
                                8.dp
                            )
                    )


                    Text(
                        text =
                            "${episodes.size}",

                        color =
                            ClinchYellow,

                        fontSize =
                            11.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            5.dp
                        )
                )


                // ====================================================
                // SEASON KEYS
                // ====================================================

                val seasonKeys =
                    episodes
                        .keys
                        .sortedWith(
                            compareBy {

                                it.toIntOrNull()
                                    ?: Int.MAX_VALUE
                            }
                        )


                // ====================================================
                // SEASONS
                // ====================================================

                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            6.dp
                        ),

                    contentPadding =
                        PaddingValues(
                            start = 0.dp,
                            end = 30.dp,
                            top = 2.dp,
                            bottom = 4.dp
                        )
                ) {

                    items(
                        items =
                            seasonKeys,

                        key = {
                            it
                        }
                    ) {
                            season ->

                        SeasonButton(

                            season =
                                season,

                            selected =
                                season ==
                                        selectedSeason,

                            onClick = {

                                viewModel
                                    .selectSeason(
                                        season
                                    )
                            }
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )


                // ====================================================
                // SELECTED EPISODES
                // ====================================================

                val selectedEpisodes =
                    viewModel
                        .getSelectedSeasonEpisodes()


                // ====================================================
                // EPISODES HEADER
                // ====================================================

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            "EPISODIOS",

                        color =
                            Color.White,

                        fontSize =
                            17.sp,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Spacer(
                        modifier =
                            Modifier.width(
                                8.dp
                            )
                    )


                    Text(
                        text =
                            "${selectedEpisodes.size}",

                        color =
                            ClinchYellow,

                        fontSize =
                            11.sp,

                        fontWeight =
                            FontWeight.Bold
                    )


                    if (
                        !selectedSeason
                            .isNullOrBlank()
                    ) {

                        Spacer(
                            modifier =
                                Modifier.width(
                                    12.dp
                                )
                        )


                        Text(
                            text =
                                "TEMPORADA $selectedSeason",

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.45f
                                ),

                            fontSize =
                                10.sp
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            7.dp
                        )
                )


                // ====================================================
                // EPISODES
                // ====================================================

                LazyRow(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(
                                1f
                            ),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        ),

                    contentPadding =
                        PaddingValues(
                            start = 4.dp,
                            end = 40.dp,
                            top = 8.dp,
                            bottom = 16.dp
                        )
                ) {

                    items(
                        items =
                            selectedEpisodes,

                        key = {
                            it.id
                        }
                    ) {
                            episode ->

                        EpisodeCard(

                            episode =
                                episode,

                            onClick = {

                                val url =
                                    viewModel
                                        .getEpisodeUrl(
                                            episode
                                        )


                                if (
                                    url.isNotBlank()
                                ) {

                                    onEpisodeClick(
                                        episode,
                                        url
                                    )
                                }
                            }
                        )
                    }
                }

            } else if (
                !viewModel
                    .isLoadingSeriesInfo
            ) {

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(
                                1f
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text =
                            "No hay episodios disponibles",

                        color =
                            Color.White.copy(
                                alpha =
                                    0.55f
                            ),

                        fontSize =
                            13.sp
                    )
                }
            }
        }
    }
}


// ====================================================================
// FAVORITE SERIES BUTTON
// ====================================================================

@Composable
private fun FavoriteSeriesButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(
            false
        )
    }


    val scale by
    animateFloatAsState(

        targetValue =
            if (
                focused
            ) {
                1.06f
            } else {
                1f
            },

        animationSpec =
            tween(
                120
            ),

        label =
            "seriesFavoriteScale"
    )


    Surface(
        onClick =
            onClick,

        modifier =
            Modifier
                .width(
                    205.dp
                )
                .height(
                    42.dp
                )
                .scale(
                    scale
                )
                .onFocusChanged {

                    focused =
                        it.isFocused
                },

        colors =
            ClickableSurfaceDefaults
                .colors(

                    containerColor =
                        if (
                            isFavorite
                        ) {
                            Color(
                                0xFF352C00
                            )
                        } else {
                            Color.Black.copy(
                                alpha =
                                    0.55f
                            )
                        },

                    focusedContainerColor =
                        ClinchYellow,

                    pressedContainerColor =
                        ClinchYellow
                ),

        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(
                        7.dp
                    )
                )
    ) {

        Box(
            modifier =
                Modifier
                    .fillMaxSize(),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text =
                    if (
                        isFavorite
                    ) {
                        "★   EN FAVORITOS"
                    } else {
                        "☆   AGREGAR"
                    },

                color =
                    if (
                        focused
                    ) {
                        Color.Black
                    } else if (
                        isFavorite
                    ) {
                        ClinchYellow
                    } else {
                        Color.White
                    },

                fontSize =
                    13.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


// ====================================================================
// SEASON BUTTON
// ====================================================================

@Composable
private fun SeasonButton(
    season: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(
            false
        )
    }


    val scale by
    animateFloatAsState(

        targetValue =
            if (
                focused
            ) {
                1.08f
            } else {
                1f
            },

        animationSpec =
            tween(
                120
            ),

        label =
            "seasonScale"
    )


    Surface(
        onClick =
            onClick,

        modifier =
            Modifier
                .height(
                    42.dp
                )
                .scale(
                    scale
                )
                .onFocusChanged {

                    focused =
                        it.isFocused
                },

        colors =
            ClickableSurfaceDefaults
                .colors(

                    containerColor =
                        Color.Transparent,

                    focusedContainerColor =
                        Color.Transparent,

                    pressedContainerColor =
                        Color.Transparent
                ),

        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(
                        0.dp
                    )
                )
    ) {

        Box(
            modifier =
                Modifier.padding(
                    horizontal =
                        14.dp
                ),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text =
                    "TEMP. $season",

                color =
                    when {

                        focused ->
                            Color.White

                        selected ->
                            Color.White

                        else ->
                            Color.White.copy(
                                alpha =
                                    0.65f
                            )
                    },

                fontSize =
                    14.sp,

                fontWeight =
                    if (
                        focused ||
                        selected
                    ) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    }
            )
        }
    }
}


// ====================================================================
// EPISODE CARD
// ====================================================================

@Composable
private fun EpisodeCard(
    episode: SeriesEpisode,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(
            false
        )
    }


    val scale by
    animateFloatAsState(

        targetValue =
            if (
                focused
            ) {
                1.05f
            } else {
                1f
            },

        animationSpec =
            tween(
                120
            ),

        label =
            "episodeCardScale"
    )


    Column(
        modifier =
            Modifier
                .width(
                    160.dp
                )
                .scale(
                    scale
                )
    ) {

        Surface(
            onClick =
                onClick,

            modifier =
                Modifier
                    .width(
                        160.dp
                    )
                    .height(
                        240.dp
                    )
                    .onFocusChanged {

                        focused =
                            it.isFocused
                    },

            colors =
                ClickableSurfaceDefaults
                    .colors(

                        containerColor =
                            SoftBlack,

                        focusedContainerColor =
                            SoftBlack,

                        pressedContainerColor =
                            SoftBlack
                    ),

            shape =
                ClickableSurfaceDefaults
                    .shape(
                        RoundedCornerShape(
                            9.dp
                        )
                    )
        ) {

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
            ) {

                val image =
                    episode
                        .info
                        ?.movieImage


                if (
                    !image.isNullOrBlank()
                ) {

                    AsyncImage(
                        model =
                            image,

                        contentDescription =
                            episode.title,

                        contentScale =
                            ContentScale.Crop,

                        modifier =
                            Modifier
                                .fillMaxSize()
                    )

                } else {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(
                                    SoftBlack
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                "▶",

                            color =
                                Color.White,

                            fontSize =
                                30.sp
                        )
                    }
                }


                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            Color.Transparent,
                                            Color.Transparent,

                                            Color.Black.copy(
                                                alpha =
                                                    0.75f
                                            )
                                        )
                                )
                            )
                )


                Box(
                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomStart
                            )
                            .padding(
                                8.dp
                            )
                            .background(
                                Color.Black.copy(
                                    alpha =
                                        0.80f
                                ),

                                RoundedCornerShape(
                                    5.dp
                                )
                            )
                            .padding(
                                horizontal =
                                    8.dp,

                                vertical =
                                    4.dp
                            )
                ) {

                    Text(
                        text =
                            "E${
                                episode
                                    .episodeNum
                                    ?.toString()
                                    ?.padStart(
                                        2,
                                        '0'
                                    )
                                    ?: "--"
                            }",

                        color =
                            ClinchYellow,

                        fontSize =
                            11.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }


                if (
                    focused
                ) {

                    Box(
                        modifier =
                            Modifier
                                .align(
                                    Alignment.Center
                                )
                                .size(
                                    46.dp
                                )
                                .background(
                                    Color.Black.copy(
                                        alpha =
                                            0.72f
                                    ),

                                    RoundedCornerShape(
                                        50
                                    )
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                "▶",

                            color =
                                Color.White,

                            fontSize =
                                20.sp
                        )
                    }
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(
                    7.dp
                )
        )


        Text(
            text =
                episode.title
                    ?: "Episodio ${episode.episodeNum ?: ""}",

            color =
                if (
                    focused
                ) {
                    ClinchYellow
                } else {
                    Color.White
                },

            fontSize =
                11.sp,

            fontWeight =
                if (
                    focused
                ) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )


        Spacer(
            modifier =
                Modifier.height(
                    3.dp
                )
        )


        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            val duration =
                episode
                    .info
                    ?.duration


            val rating =
                episode
                    .info
                    ?.rating


            if (
                !duration.isNullOrBlank()
            ) {

                Text(
                    text =
                        duration,

                    color =
                        Color.White.copy(
                            alpha =
                                0.55f
                        ),

                    fontSize =
                        9.sp
                )
            }


            if (
                !rating.isNullOrBlank()
            ) {

                if (
                    !duration.isNullOrBlank()
                ) {

                    Text(
                        text =
                            "  •  ",

                        color =
                            Color.White.copy(
                                alpha =
                                    0.35f
                            ),

                        fontSize =
                            9.sp
                    )
                }


                Text(
                    text =
                        "★ $rating",

                    color =
                        ClinchYellow.copy(
                            alpha =
                                0.85f
                        ),

                    fontSize =
                        9.sp
                )
            }
        }
    }
}


// ====================================================================
// BACK BUTTON
// ====================================================================

@Composable
private fun BackButton(
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(
            false
        )
    }


    val scale by
    animateFloatAsState(

        targetValue =
            if (
                focused
            ) {
                1.08f
            } else {
                1f
            },

        animationSpec =
            tween(
                120
            ),

        label =
            "backButtonScale"
    )


    Surface(
        onClick =
            onClick,

        modifier =
            Modifier
                .width(
                    130.dp
                )
                .height(
                    42.dp
                )
                .scale(
                    scale
                )
                .onFocusChanged {

                    focused =
                        it.isFocused
                },

        colors =
            ClickableSurfaceDefaults
                .colors(

                    containerColor =
                        Color.Transparent,

                    focusedContainerColor =
                        Color.Transparent,

                    pressedContainerColor =
                        Color.Transparent
                ),

        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(
                        0.dp
                    )
                )
    ) {

        Box(
            modifier =
                Modifier
                    .fillMaxSize(),

            contentAlignment =
                Alignment.CenterStart
        ) {

            Text(
                text =
                    "←  VOLVER",

                color =
                    Color.White,

                fontSize =
                    14.sp,

                fontWeight =
                    if (
                        focused
                    ) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    }
            )
        }
    }
}


// ====================================================================
// METADATA SEPARATOR
// ====================================================================

@Composable
private fun MetadataSeparator() {

    Text(
        text =
            "  •  ",

        color =
            Color.White.copy(
                alpha =
                    0.45f
            ),

        fontSize =
            11.sp
    )
}