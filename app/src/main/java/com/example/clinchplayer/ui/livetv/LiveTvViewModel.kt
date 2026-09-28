package com.example.clinchplayer.ui.livetv

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinchplayer.data.IPTVSession
import com.example.clinchplayer.data.SessionManager
import com.example.clinchplayer.network.RetrofitClient
import com.example.clinchplayer.network.models.LiveCategory
import com.example.clinchplayer.network.models.LiveStream
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LiveTvViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private var session: IPTVSession? = null
    private var streamsJob: Job? = null

    var categories by mutableStateOf<List<LiveCategory>>(emptyList())
    var streams by mutableStateOf<List<LiveStream>>(emptyList())

    var selectedCategory by mutableStateOf<LiveCategory?>(null)
    var selectedStream by mutableStateOf<LiveStream?>(null)

    var isLoadingCategories by mutableStateOf(false)
    var isLoadingStreams by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                session = sessionManager.sessionFlow.first()
                if (session?.isLoggedIn == true) {
                    loadCategories()
                } else {
                    errorMessage = "Sesión no válida"
                }
            } catch (e: Exception) {
                Log.e("LiveTvViewModel", "Error al obtener sesión", e)
                errorMessage = "Error al obtener sesión"
            }
        }
    }

    fun getStreamUrl(
        stream: LiveStream,
        preferredFormat: String = "auto"
    ): String {
        val s = session ?: return ""
        val baseUrl = s.server.trim().removeSuffix("/")

        val extension =
            when (
                preferredFormat
                    .trim()
                    .lowercase()
            ) {
                "ts" ->
                    ".ts"

                "m3u8" ->
                    ".m3u8"

                else -> {
                    val original =
                        stream.containerExtension
                            ?.trim()
                            ?.removePrefix(".")
                            ?.takeIf {
                                it.isNotBlank()
                            }

                    if (original != null) {
                        ".$original"
                    } else {
                        ".ts"
                    }
                }
            }

        Log.d(
            "LiveTvViewModel",
            "Constructing stream URL. StreamId: ${stream.streamId}, PreferredFormat: $preferredFormat, Extension: $extension"
        )

        return "$baseUrl/live/${s.username}/${s.password}/${stream.streamId}$extension"
    }

    fun selectStream(stream: LiveStream) {
        Log.d("LiveTvViewModel", "Channel selected: ${stream.streamId} - ${stream.name}")
        Log.d("LiveTvViewModel", "Stream Details -> type: ${stream.streamType}, ext: ${stream.containerExtension}")
        selectedStream = stream
    }

    private suspend fun loadCategories() {
        val s = session ?: return
        isLoadingCategories = true
        errorMessage = null
        try {
            val api = RetrofitClient.createApi(s.server)
            Log.d("LiveTvViewModel", "Requesting action=get_live_categories for user: ${s.username}")
            val response = api.getLiveCategories(s.username, s.password)

            Log.d("LiveTvViewModel", "HTTP Status (Categories): ${response.code()}")
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                Log.d("LiveTvViewModel", "Categories received: ${list.size}")
                categories = list
                if (categories.isNotEmpty() && selectedCategory == null) {
                    selectCategory(categories.first())
                }
            } else {
                Log.e("LiveTvViewModel", "Error en categorías: ${response.code()}")
                errorMessage = "Error cargando categorías (Code ${response.code()})"
            }
        } catch (e: Exception) {
            Log.e("LiveTvViewModel", "Excepción cargando categorías", e)
            errorMessage = "Error de conexión al cargar categorías"
        } finally {
            isLoadingCategories = false
        }
    }

    fun selectCategory(category: LiveCategory) {
        if (selectedCategory?.categoryId == category.categoryId) return
        selectedCategory = category
        streamsJob?.cancel()
        streamsJob = viewModelScope.launch {
            delay(300) // Debounce para navegación rápida
            loadStreams(category.categoryId)
        }
    }

    private suspend fun loadStreams(categoryId: String) {
        val s = session ?: return
        isLoadingStreams = true
        try {
            val api = RetrofitClient.createApi(s.server)
            Log.d("LiveTvViewModel", "Requesting action=get_live_streams category_id=$categoryId")
            val response = api.getLiveStreams(s.username, s.password, categoryId = categoryId)

            Log.d("LiveTvViewModel", "HTTP Status (Streams): ${response.code()}")
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                Log.d("LiveTvViewModel", "Streams received for category $categoryId: ${list.size}")
                streams = list
            } else {
                Log.e("LiveTvViewModel", "Error en canales: ${response.code()}")
                // No mostramos error global para no tapar las categorías si fallan los canales
            }
        } catch (e: Exception) {
            Log.e("LiveTvViewModel", "Excepción cargando canales", e)
        } finally {
            isLoadingStreams = false
        }
    }


    // ================================================================
    // CATCH UP / REFRESH
    // ================================================================

    fun refreshData() {
        streamsJob?.cancel()

        selectedCategory = null
        selectedStream = null
        streams = emptyList()
        errorMessage = null

        loadData()
    }

}
