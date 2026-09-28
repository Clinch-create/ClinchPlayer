package com.example.clinchplayer.ui.favorites

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.example.clinchplayer.R
import com.example.clinchplayer.network.models.LiveStream
import com.example.clinchplayer.network.models.SeriesStream
import com.example.clinchplayer.network.models.VodStream

private val ClinchYellow =
    Color(0xFFFFD600)

private val CardBackground =
    Color(0xFF141414)


@Composable
private fun favoritesText(es: String, en: String): String {
    val configuration = LocalConfiguration.current
    val language = configuration.locales[0]?.language ?: "es"
    return if (language == "en") en else es
}


@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onBack: () -> Unit,
    onLiveClick: (
        LiveStream,
        String
    ) -> Unit,
    onMovieClick: (
        VodStream,
        String
    ) -> Unit,
    onSeriesClick: (
        SeriesStream
    ) -> Unit
) {

    /*
     * Cada vez que entramos a Favoritos
     * actualizamos la lista.
     */
    LaunchedEffect(Unit) {

        viewModel.refreshFavorites()
    }


    val liveFavorites =
        viewModel.favoriteLiveStreams

    val movieFavorites =
        viewModel.favoriteMovies

    val seriesFavorites =
        viewModel.favoriteSeries


    val totalFavorites =
        liveFavorites.size +
                movieFavorites.size +
                seriesFavorites.size


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                start = 35.dp,
                end = 35.dp,
                top = 25.dp,
                bottom = 40.dp
            )
    ) {

        /*
         * ============================================================
         * HEADER
         * ============================================================
         */

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            BackButton(
                onClick =
                    onBack
            )


            Spacer(
                modifier =
                    Modifier.width(
                        22.dp
                    )
            )


            Column {

                Text(
                    text =
                        stringResource(R.string.favorites).uppercase(),

                    color =
                        Color.White,

                    fontSize =
                        30.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Text(
                    text =
                        favoritesText("$totalFavorites ELEMENTOS GUARDADOS", "$totalFavorites SAVED ITEMS"),

                    color =
                        ClinchYellow,

                    fontSize =
                        11.sp,

                    fontWeight =
                        FontWeight.Bold,

                    letterSpacing =
                        2.sp
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(
                    30.dp
                )
        )


        /*
         * ============================================================
         * LOADING
         * ============================================================
         */

        if (
            viewModel.isLoading
        ) {

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            200.dp
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        favoritesText("Cargando favoritos...", "Loading favorites..."),

                    color =
                        Color.White.copy(
                            alpha =
                                0.65f
                        ),

                    fontSize =
                        16.sp
                )
            }

            return@Column
        }


        /*
         * ============================================================
         * ERROR
         * ============================================================
         */

        val error =
            viewModel.errorMessage


        if (
            !error.isNullOrBlank()
        ) {

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Color(
                                0xFF241515
                            ),

                            RoundedCornerShape(
                                8.dp
                            )
                        )
                        .padding(
                            20.dp
                        )
            ) {

                Text(
                    text =
                        error,

                    color =
                        Color(
                            0xFFFF7777
                        ),

                    fontSize =
                        14.sp
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        25.dp
                    )
            )
        }


        /*
         * ============================================================
         * SIN FAVORITOS
         * ============================================================
         */

        if (
            liveFavorites.isEmpty() &&
            movieFavorites.isEmpty() &&
            seriesFavorites.isEmpty()
        ) {

            EmptyFavorites()

        } else {

            /*
             * ========================================================
             * TV EN VIVO
             * ========================================================
             */

            if (
                liveFavorites.isNotEmpty()
            ) {

                SectionHeader(
                    title =
                        stringResource(R.string.live_tv).uppercase(),

                    count =
                        liveFavorites.size
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )


                LazyRow(
                    contentPadding =
                        PaddingValues(
                            horizontal =
                                4.dp,
                            vertical =
                                8.dp
                        ),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        )
                ) {

                    items(
                        items =
                            liveFavorites,

                        key = {
                            "live_${it.streamId}"
                        }
                    ) { stream ->

                        FavoriteCard(
                            title =
                                stream.name,

                            image =
                                stream.streamIcon,

                            type =
                                "TV",

                            onClick = {

                                onLiveClick(
                                    stream,

                                    viewModel
                                        .getLiveUrl(
                                            stream
                                        )
                                )
                            },

                            onRemove = {

                                viewModel
                                    .removeLive(
                                        stream
                                    )
                            }
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            30.dp
                        )
                )
            }


            /*
             * ========================================================
             * PELÍCULAS
             * ========================================================
             */

            if (
                movieFavorites.isNotEmpty()
            ) {

                SectionHeader(
                    title =
                        stringResource(R.string.movies).uppercase(),

                    count =
                        movieFavorites.size
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )


                LazyRow(
                    contentPadding =
                        PaddingValues(
                            horizontal =
                                4.dp,
                            vertical =
                                8.dp
                        ),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        )
                ) {

                    items(
                        items =
                            movieFavorites,

                        key = {
                            "movie_${it.streamId}"
                        }
                    ) { movie ->

                        FavoriteCard(
                            title =
                                movie.name,

                            image =
                                movie.streamIcon,

                            type =
                                favoritesText("PELÍCULA", "MOVIE"),

                            onClick = {

                                onMovieClick(
                                    movie,

                                    viewModel
                                        .getMovieUrl(
                                            movie
                                        )
                                )
                            },

                            onRemove = {

                                viewModel
                                    .removeMovie(
                                        movie
                                    )
                            }
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            30.dp
                        )
                )
            }


            /*
             * ========================================================
             * SERIES
             * ========================================================
             */

            if (
                seriesFavorites.isNotEmpty()
            ) {

                SectionHeader(
                    title =
                        stringResource(R.string.series).uppercase(),

                    count =
                        seriesFavorites.size
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )


                LazyRow(
                    contentPadding =
                        PaddingValues(
                            horizontal =
                                4.dp,
                            vertical =
                                8.dp
                        ),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        )
                ) {

                    items(
                        items =
                            seriesFavorites,

                        key = {
                            "series_${it.seriesId}"
                        }
                    ) { series ->

                        FavoriteCard(
                            title =
                                series.name,

                            image =
                                series.cover,

                            type =
                                favoritesText("SERIE", "SERIES"),

                            onClick = {

                                onSeriesClick(
                                    series
                                )
                            },

                            onRemove = {

                                viewModel
                                    .removeSeries(
                                        series
                                    )
                            }
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            30.dp
                        )
                )
            }
        }
    }
}


// ================================================================
// EMPTY
// ================================================================

@Composable
private fun EmptyFavorites() {

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    360.dp
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    "☆",

                color =
                    ClinchYellow,

                fontSize =
                    70.sp
            )


            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )


            Text(
                text =
                    favoritesText("AÚN NO TIENES FAVORITOS", "YOU DON’T HAVE FAVORITES YET"),

                color =
                    Color.White,

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )


            Text(
                text =
                    favoritesText("Guarda películas, series o canales para encontrarlos aquí.", "Save movies, series or channels to find them here."),

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


// ================================================================
// SECTION HEADER
// ================================================================

@Composable
private fun SectionHeader(
    title: String,
    count: Int
) {

    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text =
                title,

            color =
                Color.White,

            fontSize =
                19.sp,

            fontWeight =
                FontWeight.Bold
        )


        Spacer(
            modifier =
                Modifier.width(
                    10.dp
                )
        )


        Text(
            text =
                "$count",

            color =
                ClinchYellow,

            fontSize =
                12.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}


// ================================================================
// FAVORITE CARD
// ================================================================

@Composable
private fun FavoriteCard(
    title: String,
    image: String?,
    type: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }


    val scale by
    animateFloatAsState(

        targetValue =
            if (focused) {
                1.08f
            } else {
                1f
            },

        animationSpec =
            tween(
                130
            ),

        label =
            "favoriteCardScale"
    )


    Column(
        modifier =
            Modifier
                .width(
                    130.dp
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
                        130.dp
                    )
                    .height(
                        185.dp
                    )
                    .onFocusChanged {

                        focused =
                            it.isFocused
                    },

            colors =
                ClickableSurfaceDefaults
                    .colors(

                        containerColor =
                            CardBackground,

                        focusedContainerColor =
                            CardBackground,

                        pressedContainerColor =
                            CardBackground
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

                if (
                    !image.isNullOrBlank()
                ) {

                    AsyncImage(
                        model =
                            image,

                        contentDescription =
                            title,

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
                                    CardBackground
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                when (
                                    type
                                ) {

                                    "TV" ->
                                        "▣"

                                    favoritesText("SERIE", "SERIES") ->
                                        "▶"

                                    else ->
                                        "●"
                                },

                            color =
                                ClinchYellow,

                            fontSize =
                                34.sp
                        )
                    }
                }


                /*
                 * TIPO
                 */
                Box(
                    modifier =
                        Modifier
                            .align(
                                Alignment.TopStart
                            )
                            .padding(
                                7.dp
                            )
                            .background(
                                Color.Black.copy(
                                    alpha =
                                        0.72f
                                ),

                                RoundedCornerShape(
                                    4.dp
                                )
                            )
                            .padding(
                                horizontal =
                                    7.dp,

                                vertical =
                                    4.dp
                            )
                ) {

                    Text(
                        text =
                            type,

                        color =
                            ClinchYellow,

                        fontSize =
                            8.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }


                /*
                 * FAVORITO
                 */
                Text(
                    text =
                        "★",

                    color =
                        ClinchYellow,

                    fontSize =
                        21.sp,

                    modifier =
                        Modifier
                            .align(
                                Alignment.TopEnd
                            )
                            .padding(
                                8.dp
                            )
                )


                /*
                 * PLAY AL TENER FOCO
                 */
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
                                    45.dp
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
                                18.sp
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
                title,

            color =
                Color.White,

            fontSize =
                11.sp,

            fontWeight =
                if (
                    focused
                ) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )


        Spacer(
            modifier =
                Modifier.height(
                    6.dp
                )
        )


        /*
         * BOTÓN ELIMINAR
         */
        RemoveFavoriteButton(
            onClick =
                onRemove
        )
    }
}


// ================================================================
// REMOVE BUTTON
// ================================================================

@Composable
private fun RemoveFavoriteButton(
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }


    Surface(
        onClick =
            onClick,

        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    30.dp
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
                        Color(
                            0xFF352A00
                        ),

                    pressedContainerColor =
                        Color(
                            0xFF443600
                        )
                ),

        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(
                        5.dp
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
                        focused
                    ) {
                        favoritesText("★  QUITAR", "★  REMOVE")
                    } else {
                        "★"
                    },

                color =
                    ClinchYellow,

                fontSize =
                    10.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


// ================================================================
// BACK BUTTON
// ================================================================

@Composable
private fun BackButton(
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }


    val scale by
    animateFloatAsState(

        targetValue =
            if (focused) {
                1.07f
            } else {
                1f
            },

        animationSpec =
            tween(
                120
            ),

        label =
            "favoritesBackScale"
    )


    Surface(
        onClick =
            onClick,

        modifier =
            Modifier
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
                        Color.White.copy(
                            alpha =
                                0.12f
                        ),

                    pressedContainerColor =
                        Color.White.copy(
                            alpha =
                                0.18f
                        )
                ),

        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(
                        6.dp
                    )
                )
    ) {

        Text(
            text =
                "← ${stringResource(R.string.back).uppercase()}",

            color =
                Color.White,

            fontSize =
                13.sp,

            fontWeight =
                FontWeight.Bold,

            modifier =
                Modifier.padding(
                    horizontal =
                        14.dp,

                    vertical =
                        10.dp
                )
        )
    }
}