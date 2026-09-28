package com.example.clinchplayer.ui.movies

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import com.example.clinchplayer.data.FavoritesManager
import com.example.clinchplayer.network.models.VodInfo
import com.example.clinchplayer.network.models.VodStream


private val DetailsYellow =
    Color(0xFFFFD600)

private val DetailsBackground =
    Color(0xFF080A0C)


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieDetailsScreen(
    movie: VodStream,
    movieInfo: VodInfo?,
    isLoading: Boolean,
    onPlay: () -> Unit,
    onBack: () -> Unit
) {

    BackHandler {
        onBack()
    }


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
        movie.streamId
    ) {

        mutableStateOf(
            favoritesManager
                .isFavorite(
                    movie
                )
        )
    }


    // ================================================================
    // DATOS
    // ================================================================

    val title =
        movieInfo
            ?.name
            ?.takeIf {
                it.isNotBlank()
            }
            ?: movie.name


    val poster =
        movieInfo
            ?.movieImage
            ?.takeIf {
                it.isNotBlank()
            }
            ?: movieInfo
                ?.coverBig
                ?.takeIf {
                    it.isNotBlank()
                }
            ?: movie.streamIcon


    val backdrop =
        movieInfo
            ?.backdropPath
            ?.firstOrNull()
            ?.takeIf {
                it.isNotBlank()
            }


    val rating =
        movieInfo
            ?.rating
            ?.takeIf {
                it.isNotBlank()
            }
            ?: movie.rating


    val genre =
        movieInfo
            ?.genre
            ?.takeIf {
                it.isNotBlank()
            }
            ?: movie.genre


    val duration =
        movieInfo
            ?.duration
            ?.takeIf {
                it.isNotBlank()
            }
            ?: movie.duration


    val plot =
        movieInfo
            ?.plot
            ?.takeIf {
                it.isNotBlank()
            }
            ?: movie.plot


    val director =
        movieInfo
            ?.director
            ?.takeIf {
                it.isNotBlank()
            }
            ?: movie.director


    val cast =
        movieInfo
            ?.cast
            ?.takeIf {
                it.isNotBlank()
            }
            ?: movie.cast


    val releaseDate =
        movieInfo
            ?.releaseDate
            ?.takeIf {
                it.isNotBlank()
            }
            ?: movieInfo
                ?.releaseDateAlt
                ?.takeIf {
                    it.isNotBlank()
                }
            ?: movie.releaseDate


    val country =
        movieInfo
            ?.country
            ?.takeIf {
                it.isNotBlank()
            }


    // ================================================================
    // SCREEN
    // ================================================================

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    DetailsBackground
                )
    ) {

        // ============================================================
        // BACKDROP
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
                        .fillMaxSize()
                        .align(
                            Alignment.Center
                        )
            )


            // ========================================================
            // GRADIENTE
            // ========================================================

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        DetailsBackground,

                                        DetailsBackground.copy(
                                            alpha =
                                                0.96f
                                        ),

                                        DetailsBackground.copy(
                                            alpha =
                                                0.78f
                                        ),

                                        Color.Transparent
                                    )
                            )
                        )
            )


            // ========================================================
            // OSCURECER BACKDROP
            // ========================================================

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(
                                alpha =
                                    0.25f
                            )
                        )
            )
        }


        // ============================================================
        // CONTENT
        // ============================================================

        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = 50.dp,
                        end = 50.dp,
                        top = 34.dp,
                        bottom = 34.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            // ========================================================
            // POSTER
            // ========================================================

            Box(
                modifier =
                    Modifier
                        .width(
                            220.dp
                        )
                        .height(
                            330.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                10.dp
                            )
                        )
                        .background(
                            Color(
                                0xFF17191C
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                if (
                    !poster.isNullOrBlank()
                ) {

                    AsyncImage(
                        model =
                            poster,

                        contentDescription =
                            title,

                        contentScale =
                            ContentScale.Crop,

                        modifier =
                            Modifier
                                .fillMaxSize()
                    )

                } else {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text =
                                "▶",

                            color =
                                DetailsYellow,

                            fontSize =
                                44.sp
                        )


                        Spacer(
                            modifier =
                                Modifier.height(
                                    8.dp
                                )
                        )


                        Text(
                            text =
                                "SIN POSTER",

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.45f
                                ),

                            fontSize =
                                11.sp
                        )
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.width(
                        42.dp
                    )
            )


            // ========================================================
            // DETAILS
            // ========================================================

            Column(
                modifier =
                    Modifier
                        .weight(
                            1f
                        )
                        .fillMaxHeight(),

                verticalArrangement =
                    Arrangement.Center
            ) {

                // ====================================================
                // BRAND
                // ====================================================

                Text(
                    text =
                        "CLINCH PLAYER",

                    color =
                        DetailsYellow,

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            7.dp
                        )
                )


                // ====================================================
                // TITLE
                // ====================================================

                Text(
                    text =
                        title,

                    color =
                        Color.White,

                    style =
                        MaterialTheme
                            .typography
                            .displaySmall,

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
                            12.dp
                        )
                )


                // ====================================================
                // META DATA
                // ====================================================

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    if (
                        !rating.isNullOrBlank()
                    ) {

                        Text(
                            text =
                                "★ $rating",

                            color =
                                DetailsYellow,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }


                    if (
                        !releaseDate.isNullOrBlank()
                    ) {

                        if (
                            !rating.isNullOrBlank()
                        ) {

                            DetailsSeparator()
                        }


                        Text(
                            text =
                                releaseDate
                                    .take(
                                        4
                                    ),

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.80f
                                )
                        )
                    }


                    if (
                        !duration.isNullOrBlank()
                    ) {

                        DetailsSeparator()


                        Text(
                            text =
                                duration,

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.80f
                                )
                        )
                    }


                    if (
                        !country.isNullOrBlank()
                    ) {

                        DetailsSeparator()


                        Text(
                            text =
                                country,

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.80f
                                ),

                            maxLines =
                                1
                        )
                    }
                }


                // ====================================================
                // GENRE
                // ====================================================

                if (
                    !genre.isNullOrBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                10.dp
                            )
                    )


                    Text(
                        text =
                            genre,

                        color =
                            Color.White.copy(
                                alpha =
                                    0.72f
                            ),

                        fontSize =
                            14.sp,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )
                }


                // ====================================================
                // LOADING
                // ====================================================

                if (
                    isLoading &&
                    movieInfo == null
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                18.dp
                            )
                    )


                    Text(
                        text =
                            "Cargando información...",

                        color =
                            DetailsYellow.copy(
                                alpha =
                                    0.80f
                            ),

                        fontSize =
                            13.sp
                    )
                }


                // ====================================================
                // PLOT
                // ====================================================

                if (
                    !plot.isNullOrBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                18.dp
                            )
                    )


                    Text(
                        text =
                            plot,

                        color =
                            Color.White.copy(
                                alpha =
                                    0.88f
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .bodyLarge,

                        lineHeight =
                            22.sp,

                        maxLines =
                            5,

                        overflow =
                            TextOverflow.Ellipsis,

                        modifier =
                            Modifier
                                .fillMaxWidth(
                                    0.92f
                                )
                    )
                }


                // ====================================================
                // DIRECTOR
                // ====================================================

                if (
                    !director.isNullOrBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                13.dp
                            )
                    )


                    Row {

                        Text(
                            text =
                                "Director: ",

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.48f
                                )
                        )


                        Text(
                            text =
                                director,

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.85f
                                ),

                            maxLines =
                                1,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }
                }


                // ====================================================
                // CAST
                // ====================================================

                if (
                    !cast.isNullOrBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                5.dp
                            )
                    )


                    Row {

                        Text(
                            text =
                                "Reparto: ",

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.48f
                                )
                        )


                        Text(
                            text =
                                cast,

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.85f
                                ),

                            maxLines =
                                2,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            25.dp
                        )
                )


                // ====================================================
                // BOTONES
                // ====================================================

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    /*
                     * REPRODUCIR
                     */

                    PlayMovieButton(
                        onClick =
                            onPlay
                    )


                    /*
                     * FAVORITOS
                     */

                    FavoriteMovieButton(

                        isFavorite =
                            isFavorite,

                        onClick = {

                            isFavorite =
                                favoritesManager
                                    .toggleFavorite(
                                        movie
                                    )
                        }
                    )
                }
            }
        }
    }
}


// ====================================================================
// SEPARATOR
// ====================================================================

@Composable
private fun DetailsSeparator() {

    Text(
        text =
            "  •  ",

        color =
            Color.White.copy(
                alpha =
                    0.40f
            )
    )
}


// ====================================================================
// PLAY BUTTON
// ====================================================================

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlayMovieButton(
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(
            false
        )
    }


    Surface(
        onClick =
            onClick,

        modifier =
            Modifier
                .width(
                    190.dp
                )
                .height(
                    50.dp
                )
                .onFocusChanged {

                    focused =
                        it.isFocused
                },

        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(
                        8.dp
                    )
                ),

        colors =
            ClickableSurfaceDefaults
                .colors(

                    containerColor =
                        Color.White,

                    focusedContainerColor =
                        DetailsYellow,

                    pressedContainerColor =
                        DetailsYellow,

                    contentColor =
                        Color.Black,

                    focusedContentColor =
                        Color.Black
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
                    "▶   REPRODUCIR",

                color =
                    Color.Black,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    15.sp
            )
        }
    }
}


// ====================================================================
// FAVORITE BUTTON
// ====================================================================

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FavoriteMovieButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(
            false
        )
    }


    Surface(
        onClick =
            onClick,

        modifier =
            Modifier
                .width(
                    210.dp
                )
                .height(
                    50.dp
                )
                .onFocusChanged {

                    focused =
                        it.isFocused
                },

        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(
                        8.dp
                    )
                ),

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
                                    0.65f
                            )
                        },

                    focusedContainerColor =
                        DetailsYellow,

                    pressedContainerColor =
                        DetailsYellow,

                    contentColor =
                        if (
                            isFavorite
                        ) {
                            DetailsYellow
                        } else {
                            Color.White
                        },

                    focusedContentColor =
                        Color.Black
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
                        DetailsYellow
                    } else {
                        Color.White
                    },

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    14.sp
            )
        }
    }
}