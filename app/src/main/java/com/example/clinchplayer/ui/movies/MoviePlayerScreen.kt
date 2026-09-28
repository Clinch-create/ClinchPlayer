package com.example.clinchplayer.ui.movies

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.clinchplayer.data.FavoritesManager
import com.example.clinchplayer.data.SettingsManager
import com.example.clinchplayer.network.models.VodStream
import kotlinx.coroutines.delay
import java.util.Locale

private val ClinchYellow = Color(0xFFFFD600)

private data class PlayerTrackOption(
    val group: Tracks.Group,
    val trackIndex: Int,
    val name: String,
    val language: String?,
    val selected: Boolean
)

private enum class TrackMenuType {
    AUDIO,
    SUBTITLES
}

@OptIn(UnstableApi::class)
@Composable
fun MoviePlayerScreen(
    movie: VodStream,
    url: String,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val settingsManager =
        remember {
            SettingsManager(
                context.applicationContext
            )
        }

    val playerUserAgent =
        settingsManager.userAgent
            .ifBlank {
                "ClinchPlayer/1.0"
            }

    val playerBufferSeconds =
        settingsManager.bufferSize
            .coerceIn(
                5,
                60
            )

    val subtitlesEnabledByDefault =
        settingsManager.subtitlesEnabled

    // ================================================================
    // FAVORITOS
    // ================================================================

    val favoritesManager = remember {
        FavoritesManager(context)
    }

    var isFavorite by remember(movie.streamId) {
        mutableStateOf(
            favoritesManager.isFavorite(movie)
        )
    }

    // ================================================================
    // PLAYER STATES
    // ================================================================

    var controlsVisible by remember {
        mutableStateOf(true)
    }

    var isPlaying by remember {
        mutableStateOf(false)
    }

    var currentPosition by remember {
        mutableLongStateOf(0L)
    }

    var duration by remember {
        mutableLongStateOf(0L)
    }

    var interactionId by remember {
        mutableIntStateOf(0)
    }

    var currentTracks by remember {
        mutableStateOf(Tracks.EMPTY)
    }

    var trackMenu by remember {
        mutableStateOf<TrackMenuType?>(null)
    }

    val playFocusRequester = remember {
        FocusRequester()
    }

    // ================================================================
    // EXOPLAYER
    // ================================================================

    val exoPlayer =
        remember(
            url,
            playerUserAgent,
            playerBufferSeconds,
            subtitlesEnabledByDefault
        ) {

            val httpFactory =
                DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(
                        true
                    )
                    .setUserAgent(
                        playerUserAgent
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
                        httpFactory
                    )

            val minBufferMs =
                playerBufferSeconds *
                        1000

            val maxBufferMs =
                (
                        playerBufferSeconds *
                                2 *
                                1000
                        )
                    .coerceAtLeast(
                        minBufferMs
                    )

            val loadControl =
                DefaultLoadControl
                    .Builder()
                    .setBufferDurationsMs(
                        minBufferMs,
                        maxBufferMs,
                        1500,
                        2500
                    )
                    .build()

            ExoPlayer
                .Builder(
                    context
                )
                .setMediaSourceFactory(
                    mediaSourceFactory
                )
                .setLoadControl(
                    loadControl
                )
                .build()
                .apply {

                    volume =
                        1f

                    if (
                        !subtitlesEnabledByDefault
                    ) {
                        trackSelectionParameters =
                            trackSelectionParameters
                                .buildUpon()
                                .setTrackTypeDisabled(
                                    C.TRACK_TYPE_TEXT,
                                    true
                                )
                                .build()
                    }

                    setMediaItem(
                        MediaItem
                            .Builder()
                            .setUri(
                                url
                            )
                            .build()
                    )

                    prepare()

                    playWhenReady =
                        true
                }
        }

    // ================================================================
    // PLAYER LISTENER
    // ================================================================

    DisposableEffect(exoPlayer) {

        val listener =
            object : Player.Listener {

                override fun onIsPlayingChanged(
                    playing: Boolean
                ) {
                    isPlaying = playing
                }

                override fun onTracksChanged(
                    tracks: Tracks
                ) {
                    currentTracks = tracks
                }
            }

        exoPlayer.addListener(listener)

        currentTracks =
            exoPlayer.currentTracks

        onDispose {

            exoPlayer.removeListener(listener)

            try {
                exoPlayer.stop()
                exoPlayer.clearVideoSurface()
                exoPlayer.release()
            } catch (_: Exception) {
            }
        }
    }

    // ================================================================
    // ACTUALIZAR TIEMPO
    // ================================================================

    LaunchedEffect(exoPlayer) {

        while (true) {

            currentPosition =
                exoPlayer.currentPosition
                    .coerceAtLeast(0L)

            val playerDuration =
                exoPlayer.duration

            duration =
                if (playerDuration > 0) {
                    playerDuration
                } else {
                    0L
                }

            delay(500)
        }
    }

    // ================================================================
    // OCULTAR CONTROLES
    // ================================================================

    LaunchedEffect(
        controlsVisible,
        interactionId,
        trackMenu
    ) {

        if (
            controlsVisible &&
            trackMenu == null
        ) {

            delay(5000)

            controlsVisible = false
        }
    }

    // ================================================================
    // FOCO PLAY / PAUSE
    // ================================================================

    LaunchedEffect(
        controlsVisible,
        trackMenu
    ) {

        if (
            controlsVisible &&
            trackMenu == null
        ) {

            delay(150)

            try {
                playFocusRequester.requestFocus()
            } catch (_: Exception) {
            }
        }
    }

    // ================================================================
    // BACK
    // ================================================================

    BackHandler {

        when {

            trackMenu != null -> {

                trackMenu = null
                controlsVisible = true
                interactionId++
            }

            controlsVisible -> {

                controlsVisible = false
            }

            else -> {

                onBack()
            }
        }
    }

    // ================================================================
    // PLAYER
    // ================================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { composeEvent ->

                val event =
                    composeEvent.nativeKeyEvent

                if (
                    event.action !=
                    KeyEvent.ACTION_DOWN
                ) {
                    return@onKeyEvent false
                }

                if (trackMenu != null) {
                    return@onKeyEvent false
                }

                if (!controlsVisible) {

                    when (event.keyCode) {

                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_DPAD_UP,
                        KeyEvent.KEYCODE_DPAD_DOWN,
                        KeyEvent.KEYCODE_DPAD_LEFT,
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {

                            controlsVisible = true
                            interactionId++

                            return@onKeyEvent true
                        }
                    }
                }

                when (event.keyCode) {

                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {

                        toggleMoviePlayPause(exoPlayer)

                        controlsVisible = true
                        interactionId++

                        true
                    }

                    KeyEvent.KEYCODE_MEDIA_PLAY -> {

                        exoPlayer.play()

                        controlsVisible = true
                        interactionId++

                        true
                    }

                    KeyEvent.KEYCODE_MEDIA_PAUSE -> {

                        exoPlayer.pause()

                        controlsVisible = true
                        interactionId++

                        true
                    }

                    else -> false
                }
            }
            .focusable()
    ) {

        // ============================================================
        // VIDEO
        // ============================================================

        AndroidView(

            factory = { ctx ->

                PlayerView(ctx).apply {

                    player = exoPlayer

                    useController = false

                    resizeMode =
                        AspectRatioFrameLayout.RESIZE_MODE_FIT

                    setShutterBackgroundColor(
                        android.graphics.Color.BLACK
                    )

                    keepScreenOn = true
                }
            },

            update = { playerView ->

                if (
                    playerView.player !==
                    exoPlayer
                ) {

                    playerView.player =
                        exoPlayer
                }
            },

            modifier =
                Modifier.fillMaxSize()
        )

        // ============================================================
        // CONTROLES
        // ============================================================

        if (controlsVisible) {

            MovieControls(
                movieName = movie.name,

                exoPlayer = exoPlayer,

                isPlaying = isPlaying,

                currentPosition =
                    currentPosition,

                duration =
                    duration,

                currentTracks =
                    currentTracks,

                playFocusRequester =
                    playFocusRequester,

                isFavorite =
                    isFavorite,

                onFavoriteClick = {

                    isFavorite =
                        favoritesManager
                            .toggleFavorite(movie)

                    controlsVisible = true
                    interactionId++
                },

                onAudioClick = {

                    trackMenu =
                        TrackMenuType.AUDIO

                    interactionId++
                },

                onSubtitlesClick = {

                    trackMenu =
                        TrackMenuType.SUBTITLES

                    interactionId++
                },

                onInteraction = {

                    controlsVisible = true
                    interactionId++
                }
            )
        }

        // ============================================================
        // AUDIO / SUBTITLE MENU
        // ============================================================

        trackMenu?.let { menu ->

            TrackSelectionMenu(
                title =
                    if (
                        menu ==
                        TrackMenuType.AUDIO
                    ) {
                        "AUDIO"
                    } else {
                        "SUBTÍTULOS"
                    },

                options =
                    getTrackOptions(
                        tracks = currentTracks,

                        trackType =
                            if (
                                menu ==
                                TrackMenuType.AUDIO
                            ) {
                                C.TRACK_TYPE_AUDIO
                            } else {
                                C.TRACK_TYPE_TEXT
                            }
                    ),

                subtitles =
                    menu ==
                            TrackMenuType.SUBTITLES,

                subtitlesDisabled =
                    exoPlayer
                        .trackSelectionParameters
                        .disabledTrackTypes
                        .contains(
                            C.TRACK_TYPE_TEXT
                        ),

                onSelect = { option ->

                    selectTrack(
                        player = exoPlayer,
                        option = option
                    )

                    currentTracks =
                        exoPlayer.currentTracks

                    trackMenu = null
                    controlsVisible = true
                    interactionId++
                },

                onDisableSubtitles = {

                    disableSubtitles(
                        exoPlayer
                    )

                    currentTracks =
                        exoPlayer.currentTracks

                    trackMenu = null
                    controlsVisible = true
                    interactionId++
                },

                onClose = {

                    trackMenu = null
                    controlsVisible = true
                    interactionId++
                }
            )
        }
    }
}

// ====================================================================
// CONTROLES
// ====================================================================

@Composable
private fun MovieControls(
    movieName: String,
    exoPlayer: ExoPlayer,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    currentTracks: Tracks,
    playFocusRequester: FocusRequester,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onAudioClick: () -> Unit,
    onSubtitlesClick: () -> Unit,
    onInteraction: () -> Unit
) {

    val audioCount =
        getTrackOptions(
            currentTracks,
            C.TRACK_TYPE_AUDIO
        ).size

    val subtitleCount =
        getTrackOptions(
            currentTracks,
            C.TRACK_TYPE_TEXT
        ).size

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {

        // ============================================================
        // TÍTULO
        // ============================================================

        Column(
            modifier = Modifier
                .align(
                    Alignment.TopStart
                )
                .padding(
                    start = 32.dp,
                    top = 24.dp
                )
        ) {

            Text(
                text = movieName,

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

                color = Color.White
            )

            Spacer(
                modifier =
                    Modifier.height(2.dp)
            )

            Text(
                text = "CLINCH PLAYER",

                color = ClinchYellow,

                fontSize = 12.sp
            )
        }

        // ============================================================
        // PARTE INFERIOR
        // ============================================================

        Column(
            modifier = Modifier
                .align(
                    Alignment.BottomCenter
                )
                .fillMaxWidth()
                .padding(
                    start = 60.dp,
                    end = 60.dp,
                    bottom = 22.dp
                )
        ) {

            MovieProgressBar(
                currentPosition =
                    currentPosition,

                duration =
                    duration
            )

            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        formatMovieTime(
                            currentPosition
                        ),

                    color =
                        Color.White,

                    fontSize =
                        13.sp
                )

                Text(
                    text =
                        formatMovieTime(
                            duration
                        ),

                    color =
                        Color.White,

                    fontSize =
                        13.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            // ========================================================
            // PLAYER BUTTONS
            // ========================================================

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.Center,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                MovieControlButton(
                    text = "−10",

                    onClick = {

                        onInteraction()

                        val newPosition =
                            (
                                    exoPlayer.currentPosition -
                                            10_000L
                                    )
                                .coerceAtLeast(
                                    0L
                                )

                        exoPlayer.seekTo(
                            newPosition
                        )
                    }
                )

                Spacer(
                    modifier =
                        Modifier.width(14.dp)
                )

                MovieControlButton(
                    text =
                        if (isPlaying) {
                            "Ⅱ"
                        } else {
                            "▶"
                        },

                    large = true,

                    modifier =
                        Modifier
                            .focusRequester(
                                playFocusRequester
                            ),

                    onClick = {

                        onInteraction()

                        toggleMoviePlayPause(
                            exoPlayer
                        )
                    }
                )

                Spacer(
                    modifier =
                        Modifier.width(14.dp)
                )

                MovieControlButton(
                    text = "+10",

                    onClick = {

                        onInteraction()

                        val target =
                            exoPlayer.currentPosition +
                                    10_000L

                        val maxDuration =
                            exoPlayer.duration

                        if (
                            maxDuration > 0
                        ) {

                            exoPlayer.seekTo(
                                target.coerceAtMost(
                                    maxDuration
                                )
                            )

                        } else {

                            exoPlayer.seekTo(
                                target
                            )
                        }
                    }
                )
            }

            // ========================================================
            // AUDIO + CC + FAVORITO
            // ========================================================

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.Center,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                PlayerOptionButton(
                    text =
                        if (audioCount > 1) {
                            "AUDIO  $audioCount"
                        } else {
                            "AUDIO"
                        },

                    enabled =
                        audioCount > 0,

                    onClick = {

                        onInteraction()
                        onAudioClick()
                    }
                )

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                PlayerOptionButton(
                    text =
                        if (subtitleCount > 0) {
                            "CC  $subtitleCount"
                        } else {
                            "CC"
                        },

                    enabled =
                        subtitleCount > 0,

                    onClick = {

                        onInteraction()
                        onSubtitlesClick()
                    }
                )

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                FavoriteStarButton(
                    isFavorite =
                        isFavorite,

                    onClick = {

                        onInteraction()
                        onFavoriteClick()
                    }
                )
            }
        }
    }
}

// ====================================================================
// BOTÓN PLAYER
// SIN CUADRO AMARILLO
// ====================================================================

@Composable
private fun MovieControlButton(
    text: String,
    modifier: Modifier = Modifier,
    large: Boolean = false,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
            .size(
                if (large) {
                    70.dp
                } else {
                    50.dp
                }
            )
            .background(
                Color.Transparent
            )
            .onFocusChanged {

                focused =
                    it.isFocused
            }
            .onKeyEvent { composeEvent ->

                val event =
                    composeEvent.nativeKeyEvent

                if (
                    event.action ==
                    KeyEvent.ACTION_UP &&
                    (
                            event.keyCode ==
                                    KeyEvent.KEYCODE_DPAD_CENTER ||
                                    event.keyCode ==
                                    KeyEvent.KEYCODE_ENTER
                            )
                ) {

                    onClick()

                    true

                } else {

                    false
                }
            }
            .focusable(),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text = text,

            color =
                if (focused) {
                    ClinchYellow
                } else {
                    Color.White
                },

            fontSize =
                if (large) {
                    30.sp
                } else {
                    17.sp
                },

            fontWeight =
                FontWeight.Bold
        )
    }
}

// ====================================================================
// AUDIO / CC
// SIN CUADRO AMARILLO
// ====================================================================

@Composable
private fun PlayerOptionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .height(36.dp)
            .widthIn(
                min = 100.dp
            )
            .background(
                Color.Transparent
            )
            .onFocusChanged {

                focused =
                    it.isFocused
            }
            .onKeyEvent { composeEvent ->

                val event =
                    composeEvent.nativeKeyEvent

                if (
                    enabled &&
                    event.action ==
                    KeyEvent.ACTION_UP &&
                    (
                            event.keyCode ==
                                    KeyEvent.KEYCODE_DPAD_CENTER ||
                                    event.keyCode ==
                                    KeyEvent.KEYCODE_ENTER
                            )
                ) {

                    onClick()
                    true

                } else {

                    false
                }
            }
            .focusable(enabled)
            .padding(
                horizontal = 14.dp
            ),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text = text,

            color =
                when {

                    !enabled ->
                        Color.White.copy(
                            alpha = 0.30f
                        )

                    focused ->
                        ClinchYellow

                    else ->
                        Color.White
                },

            fontWeight =
                FontWeight.Bold,

            fontSize = 12.sp
        )
    }
}

// ====================================================================
// FAVORITO
// SIN CUADRO AMARILLO
// ====================================================================

@Composable
private fun FavoriteStarButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                Color.Transparent
            )
            .onFocusChanged {

                focused =
                    it.isFocused
            }
            .onKeyEvent { composeEvent ->

                val event =
                    composeEvent.nativeKeyEvent

                if (
                    event.action ==
                    KeyEvent.ACTION_UP &&
                    (
                            event.keyCode ==
                                    KeyEvent.KEYCODE_DPAD_CENTER ||
                                    event.keyCode ==
                                    KeyEvent.KEYCODE_ENTER
                            )
                ) {

                    onClick()
                    true

                } else {

                    false
                }
            }
            .focusable(),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text =
                if (isFavorite) {
                    "★"
                } else {
                    "☆"
                },

            color =
                if (
                    focused ||
                    isFavorite
                ) {
                    ClinchYellow
                } else {
                    Color.White
                },

            fontSize = 24.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}

// ====================================================================
// TRACK MENU
// ====================================================================

@Composable
private fun TrackSelectionMenu(
    title: String,
    options: List<PlayerTrackOption>,
    subtitles: Boolean,
    subtitlesDisabled: Boolean,
    onSelect: (PlayerTrackOption) -> Unit,
    onDisableSubtitles: () -> Unit,
    onClose: () -> Unit
) {

    val firstFocusRequester =
        remember {
            FocusRequester()
        }

    LaunchedEffect(Unit) {

        delay(150)

        try {

            firstFocusRequester
                .requestFocus()

        } catch (_: Exception) {
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = 0.50f
                )
            )
    ) {

        Column(
            modifier = Modifier
                .align(
                    Alignment.BottomEnd
                )
                .padding(
                    end = 55.dp,
                    bottom = 70.dp
                )
                .width(310.dp)
                .clip(
                    RoundedCornerShape(
                        12.dp
                    )
                )
                .background(
                    Color(0xEE111315)
                )
                .padding(
                    18.dp
                )
        ) {

            Text(
                text = title,

                color =
                    ClinchYellow,

                fontSize = 18.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            if (subtitles) {

                TrackMenuItem(
                    text =
                        "Desactivados",

                    selected =
                        subtitlesDisabled,

                    modifier =
                        Modifier
                            .focusRequester(
                                firstFocusRequester
                            ),

                    onClick =
                        onDisableSubtitles
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )
            }

            options.forEachIndexed {
                    index,
                    option ->

                TrackMenuItem(
                    text =
                        option.name,

                    selected =
                        option.selected,

                    modifier =
                        if (
                            index == 0 &&
                            !subtitles
                        ) {

                            Modifier
                                .focusRequester(
                                    firstFocusRequester
                                )

                        } else {

                            Modifier
                        },

                    onClick = {

                        onSelect(
                            option
                        )
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )
            }

            if (options.isEmpty()) {

                Text(
                    text =
                        "No hay pistas disponibles",

                    color =
                        Color.White.copy(
                            alpha = 0.55f
                        ),

                    fontSize = 13.sp,

                    modifier =
                        Modifier.padding(
                            vertical = 10.dp
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            TrackMenuItem(
                text = "Cerrar",
                selected = false,
                onClick = onClose
            )
        }
    }
}

// ====================================================================
// TRACK MENU ITEM
// SIN CUADRO AMARILLO
// ====================================================================

@Composable
private fun TrackMenuItem(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(
                Color.Transparent
            )
            .onFocusChanged {

                focused =
                    it.isFocused
            }
            .onKeyEvent { composeEvent ->

                val event =
                    composeEvent.nativeKeyEvent

                if (
                    event.action ==
                    KeyEvent.ACTION_UP &&
                    (
                            event.keyCode ==
                                    KeyEvent.KEYCODE_DPAD_CENTER ||
                                    event.keyCode ==
                                    KeyEvent.KEYCODE_ENTER
                            )
                ) {

                    onClick()
                    true

                } else {

                    false
                }
            }
            .focusable()
            .padding(
                horizontal = 12.dp
            ),

        contentAlignment =
            Alignment.CenterStart
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    if (selected) {
                        "✓"
                    } else {
                        ""
                    },

                color =
                    ClinchYellow,

                fontWeight =
                    FontWeight.Bold,

                modifier =
                    Modifier.width(
                        25.dp
                    )
            )

            Text(
                text = text,

                color =
                    if (
                        focused ||
                        selected
                    ) {
                        ClinchYellow
                    } else {
                        Color.White
                    },

                fontSize = 14.sp,

                fontWeight =
                    if (selected) {
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

// ====================================================================
// OBTENER TRACKS
// ====================================================================

private fun getTrackOptions(
    tracks: Tracks,
    trackType: Int
): List<PlayerTrackOption> {

    val options =
        mutableListOf<PlayerTrackOption>()

    tracks.groups.forEach { group ->

        if (
            group.type ==
            trackType
        ) {

            for (
            index in
            0 until group.length
            ) {

                if (
                    group.isTrackSupported(
                        index
                    )
                ) {

                    val format =
                        group.getTrackFormat(
                            index
                        )

                    options.add(
                        PlayerTrackOption(
                            group = group,
                            trackIndex = index,
                            name =
                                getTrackDisplayName(
                                    format,
                                    options.size + 1,
                                    trackType
                                ),
                            language =
                                format.language,
                            selected =
                                group.isTrackSelected(
                                    index
                                )
                        )
                    )
                }
            }
        }
    }

    return options
}

// ====================================================================
// TRACK DISPLAY NAME
// ====================================================================

private fun getTrackDisplayName(
    format: Format,
    number: Int,
    trackType: Int
): String {

    val label =
        format.label
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }

    val language =
        format.language
            ?.trim()
            ?.takeIf {
                it.isNotBlank() &&
                        it != "und"
            }

    val languageName =
        language?.let {

            try {

                val locale =
                    Locale.forLanguageTag(
                        it
                    )

                locale.getDisplayLanguage(
                    Locale.getDefault()
                )
                    .takeIf { name ->
                        name.isNotBlank()
                    }

            } catch (_: Exception) {

                null
            }
        }

    return when {

        !label.isNullOrBlank() &&
                !languageName.isNullOrBlank() &&
                !label.equals(
                    languageName,
                    ignoreCase = true
                ) -> {

            "$label · $languageName"
        }

        !label.isNullOrBlank() -> {

            label
        }

        !languageName.isNullOrBlank() -> {

            languageName.replaceFirstChar {

                if (it.isLowerCase()) {
                    it.titlecase(
                        Locale.getDefault()
                    )
                } else {
                    it.toString()
                }
            }
        }

        trackType ==
                C.TRACK_TYPE_AUDIO -> {

            "Audio $number"
        }

        else -> {

            "Subtítulo $number"
        }
    }
}

// ====================================================================
// SELECT TRACK
// ====================================================================

private fun selectTrack(
    player: ExoPlayer,
    option: PlayerTrackOption
) {

    val trackType =
        option.group.type

    val builder =
        player
            .trackSelectionParameters
            .buildUpon()

    builder.setTrackTypeDisabled(
        trackType,
        false
    )

    builder.clearOverridesOfType(
        trackType
    )

    builder.setOverrideForType(
        androidx.media3.common.TrackSelectionOverride(
            option.group.mediaTrackGroup,
            listOf(
                option.trackIndex
            )
        )
    )

    player.trackSelectionParameters =
        builder.build()
}

// ====================================================================
// DISABLE SUBTITLES
// ====================================================================

private fun disableSubtitles(
    player: ExoPlayer
) {

    val builder =
        player
            .trackSelectionParameters
            .buildUpon()

    builder.clearOverridesOfType(
        C.TRACK_TYPE_TEXT
    )

    builder.setTrackTypeDisabled(
        C.TRACK_TYPE_TEXT,
        true
    )

    player.trackSelectionParameters =
        builder.build()
}

// ====================================================================
// PROGRESS BAR
// ====================================================================

@Composable
private fun MovieProgressBar(
    currentPosition: Long,
    duration: Long
) {

    val progress =

        if (
            duration > 0 &&
            currentPosition >= 0
        ) {

            (
                    currentPosition.toFloat() /
                            duration.toFloat()
                    )
                .coerceIn(
                    0f,
                    1f
                )

        } else {

            0f
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(
                RoundedCornerShape(
                    50
                )
            )
            .background(
                Color.White.copy(
                    alpha = 0.35f
                )
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth(
                    progress
                )
                .fillMaxHeight()
                .background(
                    ClinchYellow
                )
        )
    }
}

// ====================================================================
// PLAY / PAUSE
// ====================================================================

private fun toggleMoviePlayPause(
    player: ExoPlayer
) {

    if (player.isPlaying) {

        player.pause()

    } else {

        player.play()
    }
}

// ====================================================================
// FORMATO DE TIEMPO
// ====================================================================

private fun formatMovieTime(
    milliseconds: Long
): String {

    if (milliseconds <= 0) {
        return "00:00"
    }

    val totalSeconds =
        milliseconds / 1000

    val hours =
        totalSeconds / 3600

    val minutes =
        (
                totalSeconds % 3600
                ) / 60

    val seconds =
        totalSeconds % 60

    return if (hours > 0) {

        String.format(
            "%02d:%02d:%02d",
            hours,
            minutes,
            seconds
        )

    } else {

        String.format(
            "%02d:%02d",
            minutes,
            seconds
        )
    }
}
