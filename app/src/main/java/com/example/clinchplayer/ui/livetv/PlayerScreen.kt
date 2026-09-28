package com.example.clinchplayer.ui.livetv

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.clinchplayer.data.FavoritesManager
import com.example.clinchplayer.network.models.LiveStream
import kotlinx.coroutines.delay


private val ClinchYellow =
    Color(0xFFFFD600)


// ====================================================================
// PLAYER TV EN VIVO
// ====================================================================

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    stream: LiveStream,
    url: String,
    onBack: () -> Unit
) {

    val context =
        LocalContext.current


    // ================================================================
    // FAVORITOS
    // ================================================================

    val favoritesManager =
        remember {

            FavoritesManager(
                context.applicationContext
            )
        }


    var isFavorite by
    remember(
        stream.streamId
    ) {

        mutableStateOf(
            favoritesManager
                .isFavorite(
                    stream
                )
        )
    }


    // ================================================================
    // STATES
    // ================================================================

    var controlsVisible by remember {
        mutableStateOf(true)
    }


    var isPlaying by remember {
        mutableStateOf(true)
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


    val playFocusRequester =
        remember {

            FocusRequester()
        }


    // ================================================================
    // EXOPLAYER
    // ================================================================

    val exoPlayer =
        remember(
            url
        ) {

            val httpFactory =
                DefaultHttpDataSource
                    .Factory()
                    .setAllowCrossProtocolRedirects(
                        true
                    )
                    .setUserAgent(
                        "ClinchPlayer/1.0"
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


            ExoPlayer
                .Builder(
                    context
                )
                .setMediaSourceFactory(
                    mediaSourceFactory
                )
                .build()
                .apply {

                    volume =
                        1f


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
    // LISTENER
    // ================================================================

    DisposableEffect(
        exoPlayer
    ) {

        val listener =
            object :
                Player.Listener {

                override fun onIsPlayingChanged(
                    playing: Boolean
                ) {

                    isPlaying =
                        playing
                }
            }


        exoPlayer
            .addListener(
                listener
            )


        onDispose {

            exoPlayer
                .removeListener(
                    listener
                )


            try {

                exoPlayer.stop()

                exoPlayer
                    .clearVideoSurface()

                exoPlayer.release()

            } catch (_: Exception) {
            }
        }
    }


    // ================================================================
    // PROGRESO
    // ================================================================

    LaunchedEffect(
        exoPlayer
    ) {

        while (true) {

            currentPosition =
                exoPlayer
                    .currentPosition
                    .coerceAtLeast(
                        0L
                    )


            val playerDuration =
                exoPlayer.duration


            duration =
                if (
                    playerDuration > 0
                ) {

                    playerDuration

                } else {

                    0L
                }


            delay(
                500
            )
        }
    }


    // ================================================================
    // AUTO OCULTAR CONTROLES
    // ================================================================

    LaunchedEffect(
        controlsVisible,
        interactionId
    ) {

        if (
            controlsVisible
        ) {

            delay(
                5000
            )


            controlsVisible =
                false
        }
    }


    // ================================================================
    // FOCO PLAY
    // ================================================================

    LaunchedEffect(
        controlsVisible
    ) {

        if (
            controlsVisible
        ) {

            delay(
                120
            )


            try {

                playFocusRequester
                    .requestFocus()

            } catch (_: Exception) {
            }
        }
    }


    // ================================================================
    // BACK
    // ================================================================

    BackHandler {

        if (
            controlsVisible
        ) {

            controlsVisible =
                false

        } else {

            onBack()
        }
    }


    // ================================================================
    // PANTALLA
    // ================================================================

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black
                )
                .onKeyEvent {
                        composeEvent ->

                    val event =
                        composeEvent
                            .nativeKeyEvent


                    if (
                        event.action !=
                        KeyEvent.ACTION_DOWN
                    ) {

                        return@onKeyEvent false
                    }


                    // ====================================================
                    // MOSTRAR CONTROLES
                    // ====================================================

                    if (
                        !controlsVisible
                    ) {

                        when (
                            event.keyCode
                        ) {

                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER,
                            KeyEvent.KEYCODE_DPAD_UP,
                            KeyEvent.KEYCODE_DPAD_DOWN,
                            KeyEvent.KEYCODE_DPAD_LEFT,
                            KeyEvent.KEYCODE_DPAD_RIGHT -> {

                                controlsVisible =
                                    true

                                interactionId++


                                return@onKeyEvent true
                            }
                        }
                    }


                    // ====================================================
                    // BOTONES MULTIMEDIA
                    // ====================================================

                    when (
                        event.keyCode
                    ) {

                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {

                            togglePlayPause(
                                exoPlayer
                            )


                            controlsVisible =
                                true

                            interactionId++


                            true
                        }


                        KeyEvent.KEYCODE_MEDIA_PLAY -> {

                            exoPlayer.play()


                            controlsVisible =
                                true

                            interactionId++


                            true
                        }


                        KeyEvent.KEYCODE_MEDIA_PAUSE -> {

                            exoPlayer.pause()


                            controlsVisible =
                                true

                            interactionId++


                            true
                        }


                        else ->
                            false
                    }
                }
                .focusable()
    ) {

        // ============================================================
        // VIDEO
        // ============================================================

        AndroidView(

            factory = {
                    ctx ->

                PlayerView(
                    ctx
                )
                    .apply {

                        player =
                            exoPlayer


                        useController =
                            false


                        resizeMode =
                            AspectRatioFrameLayout
                                .RESIZE_MODE_FIT


                        setShutterBackgroundColor(
                            android.graphics.Color.BLACK
                        )


                        keepScreenOn =
                            true
                    }
            },


            update = {
                    playerView ->

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

        if (
            controlsVisible
        ) {

            PlayerControls(

                streamName =
                    stream.name,

                exoPlayer =
                    exoPlayer,

                isPlaying =
                    isPlaying,

                currentPosition =
                    currentPosition,

                duration =
                    duration,

                playFocusRequester =
                    playFocusRequester,

                isFavorite =
                    isFavorite,


                // =====================================================
                // FAVORITO
                // =====================================================

                onFavoriteClick = {

                    isFavorite =
                        favoritesManager
                            .toggleFavorite(
                                stream
                            )


                    controlsVisible =
                        true

                    interactionId++
                },


                onInteraction = {

                    controlsVisible =
                        true

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
private fun PlayerControls(
    streamName: String,
    exoPlayer: ExoPlayer,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    playFocusRequester: FocusRequester,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onInteraction: () -> Unit
) {

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {

        // ============================================================
        // NOMBRE DEL CANAL
        // ============================================================

        Column(
            modifier =
                Modifier
                    .align(
                        Alignment.TopStart
                    )
                    .padding(
                        start = 32.dp,
                        top = 24.dp
                    )
        ) {

            Text(
                text =
                    streamName,

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

                color =
                    Color.White
            )


            Spacer(
                modifier =
                    Modifier.height(
                        2.dp
                    )
            )


            Text(
                text =
                    "CLINCH PLAYER • TV EN VIVO",

                fontSize =
                    12.sp,

                color =
                    ClinchYellow
            )
        }


        // ============================================================
        // CONTROLES INFERIORES
        // ============================================================

        Column(
            modifier =
                Modifier
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

            // ========================================================
            // BARRA
            // ========================================================

            PlayerProgressBar(

                currentPosition =
                    currentPosition,

                duration =
                    duration
            )


            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )


            // ========================================================
            // TIEMPO
            // ========================================================

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        if (
                            duration > 0
                        ) {

                            formatTime(
                                currentPosition
                            )

                        } else {

                            "EN VIVO"
                        },

                    color =
                        Color.White,

                    fontSize =
                        13.sp
                )


                Text(
                    text =
                        if (
                            duration > 0
                        ) {

                            formatTime(
                                duration
                            )

                        } else {

                            "LIVE"
                        },

                    color =
                        Color.White,

                    fontSize =
                        13.sp
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )


            // ========================================================
            // CONTROLES DE REPRODUCCIÓN
            // ========================================================

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.Center,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                // ====================================================
                // ANTERIOR
                // ====================================================

                PlayerControlButton(

                    text =
                        "◀|",

                    contentDescription =
                        "Anterior",

                    onClick = {

                        onInteraction()


                        if (
                            exoPlayer
                                .hasPreviousMediaItem()
                        ) {

                            exoPlayer
                                .seekToPreviousMediaItem()
                        }
                    }
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            10.dp
                        )
                )


                // ====================================================
                // -10
                // ====================================================

                PlayerControlButton(

                    text =
                        "−10",

                    contentDescription =
                        "Retroceder 10 segundos",

                    onClick = {

                        onInteraction()


                        if (
                            exoPlayer
                                .isCurrentMediaItemSeekable
                        ) {

                            exoPlayer
                                .seekTo(
                                    (
                                            exoPlayer
                                                .currentPosition -
                                                    10_000L
                                            )
                                        .coerceAtLeast(
                                            0L
                                        )
                                )
                        }
                    }
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            10.dp
                        )
                )


                // ====================================================
                // PLAY / PAUSE
                // ====================================================

                PlayerControlButton(

                    text =
                        if (
                            isPlaying
                        ) {

                            "Ⅱ"

                        } else {

                            "▶"
                        },

                    contentDescription =
                        if (
                            isPlaying
                        ) {

                            "Pausa"

                        } else {

                            "Reproducir"
                        },

                    large =
                        true,

                    modifier =
                        Modifier
                            .focusRequester(
                                playFocusRequester
                            ),

                    onClick = {

                        onInteraction()


                        togglePlayPause(
                            exoPlayer
                        )
                    }
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            10.dp
                        )
                )


                // ====================================================
                // +10
                // ====================================================

                PlayerControlButton(

                    text =
                        "+10",

                    contentDescription =
                        "Adelantar 10 segundos",

                    onClick = {

                        onInteraction()


                        if (
                            exoPlayer
                                .isCurrentMediaItemSeekable
                        ) {

                            val target =
                                exoPlayer
                                    .currentPosition +
                                        10_000L


                            if (
                                exoPlayer.duration > 0
                            ) {

                                exoPlayer
                                    .seekTo(
                                        target
                                            .coerceAtMost(
                                                exoPlayer
                                                    .duration
                                            )
                                    )

                            } else {

                                exoPlayer
                                    .seekTo(
                                        target
                                    )
                            }
                        }
                    }
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            10.dp
                        )
                )


                // ====================================================
                // SIGUIENTE
                // ====================================================

                PlayerControlButton(

                    text =
                        "|▶",

                    contentDescription =
                        "Siguiente",

                    onClick = {

                        onInteraction()


                        if (
                            exoPlayer
                                .hasNextMediaItem()
                        ) {

                            exoPlayer
                                .seekToNextMediaItem()
                        }
                    }
                )
            }


            // ========================================================
            // FAVORITOS
            // ========================================================

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )


            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.Center,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                LiveFavoriteButton(

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
// FAVORITO TV EN VIVO
// ====================================================================

@Composable
private fun LiveFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }


    Box(
        modifier =
            Modifier
                .height(
                    40.dp
                )
                .widthIn(
                    min =
                        170.dp
                )
                .onFocusChanged {

                    focused =
                        it.isFocused
                }
                .onKeyEvent {
                        composeEvent ->

                    val event =
                        composeEvent
                            .nativeKeyEvent


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
                    horizontal =
                        16.dp
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text =
                if (
                    isFavorite
                ) {

                    "★  EN FAVORITOS"

                } else {

                    "☆  AGREGAR"
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

            fontSize =
                13.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}


// ====================================================================
// BOTÓN PLAYER
// ====================================================================

@Composable
private fun PlayerControlButton(
    text: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    large: Boolean = false,
    onClick: () -> Unit
) {

    var focused by remember {
        mutableStateOf(false)
    }


    Box(
        modifier =
            modifier
                .size(
                    if (
                        large
                    ) {

                        70.dp

                    } else {

                        50.dp
                    }
                )
                .clip(
                    CircleShape
                )
                .background(
                    when {

                        focused ->

                            ClinchYellow


                        large ->

                            Color.Black.copy(
                                alpha =
                                    0.45f
                            )


                        else ->

                            Color.Transparent
                    }
                )
                .onFocusChanged {

                    focused =
                        it.isFocused
                }
                .onKeyEvent {
                        composeEvent ->

                    val event =
                        composeEvent
                            .nativeKeyEvent


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
                text,

            color =
                if (
                    focused
                ) {

                    Color.Black

                } else {

                    Color.White
                },

            fontSize =
                if (
                    large
                ) {

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
// BARRA
// ====================================================================

@Composable
private fun PlayerProgressBar(
    currentPosition: Long,
    duration: Long
) {

    val progress =
        if (
            duration > 0 &&
            currentPosition >= 0
        ) {

            (
                    currentPosition
                        .toFloat() /
                            duration
                                .toFloat()
                    )
                .coerceIn(
                    0f,
                    1f
                )

        } else {

            1f
        }


    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    3.dp
                )
                .clip(
                    RoundedCornerShape(
                        50
                    )
                )
                .background(
                    Color.White.copy(
                        alpha =
                            0.35f
                    )
                )
    ) {

        Box(
            modifier =
                Modifier
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

private fun togglePlayPause(
    player: ExoPlayer
) {

    if (
        player.isPlaying
    ) {

        player.pause()

    } else {

        player.play()
    }
}


// ====================================================================
// TIEMPO
// ====================================================================

private fun formatTime(
    milliseconds: Long
): String {

    if (
        milliseconds <= 0
    ) {

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


    return if (
        hours > 0
    ) {

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