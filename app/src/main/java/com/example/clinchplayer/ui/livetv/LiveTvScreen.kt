package com.example.clinchplayer.ui.livetv

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.example.clinchplayer.R
import com.example.clinchplayer.data.SettingsManager
import com.example.clinchplayer.network.models.LiveCategory
import com.example.clinchplayer.network.models.LiveStream
import com.example.clinchplayer.ui.components.ClinchLogo

private val ClinchYellow = Color(0xFFFFD600)
private val ClinchBlack = Color.Black
private val SoftWhite = Color.White.copy(alpha = 0.72f)
private val DividerColor = Color.White.copy(alpha = 0.08f)


@Composable
private fun liveText(es: String, en: String): String {
    val configuration = LocalConfiguration.current
    val language = configuration.locales[0]?.language ?: "es"
    return if (language == "en") en else es
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LiveTvScreen(
    viewModel: LiveTvViewModel,
    onBack: () -> Unit,
    onPlayStream: (LiveStream, String) -> Unit,
    onFavoritesClick: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context =
        LocalContext.current.applicationContext

    val settingsManager =
        remember {
            SettingsManager(context)
        }

    val activeProfileId =
        settingsManager.activeProfileId

    /*
     * ============================================================
     * PARENTAL CONTROL
     * ============================================================
     *
     * IMPORTANTE:
     * viewModel.categories conserva TODAS las categorías del servidor.
     * visibleCategories es la lista que realmente mostramos.
     *
     * Cuando Parental Control está ON para el perfil activo,
     * cualquier categoría marcada en Settings desaparece de la lista.
     */
    val visibleCategories =
        viewModel.categories.filterNot { category ->

            settingsManager.isCategoryHidden(
                contentType = "live",
                categoryId = category.categoryId,
                profileId = activeProfileId
            )
        }

    /*
     * Si la categoría que estaba seleccionada fue bloqueada,
     * movemos automáticamente la selección a la primera visible.
     */
    LaunchedEffect(
        visibleCategories.map { it.categoryId },
        viewModel.selectedCategory?.categoryId
    ) {
        val selectedId =
            viewModel.selectedCategory?.categoryId

        val selectedIsVisible =
            visibleCategories.any {
                it.categoryId == selectedId
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

    // Cuando cambia la categoría, dejamos listo el primer canal
    // para que el preview no se quede mostrando el canal anterior.
    LaunchedEffect(viewModel.streams) {
        val first = viewModel.streams.firstOrNull()

        if (first != null) {
            val currentBelongsToList =
                viewModel.streams.any {
                    it.streamId == viewModel.selectedStream?.streamId
                }

            if (!currentBelongsToList) {
                viewModel.selectStream(first)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClinchBlack)
    ) {
        when {
            viewModel.isLoadingCategories &&
                    viewModel.categories.isEmpty() -> {

                LoadingState()
            }

            viewModel.errorMessage != null &&
                    viewModel.categories.isEmpty() -> {

                ErrorState(
                    message = viewModel.errorMessage
                        ?: liveText("Error cargando TV en vivo", "Error loading Live TV"),
                    onRetry = viewModel::loadData,
                    onBack = onBack
                )
            }

            else -> {
                LiveTvContent(
                    viewModel = viewModel,
                    categories = visibleCategories,
                    streamFormat = settingsManager.streamFormat,
                    userAgent =
                        settingsManager.userAgent
                            .ifBlank {
                                "ClinchPlayer/1.0"
                            },
                    onPlayStream = onPlayStream,
                    onFavoritesClick = onFavoritesClick
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LiveTvContent(
    viewModel: LiveTvViewModel,
    categories: List<LiveCategory>,
    streamFormat: String,
    userAgent: String,
    onPlayStream: (LiveStream, String) -> Unit,
    onFavoritesClick: () -> Unit
) {
    val hasVisibleCategories =
        categories.isNotEmpty()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 14.dp,
                end = 24.dp,
                top = 10.dp,
                bottom = 16.dp
            )
    ) {
        // ============================================================
        // IZQUIERDA: LOGO + FAVORITOS + CATEGORÍAS
        // ============================================================
        Column(
            modifier = Modifier
                .width(230.dp)
                .fillMaxHeight()
        ) {
            ClinchLogo(
                modifier = Modifier.fillMaxWidth(),
                height = 125.dp
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            FavoritesButton(
                onClick = onFavoritesClick
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DividerColor)
            )

            Spacer(Modifier.height(16.dp))

            SectionHeader(
                title = stringResource(R.string.categories).uppercase(),
                subtitle = categories.size.toString()
            )

            Spacer(Modifier.height(8.dp))

            if (categories.isEmpty()) {
                Text(
                    text = liveText("No hay categorías visibles.", "There are no visible categories."),
                    color = SoftWhite,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(
                        top = 12.dp
                    )
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        top = 2.dp,
                        bottom = 18.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
                ) {
                    items(
                        items = categories,
                        key = { it.categoryId }
                    ) { category ->
                        CategoryItem(
                            category = category,
                            isSelected =
                                viewModel.selectedCategory
                                    ?.categoryId ==
                                        category.categoryId,
                            onFocused = {
                                viewModel.selectCategory(
                                    category
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(18.dp))

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(DividerColor)
        )

        Spacer(Modifier.width(20.dp))

        // ============================================================
        // CENTRO: CANALES
        // ============================================================
        Column(
            modifier = Modifier
                .width(355.dp)
                .fillMaxHeight()
        ) {
            SectionHeader(
                title = liveText("CANALES", "CHANNELS"),
                subtitle =
                    when {
                        !hasVisibleCategories ->
                            "0"

                        viewModel.isLoadingStreams ->
                            liveText("CARGANDO", "LOADING")

                        else ->
                            viewModel.streams.size.toString()
                    }
            )

            Spacer(Modifier.height(10.dp))

            when {
                !hasVisibleCategories -> {
                    Text(
                        text =
                            liveText("Activa al menos una categoría visible en Control Parental.", "Enable at least one visible category in Parental Control."),
                        color = SoftWhite,
                        fontSize = 12.sp,
                        modifier =
                            Modifier.padding(
                                top = 12.dp
                            )
                    )
                }

                viewModel.isLoadingStreams -> {
                    Text(
                        text = liveText("Cargando canales...", "Loading channels..."),
                        color = ClinchYellow,
                        fontSize = 12.sp,
                        modifier =
                            Modifier.padding(
                                top = 12.dp
                            )
                    )
                }

                viewModel.streams.isEmpty() -> {
                    Text(
                        text =
                            liveText("No hay canales en esta categoría.", "There are no channels in this category."),
                        color = SoftWhite,
                        fontSize = 12.sp,
                        modifier =
                            Modifier.padding(
                                top = 12.dp
                            )
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding =
                            PaddingValues(
                                top = 2.dp,
                                bottom = 18.dp
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                3.dp
                            )
                    ) {
                        items(
                            items = viewModel.streams,
                            key = { it.streamId }
                        ) { stream ->
                            StreamItem(
                                stream = stream,
                                isSelected =
                                    viewModel.selectedStream
                                        ?.streamId ==
                                            stream.streamId,
                                onFocused = {
                                    viewModel.selectStream(
                                        stream
                                    )
                                },
                                onClick = {
                                    viewModel.selectStream(
                                        stream
                                    )

                                    val url =
                                        viewModel
                                            .getStreamUrl(
                                                stream,
                                                streamFormat
                                            )

                                    if (
                                        url.isNotBlank()
                                    ) {
                                        onPlayStream(
                                            stream,
                                            url
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.width(24.dp))

        // ============================================================
        // DERECHA: PREVIEW
        // ============================================================
        val previewStream =
            if (hasVisibleCategories) {
                viewModel.selectedStream
            } else {
                null
            }

        PreviewPanel(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            stream = previewStream,
            userAgent = userAgent,
            streamUrl =
                previewStream
                    ?.let(
                        { selected ->
                            viewModel.getStreamUrl(
                                selected,
                                streamFormat
                            )
                        }
                    )
                    .orEmpty(),
            onPlay = {
                val stream =
                    previewStream
                        ?: return@PreviewPanel

                val url =
                    viewModel.getStreamUrl(
                        stream,
                        streamFormat
                    )

                if (url.isNotBlank()) {
                    onPlayStream(
                        stream,
                        url
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FavoritesButton(
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                1.06f
            } else {
                1f
            },
        animationSpec = tween(130),
        label = "liveFavoritesScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
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
            RoundedCornerShape(6.dp)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "♡",
                color =
                    if (focused) {
                        ClinchYellow
                    } else {
                        Color.White
                    },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = stringResource(R.string.favorites).uppercase(),
                color =
                    if (focused) {
                        ClinchYellow
                    } else {
                        Color.White
                    },
                fontSize = 12.sp,
                fontWeight =
                    if (focused) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },
                letterSpacing = 0.7.sp
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = "• $subtitle",
            color = ClinchYellow,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryItem(
    category: LiveCategory,
    isSelected: Boolean,
    onFocused: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                1.045f
            } else {
                1f
            },
        animationSpec = tween(120),
        label = "liveCategoryScale"
    )

    Surface(
        onClick = onFocused,
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .scale(scale)
            .onFocusChanged {
                val gainedFocus =
                    it.isFocused &&
                            !focused

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
        shape = ClickableSurfaceDefaults.shape(
            RoundedCornerShape(5.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 8.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    if (isSelected) {
                        "•"
                    } else {
                        " "
                    },
                color = ClinchYellow,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(7.dp))

            Text(
                text =
                    category.categoryName,
                color =
                    when {
                        focused ->
                            ClinchYellow

                        isSelected ->
                            Color.White

                        else ->
                            Color.White.copy(
                                alpha = 0.78f
                            )
                    },
                fontSize = 12.sp,
                fontWeight =
                    if (
                        focused ||
                        isSelected
                    ) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StreamItem(
    stream: LiveStream,
    isSelected: Boolean,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                1.045f
            } else {
                1f
            },
        animationSpec = tween(120),
        label = "liveChannelScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .scale(scale)
            .onFocusChanged {
                val gainedFocus =
                    it.isFocused &&
                            !focused

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
        shape = ClickableSurfaceDefaults.shape(
            RoundedCornerShape(6.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 4.dp,
                    end = 8.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        Color.White.copy(
                            alpha = 0.045f
                        ),
                        RoundedCornerShape(
                            6.dp
                        )
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                if (
                    !stream.streamIcon
                        .isNullOrBlank()
                ) {
                    AsyncImage(
                        model =
                            stream.streamIcon,
                        contentDescription =
                            stream.name,
                        contentScale =
                            ContentScale.Fit,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(
                                    4.dp
                                )
                    )
                } else {
                    Text(
                        text = "▣",
                        color =
                            if (focused) {
                                ClinchYellow
                            } else {
                                Color.White.copy(
                                    alpha = 0.65f
                                )
                            },
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.width(11.dp))

            Text(
                text = stream.name,
                color =
                    when {
                        focused ->
                            ClinchYellow

                        isSelected ->
                            Color.White

                        else ->
                            Color.White.copy(
                                alpha = 0.82f
                            )
                    },
                fontSize = 12.sp,
                fontWeight =
                    if (
                        focused ||
                        isSelected
                    ) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PreviewPanel(
    modifier: Modifier,
    stream: LiveStream?,
    userAgent: String,
    streamUrl: String,
    onPlay: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement =
            Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.live_tv).uppercase(),
            color = ClinchYellow,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(7.dp))

        Text(
            text =
                stream?.name
                    ?: liveText("Selecciona un canal", "Select a channel"),
            color = Color.White,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(14.dp))

        if (
            stream != null &&
            streamUrl.isNotBlank()
        ) {
            LivePreview(
                url = streamUrl,
                userAgent = userAgent,
                onFullScreen = onPlay
            )

            Spacer(Modifier.height(14.dp))

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text = "▶",
                    color = ClinchYellow,
                    fontSize = 12.sp
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text =
                        liveText("Pulsa OK para pantalla completa", "Press OK for full screen"),
                    color = SoftWhite,
                    fontSize = 11.sp
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        16f / 9f
                    )
                    .background(
                        Color(0xFF0D0D0D),
                        RoundedCornerShape(
                            9.dp
                        )
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
                            "CLINCH PLAYER",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        Modifier.height(
                            5.dp
                        )
                    )

                    Text(
                        text =
                            liveText("SELECCIONA UN CANAL", "SELECT A CHANNEL"),
                        color =
                            ClinchYellow,
                        fontSize = 11.sp,
                        fontWeight =
                            FontWeight.Bold,
                        letterSpacing =
                            1.2.sp
                    )
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun LivePreview(
    url: String,
    userAgent: String = "ClinchPlayer/1.0",
    onFullScreen: () -> Unit
) {
    val context =
        LocalContext.current

    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                1.025f
            } else {
                1f
            },
        animationSpec = tween(130),
        label = "livePreviewScale"
    )

    val exoPlayer =
        remember(
            url,
            userAgent
        ) {
            Log.d(
                "LiveTvScreen",
                "Preview started for URL: $url"
            )

            val dataSourceFactory =
                DefaultHttpDataSource
                    .Factory()
                    .setAllowCrossProtocolRedirects(
                        true
                    )
                    .setUserAgent(
                        userAgent
                    )
                    .setConnectTimeoutMs(
                        15000
                    )
                    .setReadTimeoutMs(
                        30000
                    )

            val mediaSourceFactory =
                DefaultMediaSourceFactory(
                    context
                )
                    .setDataSourceFactory(
                        dataSourceFactory
                    )

            ExoPlayer
                .Builder(context)
                .setMediaSourceFactory(
                    mediaSourceFactory
                )
                .build()
                .apply {
                    setMediaItem(
                        MediaItem.fromUri(
                            url
                        )
                    )

                    volume = 0f
                    playWhenReady = true
                    prepare()

                    addListener(
                        object :
                            Player.Listener {

                            override fun onPlaybackStateChanged(
                                state: Int
                            ) {
                                val stateText =
                                    when (state) {
                                        Player.STATE_BUFFERING ->
                                            "BUFFERING"

                                        Player.STATE_READY ->
                                            "READY"

                                        Player.STATE_ENDED ->
                                            "ENDED"

                                        Player.STATE_IDLE ->
                                            "IDLE"

                                        else ->
                                            "UNKNOWN"
                                    }

                                Log.d(
                                    "LiveTvScreen",
                                    "ExoPlayer State: $stateText"
                                )
                            }

                            override fun onIsPlayingChanged(
                                isPlaying: Boolean
                            ) {
                                Log.d(
                                    "LiveTvScreen",
                                    "ExoPlayer isPlaying: $isPlaying"
                                )
                            }

                            override fun onPlayerError(
                                error:
                                PlaybackException
                            ) {
                                val cause =
                                    error.cause

                                val message =
                                    cause?.message
                                        ?: error.message

                                Log.e(
                                    "LiveTvScreen",
                                    "ExoPlayer Error: ${error.errorCodeName} ($message)"
                                )

                                if (
                                    cause is
                                            HttpDataSource
                                            .InvalidResponseCodeException
                                ) {
                                    Log.e(
                                        "LiveTvScreen",
                                        "HTTP Response Code: ${cause.responseCode}"
                                    )
                                }
                            }

                            override fun onVideoSizeChanged(
                                videoSize:
                                VideoSize
                            ) {
                                Log.d(
                                    "LiveTvScreen",
                                    "Video Size: ${videoSize.width}x${videoSize.height}"
                                )
                            }
                        }
                    )
                }
        }

    DisposableEffect(exoPlayer) {
        onDispose {
            try {
                exoPlayer.stop()
                exoPlayer.clearVideoSurface()
                exoPlayer.release()
            } catch (_: Exception) {
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(
                16f / 9f
            )
            .scale(scale)
            .background(
                Color.Black,
                RoundedCornerShape(
                    9.dp
                )
            ),
        contentAlignment =
            Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx)
                    .apply {
                        useController =
                            false
                        resizeMode =
                            AspectRatioFrameLayout
                                .RESIZE_MODE_FIT
                        player =
                            exoPlayer
                    }
            },
            update = {
                it.player =
                    exoPlayer
            },
            modifier =
                Modifier.fillMaxSize()
        )

        Surface(
            onClick = onFullScreen,
            modifier = Modifier
                .fillMaxSize()
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
                                    0.035f
                            ),
                        pressedContainerColor =
                            Color.Transparent
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
                    Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = "CLINCH PLAYER",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Text(
                text =
                    liveText("CARGANDO TV EN VIVO...", "LOADING LIVE TV..."),
                color = ClinchYellow,
                fontSize = 11.sp,
                fontWeight =
                    FontWeight.Bold,
                letterSpacing =
                    1.5.sp
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text =
                    liveText("NO SE PUDO CARGAR TV EN VIVO", "COULD NOT LOAD LIVE TV"),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(
                    8.dp
                )
            )

            Text(
                text = message,
                color = SoftWhite,
                fontSize = 12.sp
            )

            Spacer(
                Modifier.height(
                    18.dp
                )
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        18.dp
                    )
            ) {
                SimpleAction(
                    text =
                        liveText("↻ REINTENTAR", "↻ RETRY"),
                    onClick =
                        onRetry
                )

                SimpleAction(
                    text =
                        "← ${stringResource(R.string.back).uppercase()}",
                    onClick =
                        onBack
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SimpleAction(
    text: String,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                1.06f
            } else {
                1f
            },
        animationSpec = tween(120),
        label = "liveActionScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
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
                        5.dp
                    )
                )
    ) {
        Text(
            text = text,
            color =
                if (focused) {
                    ClinchYellow
                } else {
                    Color.White
                },
            fontSize = 12.sp,
            fontWeight =
                FontWeight.Bold,
            modifier =
                Modifier.padding(
                    horizontal =
                        12.dp,
                    vertical =
                        8.dp
                )
        )
    }
}
