package com.example.clinchplayer.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient {

    // ================================================================
    // OKHTTP CLIENT
    // ================================================================

    private val okHttpClient: OkHttpClient by lazy {

        OkHttpClient.Builder()

            // Tiempo máximo para conectar con el servidor
            .connectTimeout(
                15,
                TimeUnit.SECONDS
            )

            // Tiempo máximo esperando respuesta/datos
            .readTimeout(
                30,
                TimeUnit.SECONDS
            )

            // Tiempo máximo enviando datos
            .writeTimeout(
                15,
                TimeUnit.SECONDS
            )

            // Reintentar si la conexión falla temporalmente
            .retryOnConnectionFailure(
                true
            )

            .build()
    }


    // ================================================================
    // CREATE API
    // ================================================================

    fun createApi(
        baseUrl: String
    ): XtreamCodesApi {

        // ============================================================
        // CLEAN SERVER URL
        // ============================================================

        var sanitizedUrl =
            baseUrl.trim()


        // Si por alguna razón llega player_api.php,
        // dejamos solamente la dirección base del servidor.
        sanitizedUrl =
            sanitizedUrl.substringBefore(
                "/player_api.php",
                missingDelimiterValue = sanitizedUrl
            )


        // Si llega una URL M3U completa con get.php,
        // dejamos solamente la dirección base.
        sanitizedUrl =
            sanitizedUrl.substringBefore(
                "/get.php",
                missingDelimiterValue = sanitizedUrl
            )


        // ============================================================
        // ADD PROTOCOL IF MISSING
        // ============================================================

        if (
            !sanitizedUrl.startsWith(
                "http://",
                ignoreCase = true
            ) &&
            !sanitizedUrl.startsWith(
                "https://",
                ignoreCase = true
            )
        ) {

            sanitizedUrl =
                "http://$sanitizedUrl"
        }


        // ============================================================
        // RETROFIT REQUIRES FINAL /
        // ============================================================

        sanitizedUrl =
            sanitizedUrl.trimEnd('/') + "/"


        // ============================================================
        // RETROFIT
        // ============================================================

        return Retrofit.Builder()

            .baseUrl(
                sanitizedUrl
            )

            .client(
                okHttpClient
            )

            .addConverterFactory(
                GsonConverterFactory.create()
            )

            .build()

            .create(
                XtreamCodesApi::class.java
            )
    }
}