package com.example.clinchplayer.ui.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import android.os.Build
import com.example.clinchplayer.data.SessionManager
import com.example.clinchplayer.data.SettingsManager
import com.example.clinchplayer.data.UserProfile
import com.example.clinchplayer.network.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val ClinchYellow = Color(0xFFFFD600)
private val SettingsCard = Color(0xFF171717)
private val SettingsCardFocused = Color(0xFF242424)
private val SoftWhite = Color.White.copy(alpha = 0.62f)
private val DividerColor = Color.White.copy(alpha = 0.08f)


@Composable
private fun settingsText(es: String, en: String): String {
    val configuration = LocalConfiguration.current
    val language = configuration.locales[0]?.language ?: "es"
    return if (language == "en") en else es
}

@Composable
private fun SettingsCategory.localizedTitle(): String =
    when (this) {
        SettingsCategory.GENERAL -> settingsText("GENERAL", "GENERAL")
        SettingsCategory.PLAYER -> settingsText("REPRODUCTOR", "PLAYER")
        SettingsCategory.STREAM -> settingsText("FORMATO DE STREAM", "STREAM FORMAT")
        SettingsCategory.AUTOMATION -> settingsText("AUTOMATIZACIÓN", "AUTOMATION")
        SettingsCategory.EPG -> "EPG"
        SettingsCategory.PARENTAL -> settingsText("CONTROL PARENTAL", "PARENTAL CONTROL")
        SettingsCategory.MULTISCREEN -> settingsText("MULTI-PANTALLA", "MULTI-SCREEN")
        SettingsCategory.TIME -> settingsText("FORMATO DE HORA", "TIME FORMAT")
        SettingsCategory.RECORDINGS -> settingsText("GRABACIONES", "RECORDINGS")
        SettingsCategory.ACCOUNT -> settingsText("CUENTA / PLAYLIST", "ACCOUNT / PLAYLIST")
        SettingsCategory.PROFILES -> settingsText("PERFILES", "PROFILES")
        SettingsCategory.UPDATES -> settingsText("ACTUALIZACIONES", "UPDATES")
        SettingsCategory.ABOUT -> settingsText("ACERCA DE", "ABOUT")
    }

private data class ParentalCategoryEntry(
    val id: String,
    val name: String
)

private enum class SettingsCategory(
    val icon: String,
    val title: String
) {
    GENERAL("⚙", "GENERAL"),
    PLAYER("▶", "REPRODUCTOR"),
    STREAM("◉", "FORMATO DE STREAM"),
    AUTOMATION("↻", "AUTOMATIZACIÓN"),
    EPG("▤", "EPG"),
    PARENTAL("◆", "CONTROL PARENTAL"),
    MULTISCREEN("▦", "MULTI-PANTALLA"),
    TIME("◷", "FORMATO DE HORA"),
    RECORDINGS("●", "GRABACIONES"),
    ACCOUNT("☁", "CUENTA / PLAYLIST"),
    PROFILES("♙", "PERFILES"),
    UPDATES("↓", "ACTUALIZACIONES"),
    ABOUT("ⓘ", "ACERCA DE")
}
@Composable
fun SettingsScreen(
    username: String,
    server: String,
    settingsManager: SettingsManager,
    onBack: () -> Unit,
    onCheckForUpdates: () -> Unit = {}
) {
    val context = LocalContext.current
    val appContext = context.applicationContext

    val sessionManager =
        remember {
            SessionManager(appContext)
        }

    val scope = rememberCoroutineScope()

    var selectedCategory by
    remember {
        mutableStateOf(SettingsCategory.GENERAL)
    }

    var profileRefreshKey by
    remember {
        mutableIntStateOf(0)
    }

    val activeProfile =
        remember(profileRefreshKey) {
            settingsManager.getActiveProfile()
        }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        SettingsSidebar(
            selectedCategory = selectedCategory,
            activeProfile = activeProfile,
            onCategoryClick = {
                selectedCategory = it
            },
            onBack = onBack
        )

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(
                    Color.White.copy(
                        alpha = 0.12f
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(
                    start = 52.dp,
                    end = 56.dp,
                    top = 32.dp,
                    bottom = 36.dp
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
                SettingsHeader(
                    category = selectedCategory,
                    activeProfile = activeProfile
                )

                Spacer(
                    modifier = Modifier.height(26.dp)
                )

                when (selectedCategory) {
                    SettingsCategory.GENERAL ->
                        GeneralSettings(
                            settingsManager = settingsManager
                        )

                    SettingsCategory.PLAYER ->
                        PlayerSettings(
                            settingsManager = settingsManager
                        )

                    SettingsCategory.STREAM ->
                        StreamSettings(
                            settingsManager = settingsManager
                        )

                    SettingsCategory.AUTOMATION ->
                        AutomationSettings(
                            settingsManager = settingsManager
                        )

                    SettingsCategory.EPG ->
                        EpgSettings(
                            settingsManager = settingsManager
                        )

                    SettingsCategory.PARENTAL ->
                        ParentalSettings(
                            settingsManager = settingsManager,
                            sessionManager = sessionManager,
                            profile = activeProfile
                        )

                    SettingsCategory.MULTISCREEN ->
                        MultiScreenSettings(
                            settingsManager = settingsManager
                        )

                    SettingsCategory.TIME ->
                        TimeSettings(
                            settingsManager = settingsManager
                        )

                    SettingsCategory.RECORDINGS ->
                        RecordingSettings(
                            settingsManager = settingsManager
                        )

                    SettingsCategory.ACCOUNT ->
                        AccountSettings(
                            username = username,
                            server = server,
                            settingsManager = settingsManager,
                            onLogout = {
                                settingsManager.lastSection = "home"

                                scope.launch {
                                    sessionManager.clearSession()
                                }
                            }
                        )

                    SettingsCategory.PROFILES ->
                        ProfilesSettings(
                            settingsManager = settingsManager,
                            onProfilesChanged = {
                                profileRefreshKey++
                            }
                        )

                    SettingsCategory.UPDATES ->
                        UpdateSettings(
                            onCheckForUpdates = onCheckForUpdates
                        )

                    SettingsCategory.ABOUT ->
                        AboutSettings()
                }

                Spacer(
                    modifier = Modifier.height(40.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsSidebar(
    selectedCategory: SettingsCategory,
    activeProfile: UserProfile,
    onCategoryClick: (SettingsCategory) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(235.dp)
            .fillMaxHeight()
            .padding(
                start = 22.dp,
                end = 18.dp,
                top = 26.dp,
                bottom = 24.dp
            )
    ) {
        SettingsButton(
            text = settingsText("← VOLVER", "← BACK"),
            onClick = onBack
        )

        Spacer(
            modifier = Modifier.height(26.dp)
        )

        Text(
            text = settingsText("CONFIGURACIÓN", "SETTINGS"),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "CLINCH PLAYER",
            color = ClinchYellow,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(
                    rememberScrollState()
                )
        ) {
            SettingsCategory.entries.forEach { category ->
                CategoryButton(
                    icon = category.icon,
                    text = category.localizedTitle(),
                    selected = category == selectedCategory,
                    onFocus = {
                        onCategoryClick(category)
                    },
                    onClick = {
                        onCategoryClick(category)
                    }
                )

                Spacer(
                    modifier = Modifier.height(7.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = settingsText("PERFIL ACTIVO", "ACTIVE PROFILE"),
            color = SoftWhite,
            fontSize = 9.sp,
            letterSpacing = 1.5.sp
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = activeProfile.name,
            color = ClinchYellow,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingsHeader(
    category: SettingsCategory,
    activeProfile: UserProfile
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = category.localizedTitle(),
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = settingsText("Configuración de ${activeProfile.name}", "Settings for ${activeProfile.name}"),
                color = SoftWhite,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .background(
                    Color.White.copy(
                        alpha = 0.06f
                    ),
                    RoundedCornerShape(20.dp)
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        ) {
            Text(
                text =
                    if (activeProfile.isKid) {
                        settingsText("${activeProfile.name} · INFANTIL", "${activeProfile.name} · KIDS")
                    } else {
                        activeProfile.name
                    },
                color = ClinchYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GeneralSettings(
    settingsManager: SettingsManager
) {
    val context = LocalContext.current

    var appLanguage by
    remember {
        mutableStateOf(
            settingsManager.appLanguage
        )
    }

    ChoiceSetting(
        title = if (appLanguage == "en") "Language" else "Idioma",
        options =
            listOf(
                "Español" to "es",
                "English" to "en"
            ),
        selectedValue = appLanguage,
        onSelected = { language ->
            if (language != appLanguage) {
                appLanguage = language
                settingsManager.appLanguage = language
            }
        }
    )

    SettingGap()

    var autoStart by
    remember {
        mutableStateOf(
            settingsManager.autoStartOnBoot
        )
    }

    var showFullEpg by
    remember {
        mutableStateOf(
            settingsManager.showFullEpg
        )
    }

    var subtitles by
    remember {
        mutableStateOf(
            settingsManager.subtitlesEnabled
        )
    }

    var autoNext by
    remember {
        mutableStateOf(
            settingsManager.autoPlayNextEpisode
        )
    }

    var autoClearCache by
    remember {
        mutableStateOf(
            settingsManager.autoClearCache
        )
    }

    var showEpgChannels by
    remember {
        mutableStateOf(
            settingsManager.showEpgInChannelList
        )
    }

    var autoPlayLive by
    remember {
        mutableStateOf(
            settingsManager.autoPlayLiveChannel
        )
    }

    var rememberSection by
    remember {
        mutableStateOf(
            settingsManager.rememberLastSection
        )
    }

    var animations by
    remember {
        mutableStateOf(
            settingsManager.animationsEnabled
        )
    }

    var nextDelay by
    remember {
        mutableIntStateOf(
            settingsManager.autoPlayNextDelaySeconds
        )
    }

    var recentLimit by
    remember {
        mutableIntStateOf(
            settingsManager.recentlyAddedLimit
        )
    }

    var historyLimit by
    remember {
        mutableIntStateOf(
            settingsManager.channelHistoryLimit
        )
    }

    var userAgent by
    remember {
        mutableStateOf(
            settingsManager.userAgent
        )
    }

    val cacheClearedMessage =
        settingsText("Caché limpiada", "Cache cleared")

    ToggleSetting(
        title = settingsText("Iniciar Clinch Player al encender", "Start Clinch Player on boot"),
        description = settingsText("Guarda la preferencia para AutoStart.", "Saves the AutoStart preference."),
        checked = autoStart,
        onClick = {
            autoStart = !autoStart
            settingsManager.autoStartOnBoot = autoStart
        }
    )

    SettingGap()

    ToggleSetting(
        title = settingsText("Mostrar EPG completo", "Show full EPG"),
        description = settingsText("Permite usar la guía completa de programación.", "Allows use of the full program guide."),
        checked = showFullEpg,
        onClick = {
            showFullEpg = !showFullEpg
            settingsManager.showFullEpg = showFullEpg
        }
    )

    SettingGap()

    ToggleSetting(
        title = settingsText("Subtítulos activos", "Subtitles enabled"),
        description = settingsText("Activa subtítulos por defecto cuando estén disponibles.", "Enables subtitles by default when available."),
        checked = subtitles,
        onClick = {
            subtitles = !subtitles
            settingsManager.subtitlesEnabled = subtitles
        }
    )

    SettingGap()

    ToggleSetting(
        title = settingsText("Reproducir próximo episodio", "Play next episode"),
        description = settingsText("Reproduce automáticamente el próximo episodio.", "Automatically plays the next episode."),
        checked = autoNext,
        onClick = {
            autoNext = !autoNext
            settingsManager.autoPlayNextEpisode = autoNext
        }
    )

    SettingGap()

    NumberSetting(
        title = settingsText("Espera para próximo episodio", "Next episode delay"),
        value = nextDelay,
        suffix = " s",
        min = 0,
        max = 120,
        step = 5,
        onValueChange = {
            nextDelay = it
            settingsManager.autoPlayNextDelaySeconds = it
        }
    )

    SettingGap()

    ToggleSetting(
        title = settingsText("Limpiar caché automáticamente", "Clear cache automatically"),
        description = settingsText("Guarda la preferencia de limpieza automática.", "Saves the automatic cache cleaning preference."),
        checked = autoClearCache,
        onClick = {
            autoClearCache = !autoClearCache
            settingsManager.autoClearCache = autoClearCache
        }
    )

    SettingGap()

    ActionSetting(
        title = settingsText("Limpiar caché ahora", "Clear cache now"),
        description = settingsText("Elimina los archivos temporales de Clinch Player.", "Deletes Clinch Player temporary files."),
        actionText = settingsText("LIMPIAR", "CLEAR"),
        onClick = {
            runCatching {
                context.cacheDir.deleteRecursively()
                context.cacheDir.mkdirs()
            }

            Toast
                .makeText(
                    context,
                    cacheClearedMessage,
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    )

    SettingGap()

    ToggleSetting(
        title = settingsText("Mostrar EPG en lista de canales", "Show EPG in channel list"),
        description = settingsText("Muestra información de programación junto a los canales.", "Shows program information next to channels."),
        checked = showEpgChannels,
        onClick = {
            showEpgChannels = !showEpgChannels
            settingsManager.showEpgInChannelList = showEpgChannels
        }
    )

    SettingGap()

    ToggleSetting(
        title = settingsText("Reproducir canal automáticamente en TV en vivo", "Auto-play channel in Live TV"),
        description = settingsText("Inicia el canal seleccionado sin paso adicional.", "Starts the selected channel without an extra step."),
        checked = autoPlayLive,
        onClick = {
            autoPlayLive = !autoPlayLive
            settingsManager.autoPlayLiveChannel = autoPlayLive
        }
    )

    SettingGap()

    NumberSetting(
        title = settingsText("Límite de Recién agregados", "Recently added limit"),
        value = recentLimit,
        min = 10,
        max = 100,
        step = 10,
        onValueChange = {
            recentLimit = it
            settingsManager.recentlyAddedLimit = it
        }
    )

    SettingGap()

    NumberSetting(
        title = settingsText("Historial de canales", "Channel history"),
        value = historyLimit,
        min = 0,
        max = 100,
        step = 5,
        onValueChange = {
            historyLimit = it
            settingsManager.channelHistoryLimit = it
        }
    )

    SettingGap()

    ToggleSetting(
        title = settingsText("Recordar última sección", "Remember last section"),
        description = settingsText("Recuerda Home, TV, películas, series, búsqueda y otras secciones.", "Remembers Home, TV, movies, series, search and other sections."),
        checked = rememberSection,
        onClick = {
            rememberSection = !rememberSection
            settingsManager.rememberLastSection = rememberSection

            if (!rememberSection) {
                settingsManager.lastSection = "home"
            }
        }
    )

    SettingGap()

    ToggleSetting(
        title = settingsText("Animaciones", "Animations"),
        description = settingsText("Activa los efectos de foco y movimiento de Clinch Player.", "Enables Clinch Player focus and motion effects."),
        checked = animations,
        onClick = {
            animations = !animations
            settingsManager.animationsEnabled = animations
        }
    )

    SettingGap()

    EditableTextSetting(
        title = "User Agent",
        description = settingsText("Déjalo vacío para usar ClinchPlayer/1.0.", "Leave blank to use ClinchPlayer/1.0."),
        value = userAgent,
        onValueChange = {
            userAgent =
                it.take(
                    120
                )

            settingsManager.userAgent =
                userAgent
        }
    )
}

@Composable
private fun PlayerSettings(
    settingsManager: SettingsManager
) {
    var decoderMode by
    remember {
        mutableStateOf(
            settingsManager.decoderMode
        )
    }

    var bufferSize by
    remember {
        mutableIntStateOf(
            settingsManager.bufferSize
        )
    }

    var hardwareAudio by
    remember {
        mutableStateOf(
            settingsManager.hardwareAudioEnabled
        )
    }

    var openGl by
    remember {
        mutableStateOf(
            settingsManager.openGlEnabled
        )
    }

    ChoiceSetting(
        title = settingsText("Decodificador", "Decoder"),
        options =
            listOf(
                "Hardware" to "hardware",
                "Software" to "software"
            ),
        selectedValue = decoderMode,
        onSelected = {
            decoderMode = it
            settingsManager.decoderMode = it
        }
    )

    SettingGap()

    NumberSetting(
        title = settingsText("Tamaño del buffer", "Buffer size"),
        value = bufferSize,
        min = 5,
        max = 60,
        step = 5,
        onValueChange = {
            bufferSize = it
            settingsManager.bufferSize = it
        }
    )

    SettingGap()

    ToggleSetting(
        title = settingsText("Audio acelerado por hardware", "Hardware-accelerated audio"),
        description = settingsText("Preferencia para aceleración de audio del reproductor.", "Preference for player audio acceleration."),
        checked = hardwareAudio,
        onClick = {
            hardwareAudio = !hardwareAudio
            settingsManager.hardwareAudioEnabled = hardwareAudio
        }
    )

    SettingGap()

    ToggleSetting(
        title = "OpenGL",
        description = settingsText("Preferencia para renderizado de video mediante OpenGL.", "Preference for video rendering using OpenGL."),
        checked = openGl,
        onClick = {
            openGl = !openGl
            settingsManager.openGlEnabled = openGl
        }
    )

    Spacer(
        modifier = Modifier.height(18.dp)
    )

    InfoNote(
        text = "Estas preferencias quedan guardadas. La conexión final con Media3/ExoPlayer se hace en el módulo 05 - VIDEO PLAYER."
    )
}

@Composable
private fun StreamSettings(
    settingsManager: SettingsManager
) {
    var format by
    remember {
        mutableStateOf(
            settingsManager.streamFormat
        )
    }

    ChoiceSetting(
        title = settingsText("Formato de stream", "Stream format"),
        options =
            listOf(
                settingsText("Automático", "Automatic") to "auto",
                "MPEG-TS (.ts)" to "ts",
                "HLS (.m3u8)" to "m3u8"
            ),
        selectedValue = format,
        onSelected = {
            format = it
            settingsManager.streamFormat = it
        }
    )

    Spacer(
        modifier = Modifier.height(18.dp)
    )

    InfoNote(
        text = "Automático será el valor recomendado. Luego conectaremos esta preferencia al constructor de URL de Xtream."
    )
}

@Composable
private fun AutomationSettings(
    settingsManager: SettingsManager
) {
    var contentEnabled by
    remember {
        mutableStateOf(
            settingsManager.contentAutoUpdateEnabled
        )
    }

    var contentDays by
    remember {
        mutableIntStateOf(
            settingsManager.contentUpdateDays
        )
    }

    var epgEnabled by
    remember {
        mutableStateOf(
            settingsManager.epgAutoUpdateEnabled
        )
    }

    var epgDays by
    remember {
        mutableIntStateOf(
            settingsManager.epgUpdateDays
        )
    }

    ToggleSetting(
        title = "Auto-Update Live, Películas y Series",
        description = "Guarda si el contenido debe actualizarse automáticamente.",
        checked = contentEnabled,
        onClick = {
            contentEnabled = !contentEnabled
            settingsManager.contentAutoUpdateEnabled = contentEnabled
        }
    )

    SettingGap()

    NumberSetting(
        title = settingsText("Actualizar contenido cada", "Update content every"),
        value = contentDays,
        suffix = " día(s)",
        min = 1,
        max = 7,
        step = 1,
        onValueChange = {
            contentDays = it
            settingsManager.contentUpdateDays = it
        }
    )

    SettingGap()

    ToggleSetting(
        title = "Auto-Update EPG",
        description = "Guarda si la guía debe actualizarse automáticamente.",
        checked = epgEnabled,
        onClick = {
            epgEnabled = !epgEnabled
            settingsManager.epgAutoUpdateEnabled = epgEnabled
        }
    )

    SettingGap()

    NumberSetting(
        title = settingsText("Actualizar EPG cada", "Update EPG every"),
        value = epgDays,
        suffix = " día(s)",
        min = 1,
        max = 7,
        step = 1,
        onValueChange = {
            epgDays = it
            settingsManager.epgUpdateDays = it
        }
    )

    Spacer(
        modifier = Modifier.height(18.dp)
    )

    InfoNote(
        text = "Las opciones quedan guardadas. El trabajo periódico se conectará con WorkManager cuando integremos el repositorio de actualización."
    )
}

@Composable
private fun EpgSettings(
    settingsManager: SettingsManager
) {
    var timeshift by
    remember {
        mutableIntStateOf(
            settingsManager.epgTimeshiftHours
        )
    }

    InfoCard(
        title = settingsText("Fuente EPG", "EPG source"),
        value = "EPG integrado de la cuenta Xtream"
    )

    SettingGap()

    NumberSetting(
        title = "EPG Timeshift",
        value = timeshift,
        suffix = " h",
        min = -12,
        max = 12,
        step = 1,
        onValueChange = {
            timeshift = it
            settingsManager.epgTimeshiftHours = it
        }
    )

    Spacer(
        modifier = Modifier.height(18.dp)
    )

    InfoNote(
        text = "La pantalla ya guarda el timeshift. El botón de Refresh EPG se conectará cuando el repositorio EPG esté integrado."
    )
}

@Composable
private fun ParentalSettings(
    settingsManager: SettingsManager,
    sessionManager: SessionManager,
    profile: UserProfile
) {
    var enabled by
    remember(profile.id) {
        mutableStateOf(
            settingsManager.parentalControlEnabled(
                profile.id
            )
        )
    }

    var showPinDialog by
    remember {
        mutableStateOf(false)
    }

    var hasPin by
    remember(profile.id) {
        mutableStateOf(
            settingsManager.hasParentalPin(
                profile.id
            )
        )
    }

    var selectedType by
    remember {
        mutableStateOf("live")
    }

    var liveCategories by
    remember {
        mutableStateOf(
            emptyList<ParentalCategoryEntry>()
        )
    }

    var movieCategories by
    remember {
        mutableStateOf(
            emptyList<ParentalCategoryEntry>()
        )
    }

    var seriesCategories by
    remember {
        mutableStateOf(
            emptyList<ParentalCategoryEntry>()
        )
    }

    var loadingCategories by
    remember {
        mutableStateOf(false)
    }

    var categoriesError by
    remember {
        mutableStateOf<String?>(null)
    }

    val noActiveSessionMessage =
        settingsText(
            "No hay una sesión IPTV activa.",
            "There is no active IPTV session."
        )

    val categoriesLoadErrorMessage =
        settingsText(
            "No se pudieron cargar las categorías.",
            "Categories could not be loaded."
        )

    var blockedLive by
    remember(profile.id) {
        mutableStateOf(
            settingsManager
                .getBlockedCategoryIds(
                    "live",
                    profile.id
                )
        )
    }

    var blockedMovies by
    remember(profile.id) {
        mutableStateOf(
            settingsManager
                .getBlockedCategoryIds(
                    "movies",
                    profile.id
                )
        )
    }

    var blockedSeries by
    remember(profile.id) {
        mutableStateOf(
            settingsManager
                .getBlockedCategoryIds(
                    "series",
                    profile.id
                )
        )
    }

    LaunchedEffect(
        profile.id
    ) {
        loadingCategories = true
        categoriesError = null

        try {
            val session =
                sessionManager
                    .sessionFlow
                    .first()

            if (
                !session.isLoggedIn
            ) {
                categoriesError =
                    noActiveSessionMessage
            } else {
                val api =
                    RetrofitClient
                        .createApi(
                            session.server
                        )

                val liveResponse =
                    api.getLiveCategories(
                        username =
                            session.username,
                        password =
                            session.password
                    )

                val movieResponse =
                    api.getVodCategories(
                        username =
                            session.username,
                        password =
                            session.password
                    )

                val seriesResponse =
                    api.getSeriesCategories(
                        username =
                            session.username,
                        password =
                            session.password
                    )

                liveCategories =
                    if (
                        liveResponse
                            .isSuccessful
                    ) {
                        liveResponse
                            .body()
                            .orEmpty()
                            .map {
                                ParentalCategoryEntry(
                                    id =
                                        it.categoryId,
                                    name =
                                        it.categoryName
                                )
                            }
                    } else {
                        emptyList()
                    }

                movieCategories =
                    if (
                        movieResponse
                            .isSuccessful
                    ) {
                        movieResponse
                            .body()
                            .orEmpty()
                            .map {
                                ParentalCategoryEntry(
                                    id =
                                        it.categoryId,
                                    name =
                                        it.categoryName
                                )
                            }
                    } else {
                        emptyList()
                    }

                seriesCategories =
                    if (
                        seriesResponse
                            .isSuccessful
                    ) {
                        seriesResponse
                            .body()
                            .orEmpty()
                            .map {
                                ParentalCategoryEntry(
                                    id =
                                        it.categoryId,
                                    name =
                                        it.categoryName
                                )
                            }
                    } else {
                        emptyList()
                    }

                if (
                    liveCategories.isEmpty() &&
                    movieCategories.isEmpty() &&
                    seriesCategories.isEmpty()
                ) {
                    categoriesError =
                        categoriesLoadErrorMessage
                }
            }
        } catch (_: Exception) {
            categoriesError =
                categoriesLoadErrorMessage
        } finally {
            loadingCategories = false
        }
    }

    InfoCard(
        title = settingsText("Perfil", "Profile"),
        value =
            if (profile.isKid) {
                settingsText("${profile.name} · Perfil infantil", "${profile.name} · Kids profile")
            } else {
                profile.name
            }
    )

    SettingGap()

    ToggleSetting(
        title =
            settingsText("Control parental", "Parental control"),
        description =
            "Cuando está activo, las categorías marcadas abajo desaparecen de TV en vivo, Películas y Series para este perfil.",
        checked =
            enabled,
        onClick = {
            if (
                !hasPin &&
                !enabled
            ) {
                showPinDialog =
                    true
            } else {
                enabled =
                    !enabled

                settingsManager
                    .setParentalControlEnabled(
                        enabled,
                        profile.id
                    )
            }
        }
    )

    SettingGap()

    ActionSetting(
        title =
            if (hasPin) {
                settingsText("Cambiar PIN", "Change PIN")
            } else {
                settingsText("Crear PIN", "Create PIN")
            },
        description =
            "PIN de 4 dígitos para ${profile.name}.",
        actionText =
            if (hasPin) {
                settingsText("CAMBIAR", "CHANGE")
            } else {
                settingsText("CREAR", "CREATE")
            },
        onClick = {
            showPinDialog =
                true
        }
    )

    if (hasPin) {
        SettingGap()

        ActionSetting(
            title =
                settingsText("Eliminar PIN", "Delete PIN"),
            description =
                "Desactiva el control parental de este perfil.",
            actionText =
                settingsText("ELIMINAR", "DELETE"),
            danger =
                true,
            onClick = {
                settingsManager
                    .clearParentalPin(
                        profile.id
                    )

                enabled = false
                hasPin = false
            }
        )
    }

    Spacer(
        modifier =
            Modifier.height(
                28.dp
            )
    )

    Text(
        text =
            settingsText("CATEGORÍAS PROTEGIDAS", "PROTECTED CATEGORIES"),
        color =
            Color.White,
        fontSize =
            18.sp,
        fontWeight =
            FontWeight.Bold
    )

    Spacer(
        modifier =
            Modifier.height(
                5.dp
            )
    )

    Text(
        text =
            settingsText("Marca las categorías que quieres ocultar cuando el control parental esté activo.", "Select the categories you want to hide when parental control is enabled."),
        color =
            SoftWhite,
        fontSize =
            11.sp
    )

    Spacer(
        modifier =
            Modifier.height(
                16.dp
            )
    )

    ChoiceSetting(
        title =
            settingsText("Contenido", "Content"),
        options =
            listOf(
                settingsText("TV EN VIVO", "LIVE TV") to "live",
                settingsText("PELÍCULAS", "MOVIES") to "movies",
                "SERIES" to "series"
            ),
        selectedValue =
            selectedType,
        onSelected = {
            selectedType =
                it
        }
    )

    Spacer(
        modifier =
            Modifier.height(
                14.dp
            )
    )

    if (loadingCategories) {
        Text(
            text =
                settingsText("Cargando categorías...", "Loading categories..."),
            color =
                ClinchYellow,
            fontSize =
                12.sp,
            fontWeight =
                FontWeight.Bold
        )
    } else if (
        categoriesError != null
    ) {
        Text(
            text =
                categoriesError
                    ?: settingsText("Error cargando categorías", "Error loading categories"),
            color =
                SoftWhite,
            fontSize =
                12.sp
        )
    } else {
        val currentCategories =
            when (
                selectedType
            ) {
                "movies" ->
                    movieCategories

                "series" ->
                    seriesCategories

                else ->
                    liveCategories
            }

        val currentBlocked =
            when (
                selectedType
            ) {
                "movies" ->
                    blockedMovies

                "series" ->
                    blockedSeries

                else ->
                    blockedLive
            }

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    settingsText("${currentBlocked.size} de ${currentCategories.size} ocultas", "${currentBlocked.size} of ${currentCategories.size} hidden"),
                color =
                    ClinchYellow,
                fontSize =
                    11.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            SmallTextButton(
                text =
                    settingsText("OCULTAR TODAS", "HIDE ALL"),
                onClick = {
                    val allIds =
                        currentCategories
                            .map {
                                it.id
                            }

                    settingsManager
                        .setAllCategoriesBlocked(
                            selectedType,
                            allIds,
                            true,
                            profile.id
                        )

                    when (
                        selectedType
                    ) {
                        "movies" ->
                            blockedMovies =
                                allIds.toSet()

                        "series" ->
                            blockedSeries =
                                allIds.toSet()

                        else ->
                            blockedLive =
                                allIds.toSet()
                    }
                }
            )

            Spacer(
                modifier =
                    Modifier.width(
                        14.dp
                    )
            )

            SmallTextButton(
                text =
                    settingsText("MOSTRAR TODAS", "SHOW ALL"),
                onClick = {
                    settingsManager
                        .setAllCategoriesBlocked(
                            selectedType,
                            emptyList(),
                            false,
                            profile.id
                        )

                    when (
                        selectedType
                    ) {
                        "movies" ->
                            blockedMovies =
                                emptySet()

                        "series" ->
                            blockedSeries =
                                emptySet()

                        else ->
                            blockedLive =
                                emptySet()
                    }
                }
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )

        if (
            currentCategories
                .isEmpty()
        ) {
            Text(
                text =
                    settingsText("No hay categorías disponibles.", "No categories available."),
                color =
                    SoftWhite,
                fontSize =
                    12.sp
            )
        } else {
            currentCategories
                .forEach {
                        category ->

                    val blocked =
                        category.id in
                                currentBlocked

                    ParentalCategoryRow(
                        name =
                            category.name,
                        blocked =
                            blocked,
                        onClick = {
                            val newValue =
                                !blocked

                            settingsManager
                                .setCategoryBlocked(
                                    selectedType,
                                    category.id,
                                    newValue,
                                    profile.id
                                )

                            when (
                                selectedType
                            ) {
                                "movies" -> {
                                    blockedMovies =
                                        blockedMovies
                                            .toMutableSet()
                                            .apply {
                                                if (
                                                    newValue
                                                ) {
                                                    add(
                                                        category.id
                                                    )
                                                } else {
                                                    remove(
                                                        category.id
                                                    )
                                                }
                                            }
                                }

                                "series" -> {
                                    blockedSeries =
                                        blockedSeries
                                            .toMutableSet()
                                            .apply {
                                                if (
                                                    newValue
                                                ) {
                                                    add(
                                                        category.id
                                                    )
                                                } else {
                                                    remove(
                                                        category.id
                                                    )
                                                }
                                            }
                                }

                                else -> {
                                    blockedLive =
                                        blockedLive
                                            .toMutableSet()
                                            .apply {
                                                if (
                                                    newValue
                                                ) {
                                                    add(
                                                        category.id
                                                    )
                                                } else {
                                                    remove(
                                                        category.id
                                                    )
                                                }
                                            }
                                }
                            }
                        }
                    )
                }
        }
    }

    Spacer(
        modifier =
            Modifier.height(
                18.dp
            )
    )

    InfoNote(
        text =
            if (enabled) {
                "Control parental activo: las categorías marcadas quedarán ocultas de sus listas."
            } else {
                "Las categorías marcadas están guardadas, pero se muestran mientras el control parental esté desactivado."
            }
    )

    if (showPinDialog) {
        PinDialog(
            title =
                if (hasPin) {
                    settingsText("Cambiar PIN", "Change PIN")
                } else {
                    settingsText("Crear PIN", "Create PIN")
                },
            onDismiss = {
                showPinDialog =
                    false
            },
            onSave = {
                    pin ->

                val saved =
                    settingsManager
                        .setParentalPin(
                            pin,
                            profile.id
                        )

                if (saved) {
                    hasPin =
                        true

                    enabled =
                        true

                    settingsManager
                        .setParentalControlEnabled(
                            true,
                            profile.id
                        )

                    showPinDialog =
                        false
                }
            }
        )
    }
}


@Composable
private fun ParentalCategoryRow(
    name: String,
    blocked: Boolean,
    onClick: () -> Unit
) {
    var focused by
    remember {
        mutableStateOf(false)
    }

    Surface(
        onClick =
            onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    38.dp
                )
                .onFocusChanged {
                    focused =
                        it.isFocused
                },
        colors =
            ClickableSurfaceDefaults
                .colors(
                    containerColor =
                        Color.Transparent,
                    focusedContainerColor =
                        Color.Transparent,
                    pressedContainerColor =
                        Color.Transparent
                ),
        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(
                        0.dp
                    )
                )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    if (blocked) {
                        "●"
                    } else {
                        "○"
                    },
                color =
                    if (
                        focused ||
                        blocked
                    ) {
                        ClinchYellow
                    } else {
                        Color.White.copy(
                            alpha =
                                0.60f
                        )
                    },
                fontSize =
                    12.sp
            )

            Spacer(
                modifier =
                    Modifier.width(
                        12.dp
                    )
            )

            Text(
                text =
                    name,
                color =
                    if (focused) {
                        ClinchYellow
                    } else {
                        Color.White
                    },
                fontSize =
                    12.sp,
                fontWeight =
                    if (
                        focused ||
                        blocked
                    ) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                maxLines =
                    1
            )

            Spacer(
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            if (blocked) {
                Text(
                    text =
                        settingsText("OCULTA", "HIDDEN"),
                    color =
                        ClinchYellow,
                    fontSize =
                        9.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }

    RightSideDivider()
}


@Composable
private fun MultiScreenSettings(
    settingsManager: SettingsManager
) {
    var enabled by
    remember {
        mutableStateOf(
            settingsManager.multiScreenEnabled
        )
    }

    var layout by
    remember {
        mutableStateOf(
            settingsManager.multiScreenLayout
        )
    }

    ToggleSetting(
        title = settingsText("Mostrar opción Multi-Pantalla", "Show Multi-Screen option"),
        description = settingsText("Permite habilitar varios streams simultáneamente.", "Allows multiple streams simultaneously."),
        checked = enabled,
        onClick = {
            enabled = !enabled
            settingsManager.multiScreenEnabled = enabled
        }
    )

    SettingGap()

    ChoiceSetting(
        title = settingsText("Diseño", "Layout"),
        options =
            listOf(
                settingsText("2 canales", "2 channels") to "1x2",
                settingsText("3 canales", "3 channels") to "1+2",
                settingsText("4 canales", "4 channels") to "2x2"
            ),
        selectedValue = layout,
        onSelected = {
            layout = it
            settingsManager.multiScreenLayout = it
        }
    )

    Spacer(
        modifier = Modifier.height(18.dp)
    )

    InfoNote(
        text = "La preferencia queda guardada. El reproductor Multi-Screen se implementará después de estabilizar TV en vivo."
    )
}

@Composable
private fun TimeSettings(
    settingsManager: SettingsManager
) {
    var use24Hour by
    remember {
        mutableStateOf(
            settingsManager.use24HourFormat
        )
    }

    ChoiceSetting(
        title = settingsText("Formato de hora", "Time format"),
        options =
            listOf(
                settingsText("12 horas", "12 hours") to "12",
                settingsText("24 horas", "24 hours") to "24"
            ),
        selectedValue =
            if (use24Hour) {
                "24"
            } else {
                "12"
            },
        onSelected = {
            use24Hour = it == "24"
            settingsManager.use24HourFormat = use24Hour
        }
    )

    SettingGap()

    InfoCard(
        title = settingsText("Ejemplo", "Example"),
        value =
            if (use24Hour) {
                "18:05"
            } else {
                "6:05 PM"
            }
    )
}

@Composable
private fun RecordingSettings(
    settingsManager: SettingsManager
) {
    val context = LocalContext.current

    var directory by
    remember {
        mutableStateOf(
            settingsManager.recordingDirectoryUri
        )
    }

    val directoryPicker =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .OpenDocumentTree()
        ) { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                }

                directory = uri.toString()
                settingsManager.recordingDirectoryUri = directory
            }
        }

    InfoCard(
        title = settingsText("Carpeta actual", "Current folder"),
        value =
            directory.ifBlank {
                settingsText("No configurada", "Not configured")
            }
    )

    SettingGap()

    ActionSetting(
        title = settingsText("Directorio de grabaciones", "Recording directory"),
        description = settingsText("Selecciona la carpeta usando el selector seguro de Android.", "Select the folder using Android’s secure picker."),
        actionText = settingsText("CAMBIAR", "CHANGE"),
        onClick = {
            directoryPicker.launch(null)
        }
    )

    Spacer(
        modifier = Modifier.height(18.dp)
    )

    InfoNote(
        text = "La carpeta seleccionada queda guardada con permiso persistente. El módulo de grabación usará esta URI."
    )
}

@Composable
private fun AccountSettings(
    username: String,
    server: String,
    settingsManager: SettingsManager,
    onLogout: () -> Unit
) {
    InfoCard(
        title = settingsText("Usuario", "Username"),
        value =
            username.ifBlank {
                settingsText("No disponible", "Not available")
            }
    )

    SettingGap()

    InfoCard(
        title = settingsText("Servidor", "Server"),
        value =
            server.ifBlank {
                settingsText("No disponible", "Not available")
            }
    )

    SettingGap()

    InfoCard(
        title = settingsText("Formato de stream", "Stream format"),
        value =
            when (
                settingsManager.streamFormat
            ) {
                "ts" -> "MPEG-TS (.ts)"
                "m3u8" -> "HLS (.m3u8)"
                else -> settingsText("Automático", "Automatic")
            }
    )

    Spacer(
        modifier = Modifier.height(24.dp)
    )

    DangerButton(
        text = settingsText("CERRAR SESIÓN", "LOG OUT"),
        onClick = onLogout
    )
}

@Composable
private fun ProfilesSettings(
    settingsManager: SettingsManager,
    onProfilesChanged: () -> Unit
) {
    var profiles by
    remember {
        mutableStateOf(
            settingsManager.profiles
        )
    }

    var activeId by
    remember {
        mutableStateOf(
            settingsManager.activeProfileId
        )
    }

    var showAddDialog by
    remember {
        mutableStateOf(false)
    }

    Text(
        text = settingsText("¿QUIÉN ESTÁ VIENDO?", "WHO’S WATCHING?"),
        color = Color.White,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(
        modifier = Modifier.height(14.dp)
    )

    profiles.forEach { profile ->
        ProfileRow(
            profile = profile,
            selected = profile.id == activeId,
            canDelete = profiles.size > 1,
            onSelect = {
                settingsManager
                    .setActiveProfile(
                        profile.id
                    )

                activeId = profile.id
                onProfilesChanged()
            },
            onDelete = {
                val deleted =
                    settingsManager
                        .deleteProfile(
                            profile.id
                        )

                if (deleted) {
                    profiles =
                        settingsManager
                            .profiles

                    activeId =
                        settingsManager
                            .activeProfileId

                    onProfilesChanged()
                }
            }
        )

        SettingGap()
    }

    ActionSetting(
        title = settingsText("Agregar perfil", "Add profile"),
        description = settingsText("Crea otro perfil independiente en Clinch Player.", "Creates another independent profile in Clinch Player."),
        actionText = settingsText("+ AGREGAR", "+ ADD"),
        onClick = {
            showAddDialog = true
        }
    )

    Spacer(
        modifier = Modifier.height(18.dp)
    )

    InfoNote(
        text = "El perfil activo ya queda persistido. En el siguiente paso conectaremos profileId con Favoritos, Continue Watching e Historial para separar esos datos por usuario."
    )

    if (showAddDialog) {
        AddProfileDialog(
            onDismiss = {
                showAddDialog = false
            },
            onSave = { name, isKid ->
                val newProfile =
                    settingsManager
                        .addProfile(
                            name = name,
                            isKid = isKid
                        )

                settingsManager
                    .setActiveProfile(
                        newProfile.id
                    )

                profiles =
                    settingsManager
                        .profiles

                activeId =
                    newProfile.id

                showAddDialog = false
                onProfilesChanged()
            }
        )
    }
}

@Composable
private fun UpdateSettings(
    onCheckForUpdates: () -> Unit
) {
    val context = LocalContext.current

    val packageInfo =
        remember {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
        }

    val versionName =
        packageInfo.versionName
            ?: "1.0"

    val versionCode =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toString()
        }

    InfoCard(
        title = settingsText(
            "Versión instalada",
            "Installed version"
        ),
        value = versionName
    )

    SettingGap()

    InfoCard(
        title = settingsText(
            "Código de versión",
            "Version code"
        ),
        value = versionCode
    )

    SettingGap()

    ActionSetting(
        title = settingsText(
            "Buscar actualizaciones",
            "Check for updates"
        ),
        description = settingsText(
            "Comprueba si existe una versión nueva de Clinch Player.",
            "Checks whether a newer version of Clinch Player is available."
        ),
        actionText = settingsText(
            "BUSCAR",
            "CHECK"
        ),
        onClick = onCheckForUpdates
    )

    Spacer(
        modifier = Modifier.height(18.dp)
    )

    InfoNote(
        text = settingsText(
            "Clinch Player puede descargar nuevas versiones directamente sin tener que volver a usar Downloader.",
            "Clinch Player can download new versions directly without using Downloader again."
        )
    )
}


@Composable
private fun AboutSettings() {
    InfoCard(
        title = settingsText("Aplicación", "Application"),
        value = "Clinch Player"
    )

    SettingGap()

    InfoCard(
        title = settingsText("Plataforma", "Platform"),
        value = "Android TV"
    )

    SettingGap()

    InfoCard(
        title = settingsText("Estado", "Status"),
        value = settingsText("En desarrollo", "In development")
    )

    Spacer(
        modifier = Modifier.height(18.dp)
    )

    InfoNote(
        text = "Clinch Player organiza TV en vivo, películas, series, favoritos, perfiles y reproducción dentro de una sola interfaz."
    )
}

@Composable
private fun CategoryButton(
    icon: String,
    text: String,
    selected: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val active =
        focused || selected

    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                1.05f
            } else {
                1f
            },
        animationSpec =
            tween(120),
        label =
            "settingsMenuScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .scale(scale)
            .onFocusChanged {
                focused = it.isFocused

                if (it.isFocused) {
                    onFocus()
                }
            },
        colors =
            ClickableSurfaceDefaults
                .colors(
                    containerColor =
                        Color.Transparent,
                    focusedContainerColor =
                        Color.Transparent,
                    pressedContainerColor =
                        Color.Transparent
                ),
        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(0.dp)
                )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 5.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                color =
                    if (active) {
                        ClinchYellow
                    } else {
                        Color.White.copy(
                            alpha = 0.72f
                        )
                    },
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = text,
                color =
                    if (active) {
                        ClinchYellow
                    } else {
                        Color.White.copy(
                            alpha = 0.82f
                        )
                    },
                fontSize = 12.sp,
                fontWeight =
                    if (active) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },
                maxLines = 1
            )
        }
    }
}


@Composable
private fun ChoiceSetting(
    title: String,
    options: List<Pair<String, String>>,
    selectedValue: String,
    onSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 8.dp
            )
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    18.dp
                )
        ) {
            options.forEach { option ->
                ChoiceButton(
                    text = option.first,
                    selected =
                        selectedValue ==
                                option.second,
                    onClick = {
                        onSelected(
                            option.second
                        )
                    }
                )
            }
        }

        RightSideDivider()
    }
}


@Composable
private fun ChoiceButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(120),
        label = "settingsChoiceScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .height(34.dp)
            .scale(scale)
            .onFocusChanged {
                focused = it.isFocused
            },
        colors =
            ClickableSurfaceDefaults
                .colors(
                    containerColor =
                        Color.Transparent,
                    focusedContainerColor =
                        Color.Transparent,
                    pressedContainerColor =
                        Color.Transparent
                ),
        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(0.dp)
                )
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = 4.dp
            ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    if (selected) {
                        "● $text"
                    } else {
                        "○ $text"
                    },
                color =
                    if (focused || selected) {
                        ClinchYellow
                    } else {
                        Color.White.copy(
                            alpha = 0.78f
                        )
                    },
                fontSize = 11.sp,
                fontWeight =
                    if (focused || selected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    }
            )
        }
    }
}


@Composable
private fun NumberSetting(
    title: String,
    value: Int,
    suffix: String = "",
    min: Int,
    max: Int,
    step: Int,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 8.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            SmallActionButton(
                text = "−",
                enabled = value > min,
                onClick = {
                    onValueChange(
                        (value - step)
                            .coerceAtLeast(min)
                    )
                }
            )

            Text(
                text = "$value$suffix",
                color = ClinchYellow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    horizontal = 18.dp
                )
            )

            SmallActionButton(
                text = "+",
                enabled = value < max,
                onClick = {
                    onValueChange(
                        (value + step)
                            .coerceAtMost(max)
                    )
                }
            )
        }

        RightSideDivider()
    }
}


@Composable
private fun SmallActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(120),
        label = "settingsSmallActionScale"
    )

    Surface(
        onClick = {
            if (enabled) {
                onClick()
            }
        },
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
            .onFocusChanged {
                focused = it.isFocused
            },
        colors =
            ClickableSurfaceDefaults
                .colors(
                    containerColor =
                        Color.Transparent,
                    focusedContainerColor =
                        Color.Transparent,
                    pressedContainerColor =
                        Color.Transparent
                ),
        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(0.dp)
                )
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text = text,
                color =
                    when {
                        !enabled ->
                            Color.White.copy(
                                alpha = 0.25f
                            )

                        focused ->
                            ClinchYellow

                        else ->
                            Color.White
                    },
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


@Composable
private fun ActionSetting(
    title: String,
    description: String,
    actionText: String,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(120),
        label = "settingsActionScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .onFocusChanged {
                focused = it.isFocused
            },
        colors =
            ClickableSurfaceDefaults
                .colors(
                    containerColor =
                        Color.Transparent,
                    focusedContainerColor =
                        Color.Transparent,
                    pressedContainerColor =
                        Color.Transparent
                ),
        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(0.dp)
                )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 12.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color =
                            when {
                                danger ->
                                    Color(
                                        0xFFFF6B6B
                                    )

                                focused ->
                                    ClinchYellow

                                else ->
                                    Color.White
                            },
                        fontSize = 14.sp,
                        fontWeight =
                            if (focused) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            }
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = description,
                        color = SoftWhite,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = actionText,
                    color =
                        if (danger) {
                            Color(0xFFFF6B6B)
                        } else if (focused) {
                            ClinchYellow
                        } else {
                            Color.White.copy(
                                alpha = 0.76f
                            )
                        },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            RightSideDivider()
        }
    }
}


@Composable
private fun ProfileRow(
    profile: UserProfile,
    selected: Boolean,
    canDelete: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 6.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Surface(
                onClick = onSelect,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        focused = it.isFocused
                    },
                colors =
                    ClickableSurfaceDefaults
                        .colors(
                            containerColor =
                                Color.Transparent,
                            focusedContainerColor =
                                Color.Transparent,
                            pressedContainerColor =
                                Color.Transparent
                        ),
                shape =
                    ClickableSurfaceDefaults
                        .shape(
                            RoundedCornerShape(0.dp)
                        )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = 10.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Text(
                            text = profile.name,
                            color =
                                if (focused || selected) {
                                    ClinchYellow
                                } else {
                                    Color.White
                                },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text =
                                if (profile.isKid) {
                                    settingsText("Perfil infantil", "Kids profile")
                                } else {
                                    settingsText("Perfil estándar", "Standard profile")
                                },
                            color = SoftWhite,
                            fontSize = 10.sp
                        )
                    }

                    if (selected) {
                        Text(
                            text = settingsText("ACTIVO", "ACTIVE"),
                            color = ClinchYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (canDelete) {
                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                SmallTextButton(
                    text = settingsText("ELIMINAR", "DELETE"),
                    danger = true,
                    onClick = onDelete
                )
            }
        }

        RightSideDivider()
    }
}


@Composable
private fun AddProfileDialog(
    onDismiss: () -> Unit,
    onSave: (
        String,
        Boolean
    ) -> Unit
) {
    var name by
    remember {
        mutableStateOf("")
    }

    var isKid by
    remember {
        mutableStateOf(false)
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .width(520.dp)
                .background(
                    Color(0xFF141414),
                    RoundedCornerShape(14.dp)
                )
                .padding(26.dp)
        ) {
            Text(
                text = settingsText("NUEVO PERFIL", "NEW PROFILE"),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = settingsText("Nombre", "Name"),
                color = SoftWhite,
                fontSize = 11.sp
            )

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            BasicTextField(
                value = name,
                onValueChange = {
                    name = it.take(24)
                },
                singleLine = true,
                textStyle =
                    TextStyle(
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(
                            alpha = 0.08f
                        ),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(
                        horizontal = 14.dp,
                        vertical = 13.dp
                    )
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            ToggleSetting(
                title = settingsText("Perfil infantil", "Kids profile"),
                description = settingsText("Permite aplicar control parental separado.", "Allows separate parental control settings."),
                checked = isKid,
                onClick = {
                    isKid = !isKid
                }
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {
                SmallTextButton(
                    text = settingsText("CANCELAR", "CANCEL"),
                    onClick = onDismiss
                )

                SmallTextButton(
                    text = settingsText("GUARDAR", "SAVE"),
                    highlighted = true,
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(
                                name,
                                isKid
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PinDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var pin by
    remember {
        mutableStateOf("")
    }

    var confirmPin by
    remember {
        mutableStateOf("")
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .width(480.dp)
                .background(
                    Color(0xFF141414),
                    RoundedCornerShape(14.dp)
                )
                .padding(26.dp)
        ) {
            Text(
                text = title.uppercase(),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            PasswordField(
                label = settingsText("PIN de 4 dígitos", "4-digit PIN"),
                value = pin,
                onValueChange = {
                    pin =
                        it
                            .filter(
                                Char::isDigit
                            )
                            .take(4)
                }
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            PasswordField(
                label = settingsText("Confirmar PIN", "Confirm PIN"),
                value = confirmPin,
                onValueChange = {
                    confirmPin =
                        it
                            .filter(
                                Char::isDigit
                            )
                            .take(4)
                }
            )

            if (
                confirmPin.isNotEmpty() &&
                pin != confirmPin
            ) {
                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = settingsText("Los PIN no coinciden.", "PINs do not match."),
                    color = Color(0xFFFF6B6B),
                    fontSize = 11.sp
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {
                SmallTextButton(
                    text = settingsText("CANCELAR", "CANCEL"),
                    onClick = onDismiss
                )

                SmallTextButton(
                    text = settingsText("GUARDAR", "SAVE"),
                    highlighted = true,
                    onClick = {
                        if (
                            pin.length == 4 &&
                            pin == confirmPin
                        ) {
                            onSave(pin)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            color = SoftWhite,
            fontSize = 11.sp
        )

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation =
                PasswordVisualTransformation(),
            textStyle =
                TextStyle(
                    color = Color.White,
                    fontSize = 18.sp,
                    letterSpacing = 4.sp
                ),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White.copy(
                        alpha = 0.08f
                    ),
                    RoundedCornerShape(8.dp)
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 13.dp
                )
        )
    }
}

@Composable
private fun SmallTextButton(
    text: String,
    highlighted: Boolean = false,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(120),
        label = "settingsTextButtonScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .height(34.dp)
            .scale(scale)
            .onFocusChanged {
                focused = it.isFocused
            },
        colors =
            ClickableSurfaceDefaults
                .colors(
                    containerColor =
                        Color.Transparent,
                    focusedContainerColor =
                        Color.Transparent,
                    pressedContainerColor =
                        Color.Transparent
                ),
        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(0.dp)
                )
    ) {
        Box(
            modifier =
                Modifier.padding(
                    horizontal = 8.dp
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text = text,
                color =
                    when {
                        danger ->
                            Color(0xFFFF6B6B)

                        focused || highlighted ->
                            ClinchYellow

                        else ->
                            Color.White
                    },
                fontSize = 11.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


@Composable
private fun EditableTextSetting(
    title: String,
    description: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical =
                        10.dp
                )
    ) {
        Text(
            text =
                title,
            color =
                Color.White,
            fontSize =
                14.sp,
            fontWeight =
                FontWeight.Medium
        )

        Spacer(
            modifier =
                Modifier.height(
                    4.dp
                )
        )

        Text(
            text =
                description,
            color =
                SoftWhite,
            fontSize =
                10.sp
        )

        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        BasicTextField(
            value =
                value,
            onValueChange =
                onValueChange,
            singleLine =
                true,
            textStyle =
                TextStyle(
                    color =
                        Color.White,
                    fontSize =
                        13.sp
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(
                            alpha =
                                0.05f
                        ),
                        RoundedCornerShape(
                            6.dp
                        )
                    )
                    .padding(
                        horizontal =
                            12.dp,
                        vertical =
                            10.dp
                    )
        )

        RightSideDivider()
    }
}


@Composable
private fun InfoCard(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 10.dp
            )
    ) {
        Text(
            text = title,
            color = SoftWhite,
            fontSize = 10.sp
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        RightSideDivider()
    }
}


@Composable
private fun InfoNote(
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 10.dp
            ),
        verticalAlignment =
            Alignment.Top
    ) {
        Text(
            text = "ⓘ",
            color = ClinchYellow,
            fontSize = 12.sp
        )

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Text(
            text = text,
            color = SoftWhite,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun ToggleSetting(
    title: String,
    description: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(120),
        label = "settingsToggleScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .onFocusChanged {
                focused = it.isFocused
            },
        colors =
            ClickableSurfaceDefaults
                .colors(
                    containerColor =
                        Color.Transparent,
                    focusedContainerColor =
                        Color.Transparent,
                    pressedContainerColor =
                        Color.Transparent
                ),
        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(0.dp)
                )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 12.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color =
                            if (focused) {
                                ClinchYellow
                            } else {
                                Color.White
                            },
                        fontSize = 14.sp,
                        fontWeight =
                            if (focused) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text = description,
                        color = SoftWhite,
                        fontSize = 10.sp
                    )
                }

                Spacer(
                    modifier = Modifier.width(20.dp)
                )

                ToggleVisual(
                    checked = checked
                )
            }

            RightSideDivider()
        }
    }
}


@Composable
private fun ToggleVisual(
    checked: Boolean
) {
    Box(
        modifier = Modifier
            .width(50.dp)
            .height(26.dp)
            .background(
                color =
                    if (checked) {
                        ClinchYellow
                    } else {
                        Color(0xFF444444)
                    },
                shape =
                    RoundedCornerShape(
                        20.dp
                    )
            )
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .align(
                    if (checked) {
                        Alignment.CenterEnd
                    } else {
                        Alignment.CenterStart
                    }
                )
                .background(
                    Color.White,
                    RoundedCornerShape(50)
                )
        )
    }
}

@Composable
private fun SettingsButton(
    text: String,
    onClick: () -> Unit
) {
    var focused by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue =
            if (focused) {
                1.05f
            } else {
                1f
            },
        animationSpec =
            tween(120),
        label =
            "settingsBackScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .height(36.dp)
            .scale(scale)
            .onFocusChanged {
                focused = it.isFocused
            },
        colors =
            ClickableSurfaceDefaults
                .colors(
                    containerColor =
                        Color.Transparent,
                    focusedContainerColor =
                        Color.Transparent,
                    pressedContainerColor =
                        Color.Transparent
                ),
        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(0.dp)
                )
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(
                    horizontal = 5.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                color =
                    if (focused) {
                        ClinchYellow
                    } else {
                        Color.White.copy(
                            alpha = 0.72f
                        )
                    },
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Text(
                text = text
                    .removePrefix("←")
                    .trim(),
                color =
                    if (focused) {
                        ClinchYellow
                    } else {
                        Color.White.copy(
                            alpha = 0.82f
                        )
                    },
                fontSize = 12.sp,
                fontWeight =
                    if (focused) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    }
            )
        }
    }
}


@Composable
private fun DangerButton(
    text: String,
    onClick: () -> Unit
) {
    var focused by
    remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(120),
        label = "dangerButtonScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .onFocusChanged {
                focused = it.isFocused
            },
        colors =
            ClickableSurfaceDefaults
                .colors(
                    containerColor =
                        Color.Transparent,
                    focusedContainerColor =
                        Color.Transparent,
                    pressedContainerColor =
                        Color.Transparent
                ),
        shape =
            ClickableSurfaceDefaults
                .shape(
                    RoundedCornerShape(0.dp)
                )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 14.dp
                ),
            contentAlignment =
                Alignment.CenterStart
        ) {
            Text(
                text = text,
                color =
                    if (focused) {
                        Color(0xFFFF8A8A)
                    } else {
                        Color(0xFFFF6B6B)
                    },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
private fun RightSideDivider() {
    Spacer(
        modifier = Modifier.height(10.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Color.White.copy(
                    alpha = 0.07f
                )
            )
    )
}


private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }


@Composable
private fun SettingGap() {
    Spacer(
        modifier = Modifier.height(10.dp)
    )
}
