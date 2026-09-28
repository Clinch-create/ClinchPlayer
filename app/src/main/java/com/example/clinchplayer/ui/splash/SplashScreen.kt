package com.example.clinchplayer.ui.splash

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.clinchplayer.R


@OptIn(UnstableApi::class)
@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {

    val context =
        LocalContext.current


    var finished by
    remember {

        mutableStateOf(
            false
        )
    }


    // ================================================================
    // VIDEO URI
    // ================================================================

    val videoUri =
        remember {

            Uri.parse(
                "android.resource://" +
                        context.packageName +
                        "/" +
                        R.raw.clinch_intro
            )
        }


    // ================================================================
    // EXOPLAYER
    // ================================================================

    val exoPlayer =
        remember {

            ExoPlayer
                .Builder(
                    context
                )
                .build()
                .apply {

                    setMediaItem(
                        MediaItem.fromUri(
                            videoUri
                        )
                    )


                    repeatMode =
                        Player.REPEAT_MODE_OFF


                    playWhenReady =
                        true


                    volume =
                        1f


                    prepare()
                }
        }


    // ================================================================
    // CUANDO TERMINE EL VIDEO
    // ================================================================

    DisposableEffect(
        exoPlayer
    ) {

        val listener =
            object :
                Player.Listener {

                override fun onPlaybackStateChanged(
                    playbackState: Int
                ) {

                    if (
                        playbackState ==
                        Player.STATE_ENDED &&
                        !finished
                    ) {

                        finished =
                            true


                        onFinished()
                    }
                }


                override fun onPlayerError(
                    error:
                    androidx.media3.common.PlaybackException
                ) {

                    // Si por alguna razón el video falla,
                    // la app continúa normalmente.

                    if (
                        !finished
                    ) {

                        finished =
                            true


                        onFinished()
                    }
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

                exoPlayer.release()

            } catch (_: Exception) {
            }
        }
    }


    // ================================================================
    // VIDEO FULL SCREEN
    // ================================================================

    AndroidView(

        factory = {
                ctx ->

            PlayerView(
                ctx
            ).apply {

                player =
                    exoPlayer


                useController =
                    false


                resizeMode =
                    AspectRatioFrameLayout
                        .RESIZE_MODE_ZOOM


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
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black
                )
    )
}