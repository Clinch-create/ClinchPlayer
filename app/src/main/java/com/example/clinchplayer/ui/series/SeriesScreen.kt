package com.example.clinchplayer.ui.series

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
import com.example.clinchplayer.network.models.SeriesCategory
import com.example.clinchplayer.network.models.SeriesStream
import com.example.clinchplayer.ui.components.ClinchLogo

private val ClinchYellow = Color(0xFFFFD600)
private val CardBackground = Color(0xFF15171A)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SeriesScreen(
    viewModel: SeriesViewModel,
    onBack: () -> Unit,
    onSeriesClick: (SeriesStream) -> Unit,
    onAddSeries: (SeriesStream) -> Unit = {}
) {
    val context = LocalContext.current.applicationContext
    val settingsManager = remember { SettingsManager(context) }
    val activeProfileId = settingsManager.activeProfileId

    val visibleCategories = viewModel.categories.filterNot { category ->
        settingsManager.isCategoryHidden(
            contentType = "series",
            categoryId = category.categoryId,
            profileId = activeProfileId
        )
    }

    LaunchedEffect(
        visibleCategories.map { it.categoryId },
        viewModel.selectedCategory?.categoryId
    ) {
        val selectedId = viewModel.selectedCategory?.categoryId
        val selectedIsVisible = visibleCategories.any { it.categoryId == selectedId }

        if (visibleCategories.isNotEmpty() && !selectedIsVisible) {
            viewModel.selectCategory(visibleCategories.first())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (viewModel.isLoadingCategories && viewModel.categories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cargando series...",
                    color = ClinchYellow,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            return@Box
        }

        if (viewModel.categories.isEmpty() && viewModel.errorMessage != null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = viewModel.errorMessage ?: "Error cargando series",
                    color = Color.White,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                BackButton(onClick = onBack)
            }
            return@Box
        }

        if (viewModel.categories.isNotEmpty() && visibleCategories.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No hay categorías de series visibles para este perfil.",
                    color = Color.White,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                BackButton(onClick = onBack)
            }
            return@Box
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .width(205.dp)
                    .fillMaxHeight()
                    .background(Color.Transparent)
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 5.dp,
                        bottom = 25.dp
                    )
            ) {
                ClinchLogo(
                    modifier = Modifier.fillMaxWidth(),
                    height = 150.dp
                )

                BackButton(onClick = onBack)

                Spacer(modifier = Modifier.height(7.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(bottom = 30.dp)
                ) {
                    items(
                        items = visibleCategories,
                        key = { it.categoryId }
                    ) { category ->
                        SeriesCategoryItem(
                            category = category,
                            selected = viewModel.selectedCategory?.categoryId == category.categoryId,
                            onFocused = {
                                if (
                                    viewModel.selectedCategory?.categoryId !=
                                    category.categoryId
                                ) {
                                    viewModel.selectCategory(category)
                                }
                            },
                            onClick = {
                                if (
                                    viewModel.selectedCategory?.categoryId !=
                                    category.categoryId
                                ) {
                                    viewModel.selectCategory(category)
                                }
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(
                        Color.White.copy(alpha = 0.08f)
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(
                        start = 20.dp,
                        top = 5.dp,
                        end = 30.dp,
                        bottom = 24.dp
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = viewModel.selectedCategory?.categoryName ?: "SERIES",
                            color = Color.White,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (viewModel.series.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = "${viewModel.series.size} series disponibles",
                                color = Color.White.copy(alpha = 0.50f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    viewModel.series.isNotEmpty() -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 120.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(
                                start = 7.dp,
                                end = 25.dp,
                                top = 7.dp,
                                bottom = 30.dp
                            ),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = viewModel.series,
                                key = { it.seriesId }
                            ) { series ->
                                SeriesCard(
                                    series = series,
                                    onClick = {
                                        viewModel.selectSeries(series)
                                        onSeriesClick(series)
                                    }
                                )
                            }
                        }
                    }

                    viewModel.isLoadingSeries -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cargando series...",
                                color = ClinchYellow,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    viewModel.errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = viewModel.errorMessage ?: "Error cargando series",
                                color = Color.White.copy(alpha = 0.70f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay series en esta categoría",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeriesCategoryItem(
    category: SeriesCategory,
    selected: Boolean,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (focused) 1.05f else 1f,
        animationSpec = tween(120),
        label = "seriesCategoryScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .scale(scale)
            .onFocusChanged {
                val gainedFocus = it.isFocused && !focused
                focused = it.isFocused

                if (gainedFocus) {
                    onFocused()
                }
            },
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(0.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.categoryName,
                color = if (focused || selected) {
                    ClinchYellow
                } else {
                    Color.White.copy(alpha = 0.68f)
                },
                fontSize = 12.sp,
                fontWeight = if (focused || selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeriesCard(
    series: SeriesStream,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (focused) 1.08f else 1f,
        animationSpec = tween(130),
        label = "seriesCardScale"
    )

    Column(
        modifier = Modifier
            .width(112.dp)
            .scale(scale)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .width(112.dp)
                .height(152.dp)
                .onFocusChanged { focused = it.isFocused },
            shape = ClickableSurfaceDefaults.shape(
                RoundedCornerShape(8.dp)
            ),

            colors = ClickableSurfaceDefaults.colors(
                containerColor = CardBackground,
                focusedContainerColor = CardBackground,
                pressedContainerColor = CardBackground
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!series.cover.isNullOrBlank()) {
                    AsyncImage(
                        model = series.cover,
                        contentDescription = series.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CardBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 30.sp
                        )
                    }
                }

                if (focused) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(44.dp)
                            .background(
                                Color.Black.copy(alpha = 0.72f),
                                RoundedCornerShape(50)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶",
                            color = Color.White,
                            fontSize = 19.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = series.name,
            color = if (focused) {
                Color.White
            } else {
                Color.White.copy(alpha = 0.90f)
            },
            fontSize = 11.sp,
            fontWeight = if (focused) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (!series.rating.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "★ ${series.rating}",
                color = ClinchYellow.copy(alpha = 0.85f),
                fontSize = 9.sp
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun BackButton(
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (focused) 1.05f else 1f,
        animationSpec = tween(120),
        label = "seriesBackButtonScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .scale(scale)
            .onFocusChanged { focused = it.isFocused },
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent
        ),
        shape = ClickableSurfaceDefaults.shape(
            RoundedCornerShape(0.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                color = if (focused) {
                    ClinchYellow
                } else {
                    Color.White.copy(alpha = 0.72f)
                },
                fontSize = 16.sp
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = "VOLVER",
                color = if (focused) {
                    ClinchYellow
                } else {
                    Color.White.copy(alpha = 0.82f)
                },
                fontSize = 12.sp,
                fontWeight = if (focused) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
                maxLines = 1
            )
        }
    }
}
