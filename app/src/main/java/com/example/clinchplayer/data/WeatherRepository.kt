package com.example.clinchplayer.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume

/** Datos mínimos que necesita la tarjeta y el Hero del Home. */
data class WeatherInfo(
    val temperatureF: Double,
    val apparentTemperatureF: Double,
    val weatherCode: Int,
    val isDay: Boolean,
    val windSpeedMph: Double,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val updatedAt: Long
) {
    val conditionText: String
        get() = weatherConditionText(weatherCode)

    val icon: String
        get() = weatherIcon(weatherCode, isDay)

    val heroImageUrl: String
        get() = weatherHeroImageUrl(weatherCode, isDay)
}

class WeatherRepository {

    private companion object {
        const val TAG = "WeatherRepository"
        const val PREFS = "clinch_weather_cache"
        const val KEY_JSON = "weather_json"
        const val CACHE_MAX_AGE_MS = 45 * 60 * 1000L
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun getCached(context: Context): WeatherInfo? {
        val raw = context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_JSON, null)
            ?: return null

        return runCatching {
            val json = JSONObject(raw)
            WeatherInfo(
                temperatureF = json.getDouble("temperatureF"),
                apparentTemperatureF = json.getDouble("apparentTemperatureF"),
                weatherCode = json.getInt("weatherCode"),
                isDay = json.getBoolean("isDay"),
                windSpeedMph = json.getDouble("windSpeedMph"),
                locationName = json.getString("locationName"),
                latitude = json.getDouble("latitude"),
                longitude = json.getDouble("longitude"),
                updatedAt = json.getLong("updatedAt")
            )
        }.getOrNull()
    }

    fun cacheIsFresh(info: WeatherInfo?): Boolean {
        if (info == null) return false
        return System.currentTimeMillis() - info.updatedAt < CACHE_MAX_AGE_MS
    }

    suspend fun refresh(context: Context): WeatherInfo? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "No location permission. Returning cached weather if available.")
            return@withContext getCached(context)
        }

        val location = getBestLocation(context)
        if (location == null) {
            Log.w(TAG, "No valid device location available. Returning cached weather if available.")
            return@withContext getCached(context)
        }

        Log.d(TAG, "Using location provider=${location.provider} lat=${location.latitude} lon=${location.longitude}")

        val locationName = reverseGeocode(context, location) ?: "Ubicación actual"

        val weather = fetchCurrentWeather(
            latitude = location.latitude,
            longitude = location.longitude,
            locationName = locationName
        )

        if (weather == null) {
            Log.w(TAG, "Open-Meteo request failed. Returning cached weather if available.")
            return@withContext getCached(context)
        }

        saveCache(context, weather)
        Log.d(TAG, "Weather updated: ${weather.temperatureF}F ${weather.conditionText} at ${weather.locationName}")
        weather
    }

    @SuppressLint("MissingPermission")
    private suspend fun getBestLocation(context: Context): Location? {
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!coarseGranted && !fineGranted) {
            Log.w(TAG, "getBestLocation: permission not granted")
            return null
        }

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val allProviders = try {
            manager.allProviders.orEmpty()
        } catch (e: SecurityException) {
            Log.e(TAG, "Unable to read location providers", e)
            return null
        }

        val preferredProviders = listOf(
            "fused",
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        ).filter { it in allProviders }.distinct()

        Log.d(TAG, "Available providers=$allProviders preferred=$preferredProviders")

        // 1) First use the newest last-known location from any provider.
        val cached = preferredProviders
            .mapNotNull { provider ->
                try {
                    manager.getLastKnownLocation(provider)
                } catch (e: SecurityException) {
                    Log.w(TAG, "No permission for lastKnown provider=$provider", e)
                    null
                } catch (e: Throwable) {
                    Log.w(TAG, "lastKnown failed provider=$provider: ${e.message}")
                    null
                }
            }
            .maxByOrNull { it.time }

        if (cached != null) {
            Log.d(TAG, "Last known location provider=${cached.provider} ageMs=${System.currentTimeMillis() - cached.time}")
            if (System.currentTimeMillis() - cached.time < 30 * 60 * 1000L) {
                return cached
            }
        }

        // 2) Android 11+: ask EACH usable provider for a current location.
        // The previous implementation tried only one provider. On the Android TV
        // emulator NETWORK_PROVIDER can return null even though GPS has a location.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            for (provider in preferredProviders) {
                if (provider == LocationManager.PASSIVE_PROVIDER) continue

                val enabled = runCatching { manager.isProviderEnabled(provider) }
                    .getOrDefault(true)

                if (!enabled) {
                    Log.d(TAG, "Skipping disabled provider=$provider")
                    continue
                }

                val current = withTimeoutOrNull(3500L) {
                    suspendCancellableCoroutine<Location?> { continuation ->
                        try {
                            manager.getCurrentLocation(
                                provider,
                                null,
                                context.mainExecutor
                            ) { location ->
                                if (continuation.isActive) {
                                    continuation.resume(location)
                                }
                            }
                        } catch (e: SecurityException) {
                            Log.e(TAG, "SecurityException current location provider=$provider", e)
                            if (continuation.isActive) continuation.resume(null)
                        } catch (e: Throwable) {
                            Log.w(TAG, "Current location failed provider=$provider: ${e.message}")
                            if (continuation.isActive) continuation.resume(null)
                        }
                    }
                }

                if (current != null) {
                    Log.d(TAG, "Current location received from provider=$provider")
                    return current
                } else {
                    Log.w(TAG, "Provider $provider returned no current location")
                }
            }
        }

        // 3) An older cached value is still better than no location at all.
        return cached
    }

    @Suppress("DEPRECATION")
    private fun reverseGeocode(context: Context, location: Location): String? {
        return runCatching {
            val geocoder = Geocoder(context, Locale("es", "PR"))
            val address = geocoder
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()

            val city = address?.locality
                ?: address?.subAdminArea
                ?: address?.adminArea

            val region = address?.adminArea

            when {
                !city.isNullOrBlank() && !region.isNullOrBlank() && city != region -> "$city, $region"
                !city.isNullOrBlank() -> city
                !region.isNullOrBlank() -> region
                else -> null
            }
        }.getOrNull()
    }

    private fun fetchCurrentWeather(
        latitude: Double,
        longitude: Double,
        locationName: String
    ): WeatherInfo? {
        val endpoint = buildString {
            append("https://api.open-meteo.com/v1/forecast")
            append("?latitude=").append(latitude)
            append("&longitude=").append(longitude)
            append("&current=temperature_2m,apparent_temperature,weather_code,is_day,wind_speed_10m")
            append("&temperature_unit=fahrenheit")
            append("&wind_speed_unit=mph")
            append("&timezone=auto")
        }

        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 6500
            readTimeout = 6500
            setRequestProperty("Accept", "application/json")
        }

        return try {
            val responseCode = connection.responseCode
            Log.d(TAG, "Open-Meteo HTTP $responseCode")
            if (responseCode !in 200..299) return null

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(body).getJSONObject("current")

            WeatherInfo(
                temperatureF = current.optDouble("temperature_2m", Double.NaN),
                apparentTemperatureF = current.optDouble("apparent_temperature", Double.NaN),
                weatherCode = current.optInt("weather_code", 0),
                isDay = current.optInt("is_day", 1) == 1,
                windSpeedMph = current.optDouble("wind_speed_10m", Double.NaN),
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                updatedAt = System.currentTimeMillis()
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Open-Meteo request/parsing failed", e)
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun saveCache(context: Context, info: WeatherInfo) {
        val json = JSONObject().apply {
            put("temperatureF", info.temperatureF)
            put("apparentTemperatureF", info.apparentTemperatureF)
            put("weatherCode", info.weatherCode)
            put("isDay", info.isDay)
            put("windSpeedMph", info.windSpeedMph)
            put("locationName", info.locationName)
            put("latitude", info.latitude)
            put("longitude", info.longitude)
            put("updatedAt", info.updatedAt)
        }

        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_JSON, json.toString())
            .apply()
    }
}

fun weatherConditionText(code: Int): String = when (code) {
    0 -> "Despejado"
    1 -> "Mayormente despejado"
    2 -> "Parcialmente nublado"
    3 -> "Nublado"
    45, 48 -> "Neblina"
    51, 53, 55 -> "Llovizna"
    56, 57 -> "Llovizna helada"
    61 -> "Lluvia ligera"
    63 -> "Lluvia"
    65 -> "Lluvia fuerte"
    66, 67 -> "Lluvia helada"
    71 -> "Nieve ligera"
    73 -> "Nieve"
    75, 77 -> "Nieve fuerte"
    80, 81, 82 -> "Aguaceros"
    85, 86 -> "Aguaceros de nieve"
    95 -> "Tormentas eléctricas"
    96, 99 -> "Tormentas fuertes"
    else -> "Condiciones actuales"
}

fun weatherIcon(code: Int, isDay: Boolean): String = when {
    !isDay && code <= 2 -> "☾"
    code == 0 -> "☀"
    code in 1..3 -> "☁"
    code in 45..48 -> "≋"
    code in 51..67 || code in 80..82 -> "☂"
    code in 71..77 || code in 85..86 -> "❄"
    code >= 95 -> "ϟ"
    else -> "☁"
}

/**
 * Imágenes CC0 alojadas en Wikimedia Commons. Coil sigue el redirect de Special:FilePath.
 */
fun weatherHeroImageUrl(code: Int, isDay: Boolean): String = when {
    !isDay ->
        "https://commons.wikimedia.org/wiki/Special:FilePath/Night%20sky%20and%20trees.jpg"

    code == 0 || code in 1..2 ->
        "https://commons.wikimedia.org/wiki/Special:FilePath/BlueSkyWhiteClouds27.jpg"

    code in 45..48 ->
        "https://commons.wikimedia.org/wiki/Special:FilePath/Fog%20landscape%20scenic.jpg"

    code in 51..67 || code in 80..82 || code >= 95 ->
        "https://commons.wikimedia.org/wiki/Special:FilePath/Dark%20Clouds%20in%20the%20Sky.jpg"

    else ->
        "https://commons.wikimedia.org/wiki/Special:FilePath/IMG%20Clouds%20sky.jpg"
}
