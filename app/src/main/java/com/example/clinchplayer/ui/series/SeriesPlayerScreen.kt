package com.example.clinchplayer.ui.series

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.clinchplayer.data.ContinueWatchingManager
import com.example.clinchplayer.data.FavoritesManager
import com.example.clinchplayer.network.models.SeriesEpisode
import com.example.clinchplayer.network.models.SeriesStream
import kotlinx.coroutines.delay
import java.util.Locale


private val ClinchYellow =
    Color(0xFFFFD600)


// ====================================================================
// TRACK OPTION
// ====================================================================

private data class SeriesPlayerTrackOption(
    val group: Tracks.Group,
    val trackIndex: Int,
    val name: String,
    val selected: Boolean
)


// ====================================================================
// TRACK MENU
// ====================================================================

private enum class SeriesTrackMenuType {
    AUDIO,
    SUBTITLES
}


// ====================================================================
// SERIES PLAYER
// ====================================================================

@OptIn(UnstableApi::class)
@Composable
fun SeriesPlayerScreen(
    series: SeriesStream,
    episode: SeriesEpisode,
    url: String,
    seasonNumber: String,
    episodeNumber: Int,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    autoPlayNextEpisode: Boolean,
    onPreviousEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    onBack: () -> Unit
) {

    val context =
        LocalContext.current

    val continueWatchingManager =
        remember {
            ContinueWatchingManager(
                context.applicationContext
            )
        }

    val episodeId =
        episode.id.toString()

    val savedProgress =
        remember(
            series.seriesId,
            episodeId
        ) {
            continueWatchingManager
                .getEpisodeProgress(
                    seriesId = series.seriesId,
                    episodeId = episodeId
                )
        }


    // ================================================================
    // CALLBACKS ACTUALIZADOS
    // ================================================================

    val latestOnNextEpisode by
    rememberUpdatedState(
        onNextEpisode
    )


    val latestCanGoNext by
    rememberUpdatedState(
        canGoNext
    )


    val latestAutoPlayNext by
    rememberUpdatedState(
        autoPlayNextEpisode
    )


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
        series.seriesId
    ) {

        mutableStateOf(
            favoritesManager
                .isFavorite(
                    series
                )
        )
    }


    // ================================================================
    // PLAYER STATES
    // ================================================================

    var controlsVisible by
    remember {

        mutableStateOf(
            true
        )
    }


    var isPlaying by
    remember {

        mutableStateOf(
            false
        )
    }


    var currentPosition by
    remember {

        mutableLongStateOf(
            0L
        )
    }


    var duration by
    remember {

        mutableLongStateOf(
            0L
        )
    }


    var interactionId by
    remember {

        mutableIntStateOf(
            0
        )
    }


    var currentTracks by
    remember {

        mutableStateOf(
            Tracks.EMPTY
        )
    }


    var trackMenu by
    remember {

        mutableStateOf<SeriesTrackMenuType?>(
            null
        )
    }


    // ================================================================
    // NEXT EPISODE
    // ================================================================

    var showNextEpisodePrompt by
    remember(
        url
    ) {

        mutableStateOf(
            false
        )
    }


    var nextEpisodeCountdown by
    remember(
        url
    ) {

        mutableIntStateOf(
            10
        )
    }


    var endHandled by
    remember(
        url
    ) {

        mutableStateOf(
            false
        )
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
            url,
            series.seriesId,
            episodeId
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

                    savedProgress
                        ?.takeIf {
                            it.positionMs >= 15_000L
                        }
                        ?.let { saved ->
                            seekTo(
                                saved.positionMs
                            )
                        }


                    prepare()


                    playWhenReady =
                        true
                }
        }


    // ================================================================
    // PLAYER LISTENER
    // ================================================================

    DisposableEffect(
        exoPlayer,
        series.seriesId,
        episodeId
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


                override fun onTracksChanged(
                    tracks: Tracks
                ) {

                    currentTracks =
                        tracks
                }


                override fun onPlaybackStateChanged(
                    playbackState: Int
                ) {

                    if (
                        playbackState ==
                        Player.STATE_ENDED &&
                        !endHandled
                    ) {

                        continueWatchingManager
                            .removeEpisode(
                                seriesId = series.seriesId,
                                episodeId = episodeId
                            )

                        endHandled =
                            true


                        if (
                            latestAutoPlayNext &&
                            latestCanGoNext
                        ) {

                            controlsVisible =
                                false


                            trackMenu =
                                null


                            nextEpisodeCountdown =
                                10


                            showNextEpisodePrompt =
                                true
                        }
                    }
                }
            }


        exoPlayer
            .addListener(
                listener
            )


        currentTracks =
            exoPlayer.currentTracks


        onDispose {

            exoPlayer
                .removeListener(
                    listener
                )

            saveSeriesProgress(
                manager = continueWatchingManager,
                player = exoPlayer,
                series = series,
                episode = episode,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                url = url
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
    // ACTUALIZAR TIEMPO
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
    // GUARDAR CONTINUE WATCHING
    // ================================================================

    LaunchedEffect(
        exoPlayer,
        series.seriesId,
        episodeId
    ) {

        while (true) {

            delay(
                5000
            )

            saveSeriesProgress(
                manager = continueWatchingManager,
                player = exoPlayer,
                series = series,
                episode = episode,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                url = url
            )
        }
    }


    // ================================================================
    // CUENTA REGRESIVA
    // ================================================================

    LaunchedEffect(
        showNextEpisodePrompt,
        url
    ) {

        if (
            showNextEpisodePrompt
        ) {

            nextEpisodeCountdown =
                10


            while (
                nextEpisodeCountdown > 0 &&
                showNextEpisodePrompt
            ) {

                delay(
                    1000
                )


                nextEpisodeCountdown--
            }


            if (
                showNextEpisodePrompt &&
                nextEpisodeCountdown <= 0 &&
                latestCanGoNext
            ) {

                showNextEpisodePrompt =
                    false


                latestOnNextEpisode()
            }
        }
    }


    // ================================================================
    // OCULTAR CONTROLES
    // ================================================================

    LaunchedEffect(
        controlsVisible,
        interactionId,
        trackMenu,
        showNextEpisodePrompt
    ) {

        if (
            controlsVisible &&
            trackMenu == null &&
            !showNextEpisodePrompt
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
        controlsVisible,
        trackMenu,
        showNextEpisodePrompt
    ) {

        if (
            controlsVisible &&
            trackMenu == null &&
            !showNextEpisodePrompt
        ) {

            delay(
                150
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

        when {

            showNextEpisodePrompt -> {

                showNextEpisodePrompt =
                    false


                controlsVisible =
                    true


                interactionId++
            }


            trackMenu != null -> {

                trackMenu =
                    null


                controlsVisible =
                    true


                interactionId++
            }


            controlsVisible -> {

                controlsVisible =
                    false
            }


            else -> {

                saveSeriesProgress(
                    manager = continueWatchingManager,
                    player = exoPlayer,
                    series = series,
                    episode = episode,
                    seasonNumber = seasonNumber,
                    episodeNumber = episodeNumber,
                    url = url
                )

                onBack()
            }
        }
    }


    // ================================================================
    // PLAYER
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
                    // NEXT EPISODE PROMPT
                    // ====================================================

                    if (
                        showNextEpisodePrompt
                    ) {

                        return@onKeyEvent false
                    }


                    // ====================================================
                    // TRACK MENU
                    // ====================================================

                    if (
                        trackMenu != null
                    ) {

                        return@onKeyEvent false
                    }


                    // ====================================================
                    // CONTROLES OCULTOS
                    // ====================================================

                    if (
                        !controlsVisible
                    ) {

                        when (
                            event.keyCode
                        ) {

                            // ============================================
                            // IZQUIERDA = RETROCEDER 10 SEGUNDOS
                            // ============================================

                            KeyEvent.KEYCODE_DPAD_LEFT -> {

                                if (
                                    exoPlayer
                                        .isCurrentMediaItemSeekable
                                ) {

                                    val newPosition =
                                        (
                                                exoPlayer
                                                    .currentPosition -
                                                        10_000L
                                                )
                                            .coerceAtLeast(
                                                0L
                                            )


                                    exoPlayer
                                        .seekTo(
                                            newPosition
                                        )
                                }


                                return@onKeyEvent true
                            }


                            // ============================================
                            // DERECHA = ADELANTAR 10 SEGUNDOS
                            // ============================================

                            KeyEvent.KEYCODE_DPAD_RIGHT -> {

                                if (
                                    exoPlayer
                                        .isCurrentMediaItemSeekable
                                ) {

                                    val newPosition =
                                        exoPlayer
                                            .currentPosition +
                                                10_000L


                                    val maxDuration =
                                        exoPlayer.duration


                                    if (
                                        maxDuration > 0
                                    ) {

                                        exoPlayer
                                            .seekTo(
                                                newPosition
                                                    .coerceAtMost(
                                                        maxDuration
                                                    )
                                            )

                                    } else {

                                        exoPlayer
                                            .seekTo(
                                                newPosition
                                            )
                                    }
                                }


                                return@onKeyEvent true
                            }


                            // ============================================
                            // OK / ARRIBA / ABAJO = MOSTRAR CONTROLES
                            // ============================================

                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER,
                            KeyEvent.KEYCODE_DPAD_UP,
                            KeyEvent.KEYCODE_DPAD_DOWN -> {

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

                            toggleSeriesPlayPause(
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
            controlsVisible &&
            !showNextEpisodePrompt
        ) {

            SeriesControls(

                seriesName =
                    series.name,

                episodeName =
                    episode.title
                        ?: "Episodio",

                exoPlayer =
                    exoPlayer,

                isPlaying =
                    isPlaying,

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

                canGoPrevious =
                    canGoPrevious,

                canGoNext =
                    canGoNext,


                onPreviousEpisode = {

                    showNextEpisodePrompt =
                        false


                    onPreviousEpisode()
                },


                onNextEpisode = {

                    showNextEpisodePrompt =
                        false


                    onNextEpisode()
                },


                onFavoriteClick = {

                    isFavorite =
                        favoritesManager
                            .toggleFavorite(
                                series
                            )


                    controlsVisible =
                        true


                    interactionId++
                },


                onAudioClick = {

                    trackMenu =
                        SeriesTrackMenuType.AUDIO


                    interactionId++
                },


                onSubtitlesClick = {

                    trackMenu =
                        SeriesTrackMenuType.SUBTITLES


                    interactionId++
                },


                onInteraction = {

                    controlsVisible =
                        true


                    interactionId++
                }
            )
        }


        // ============================================================
        // AUDIO / SUBTITLE MENU
        // ============================================================

        trackMenu
            ?.let {
                    menu ->

                SeriesTrackSelectionMenu(

                    title =
                        if (
                            menu ==
                            SeriesTrackMenuType.AUDIO
                        ) {

                            "AUDIO"

                        } else {

                            "SUBTÍTULOS"
                        },


                    options =
                        getSeriesTrackOptions(

                            tracks =
                                currentTracks,

                            trackType =
                                if (
                                    menu ==
                                    SeriesTrackMenuType.AUDIO
                                ) {

                                    C.TRACK_TYPE_AUDIO

                                } else {

                                    C.TRACK_TYPE_TEXT
                                }
                        ),


                    subtitles =
                        menu ==
                                SeriesTrackMenuType
                                    .SUBTITLES,


                    subtitlesDisabled =
                        exoPlayer
                            .trackSelectionParameters
                            .disabledTrackTypes
                            .contains(
                                C.TRACK_TYPE_TEXT
                            ),


                    onSelect = {
                            option ->

                        selectSeriesTrack(
                            player =
                                exoPlayer,

                            option =
                                option
                        )


                        currentTracks =
                            exoPlayer.currentTracks


                        trackMenu =
                            null


                        controlsVisible =
                            true


                        interactionId++
                    },


                    onDisableSubtitles = {

                        disableSeriesSubtitles(
                            exoPlayer
                        )


                        currentTracks =
                            exoPlayer.currentTracks


                        trackMenu =
                            null


                        controlsVisible =
                            true


                        interactionId++
                    },


                    onClose = {

                        trackMenu =
                            null


                        controlsVisible =
                            true


                        interactionId++
                    }
                )
            }


        // ============================================================
        // NEXT EPISODE PROMPT
        // ============================================================

        if (
            showNextEpisodePrompt
        ) {

            NextEpisodePrompt(

                seriesName =
                    series.name,

                countdown =
                    nextEpisodeCountdown,


                onPlayNow = {

                    showNextEpisodePrompt =
                        false


                    if (
                        canGoNext
                    ) {

                        onNextEpisode()
                    }
                },


                onCancel = {

                    showNextEpisodePrompt =
                        false


                    controlsVisible =
                        true


                    interactionId++
                }
            )
        }
    }
}


// ====================================================================
// CONTINUE WATCHING
// ====================================================================

private fun saveSeriesProgress(
    manager: ContinueWatchingManager,
    player: ExoPlayer,
    series: SeriesStream,
    episode: SeriesEpisode,
    seasonNumber: String,
    episodeNumber: Int,
    url: String
) {
    val position =
        player.currentPosition
            .coerceAtLeast(0L)

    val duration =
        player.duration

    if (duration <= 0L) return

    manager.saveEpisodeProgress(
        series = series,
        episode = episode,
        seasonNumber = seasonNumber.ifBlank { "1" },
        episodeNumber = episodeNumber.coerceAtLeast(1),
        url = url,
        positionMs = position,
        durationMs = duration
    )
}


// ====================================================================
// NEXT EPISODE PROMPT
// ====================================================================

@Composable
private fun NextEpisodePrompt(
    seriesName: String,
    countdown: Int,
    onPlayNow: () -> Unit,
    onCancel: () -> Unit
) {

    val playFocusRequester =
        remember {

            FocusRequester()
        }


    LaunchedEffect(
        Unit
    ) {

        delay(
            150
        )


        try {

            playFocusRequester
                .requestFocus()

        } catch (_: Exception) {
        }
    }


    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha =
                            0.45f
                    )
                )
    ) {

        Column(
            modifier =
                Modifier
                    .align(
                        Alignment.BottomEnd
                    )
                    .padding(
                        end = 55.dp,
                        bottom = 50.dp
                    )
                    .width(
                        390.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            14.dp
                        )
                    )
                    .background(
                        Color(
                            0xF2111315
                        )
                    )
                    .padding(
                        24.dp
                    )
        ) {

            Text(
                text =
                    "SIGUIENTE EPISODIO",

                color =
                    ClinchYellow,

                fontSize =
                    13.sp,

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    2.sp
            )


            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )


            Text(
                text =
                    seriesName,

                color =
                    Color.White,

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.Bold,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )


            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )


            Text(
                text =
                    "Comienza automáticamente en $countdown segundos",

                color =
                    Color.White.copy(
                        alpha =
                            0.72f
                    ),

                fontSize =
                    13.sp
            )


            Spacer(
                modifier =
                    Modifier.height(
                        20.dp
                    )
            )


            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                NextEpisodeButton(

                    text =
                        "▶  REPRODUCIR AHORA",

                    primary =
                        true,

                    modifier =
                        Modifier
                            .focusRequester(
                                playFocusRequester
                            ),

                    onClick =
                        onPlayNow
                )


                NextEpisodeButton(

                    text =
                        "CANCELAR",

                    primary =
                        false,

                    onClick =
                        onCancel
                )
            }
        }
    }
}


// ====================================================================
// NEXT EPISODE BUTTON
// ====================================================================

@Composable
private fun NextEpisodeButton(
    text: String,
    modifier: Modifier = Modifier,
    primary: Boolean,
    onClick: () -> Unit
) {

    var focused by
    remember {

        mutableStateOf(
            false
        )
    }


    Box(
        modifier =
            modifier
                .height(
                    44.dp
                )
                .widthIn(
                    min =
                        if (
                            primary
                        ) {

                            190.dp

                        } else {

                            105.dp
                        }
                )
                .clip(
                    RoundedCornerShape(
                        7.dp
                    )
                )
                .background(
                    when {

                        focused ->

                            ClinchYellow


                        primary ->

                            Color.White


                        else ->

                            Color.White.copy(
                                alpha =
                                    0.12f
                            )
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
                text,

            color =
                when {

                    focused ->

                        Color.Black


                    primary ->

                        Color.Black


                    else ->

                        Color.White
                },

            fontSize =
                12.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}


// ====================================================================
// CONTROLES
// ====================================================================

@Composable
private fun SeriesControls(
    seriesName: String,
    episodeName: String,
    exoPlayer: ExoPlayer,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    currentTracks: Tracks,
    playFocusRequester: FocusRequester,
    isFavorite: Boolean,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPreviousEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAudioClick: () -> Unit,
    onSubtitlesClick: () -> Unit,
    onInteraction: () -> Unit
) {

    val audioCount =
        getSeriesTrackOptions(
            currentTracks,
            C.TRACK_TYPE_AUDIO
        ).size


    val subtitleCount =
        getSeriesTrackOptions(
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
                    episodeName,

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

                color =
                    Color.White,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )


            Spacer(
                modifier =
                    Modifier.height(
                        2.dp
                    )
            )


            Text(
                text =
                    seriesName,

                color =
                    ClinchYellow,

                fontSize =
                    12.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
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

            SeriesProgressBar(
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


            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        formatSeriesTime(
                            currentPosition
                        ),

                    color =
                        Color.White,

                    fontSize =
                        13.sp
                )


                Text(
                    text =
                        formatSeriesTime(
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
                    Modifier.height(
                        6.dp
                    )
            )


            // ========================================================
            // ⏮ -10 PLAY +10 ⏭
            // ========================================================

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.Center,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                SeriesControlButton(

                    text =
                        "⏮",

                    enabled =
                        canGoPrevious,

                    onClick = {

                        onInteraction()

                        onPreviousEpisode()
                    }
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            12.dp
                        )
                )


                SeriesControlButton(

                    text =
                        "−10",

                    onClick = {

                        onInteraction()


                        exoPlayer
                            .seekTo(
                                (
                                        exoPlayer.currentPosition -
                                                10_000L
                                        )
                                    .coerceAtLeast(
                                        0L
                                    )
                            )
                    }
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            14.dp
                        )
                )


                SeriesControlButton(

                    text =
                        if (
                            isPlaying
                        ) {

                            "Ⅱ"

                        } else {

                            "▶"
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


                        toggleSeriesPlayPause(
                            exoPlayer
                        )
                    }
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            14.dp
                        )
                )


                SeriesControlButton(

                    text =
                        "+10",

                    onClick = {

                        onInteraction()


                        val target =
                            exoPlayer.currentPosition +
                                    10_000L


                        if (
                            exoPlayer.duration > 0
                        ) {

                            exoPlayer
                                .seekTo(
                                    target
                                        .coerceAtMost(
                                            exoPlayer.duration
                                        )
                                )

                        } else {

                            exoPlayer
                                .seekTo(
                                    target
                                )
                        }
                    }
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            12.dp
                        )
                )


                SeriesControlButton(

                    text =
                        "⏭",

                    enabled =
                        canGoNext,

                    onClick = {

                        onInteraction()

                        onNextEpisode()
                    }
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )


            // ========================================================
            // AUDIO / CC / FAVORITO
            // ========================================================

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.Center,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                SeriesPlayerOptionButton(

                    text =
                        if (
                            audioCount > 1
                        ) {

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
                        Modifier.width(
                            12.dp
                        )
                )


                SeriesPlayerOptionButton(

                    text =
                        if (
                            subtitleCount > 0
                        ) {

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
                        Modifier.width(
                            12.dp
                        )
                )


                SeriesFavoriteStarButton(

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
// CONTROL BUTTON
// ====================================================================

@Composable
private fun SeriesControlButton(
    text: String,
    modifier: Modifier = Modifier,
    large: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {

    var focused by
    remember {

        mutableStateOf(
            false
        )
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
                .focusable(
                    enabled
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text =
                text,

            color =
                when {

                    !enabled ->

                        Color.White.copy(
                            alpha =
                                0.22f
                        )


                    focused ->

                        ClinchYellow


                    else ->

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
// AUDIO / CC BUTTON
// ====================================================================

@Composable
private fun SeriesPlayerOptionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {

    var focused by
    remember {

        mutableStateOf(
            false
        )
    }


    Box(
        modifier =
            Modifier
                .height(
                    36.dp
                )
                .widthIn(
                    min =
                        100.dp
                )
                .onFocusChanged {

                    focused =
                        it.isFocused
                }
                .onKeyEvent {
                        composeEvent ->

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
                .focusable(
                    enabled
                )
                .padding(
                    horizontal =
                        14.dp
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text =
                text,

            color =
                when {

                    !enabled ->

                        Color.White.copy(
                            alpha =
                                0.30f
                        )


                    focused ->

                        ClinchYellow


                    else ->

                        Color.White
                },

            fontWeight =
                FontWeight.Bold,

            fontSize =
                12.sp
        )
    }
}


// ====================================================================
// FAVORITO
// ====================================================================

@Composable
private fun SeriesFavoriteStarButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {

    var focused by
    remember {

        mutableStateOf(
            false
        )
    }


    Box(
        modifier =
            Modifier
                .size(
                    40.dp
                )
                .onFocusChanged {

                    focused =
                        it.isFocused
                }
                .onKeyEvent {
                        composeEvent ->

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
                if (
                    isFavorite
                ) {

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

            fontSize =
                24.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}


// ====================================================================
// TRACK MENU
// ====================================================================

@Composable
private fun SeriesTrackSelectionMenu(
    title: String,
    options: List<SeriesPlayerTrackOption>,
    subtitles: Boolean,
    subtitlesDisabled: Boolean,
    onSelect: (SeriesPlayerTrackOption) -> Unit,
    onDisableSubtitles: () -> Unit,
    onClose: () -> Unit
) {

    val firstFocusRequester =
        remember {

            FocusRequester()
        }


    LaunchedEffect(
        Unit
    ) {

        delay(
            150
        )


        try {

            firstFocusRequester
                .requestFocus()

        } catch (_: Exception) {
        }
    }


    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha =
                            0.50f
                    )
                )
    ) {

        Column(
            modifier =
                Modifier
                    .align(
                        Alignment.BottomEnd
                    )
                    .padding(
                        end = 55.dp,
                        bottom = 70.dp
                    )
                    .width(
                        310.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            12.dp
                        )
                    )
                    .background(
                        Color(
                            0xEE111315
                        )
                    )
                    .padding(
                        18.dp
                    )
        ) {

            Text(
                text =
                    title,

                color =
                    ClinchYellow,

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )


            if (
                subtitles
            ) {

                SeriesTrackMenuItem(

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
                        Modifier.height(
                            4.dp
                        )
                )
            }


            options
                .forEachIndexed {
                        index,
                        option ->

                    SeriesTrackMenuItem(

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
                            Modifier.height(
                                4.dp
                            )
                    )
                }


            if (
                options.isEmpty()
            ) {

                Text(
                    text =
                        "No hay pistas disponibles",

                    color =
                        Color.White.copy(
                            alpha =
                                0.55f
                        ),

                    fontSize =
                        13.sp,

                    modifier =
                        Modifier.padding(
                            vertical =
                                10.dp
                        )
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )


            SeriesTrackMenuItem(

                text =
                    "Cerrar",

                selected =
                    false,

                onClick =
                    onClose
            )
        }
    }
}


// ====================================================================
// TRACK ITEM
// ====================================================================

@Composable
private fun SeriesTrackMenuItem(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    var focused by
    remember {

        mutableStateOf(
            false
        )
    }


    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(
                    42.dp
                )
                .onFocusChanged {

                    focused =
                        it.isFocused
                }
                .onKeyEvent {
                        composeEvent ->

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
                    horizontal =
                        12.dp
                ),

        contentAlignment =
            Alignment.CenterStart
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    if (
                        selected
                    ) {

                        "✓"

                    } else {

                        ""
                    },

                color =
                    ClinchYellow,

                modifier =
                    Modifier.width(
                        25.dp
                    )
            )


            Text(
                text =
                    text,

                color =
                    if (
                        focused ||
                        selected
                    ) {

                        ClinchYellow

                    } else {

                        Color.White
                    },

                fontSize =
                    14.sp,

                fontWeight =
                    if (
                        selected
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
}


// ====================================================================
// TRACK OPTIONS
// ====================================================================

private fun getSeriesTrackOptions(
    tracks: Tracks,
    trackType: Int
): List<SeriesPlayerTrackOption> {

    val options =
        mutableListOf<SeriesPlayerTrackOption>()


    tracks.groups
        .forEach {
                group ->

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
                            group
                                .getTrackFormat(
                                    index
                                )


                        options.add(

                            SeriesPlayerTrackOption(

                                group =
                                    group,

                                trackIndex =
                                    index,

                                name =
                                    getSeriesTrackDisplayName(
                                        format =
                                            format,

                                        number =
                                            options.size + 1,

                                        trackType =
                                            trackType
                                    ),

                                selected =
                                    group
                                        .isTrackSelected(
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
// TRACK NAME
// ====================================================================

private fun getSeriesTrackDisplayName(
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
        language
            ?.let {

                try {

                    Locale
                        .forLanguageTag(
                            it
                        )
                        .getDisplayLanguage(
                            Locale.getDefault()
                        )
                        .takeIf {
                                name ->

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
                    ignoreCase =
                        true
                ) -> {

            "$label · $languageName"
        }


        !label.isNullOrBlank() -> {

            label
        }


        !languageName.isNullOrBlank() -> {

            languageName
                .replaceFirstChar {

                    if (
                        it.isLowerCase()
                    ) {

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

private fun selectSeriesTrack(
    player: ExoPlayer,
    option: SeriesPlayerTrackOption
) {

    val trackType =
        option.group.type


    val builder =
        player
            .trackSelectionParameters
            .buildUpon()


    builder
        .setTrackTypeDisabled(
            trackType,
            false
        )


    builder
        .clearOverridesOfType(
            trackType
        )


    builder
        .setOverrideForType(

            androidx.media3.common.TrackSelectionOverride(

                option
                    .group
                    .mediaTrackGroup,

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

private fun disableSeriesSubtitles(
    player: ExoPlayer
) {

    val builder =
        player
            .trackSelectionParameters
            .buildUpon()


    builder
        .clearOverridesOfType(
            C.TRACK_TYPE_TEXT
        )


    builder
        .setTrackTypeDisabled(
            C.TRACK_TYPE_TEXT,
            true
        )


    player.trackSelectionParameters =
        builder.build()
}


// ====================================================================
// PROGRESS
// ====================================================================

@Composable
private fun SeriesProgressBar(
    currentPosition: Long,
    duration: Long
) {

    val progress =
        if (
            duration > 0
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

private fun toggleSeriesPlayPause(
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
// TIME
// ====================================================================

private fun formatSeriesTime(
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