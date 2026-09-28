package com.example.clinchplayer.ui.movies

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.example.clinchplayer.data.SettingsManager
import com.example.clinchplayer.network.models.VodCategory
import com.example.clinchplayer.network.models.VodStream
import com.example.clinchplayer.ui.components.ClinchLogo


// ====================================================================
// COLORS
// ====================================================================

private val ClinchYellow =
    Color(0xFFFFD600)

private val ClinchBlack =
    Color.Black

private val SoftBlack =
    Color(0xFF101114)


// ====================================================================
// MOVIES UI
//
// VALORES PRINCIPALES PARA CAMBIAR TAMAÑOS Y POSICIONES
// ====================================================================

private object MoviesUi {

    // ================================================================
    // LEFT PANEL
    // MISMO ANCHO DEL HOME
    // ================================================================

    val leftPanelWidth = 205.dp

    val leftPadding = 10.dp
    val rightPadding = 10.dp
    val topPadding = 5.dp
    val bottomPadding = 25.dp


    // ================================================================
    // LOGO
    // MISMO HOME
    // ================================================================

    val logoHeight = 150.dp


    // ================================================================
    // TEXTOS IZQUIERDOS
    // ================================================================

    val leftTextInset = 5.dp

    val menuTextSize = 12.sp

    val menuHeight = 36.dp

    val menuSpacing = 7.dp

    const val menuFocusedScale = 1.05f


    // ================================================================
    // CATEGORÍAS
    // ================================================================

    val categoriesLabelSize = 10.sp

    val categoriesLetterSpacing = 1.8.sp

    val categoryHeight = 36.dp

    val categoryTextSize = 12.sp


    // ================================================================
    // RIGHT CONTENT
    // ================================================================

    val contentLeft = 35.dp
    val contentRight = 45.dp
    val contentTop = 32.dp
    val contentBottom = 40.dp


    // ================================================================
    // SECTION TITLE
    // MISMO ESTILO HOME
    // ================================================================

    val sectionTitleSize = 17.sp

    val sectionHighlightSize = 10.sp

    val titleToGridSpace = 14.dp


    // ================================================================
    // POSTERS
    // MISMO HOME
    // ================================================================

    val posterWidth = 112.dp
    val posterHeight = 152.dp

    val posterCorner = 8.dp

    val posterTitleSize = 10.sp

    const val posterFocusedScale = 1.08f

    val posterPlayCircle = 40.dp

    val posterPlayIcon = 17.sp


    // ================================================================
    // GRID
    // ================================================================

    val gridMinimumCellWidth = 120.dp

    val posterHorizontalSpacing = 13.dp

    val posterVerticalSpacing = 18.dp
}


// ====================================================================
// MOVIES SCREEN
// ====================================================================

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MoviesScreen(
    viewModel: MoviesViewModel,
    onBack: () -> Unit,
    onMovieClick: (VodStream, String) -> Unit
) {

    val context =
        LocalContext.current.applicationContext


    // =================================================================
    // SETTINGS / PARENTAL CONTROL
    // =================================================================

    val settingsManager =
        remember {
            SettingsManager(
                context
            )
        }


    val activeProfileId =
        settingsManager.activeProfileId


    val visibleCategories =
        viewModel.categories.filterNot { category ->

            settingsManager.isCategoryHidden(
                contentType =
                    "movies",

                categoryId =
                    category.categoryId,

                profileId =
                    activeProfileId
            )
        }


    // =================================================================
    // MAKE SURE CURRENT CATEGORY IS VISIBLE
    // =================================================================

    LaunchedEffect(
        visibleCategories.map {
            it.categoryId
        },

        viewModel
            .selectedCategory
            ?.categoryId
    ) {

        val selectedId =
            viewModel
                .selectedCategory
                ?.categoryId


        val selectedIsVisible =
            visibleCategories.any {

                it.categoryId ==
                        selectedId
            }


        if (
            visibleCategories.isNotEmpty() &&
            !selectedIsVisible
        ) {

            viewModel.selectCategory(
                visibleCategories.first()
            )
        }
    }


    // =================================================================
    // MAIN SCREEN
    // =================================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                ClinchBlack
            )
    ) {

        // =============================================================
        // INITIAL LOADING
        // =============================================================

        if (
            viewModel.isLoadingCategories &&
            viewModel.categories.isEmpty()
        ) {

            Box(
                modifier =
                    Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        "Cargando películas...",

                    color =
                        ClinchYellow,

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            return@Box
        }


        // =============================================================
        // CATEGORY ERROR
        // =============================================================

        if (
            viewModel.categories.isEmpty() &&
            viewModel.errorMessage != null
        ) {

            Column(
                modifier =
                    Modifier.align(
                        Alignment.Center
                    ),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text =
                        viewModel.errorMessage
                            ?: "Error cargando películas",

                    color =
                        Color.White
                )


                Spacer(
                    Modifier.height(
                        20.dp
                    )
                )


                MoviesBackButton(
                    onClick =
                        onBack
                )
            }

            return@Box
        }


        // =============================================================
        // ALL CATEGORIES HIDDEN
        // =============================================================

        if (
            viewModel.categories.isNotEmpty() &&
            visibleCategories.isEmpty()
        ) {

            Column(
                modifier =
                    Modifier.align(
                        Alignment.Center
                    ),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text =
                        "No hay categorías de películas visibles para este perfil.",

                    color =
                        Color.White
                )


                Spacer(
                    Modifier.height(
                        20.dp
                    )
                )


                MoviesBackButton(
                    onClick =
                        onBack
                )
            }

            return@Box
        }


        // =============================================================
        // MAIN
        // =============================================================

        Row(
            modifier =
                Modifier.fillMaxSize()
        ) {

            // =========================================================
            // LEFT
            // =========================================================

            MoviesLeftPanel(
                categories =
                    visibleCategories,

                selectedCategory =
                    viewModel.selectedCategory,

                onBack =
                    onBack,

                onCategoryFocused = { category ->

                    if (
                        viewModel
                            .selectedCategory
                            ?.categoryId !=
                        category.categoryId
                    ) {

                        viewModel
                            .selectCategory(
                                category
                            )
                    }
                }
            )


            // =========================================================
            // SUBTLE DIVIDER
            // =========================================================

            Box(
                modifier = Modifier
                    .width(
                        1.dp
                    )
                    .fillMaxHeight()
                    .background(
                        Color.White.copy(
                            alpha = 0.08f
                        )
                    )
            )


            // =========================================================
            // RIGHT CONTENT
            // =========================================================

            Column(
                modifier = Modifier
                    .weight(
                        1f
                    )
                    .fillMaxHeight()
                    .padding(
                        start =
                            MoviesUi.contentLeft,

                        end =
                            MoviesUi.contentRight,

                        top =
                            MoviesUi.contentTop,

                        bottom =
                            MoviesUi.contentBottom
                    )
            ) {

                // =====================================================
                // TITLE
                // =====================================================

                MoviesSectionTitle(
                    title =
                        viewModel
                            .selectedCategory
                            ?.categoryName
                            ?.uppercase()
                            ?: "PELÍCULAS",

                    count =
                        viewModel.movies.size
                )


                Spacer(
                    Modifier.height(
                        MoviesUi.titleToGridSpace
                    )
                )


                // =====================================================
                // MOVIES
                // =====================================================

                when {

                    // =================================================
                    // MOVIES EXIST
                    // =================================================

                    viewModel.movies
                        .isNotEmpty() -> {

                        LazyVerticalGrid(

                            columns =
                                GridCells.Adaptive(
                                    minSize =
                                        MoviesUi.gridMinimumCellWidth
                                ),

                            modifier =
                                Modifier.fillMaxSize(),

                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    MoviesUi.posterHorizontalSpacing
                                ),

                            verticalArrangement =
                                Arrangement.spacedBy(
                                    MoviesUi.posterVerticalSpacing
                                ),

                            contentPadding =
                                PaddingValues(
                                    top = 5.dp,
                                    bottom = 35.dp,
                                    end = 10.dp
                                )
                        ) {

                            items(
                                items =
                                    viewModel.movies,

                                key = {
                                    it.streamId
                                }
                            ) { movie ->

                                HomeStyleMovieCard(
                                    movie =
                                        movie,

                                    onFocused = {

                                        // Mantiene el movie seleccionado
                                        // actualizado por si lo usamos
                                        // luego en detalles u otras funciones.

                                        viewModel
                                            .selectMovie(
                                                movie
                                            )
                                    },

                                    onClick = {

                                        viewModel
                                            .selectMovie(
                                                movie
                                            )


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
                    }


                    // =================================================
                    // LOADING MOVIES
                    // =================================================

                    viewModel.isLoadingMovies -> {

                        Box(
                            modifier =
                                Modifier.fillMaxSize(),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    "Cargando películas...",

                                color =
                                    ClinchYellow,

                                fontSize =
                                    13.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }


                    // =================================================
                    // ERROR
                    // =================================================

                    viewModel.errorMessage != null -> {

                        Box(
                            modifier =
                                Modifier.fillMaxSize(),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    viewModel.errorMessage
                                        ?: "Error cargando películas",

                                color =
                                    Color.White.copy(
                                        alpha = 0.70f
                                    ),

                                fontSize =
                                    13.sp
                            )
                        }
                    }


                    // =================================================
                    // EMPTY
                    // =================================================

                    else -> {

                        Box(
                            modifier =
                                Modifier.fillMaxSize(),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    "No hay películas en esta categoría",

                                color =
                                    Color.White.copy(
                                        alpha = 0.55f
                                    ),

                                fontSize =
                                    13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


// ====================================================================
// LEFT PANEL
//
// APARIENCIA BASADA EN HOME
// ====================================================================

@Composable
private fun MoviesLeftPanel(
    categories: List<VodCategory>,
    selectedCategory: VodCategory?,
    onBack: () -> Unit,
    onCategoryFocused: (VodCategory) -> Unit
) {

    Column(
        modifier = Modifier
            .width(
                MoviesUi.leftPanelWidth
            )
            .fillMaxHeight()
            .background(
                Color.Transparent
            )
            .padding(
                start =
                    MoviesUi.leftPadding,

                end =
                    MoviesUi.rightPadding,

                top =
                    MoviesUi.topPadding,

                bottom =
                    MoviesUi.bottomPadding
            )
    ) {

        // ============================================================
        // LOGO
        // SAME HOME
        // ============================================================

        ClinchLogo(
            modifier =
                Modifier.fillMaxWidth(),

            height =
                MoviesUi.logoHeight
        )



        // ============================================================
        // VOLVER
        // ============================================================

        MoviesBackButton(
            onClick =
                onBack
        )


        Spacer(
            Modifier.height(
                MoviesUi.menuSpacing
            )
        )

        // ============================================================
        // CATEGORY LIST
        // ============================================================

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(
                    1f
                ),

            contentPadding =
                PaddingValues(
                    bottom =
                        20.dp
                )
        ) {

            items(
                items =
                    categories,

                key = {
                    it.categoryId
                }
            ) { category ->

                MovieCategoryItem(
                    category =
                        category,

                    selected =
                        selectedCategory
                            ?.categoryId ==
                                category.categoryId,

                    onFocused = {

                        onCategoryFocused(
                            category
                        )
                    },

                    onClick = {

                        onCategoryFocused(
                            category
                        )
                    }
                )
            }
        }
    }
}


// ====================================================================
// LEFT TITLE
// ====================================================================

@Composable
private fun LeftTitle(
    text: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                MoviesUi.menuHeight
            )
            .padding(
                horizontal =
                    MoviesUi.leftTextInset
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text =
                text,

            color =
                Color.White.copy(
                    alpha = 0.90f
                ),

            fontSize =
                MoviesUi.menuTextSize,

            fontWeight =
                FontWeight.Bold
        )
    }
}


// ====================================================================
// BACK BUTTON
// SAME STYLE AS HOME MENU
// ====================================================================

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MoviesBackButton(
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }


    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                MoviesUi.menuFocusedScale
            } else {
                1f
            },

        animationSpec =
            tween(
                120
            ),

        label =
            "moviesBackScale"
    )


    Surface(
        onClick =
            onClick,

        modifier = Modifier
            .fillMaxWidth()
            .height(
                MoviesUi.menuHeight
            )
            .scale(
                scale
            )
            .onFocusChanged {

                focused =
                    it.isFocused
            },

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
                    horizontal =
                        MoviesUi.leftTextInset
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    "←  VOLVER",

                color =
                    if (focused) {

                        ClinchYellow

                    } else {

                        Color.White.copy(
                            alpha = 0.82f
                        )
                    },

                fontSize =
                    MoviesUi.menuTextSize,

                fontWeight =
                    if (focused) {

                        FontWeight.Bold

                    } else {

                        FontWeight.Medium
                    }
            )
        }
    }
}


// ====================================================================
// CATEGORY ITEM
//
// MISMO COMPORTAMIENTO VISUAL DEL HOME MENU
// ====================================================================

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MovieCategoryItem(
    category: VodCategory,
    selected: Boolean,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }


    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                MoviesUi.menuFocusedScale
            } else {
                1f
            },

        animationSpec =
            tween(
                120
            ),

        label =
            "categoryScale"
    )


    val active =
        focused ||
                selected


    Surface(
        onClick =
            onClick,

        modifier = Modifier
            .fillMaxWidth()
            .height(
                MoviesUi.categoryHeight
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


                if (
                    gainedFocus
                ) {

                    onFocused()
                }
            },

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
                    horizontal =
                        MoviesUi.leftTextInset
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    category.categoryName,

                color =
                    if (active) {

                        ClinchYellow

                    } else {

                        Color.White.copy(
                            alpha = 0.82f
                        )
                    },

                fontSize =
                    MoviesUi.categoryTextSize,

                fontWeight =
                    if (active) {

                        FontWeight.Bold

                    } else {

                        FontWeight.Medium
                    },

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}


// ====================================================================
// SECTION TITLE
//
// SAME STYLE AS HOME
// ====================================================================

@Composable
private fun MoviesSectionTitle(
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
                MoviesUi.sectionTitleSize,

            fontWeight =
                FontWeight.Bold,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )


        Spacer(
            Modifier.width(
                8.dp
            )
        )


        Text(
            text =
                "$count PELÍCULAS",

            color =
                ClinchYellow,

            fontSize =
                MoviesUi.sectionHighlightSize,

            fontWeight =
                FontWeight.Bold,

            letterSpacing =
                1.8.sp
        )
    }
}


// ====================================================================
// MOVIE CARD
//
// MISMAS MEDIDAS Y APARIENCIA DEL HOME
// ====================================================================

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun HomeStyleMovieCard(
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
                MoviesUi.posterFocusedScale
            } else {
                1f
            },

        animationSpec =
            tween(
                130
            ),

        label =
            "movieScale"
    )


    Column(
        modifier = Modifier
            .width(
                MoviesUi.posterWidth
            )
            .scale(
                scale
            )
    ) {

        // ============================================================
        // POSTER
        // ============================================================

        Surface(
            onClick =
                onClick,

            modifier = Modifier
                .width(
                    MoviesUi.posterWidth
                )
                .height(
                    MoviesUi.posterHeight
                )
                .onFocusChanged {

                    val gainedFocus =
                        it.isFocused &&
                                !focused


                    focused =
                        it.isFocused


                    if (
                        gainedFocus
                    ) {

                        onFocused()
                    }
                },

            shape =
                ClickableSurfaceDefaults.shape(
                    RoundedCornerShape(
                        MoviesUi.posterCorner
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
                modifier =
                    Modifier.fillMaxSize()
            ) {

                // ====================================================
                // IMAGE
                // ====================================================

                if (
                    !movie.streamIcon
                        .isNullOrBlank()
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
                            text =
                                "▶",

                            color =
                                ClinchYellow,

                            fontSize =
                                27.sp
                        )
                    }
                }


                // ====================================================
                // FOCUS
                // ====================================================

                if (
                    focused
                ) {

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
                                MoviesUi.posterPlayCircle
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
                            text =
                                "▶",

                            color =
                                Color.White,

                            fontSize =
                                MoviesUi.posterPlayIcon
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


        // ============================================================
        // TITLE
        // ============================================================

        Text(
            text =
                movie.name,

            color =
                if (
                    focused
                ) {

                    ClinchYellow

                } else {

                    Color.White.copy(
                        alpha = 0.88f
                    )
                },

            fontSize =
                MoviesUi.posterTitleSize,

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