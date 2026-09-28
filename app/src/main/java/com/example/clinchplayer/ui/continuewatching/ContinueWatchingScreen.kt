package com.example.clinchplayer.ui.continuewatching

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.example.clinchplayer.R
import com.example.clinchplayer.data.ContinueWatchingEpisode
import com.example.clinchplayer.data.ContinueWatchingItem
import com.example.clinchplayer.data.ContinueWatchingManager
import com.example.clinchplayer.data.ContinueWatchingType
import com.example.clinchplayer.network.models.VodStream
import com.example.clinchplayer.ui.components.ClinchLogo
import kotlinx.coroutines.delay

private val ClinchYellow = Color(0xFFFFD600)
private val ClinchBlack = Color.Black
private val SoftBlack = Color(0xFF101114)


@Composable
private fun continueText(es: String, en: String): String {
    val configuration = LocalConfiguration.current
    val language = configuration.locales[0]?.language ?: "es"
    return if (language == "en") en else es
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ContinueWatchingScreen(
    onBack: () -> Unit,
    onMovieClick: (VodStream) -> Unit,
    onEpisodeClick: (ContinueWatchingEpisode) -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val manager = remember {
        ContinueWatchingManager(context)
    }

    var entries by remember {
        mutableStateOf<List<ContinueWatchingItem>>(emptyList())
    }

    LaunchedEffect(Unit) {
        while (true) {
            entries = manager.getAllItems()
            delay(1500)
        }
    }

    val visibleItems = entries.filter { item ->
        when (item.type) {
            ContinueWatchingType.MOVIE -> {
                val entry = item.movie
                entry != null &&
                        entry.streamId > 0 &&
                        entry.movie != null &&
                        entry.movie.name.isNotBlank()
            }

            ContinueWatchingType.EPISODE -> {
                val entry = item.episode
                entry != null &&
                        entry.series != null &&
                        entry.episode != null &&
                        entry.url.isNotBlank()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClinchBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 14.dp,
                    end = 32.dp,
                    top = 10.dp,
                    bottom = 20.dp
                )
        ) {
            // ============================================================
            // LEFT
            // ============================================================
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .fillMaxHeight()
            ) {
                ClinchLogo(
                    modifier = Modifier.fillMaxWidth(),
                    height = 130.dp
                )

                ContinueBackButton(
                    onClick = onBack
                )
            }

            // ============================================================
            // CONTENT
            // ============================================================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(
                        start = 28.dp,
                        top = 42.dp
                    )
            ) {
                Text(
                    text = stringResource(R.string.continue_watching).uppercase(),
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = continueText("Continúa tus películas y episodios desde donde los dejaste.", "Continue your movies and episodes from where you left off."),
                    color = Color.White.copy(alpha = 0.58f),
                    fontSize = 12.sp
                )

                Spacer(
                    modifier = Modifier.height(28.dp)
                )

                if (visibleItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = continueText("Todavía no tienes contenido pendiente.", "You don’t have anything to continue watching yet."),
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = 6.dp,
                            end = 50.dp,
                            top = 8.dp,
                            bottom = 20.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        items(
                            items = visibleItems,
                            key = { item ->
                                when (item.type) {
                                    ContinueWatchingType.MOVIE ->
                                        "movie_${item.movie?.streamId ?: item.updatedAt}"

                                    ContinueWatchingType.EPISODE ->
                                        "episode_${item.episode?.seriesId}_${item.episode?.episodeId}"
                                }
                            }
                        ) { item ->
                            ContinueWatchingCard(
                                item = item,
                                onClick = {
                                    when (item.type) {
                                        ContinueWatchingType.MOVIE -> {
                                            item.movie
                                                ?.movie
                                                ?.let(onMovieClick)
                                        }

                                        ContinueWatchingType.EPISODE -> {
                                            item.episode
                                                ?.let(onEpisodeClick)
                                        }
                                    }
                                }
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
private fun ContinueWatchingCard(
    item: ContinueWatchingItem,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = if (focused) 1.08f else 1f,
        animationSpec = tween(130),
        label = "continueWatchingCardScale"
    )

    val movieEntry = item.movie
    val episodeEntry = item.episode

    val title =
        when (item.type) {
            ContinueWatchingType.MOVIE ->
                movieEntry?.movie?.name ?: continueText("Película", "Movie")

            ContinueWatchingType.EPISODE ->
                episodeEntry?.series?.name ?: continueText("Serie", "Series")
        }

    val subtitle =
        when (item.type) {
            ContinueWatchingType.MOVIE ->
                continueText("PELÍCULA", "MOVIE")

            ContinueWatchingType.EPISODE -> {
                val season = episodeEntry?.seasonNumber ?: ""
                val episodeNumber = episodeEntry?.episodeNumber ?: 1

                if (season.isNotBlank()) {
                    "T$season  •  E$episodeNumber"
                } else {
                    continueText("EPISODIO $episodeNumber", "EPISODE $episodeNumber")
                }
            }
        }

    val image =
        when (item.type) {
            ContinueWatchingType.MOVIE ->
                movieEntry?.movie?.streamIcon

            ContinueWatchingType.EPISODE ->
                episodeEntry
                    ?.episode
                    ?.info
                    ?.movieImage
                    ?.takeIf { it.isNotBlank() }
                    ?: episodeEntry?.series?.cover
        }

    val progress =
        when (item.type) {
            ContinueWatchingType.MOVIE ->
                movieEntry?.progress ?: 0f

            ContinueWatchingType.EPISODE ->
                episodeEntry?.progress ?: 0f
        }.coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .width(145.dp)
            .scale(scale)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(202.dp)
                .onFocusChanged {
                    focused = it.isFocused
                },
            colors = ClickableSurfaceDefaults.colors(
                containerColor = SoftBlack,
                focusedContainerColor = SoftBlack,
                pressedContainerColor = SoftBlack
            ),
            shape = ClickableSurfaceDefaults.shape(
                RoundedCornerShape(8.dp)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (!image.isNullOrBlank()) {
                    AsyncImage(
                        model = image,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SoftBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 30.sp
                        )
                    }
                }

                // Progress bar.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(Color.White.copy(alpha = 0.20f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(5.dp)
                            .background(ClinchYellow)
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Text(
            text = title,
            color = if (focused) ClinchYellow else Color.White,
            fontSize = 12.sp,
            fontWeight = if (focused) FontWeight.Bold else FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(
            modifier = Modifier.height(3.dp)
        )

        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.50f),
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ContinueBackButton(
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = if (focused) 1.08f else 1f,
        animationSpec = tween(120),
        label = "continueWatchingBackScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(44.dp)
            .scale(scale)
            .onFocusChanged {
                focused = it.isFocused
            },
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent
        ),
        shape = ClickableSurfaceDefaults.shape(
            RoundedCornerShape(0.dp)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "←  ${stringResource(R.string.back).uppercase()}",
                color = if (focused) {
                    ClinchYellow
                } else {
                    Color.White.copy(alpha = 0.78f)
                },
                fontSize = 15.sp,
                fontWeight = if (focused) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                }
            )
        }
    }
}
