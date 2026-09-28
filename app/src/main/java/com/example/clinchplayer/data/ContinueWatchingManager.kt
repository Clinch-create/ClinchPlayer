package com.example.clinchplayer.data

import android.content.Context
import com.example.clinchplayer.network.models.SeriesEpisode
import com.example.clinchplayer.network.models.SeriesStream
import com.example.clinchplayer.network.models.VodStream
import com.google.gson.Gson
import org.json.JSONObject

/**
 * Historial persistente de Continue Watching para películas + series.
 *
 * Reglas:
 * - Se guarda después de 15 segundos.
 * - Se elimina al llegar al 95%.
 * - Máximo 20 elementos combinados.
 * - Para series se conserva solo el episodio más reciente de cada serie.
 * - SharedPreferences sobrevive a navegación y reinicios normales de la app.
 */

enum class ContinueWatchingType {
    MOVIE,
    EPISODE
}

data class ContinueWatchingMovie(
    val streamId: Int,
    val movie: VodStream?,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long
) {
    val progress: Float
        get() = if (durationMs > 0L) {
            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}

data class ContinueWatchingEpisode(
    val seriesId: Int,
    val episodeId: String,
    val series: SeriesStream?,
    val episode: SeriesEpisode?,
    val seasonNumber: String,
    val episodeNumber: Int,
    val url: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long
) {
    val progress: Float
        get() = if (durationMs > 0L) {
            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val episodeLabel: String
        get() = "T$seasonNumber • E$episodeNumber"
}

data class ContinueWatchingItem(
    val type: ContinueWatchingType,
    val movie: ContinueWatchingMovie? = null,
    val episode: ContinueWatchingEpisode? = null,
    val updatedAt: Long
)

class ContinueWatchingManager(
    context: Context
) {
    private val preferences =
        context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    private val gson = Gson()

    init {
        removeInvalidContinueWatchingEntries()
    }

    /**
     * Limpia automáticamente registros viejos o incompletos que pueden
     * producir tarjetas vacías en Continue Watching.
     *
     * Las películas válidas guardadas por la versión actual siempre tienen
     * el objeto VodStream completo, un streamId válido y un nombre.
     */
    private fun removeInvalidContinueWatchingEntries() {
        val keysToRemove = mutableListOf<String>()

        preferences.all.forEach { (key, value) ->
            when {
                key.startsWith(MOVIE_PREFIX) -> {
                    val streamId = key.removePrefix(MOVIE_PREFIX).toIntOrNull()
                    val raw = value as? String
                    val entry = if (streamId != null && raw != null) {
                        parseMovieEntry(streamId, raw)
                    } else {
                        null
                    }

                    val movie = entry?.movie
                    val valid =
                        entry != null &&
                            movie != null &&
                            entry.streamId > 0 &&
                            movie.streamId > 0 &&
                            movie.name.isNotBlank()

                    if (!valid) {
                        keysToRemove += key
                    }
                }

                key.startsWith(EPISODE_PREFIX) -> {
                    val raw = value as? String
                    val entry = raw?.let { parseEpisodeEntry(it) }

                    val valid =
                        entry != null &&
                            entry.seriesId > 0 &&
                            entry.series != null &&
                            entry.series.name.isNotBlank() &&
                            entry.episode != null &&
                            entry.episodeId.isNotBlank() &&
                            entry.url.isNotBlank()

                    if (!valid) {
                        keysToRemove += key
                    }
                }
            }
        }

        if (keysToRemove.isNotEmpty()) {
            preferences.edit().apply {
                keysToRemove.forEach(::remove)
            }.apply()
        }
    }

    // =================================================================
    // MOVIES
    // =================================================================

    fun saveMovieProgress(
        movie: VodStream,
        positionMs: Long,
        durationMs: Long
    ) {
        if (!shouldSave(positionMs, durationMs)) {
            if (isCompleted(positionMs, durationMs)) {
                removeMovie(movie.streamId)
            }
            return
        }

        val payload = JSONObject()
            .put(KEY_STREAM_ID, movie.streamId)
            .put(KEY_MOVIE_JSON, gson.toJson(movie))
            .put(KEY_POSITION, positionMs)
            .put(KEY_DURATION, durationMs)
            .put(KEY_UPDATED_AT, System.currentTimeMillis())
            .toString()

        preferences.edit()
            .putString(movieKey(movie.streamId), payload)
            .apply()

        trimOldEntries()
    }

    /** Compatibilidad con el código anterior de películas. */
    fun saveMovieProgress(
        streamId: Int,
        positionMs: Long,
        durationMs: Long
    ) {
        if (durationMs <= 0L || positionMs < MIN_PROGRESS_MS) return

        if (isCompleted(positionMs, durationMs)) {
            removeMovie(streamId)
            return
        }

        val existingMovie = getMovieProgress(streamId)?.movie

        if (existingMovie != null) {
            saveMovieProgress(
                movie = existingMovie,
                positionMs = positionMs,
                durationMs = durationMs
            )
            return
        }

        val value = listOf(
            positionMs,
            durationMs,
            System.currentTimeMillis()
        ).joinToString(LEGACY_SEPARATOR)

        preferences.edit()
            .putString(movieKey(streamId), value)
            .apply()

        trimOldEntries()
    }

    fun getMovieProgress(
        streamId: Int
    ): ContinueWatchingMovie? {
        val raw = preferences.getString(
            movieKey(streamId),
            null
        ) ?: return null

        return parseMovieEntry(streamId, raw)
    }

    fun getAllMovies(): List<ContinueWatchingMovie> {
        return preferences.all
            .mapNotNull { (key, value) ->
                if (!key.startsWith(MOVIE_PREFIX)) {
                    return@mapNotNull null
                }

                val streamId =
                    key.removePrefix(MOVIE_PREFIX)
                        .toIntOrNull()
                        ?: return@mapNotNull null

                parseMovieEntry(
                    streamId = streamId,
                    raw = value as? String ?: return@mapNotNull null
                )
            }
            .sortedByDescending { it.updatedAt }
    }

    fun removeMovie(
        streamId: Int
    ) {
        preferences.edit()
            .remove(movieKey(streamId))
            .apply()
    }

    // =================================================================
    // SERIES / EPISODES
    // =================================================================

    fun saveEpisodeProgress(
        series: SeriesStream,
        episode: SeriesEpisode,
        seasonNumber: String,
        episodeNumber: Int,
        url: String,
        positionMs: Long,
        durationMs: Long
    ) {
        val episodeId = episode.id.toString()

        if (!shouldSave(positionMs, durationMs)) {
            if (isCompleted(positionMs, durationMs)) {
                removeEpisode(series.seriesId, episodeId)
            }
            return
        }

        // Una sola tarjeta por serie: si empieza otro episodio de la misma
        // serie, reemplaza el anterior en Continue Watching.
        removeOtherEpisodesForSeries(
            seriesId = series.seriesId,
            keepEpisodeId = episodeId
        )

        val payload = JSONObject()
            .put(KEY_SERIES_ID, series.seriesId)
            .put(KEY_EPISODE_ID, episodeId)
            .put(KEY_SERIES_JSON, gson.toJson(series))
            .put(KEY_EPISODE_JSON, gson.toJson(episode))
            .put(KEY_SEASON_NUMBER, seasonNumber)
            .put(KEY_EPISODE_NUMBER, episodeNumber)
            .put(KEY_URL, url)
            .put(KEY_POSITION, positionMs)
            .put(KEY_DURATION, durationMs)
            .put(KEY_UPDATED_AT, System.currentTimeMillis())
            .toString()

        preferences.edit()
            .putString(
                episodeKey(series.seriesId, episodeId),
                payload
            )
            .apply()

        trimOldEntries()
    }

    fun getEpisodeProgress(
        seriesId: Int,
        episodeId: String
    ): ContinueWatchingEpisode? {
        val raw = preferences.getString(
            episodeKey(seriesId, episodeId),
            null
        ) ?: return null

        return parseEpisodeEntry(raw)
    }

    fun getAllEpisodes(): List<ContinueWatchingEpisode> {
        return preferences.all
            .mapNotNull { (key, value) ->
                if (!key.startsWith(EPISODE_PREFIX)) {
                    return@mapNotNull null
                }

                parseEpisodeEntry(
                    value as? String ?: return@mapNotNull null
                )
            }
            .sortedByDescending { it.updatedAt }
    }

    fun removeEpisode(
        seriesId: Int,
        episodeId: String
    ) {
        preferences.edit()
            .remove(episodeKey(seriesId, episodeId))
            .apply()
    }

    fun removeSeries(
        seriesId: Int
    ) {
        val editor = preferences.edit()
        val prefix = "$EPISODE_PREFIX${seriesId}_"

        preferences.all.keys
            .filter { it.startsWith(prefix) }
            .forEach(editor::remove)

        editor.apply()
    }

    // =================================================================
    // UNIFIED LIST
    // =================================================================

    fun getAllItems(): List<ContinueWatchingItem> {
        // Los registros legacy de películas pueden contener solamente
        // streamId + progreso y no el objeto VodStream. Se conservan en
        // SharedPreferences para permitir reanudar/migrar la película cuando
        // vuelva a reproducirse, pero NO se muestran como tarjetas vacías.
        val movies = getAllMovies()
            .filter { it.movie != null }
            .map {
                ContinueWatchingItem(
                    type = ContinueWatchingType.MOVIE,
                    movie = it,
                    updatedAt = it.updatedAt
                )
            }

        // Solo se muestran episodios suficientemente completos para que la
        // tarjeta pueda abrirse correctamente desde el Home.
        val episodes = getAllEpisodes()
            .filter {
                it.series != null &&
                    it.episode != null &&
                    it.url.isNotBlank()
            }
            .map {
                ContinueWatchingItem(
                    type = ContinueWatchingType.EPISODE,
                    episode = it,
                    updatedAt = it.updatedAt
                )
            }

        return (movies + episodes)
            .sortedByDescending { it.updatedAt }
            .take(MAX_ITEMS)
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    // =================================================================
    // PARSING
    // =================================================================

    private fun parseMovieEntry(
        streamId: Int,
        raw: String
    ): ContinueWatchingMovie? {
        if (raw.trimStart().startsWith("{")) {
            return try {
                val json = JSONObject(raw)

                val position = json.optLong(KEY_POSITION, -1L)
                val duration = json.optLong(KEY_DURATION, -1L)
                val updatedAt = json.optLong(KEY_UPDATED_AT, 0L)
                val movieJson = json.optString(KEY_MOVIE_JSON, "")

                if (position < 0L || duration <= 0L) {
                    return null
                }

                val movie =
                    if (movieJson.isNotBlank()) {
                        runCatching {
                            gson.fromJson(movieJson, VodStream::class.java)
                        }.getOrNull()
                    } else {
                        null
                    }

                ContinueWatchingMovie(
                    streamId = streamId,
                    movie = movie,
                    positionMs = position,
                    durationMs = duration,
                    updatedAt = updatedAt
                )
            } catch (_: Exception) {
                null
            }
        }

        val parts = raw.split(LEGACY_SEPARATOR)
        if (parts.size != 3) return null

        val position = parts[0].toLongOrNull() ?: return null
        val duration = parts[1].toLongOrNull() ?: return null
        val updatedAt = parts[2].toLongOrNull() ?: return null

        if (duration <= 0L) return null

        return ContinueWatchingMovie(
            streamId = streamId,
            movie = null,
            positionMs = position,
            durationMs = duration,
            updatedAt = updatedAt
        )
    }

    private fun parseEpisodeEntry(
        raw: String
    ): ContinueWatchingEpisode? {
        return try {
            val json = JSONObject(raw)

            val seriesId = json.optInt(KEY_SERIES_ID, -1)
            val episodeId = json.optString(KEY_EPISODE_ID, "")
            val seriesJson = json.optString(KEY_SERIES_JSON, "")
            val episodeJson = json.optString(KEY_EPISODE_JSON, "")
            val seasonNumber = json.optString(KEY_SEASON_NUMBER, "1")
            val episodeNumber = json.optInt(KEY_EPISODE_NUMBER, 1)
            val url = json.optString(KEY_URL, "")
            val position = json.optLong(KEY_POSITION, -1L)
            val duration = json.optLong(KEY_DURATION, -1L)
            val updatedAt = json.optLong(KEY_UPDATED_AT, 0L)

            if (
                seriesId < 0 ||
                episodeId.isBlank() ||
                position < 0L ||
                duration <= 0L
            ) {
                return null
            }

            val series = runCatching {
                gson.fromJson(seriesJson, SeriesStream::class.java)
            }.getOrNull()

            val episode = runCatching {
                gson.fromJson(episodeJson, SeriesEpisode::class.java)
            }.getOrNull()

            ContinueWatchingEpisode(
                seriesId = seriesId,
                episodeId = episodeId,
                series = series,
                episode = episode,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                url = url,
                positionMs = position,
                durationMs = duration,
                updatedAt = updatedAt
            )
        } catch (_: Exception) {
            null
        }
    }

    // =================================================================
    // HELPERS
    // =================================================================

    private fun shouldSave(
        positionMs: Long,
        durationMs: Long
    ): Boolean {
        if (durationMs <= 0L) return false
        if (positionMs < MIN_PROGRESS_MS) return false
        if (isCompleted(positionMs, durationMs)) return false
        return true
    }

    private fun isCompleted(
        positionMs: Long,
        durationMs: Long
    ): Boolean {
        if (durationMs <= 0L) return false

        val progress =
            (positionMs.toDouble() / durationMs.toDouble())
                .coerceIn(0.0, 1.0)

        return progress >= COMPLETED_THRESHOLD
    }

    private fun removeOtherEpisodesForSeries(
        seriesId: Int,
        keepEpisodeId: String
    ) {
        val editor = preferences.edit()
        val prefix = "$EPISODE_PREFIX${seriesId}_"
        val keepKey = episodeKey(seriesId, keepEpisodeId)

        preferences.all.keys
            .filter {
                it.startsWith(prefix) && it != keepKey
            }
            .forEach(editor::remove)

        editor.apply()
    }

    private fun trimOldEntries() {
        val entries = getAllItemsUntrimmed()

        if (entries.size <= MAX_ITEMS) return

        val editor = preferences.edit()

        entries.drop(MAX_ITEMS).forEach { item ->
            when (item.type) {
                ContinueWatchingType.MOVIE -> {
                    item.movie?.let {
                        editor.remove(movieKey(it.streamId))
                    }
                }

                ContinueWatchingType.EPISODE -> {
                    item.episode?.let {
                        editor.remove(
                            episodeKey(it.seriesId, it.episodeId)
                        )
                    }
                }
            }
        }

        editor.apply()
    }

    private fun getAllItemsUntrimmed(): List<ContinueWatchingItem> {
        val movies = getAllMovies()
            .filter { it.movie != null }
            .map {
                ContinueWatchingItem(
                    type = ContinueWatchingType.MOVIE,
                    movie = it,
                    updatedAt = it.updatedAt
                )
            }

        val episodes = getAllEpisodes()
            .filter {
                it.series != null &&
                    it.episode != null &&
                    it.url.isNotBlank()
            }
            .map {
                ContinueWatchingItem(
                    type = ContinueWatchingType.EPISODE,
                    episode = it,
                    updatedAt = it.updatedAt
                )
            }

        return (movies + episodes)
            .sortedByDescending { it.updatedAt }
    }

    private fun movieKey(
        streamId: Int
    ): String = "$MOVIE_PREFIX$streamId"

    private fun episodeKey(
        seriesId: Int,
        episodeId: String
    ): String = "$EPISODE_PREFIX${seriesId}_$episodeId"

    companion object {
        private const val PREFS_NAME = "clinch_continue_watching"
        private const val MOVIE_PREFIX = "movie_"
        private const val EPISODE_PREFIX = "episode_"

        private const val KEY_STREAM_ID = "streamId"
        private const val KEY_MOVIE_JSON = "movieJson"
        private const val KEY_SERIES_ID = "seriesId"
        private const val KEY_EPISODE_ID = "episodeId"
        private const val KEY_SERIES_JSON = "seriesJson"
        private const val KEY_EPISODE_JSON = "episodeJson"
        private const val KEY_SEASON_NUMBER = "seasonNumber"
        private const val KEY_EPISODE_NUMBER = "episodeNumber"
        private const val KEY_URL = "url"
        private const val KEY_POSITION = "positionMs"
        private const val KEY_DURATION = "durationMs"
        private const val KEY_UPDATED_AT = "updatedAt"

        private const val LEGACY_SEPARATOR = "|"
        private const val MAX_ITEMS = 20
        private const val MIN_PROGRESS_MS = 15_000L
        private const val COMPLETED_THRESHOLD = 0.95
    }
}
