package com.example.clinchplayer.ui.search

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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


// ================================================================
// COLORS
// ================================================================

private val SearchYellow =
    Color(0xFFFFD600)

private val CardBlack =
    Color(0xFF151515)


// ================================================================
// LANGUAGE
// ================================================================

@Composable
private fun searchText(
    es: String,
    en: String
): String {

    val configuration =
        LocalConfiguration.current

    val language =
        configuration.locales[0]?.language
            ?: "es"

    return if (
        language == "en"
    ) {
        en
    } else {
        es
    }
}


// ================================================================
// SEARCH SCREEN
// ================================================================

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
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

    val query =
        viewModel.query


    /*
     * SearchViewModel devuelve listas seguras.
     *
     * Protegemos nombres nulos y tampoco usamos
     * streamId / seriesId como key en LazyRow.
     */
    val liveResults =
        viewModel.liveResults

    val movieResults =
        viewModel.movieResults

    val seriesResults =
        viewModel.seriesResults


    val totalResults =
        liveResults.size +
                movieResults.size +
                seriesResults.size


    // ============================================================
    // SCREEN
    // ============================================================

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black
                )
                .padding(
                    top = 30.dp
                )
    ) {

        // ========================================================
        // HEADER
        // ========================================================

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 36.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            SearchButton(
                text =
                    "← ${stringResource(R.string.back).uppercase()}",

                onClick =
                    onBack
            )


            Spacer(
                modifier =
                    Modifier.width(
                        24.dp
                    )
            )


            Column {

                Text(
                    text =
                        stringResource(
                            R.string.search
                        ).uppercase(),

                    color =
                        Color.White,

                    fontSize =
                        28.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Text(
                    text =
                        searchText(
                            es =
                                "TV EN VIVO  •  PELÍCULAS  •  SERIES",

                            en =
                                "LIVE TV  •  MOVIES  •  SERIES"
                        ),

                    color =
                        SearchYellow,

                    fontSize =
                        11.sp,

                    fontWeight =
                        FontWeight.Bold,

                    letterSpacing =
                        1.5.sp
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(
                    22.dp
                )
        )


        // ========================================================
        // SEARCH FIELD
        // ========================================================

        OutlinedTextField(
            value =
                query,

            onValueChange = { newText ->

                viewModel.query =
                    newText
            },

            placeholder = {

                androidx.compose.material3.Text(
                    text =
                        searchText(
                            es =
                                "Busca por título, actor, año, género, director o categoría...",

                            en =
                                "Search by title, actor, year, genre, director or category..."
                        )
                )
            },

            singleLine =
                true,

            keyboardOptions =
                KeyboardOptions(
                    imeAction =
                        ImeAction.Search
                ),

            keyboardActions =
                KeyboardActions(
                    onSearch = {

                        /*
                         * No necesitamos lanzar otra petición.
                         *
                         * SearchViewModel filtra los resultados
                         * automáticamente mientras escribimos.
                         */
                    }
                ),

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 36.dp
                    ),

            colors =
                OutlinedTextFieldDefaults.colors(

                    focusedBorderColor =
                        SearchYellow,

                    unfocusedBorderColor =
                        Color.White.copy(
                            alpha = 0.30f
                        ),

                    focusedTextColor =
                        Color.White,

                    unfocusedTextColor =
                        Color.White,

                    cursorColor =
                        SearchYellow,

                    focusedContainerColor =
                        Color(
                            0xFF0E0E0E
                        ),

                    unfocusedContainerColor =
                        Color(
                            0xFF0E0E0E
                        ),

                    focusedPlaceholderColor =
                        Color.White.copy(
                            alpha = 0.45f
                        ),

                    unfocusedPlaceholderColor =
                        Color.White.copy(
                            alpha = 0.35f
                        )
                )
        )


        Spacer(
            modifier =
                Modifier.height(
                    18.dp
                )
        )


        // ========================================================
        // STATES
        // ========================================================

        when {

            // ====================================================
            // LOADING
            // ====================================================

            viewModel.isLoading -> {

                StatusText(
                    searchText(
                        es =
                            "Cargando catálogo completo para buscar...",

                        en =
                            "Loading full catalog for search..."
                    )
                )
            }


            // ====================================================
            // ERROR
            // ====================================================

            viewModel.errorMessage != null -> {

                StatusText(
                    viewModel.errorMessage
                        ?: "Error"
                )
            }


            // ====================================================
            // EMPTY QUERY
            // ====================================================

            query.isBlank() -> {

                StatusText(
                    searchText(
                        es =
                            "Escribe al menos 2 letras para comenzar la búsqueda.",

                        en =
                            "Enter at least 2 letters to start searching."
                    )
                )
            }


            // ====================================================
            // ONLY ONE LETTER
            // ====================================================

            query.trim().length < 2 -> {

                StatusText(
                    searchText(
                        es =
                            "Escribe una letra más para buscar.",

                        en =
                            "Enter one more letter to search."
                    )
                )
            }


            // ====================================================
            // NO RESULTS
            // ====================================================

            totalResults == 0 -> {

                StatusText(
                    searchText(
                        es =
                            "No encontré resultados para “${query.trim()}”.",

                        en =
                            "No results found for “${query.trim()}”."
                    )
                )
            }


            // ====================================================
            // RESULTS
            // ====================================================

            else -> {

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(
                                rememberScrollState()
                            )
                ) {

                    // =============================================
                    // LIVE TV
                    // =============================================

                    if (
                        liveResults.isNotEmpty()
                    ) {

                        SearchSectionTitle(
                            title =
                                stringResource(
                                    R.string.live_tv
                                ).uppercase(),

                            count =
                                liveResults.size
                        )


                        LazyRow(
                            contentPadding =
                                PaddingValues(
                                    horizontal =
                                        36.dp
                                ),

                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    14.dp
                                ),

                            modifier =
                                Modifier.height(
                                    150.dp
                                )
                        ) {

                            /*
                             * No usamos streamId como key.
                             *
                             * Algunos proveedores IPTV
                             * pueden enviar IDs duplicados.
                             */
                            items(
                                items =
                                    liveResults
                            ) { stream ->

                                SearchResultCard(
                                    title =
                                        stream.name
                                            .orEmpty(),

                                    image =
                                        stream.streamIcon
                                            ?: stream.thumbnail,

                                    compact =
                                        true,

                                    onClick = {

                                        onLiveClick(
                                            stream,

                                            viewModel
                                                .getLiveUrl(
                                                    stream
                                                )
                                        )
                                    }
                                )
                            }
                        }


                        Spacer(
                            modifier =
                                Modifier.height(
                                    18.dp
                                )
                        )
                    }


                    // =============================================
                    // MOVIES
                    // =============================================

                    if (
                        movieResults.isNotEmpty()
                    ) {

                        SearchSectionTitle(
                            title =
                                stringResource(
                                    R.string.movies
                                ).uppercase(),

                            count =
                                movieResults.size
                        )


                        LazyRow(
                            contentPadding =
                                PaddingValues(
                                    horizontal =
                                        36.dp
                                ),

                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    14.dp
                                ),

                            modifier =
                                Modifier.height(
                                    205.dp
                                )
                        ) {

                            /*
                             * Sin key basada en streamId.
                             *
                             * Evita crash si el proveedor
                             * devuelve IDs duplicados.
                             */
                            items(
                                items =
                                    movieResults
                            ) { movie ->

                                SearchResultCard(
                                    title =
                                        movie.name
                                            .orEmpty(),

                                    image =
                                        movie.streamIcon,

                                    onClick = {

                                        onMovieClick(
                                            movie,

                                            viewModel
                                                .getMovieUrl(
                                                    movie
                                                )
                                        )
                                    }
                                )
                            }
                        }


                        Spacer(
                            modifier =
                                Modifier.height(
                                    18.dp
                                )
                        )
                    }


                    // =============================================
                    // SERIES
                    // =============================================

                    if (
                        seriesResults.isNotEmpty()
                    ) {

                        SearchSectionTitle(
                            title =
                                stringResource(
                                    R.string.series
                                ).uppercase(),

                            count =
                                seriesResults.size
                        )


                        LazyRow(
                            contentPadding =
                                PaddingValues(
                                    horizontal =
                                        36.dp
                                ),

                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    14.dp
                                ),

                            modifier =
                                Modifier.height(
                                    205.dp
                                )
                        ) {

                            /*
                             * Sin seriesId como key.
                             */
                            items(
                                items =
                                    seriesResults
                            ) { series ->

                                SearchResultCard(
                                    title =
                                        series.name
                                            .orEmpty(),

                                    image =
                                        series.cover,

                                    onClick = {

                                        onSeriesClick(
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
    }
}


// ================================================================
// STATUS TEXT
// ================================================================

@Composable
private fun StatusText(
    text: String
) {

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        36.dp,

                    vertical =
                        26.dp
                )
    ) {

        Text(
            text =
                text,

            color =
                Color.White.copy(
                    alpha =
                        0.60f
                ),

            fontSize =
                15.sp
        )
    }
}


// ================================================================
// SECTION TITLE
// ================================================================

@Composable
private fun SearchSectionTitle(
    title: String,
    count: Int
) {

    Row(
        modifier =
            Modifier.padding(
                start =
                    36.dp,

                end =
                    36.dp,

                bottom =
                    10.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text =
                title,

            color =
                Color.White,

            fontSize =
                18.sp,

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
                searchText(
                    es =
                        "$count RESULTADOS",

                    en =
                        "$count RESULTS"
                ),

            color =
                SearchYellow,

            fontSize =
                10.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}


// ================================================================
// RESULT CARD
// ================================================================

@Composable
private fun SearchResultCard(
    title: String,
    image: String?,
    compact: Boolean = false,
    onClick: () -> Unit
) {

    var focused by
    remember {
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
                130
            ),

        label =
            "searchCardScale"
    )


    val cardWidth =

        if (
            compact
        ) {
            170.dp
        } else {
            112.dp
        }


    val cardHeight =

        if (
            compact
        ) {
            95.dp
        } else {
            162.dp
        }


    Column(
        modifier =
            Modifier
                .width(
                    cardWidth
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
                        cardWidth
                    )
                    .height(
                        cardHeight
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
                            CardBlack,

                        focusedContainerColor =
                            CardBlack,

                        pressedContainerColor =
                            CardBlack
                    )
        ) {

            Box(
                modifier =
                    Modifier.fillMaxSize()
            ) {

                // ================================================
                // IMAGE
                // ================================================

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
                            Modifier.fillMaxSize()
                    )

                } else {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(
                                    CardBlack
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                if (
                                    compact
                                ) {
                                    "TV"
                                } else {
                                    "▶"
                                },

                            color =
                                Color.White,

                            fontSize =
                                if (
                                    compact
                                ) {
                                    20.sp
                                } else {
                                    26.sp
                                },

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }


                // ================================================
                // PLAY ICON WHEN FOCUSED
                // ================================================

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
                                    42.dp
                                )
                                .background(
                                    Color.Black.copy(
                                        alpha =
                                            0.75f
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
                                17.sp
                        )
                    }
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(
                    5.dp
                )
        )


        Text(
            text =
                title,

            color =
                if (
                    focused
                ) {
                    Color.White
                } else {
                    Color.White.copy(
                        alpha =
                            0.85f
                    )
                },

            fontSize =
                10.sp,

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
    }
}


// ================================================================
// BACK BUTTON
// ================================================================

@Composable
private fun SearchButton(
    text: String,
    onClick: () -> Unit
) {

    var focused by
    remember {
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
            "searchBackScale"
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
                                0.16f
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
                text,

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