package com.example.clinchplayer.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

data class UserProfile(
    val id: String,
    val name: String,
    val isKid: Boolean = false
)

class SettingsManager(
    context: Context
) {

    private val appContext = context.applicationContext

    private val preferences =
        context.getSharedPreferences(
            "clinch_player_settings",
            Context.MODE_PRIVATE
        )

    companion object {
        private const val KEY_AUTO_PLAY_NEXT_EPISODE = "auto_play_next_episode"
        private const val KEY_REMEMBER_LAST_SECTION = "remember_last_section"
        private const val KEY_ANIMATIONS_ENABLED = "animations_enabled"
        private const val KEY_LAST_SECTION = "last_section"
        private const val KEY_APP_LANGUAGE = "app_language"

        private const val KEY_AUTO_START_ON_BOOT = "auto_start_on_boot"
        private const val KEY_SHOW_FULL_EPG = "show_full_epg"
        private const val KEY_SUBTITLES_ENABLED = "subtitles_enabled"
        private const val KEY_AUTO_PLAY_NEXT_DELAY = "auto_play_next_delay"
        private const val KEY_AUTO_CLEAR_CACHE = "auto_clear_cache"
        private const val KEY_SHOW_EPG_IN_CHANNEL_LIST = "show_epg_in_channel_list"
        private const val KEY_AUTO_PLAY_LIVE_CHANNEL = "auto_play_live_channel"
        private const val KEY_RECENTLY_ADDED_LIMIT = "recently_added_limit"
        private const val KEY_CHANNEL_HISTORY_LIMIT = "channel_history_limit"
        private const val KEY_USER_AGENT = "user_agent"

        private const val KEY_DECODER_MODE = "decoder_mode"
        private const val KEY_BUFFER_SIZE = "buffer_size"
        private const val KEY_HARDWARE_AUDIO = "hardware_audio"
        private const val KEY_OPEN_GL = "open_gl"

        private const val KEY_STREAM_FORMAT = "stream_format"

        private const val KEY_CONTENT_AUTO_UPDATE = "content_auto_update"
        private const val KEY_CONTENT_UPDATE_DAYS = "content_update_days"
        private const val KEY_EPG_AUTO_UPDATE = "epg_auto_update"
        private const val KEY_EPG_UPDATE_DAYS = "epg_update_days"

        private const val KEY_EPG_TIMESHIFT = "epg_timeshift"

        private const val KEY_MULTI_SCREEN_ENABLED = "multi_screen_enabled"
        private const val KEY_MULTI_SCREEN_LAYOUT = "multi_screen_layout"

        private const val KEY_USE_24_HOUR = "use_24_hour"

        private const val KEY_RECORDING_DIRECTORY = "recording_directory"

        private const val KEY_PROFILES = "profiles"
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
    }

    init {
        applyLanguage(appLanguage)
        ensureDefaultProfile()
    }

    var appLanguage: String
        get() = preferences.getString(KEY_APP_LANGUAGE, "es") ?: "es"
        set(value) {
            val language = if (value == "en") "en" else "es"

            preferences
                .edit()
                .putString(KEY_APP_LANGUAGE, language)
                .apply()

            applyLanguage(language)
        }

    private fun applyLanguage(language: String) {
        val languageTag =
            if (language == "en") {
                "en"
            } else {
                "es"
            }

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageTag)
        )
    }

    var autoPlayNextEpisode: Boolean
        get() = preferences.getBoolean(KEY_AUTO_PLAY_NEXT_EPISODE, true)
        set(value) = preferences.edit().putBoolean(KEY_AUTO_PLAY_NEXT_EPISODE, value).apply()

    var rememberLastSection: Boolean
        get() = preferences.getBoolean(KEY_REMEMBER_LAST_SECTION, true)
        set(value) = preferences.edit().putBoolean(KEY_REMEMBER_LAST_SECTION, value).apply()

    var animationsEnabled: Boolean
        get() = preferences.getBoolean(KEY_ANIMATIONS_ENABLED, true)
        set(value) = preferences.edit().putBoolean(KEY_ANIMATIONS_ENABLED, value).apply()

    var lastSection: String
        get() = preferences.getString(KEY_LAST_SECTION, "home") ?: "home"
        set(value) = preferences.edit().putString(KEY_LAST_SECTION, value).apply()

    var autoStartOnBoot: Boolean
        get() = preferences.getBoolean(KEY_AUTO_START_ON_BOOT, false)
        set(value) = preferences.edit().putBoolean(KEY_AUTO_START_ON_BOOT, value).apply()

    var showFullEpg: Boolean
        get() = preferences.getBoolean(KEY_SHOW_FULL_EPG, true)
        set(value) = preferences.edit().putBoolean(KEY_SHOW_FULL_EPG, value).apply()

    var subtitlesEnabled: Boolean
        get() = preferences.getBoolean(KEY_SUBTITLES_ENABLED, true)
        set(value) = preferences.edit().putBoolean(KEY_SUBTITLES_ENABLED, value).apply()

    var autoPlayNextDelaySeconds: Int
        get() = preferences.getInt(KEY_AUTO_PLAY_NEXT_DELAY, 30)
        set(value) = preferences.edit().putInt(KEY_AUTO_PLAY_NEXT_DELAY, value.coerceIn(0, 120)).apply()

    var autoClearCache: Boolean
        get() = preferences.getBoolean(KEY_AUTO_CLEAR_CACHE, false)
        set(value) = preferences.edit().putBoolean(KEY_AUTO_CLEAR_CACHE, value).apply()

    var showEpgInChannelList: Boolean
        get() = preferences.getBoolean(KEY_SHOW_EPG_IN_CHANNEL_LIST, true)
        set(value) = preferences.edit().putBoolean(KEY_SHOW_EPG_IN_CHANNEL_LIST, value).apply()

    var autoPlayLiveChannel: Boolean
        get() = preferences.getBoolean(KEY_AUTO_PLAY_LIVE_CHANNEL, false)
        set(value) = preferences.edit().putBoolean(KEY_AUTO_PLAY_LIVE_CHANNEL, value).apply()

    var recentlyAddedLimit: Int
        get() = preferences.getInt(KEY_RECENTLY_ADDED_LIMIT, 30)
        set(value) = preferences.edit().putInt(KEY_RECENTLY_ADDED_LIMIT, value.coerceIn(10, 100)).apply()

    var channelHistoryLimit: Int
        get() = preferences.getInt(KEY_CHANNEL_HISTORY_LIMIT, 10)
        set(value) = preferences.edit().putInt(KEY_CHANNEL_HISTORY_LIMIT, value.coerceIn(0, 100)).apply()

    var userAgent: String
        get() = preferences.getString(KEY_USER_AGENT, "") ?: ""
        set(value) = preferences.edit().putString(KEY_USER_AGENT, value).apply()

    var decoderMode: String
        get() = preferences.getString(KEY_DECODER_MODE, "hardware") ?: "hardware"
        set(value) = preferences.edit().putString(KEY_DECODER_MODE, value).apply()

    var bufferSize: Int
        get() = preferences.getInt(KEY_BUFFER_SIZE, 20)
        set(value) = preferences.edit().putInt(KEY_BUFFER_SIZE, value.coerceIn(5, 60)).apply()

    var hardwareAudioEnabled: Boolean
        get() = preferences.getBoolean(KEY_HARDWARE_AUDIO, false)
        set(value) = preferences.edit().putBoolean(KEY_HARDWARE_AUDIO, value).apply()

    var openGlEnabled: Boolean
        get() = preferences.getBoolean(KEY_OPEN_GL, false)
        set(value) = preferences.edit().putBoolean(KEY_OPEN_GL, value).apply()

    var streamFormat: String
        get() = preferences.getString(KEY_STREAM_FORMAT, "auto") ?: "auto"
        set(value) = preferences.edit().putString(KEY_STREAM_FORMAT, value).apply()

    var contentAutoUpdateEnabled: Boolean
        get() = preferences.getBoolean(KEY_CONTENT_AUTO_UPDATE, true)
        set(value) = preferences.edit().putBoolean(KEY_CONTENT_AUTO_UPDATE, value).apply()

    var contentUpdateDays: Int
        get() = preferences.getInt(KEY_CONTENT_UPDATE_DAYS, 2)
        set(value) = preferences.edit().putInt(KEY_CONTENT_UPDATE_DAYS, value.coerceIn(1, 7)).apply()

    var epgAutoUpdateEnabled: Boolean
        get() = preferences.getBoolean(KEY_EPG_AUTO_UPDATE, true)
        set(value) = preferences.edit().putBoolean(KEY_EPG_AUTO_UPDATE, value).apply()

    var epgUpdateDays: Int
        get() = preferences.getInt(KEY_EPG_UPDATE_DAYS, 1)
        set(value) = preferences.edit().putInt(KEY_EPG_UPDATE_DAYS, value.coerceIn(1, 7)).apply()

    var epgTimeshiftHours: Int
        get() = preferences.getInt(KEY_EPG_TIMESHIFT, 0)
        set(value) = preferences.edit().putInt(KEY_EPG_TIMESHIFT, value.coerceIn(-12, 12)).apply()

    var multiScreenEnabled: Boolean
        get() = preferences.getBoolean(KEY_MULTI_SCREEN_ENABLED, true)
        set(value) = preferences.edit().putBoolean(KEY_MULTI_SCREEN_ENABLED, value).apply()

    var multiScreenLayout: String
        get() = preferences.getString(KEY_MULTI_SCREEN_LAYOUT, "2x2") ?: "2x2"
        set(value) = preferences.edit().putString(KEY_MULTI_SCREEN_LAYOUT, value).apply()

    var use24HourFormat: Boolean
        get() = preferences.getBoolean(KEY_USE_24_HOUR, false)
        set(value) = preferences.edit().putBoolean(KEY_USE_24_HOUR, value).apply()

    var recordingDirectoryUri: String
        get() = preferences.getString(KEY_RECORDING_DIRECTORY, "") ?: ""
        set(value) = preferences.edit().putString(KEY_RECORDING_DIRECTORY, value).apply()

    fun saveLastSection(route: String) {
        if (!rememberLastSection) return

        val allowedRoutes =
            listOf(
                "home",
                "live_tv",
                "movies",
                "series",
                "search",
                "favorites",
                "continue_watching",
                "settings"
            )

        if (route in allowedRoutes) {
            lastSection = route
        }
    }

    fun getStartSection(): String {
        if (!rememberLastSection) return "home"
        return lastSection
    }

    val profiles: List<UserProfile>
        get() = loadProfiles()

    var activeProfileId: String
        get() {
            val saved = preferences.getString(KEY_ACTIVE_PROFILE_ID, null)
            val currentProfiles = loadProfiles()

            if (saved != null && currentProfiles.any { it.id == saved }) {
                return saved
            }

            val firstId = currentProfiles.first().id
            preferences.edit().putString(KEY_ACTIVE_PROFILE_ID, firstId).apply()
            return firstId
        }
        private set(value) {
            preferences.edit().putString(KEY_ACTIVE_PROFILE_ID, value).apply()
        }

    fun getActiveProfile(): UserProfile =
        loadProfiles().firstOrNull { it.id == activeProfileId }
            ?: loadProfiles().first()

    fun setActiveProfile(profileId: String) {
        if (loadProfiles().any { it.id == profileId }) {
            activeProfileId = profileId
        }
    }

    fun addProfile(
        name: String,
        isKid: Boolean = false
    ): UserProfile {
        val cleanName = name.trim().ifBlank { "Perfil" }
        val profile =
            UserProfile(
                id = UUID.randomUUID().toString(),
                name = cleanName.take(24),
                isKid = isKid
            )

        val updated = loadProfiles().toMutableList()
        updated.add(profile)
        saveProfiles(updated)

        return profile
    }

    fun deleteProfile(profileId: String): Boolean {
        val current = loadProfiles().toMutableList()

        if (current.size <= 1) return false

        val removed = current.removeAll { it.id == profileId }

        if (!removed) return false

        saveProfiles(current)

        if (activeProfileId == profileId) {
            activeProfileId = current.first().id
        }

        return true
    }

    fun parentalControlEnabled(
        profileId: String = activeProfileId
    ): Boolean =
        preferences.getBoolean(
            "parental_enabled_$profileId",
            false
        )

    fun setParentalControlEnabled(
        enabled: Boolean,
        profileId: String = activeProfileId
    ) {
        preferences
            .edit()
            .putBoolean(
                "parental_enabled_$profileId",
                enabled
            )
            .apply()
    }


    fun getBlockedCategoryIds(
        contentType: String,
        profileId: String = activeProfileId
    ): Set<String> {
        return preferences
            .getStringSet(
                blockedCategoryKey(
                    contentType,
                    profileId
                ),
                emptySet()
            )
            ?.toSet()
            ?: emptySet()
    }

    fun isCategoryBlocked(
        contentType: String,
        categoryId: String,
        profileId: String = activeProfileId
    ): Boolean =
        categoryId in
                getBlockedCategoryIds(
                    contentType,
                    profileId
                )

    fun setCategoryBlocked(
        contentType: String,
        categoryId: String,
        blocked: Boolean,
        profileId: String = activeProfileId
    ) {
        val current =
            getBlockedCategoryIds(
                contentType,
                profileId
            ).toMutableSet()

        if (blocked) {
            current.add(categoryId)
        } else {
            current.remove(categoryId)
        }

        preferences
            .edit()
            .putStringSet(
                blockedCategoryKey(
                    contentType,
                    profileId
                ),
                current
            )
            .apply()
    }

    fun setAllCategoriesBlocked(
        contentType: String,
        categoryIds: Collection<String>,
        blocked: Boolean,
        profileId: String = activeProfileId
    ) {
        val values =
            if (blocked) {
                categoryIds
                    .filter {
                        it.isNotBlank()
                    }
                    .toSet()
            } else {
                emptySet()
            }

        preferences
            .edit()
            .putStringSet(
                blockedCategoryKey(
                    contentType,
                    profileId
                ),
                values
            )
            .apply()
    }

    fun isCategoryHidden(
        contentType: String,
        categoryId: String,
        profileId: String = activeProfileId
    ): Boolean {
        if (
            !parentalControlEnabled(
                profileId
            )
        ) {
            return false
        }

        return isCategoryBlocked(
            contentType,
            categoryId,
            profileId
        )
    }

    private fun blockedCategoryKey(
        contentType: String,
        profileId: String
    ): String {
        val safeType =
            when (
                contentType
                    .trim()
                    .lowercase()
            ) {
                "live",
                "livetv",
                "live_tv" ->
                    "live"

                "movie",
                "movies",
                "vod" ->
                    "movies"

                "series",
                "tv_series" ->
                    "series"

                else ->
                    contentType
                        .trim()
                        .lowercase()
                        .replace(
                            Regex(
                                "[^a-z0-9_]"
                            ),
                            "_"
                        )
            }

        return "parental_blocked_${safeType}_$profileId"
    }


    fun hasParentalPin(
        profileId: String = activeProfileId
    ): Boolean =
        preferences.contains("parental_hash_$profileId") &&
                preferences.contains("parental_salt_$profileId")

    fun setParentalPin(
        pin: String,
        profileId: String = activeProfileId
    ): Boolean {
        if (pin.length != 4 || pin.any { !it.isDigit() }) {
            return false
        }

        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)

        val salt = saltBytes.joinToString("") { "%02x".format(it) }
        val hash = sha256("$salt:$pin")

        preferences
            .edit()
            .putString("parental_salt_$profileId", salt)
            .putString("parental_hash_$profileId", hash)
            .apply()

        return true
    }

    fun verifyParentalPin(
        pin: String,
        profileId: String = activeProfileId
    ): Boolean {
        val salt =
            preferences.getString(
                "parental_salt_$profileId",
                null
            ) ?: return false

        val savedHash =
            preferences.getString(
                "parental_hash_$profileId",
                null
            ) ?: return false

        return sha256("$salt:$pin") == savedHash
    }

    fun clearParentalPin(
        profileId: String = activeProfileId
    ) {
        preferences
            .edit()
            .remove("parental_salt_$profileId")
            .remove("parental_hash_$profileId")
            .putBoolean("parental_enabled_$profileId", false)
            .apply()
    }

    fun resetSettings() {
        preferences
            .edit()
            .clear()
            .apply()

        ensureDefaultProfile()
    }

    private fun ensureDefaultProfile() {
        if (loadProfilesInternal().isNotEmpty()) return

        val defaultProfile =
            UserProfile(
                id = UUID.randomUUID().toString(),
                name = "Principal",
                isKid = false
            )

        saveProfiles(listOf(defaultProfile))
        activeProfileId = defaultProfile.id
    }

    private fun loadProfiles(): List<UserProfile> {
        val loaded = loadProfilesInternal()

        if (loaded.isNotEmpty()) {
            return loaded
        }

        ensureDefaultProfile()
        return loadProfilesInternal()
    }

    private fun loadProfilesInternal(): List<UserProfile> {
        val raw =
            preferences.getString(
                KEY_PROFILES,
                null
            ) ?: return emptyList()

        return try {
            val array = JSONArray(raw)

            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)

                    add(
                        UserProfile(
                            id = item.getString("id"),
                            name = item.getString("name"),
                            isKid = item.optBoolean("isKid", false)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveProfiles(
        profiles: List<UserProfile>
    ) {
        val array = JSONArray()

        profiles.forEach { profile ->
            array.put(
                JSONObject()
                    .put("id", profile.id)
                    .put("name", profile.name)
                    .put("isKid", profile.isKid)
            )
        }

        preferences
            .edit()
            .putString(
                KEY_PROFILES,
                array.toString()
            )
            .apply()
    }

    private fun sha256(
        value: String
    ): String {
        val digest =
            MessageDigest
                .getInstance("SHA-256")
                .digest(value.toByteArray())

        return digest.joinToString("") { "%02x".format(it) }
    }
}