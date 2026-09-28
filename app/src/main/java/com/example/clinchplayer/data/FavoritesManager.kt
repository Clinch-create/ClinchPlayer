package com.example.clinchplayer.data

import android.content.Context
import com.example.clinchplayer.network.models.LiveStream
import com.example.clinchplayer.network.models.SeriesStream
import com.example.clinchplayer.network.models.VodStream

class FavoritesManager(
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            "clinch_player_favorites",
            Context.MODE_PRIVATE
        )


    companion object {

        private const val KEY_FAVORITE_LIVE =
            "favorite_live"

        private const val KEY_FAVORITE_MOVIES =
            "favorite_movies"

        private const val KEY_FAVORITE_SERIES =
            "favorite_series"
    }


    // ============================================================
    // COMPATIBILIDAD CON MOVIE PLAYER
    // ============================================================

    /*
     * MoviePlayerScreen actualmente usa:
     *
     * favoritesManager.isFavorite(movie)
     *
     * y
     *
     * favoritesManager.toggleFavorite(movie)
     *
     * Estas funciones mantienen compatibilidad.
     */

    fun isFavorite(
        movie: VodStream
    ): Boolean {

        return isMovieFavorite(
            movie.streamId.toString()
        )
    }


    fun toggleFavorite(
        movie: VodStream
    ): Boolean {

        return toggleMovieFavorite(
            movie.streamId.toString()
        )
    }


    // ============================================================
    // COMPATIBILIDAD CON SERIES
    // ============================================================

    fun isFavorite(
        series: SeriesStream
    ): Boolean {

        return isSeriesFavorite(
            series.seriesId.toString()
        )
    }


    fun toggleFavorite(
        series: SeriesStream
    ): Boolean {

        return toggleSeriesFavorite(
            series.seriesId.toString()
        )
    }


    // ============================================================
    // COMPATIBILIDAD CON TV EN VIVO
    // ============================================================

    fun isFavorite(
        stream: LiveStream
    ): Boolean {

        return isLiveFavorite(
            stream.streamId.toString()
        )
    }


    fun toggleFavorite(
        stream: LiveStream
    ): Boolean {

        return toggleLiveFavorite(
            stream.streamId.toString()
        )
    }


    // ============================================================
    // TV EN VIVO
    // ============================================================

    fun isLiveFavorite(
        streamId: String
    ): Boolean {

        return getLiveFavorites()
            .contains(streamId)
    }


    fun toggleLiveFavorite(
        streamId: String
    ): Boolean {

        val favorites =
            getLiveFavorites()
                .toMutableSet()


        val isNowFavorite =
            if (
                favorites.contains(
                    streamId
                )
            ) {

                favorites.remove(
                    streamId
                )

                false

            } else {

                favorites.add(
                    streamId
                )

                true
            }


        preferences
            .edit()
            .putStringSet(
                KEY_FAVORITE_LIVE,
                favorites
            )
            .apply()


        return isNowFavorite
    }


    fun addLiveFavorite(
        streamId: String
    ) {

        val favorites =
            getLiveFavorites()
                .toMutableSet()


        favorites.add(
            streamId
        )


        preferences
            .edit()
            .putStringSet(
                KEY_FAVORITE_LIVE,
                favorites
            )
            .apply()
    }


    fun removeLiveFavorite(
        streamId: String
    ) {

        val favorites =
            getLiveFavorites()
                .toMutableSet()


        favorites.remove(
            streamId
        )


        preferences
            .edit()
            .putStringSet(
                KEY_FAVORITE_LIVE,
                favorites
            )
            .apply()
    }


    fun getLiveFavorites(): Set<String> {

        return preferences
            .getStringSet(
                KEY_FAVORITE_LIVE,
                emptySet()
            )
            ?.toSet()
            ?: emptySet()
    }


    // ============================================================
    // PELÍCULAS
    // ============================================================

    fun isMovieFavorite(
        streamId: String
    ): Boolean {

        return getMovieFavorites()
            .contains(streamId)
    }


    fun toggleMovieFavorite(
        streamId: String
    ): Boolean {

        val favorites =
            getMovieFavorites()
                .toMutableSet()


        val isNowFavorite =
            if (
                favorites.contains(
                    streamId
                )
            ) {

                favorites.remove(
                    streamId
                )

                false

            } else {

                favorites.add(
                    streamId
                )

                true
            }


        preferences
            .edit()
            .putStringSet(
                KEY_FAVORITE_MOVIES,
                favorites
            )
            .apply()


        return isNowFavorite
    }


    fun addMovieFavorite(
        streamId: String
    ) {

        val favorites =
            getMovieFavorites()
                .toMutableSet()


        favorites.add(
            streamId
        )


        preferences
            .edit()
            .putStringSet(
                KEY_FAVORITE_MOVIES,
                favorites
            )
            .apply()
    }


    fun removeMovieFavorite(
        streamId: String
    ) {

        val favorites =
            getMovieFavorites()
                .toMutableSet()


        favorites.remove(
            streamId
        )


        preferences
            .edit()
            .putStringSet(
                KEY_FAVORITE_MOVIES,
                favorites
            )
            .apply()
    }


    fun getMovieFavorites(): Set<String> {

        return preferences
            .getStringSet(
                KEY_FAVORITE_MOVIES,
                emptySet()
            )
            ?.toSet()
            ?: emptySet()
    }


    // ============================================================
    // SERIES
    // ============================================================

    fun isSeriesFavorite(
        seriesId: String
    ): Boolean {

        return getSeriesFavorites()
            .contains(seriesId)
    }


    fun toggleSeriesFavorite(
        seriesId: String
    ): Boolean {

        val favorites =
            getSeriesFavorites()
                .toMutableSet()


        val isNowFavorite =
            if (
                favorites.contains(
                    seriesId
                )
            ) {

                favorites.remove(
                    seriesId
                )

                false

            } else {

                favorites.add(
                    seriesId
                )

                true
            }


        preferences
            .edit()
            .putStringSet(
                KEY_FAVORITE_SERIES,
                favorites
            )
            .apply()


        return isNowFavorite
    }


    fun addSeriesFavorite(
        seriesId: String
    ) {

        val favorites =
            getSeriesFavorites()
                .toMutableSet()


        favorites.add(
            seriesId
        )


        preferences
            .edit()
            .putStringSet(
                KEY_FAVORITE_SERIES,
                favorites
            )
            .apply()
    }


    fun removeSeriesFavorite(
        seriesId: String
    ) {

        val favorites =
            getSeriesFavorites()
                .toMutableSet()


        favorites.remove(
            seriesId
        )


        preferences
            .edit()
            .putStringSet(
                KEY_FAVORITE_SERIES,
                favorites
            )
            .apply()
    }


    fun getSeriesFavorites(): Set<String> {

        return preferences
            .getStringSet(
                KEY_FAVORITE_SERIES,
                emptySet()
            )
            ?.toSet()
            ?: emptySet()
    }


    // ============================================================
    // CONTADORES
    // ============================================================

    fun liveCount(): Int {

        return getLiveFavorites()
            .size
    }


    fun movieCount(): Int {

        return getMovieFavorites()
            .size
    }


    fun seriesCount(): Int {

        return getSeriesFavorites()
            .size
    }


    fun totalCount(): Int {

        return liveCount() +
                movieCount() +
                seriesCount()
    }


    // ============================================================
    // BORRAR TODOS LOS FAVORITOS
    // ============================================================

    fun clearAllFavorites() {

        preferences
            .edit()
            .clear()
            .apply()
    }
}