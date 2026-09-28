package com.example.clinchplayer.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(
    private val context: Context
) {

    companion object {

        private const val UPDATE_JSON_URL =
            "https://raw.githubusercontent.com/Clinch-create/ClinchPlayer/main/update.json"
    }


    // ============================================================
    // BUSCAR ACTUALIZACIÓN
    // ============================================================

    suspend fun checkForUpdate(): UpdateInfo? =
        withContext(Dispatchers.IO) {

            var connection: HttpURLConnection? = null

            try {

                connection =
                    URL(UPDATE_JSON_URL)
                        .openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.useCaches = false

                connection.setRequestProperty(
                    "Accept",
                    "application/json"
                )

                connection.setRequestProperty(
                    "User-Agent",
                    "ClinchPlayer"
                )

                connection.connect()

                if (
                    connection.responseCode !in 200..299
                ) {
                    return@withContext null
                }

                val response =
                    connection.inputStream
                        .bufferedReader()
                        .use {
                            it.readText()
                        }

                val json =
                    JSONObject(response)

                val remoteVersionCode =
                    json.getLong(
                        "versionCode"
                    )

                val installedVersionCode =
                    getInstalledVersionCode()

                if (
                    remoteVersionCode >
                    installedVersionCode
                ) {

                    UpdateInfo(
                        versionCode =
                            remoteVersionCode,

                        versionName =
                            json.optString(
                                "versionName",
                                ""
                            ),

                        apkUrl =
                            json.optString(
                                "apkUrl",
                                ""
                            ),

                        notes =
                            json.optString(
                                "notes",
                                ""
                            ),

                        required =
                            json.optBoolean(
                                "required",
                                false
                            )
                    )

                } else {

                    null
                }

            } catch (e: Exception) {

                e.printStackTrace()
                null

            } finally {

                connection?.disconnect()
            }
        }


    // ============================================================
    // VERSION INSTALADA
    // ============================================================

    private fun getInstalledVersionCode(): Long {

        @Suppress("DEPRECATION")
        val packageInfo =
            context.packageManager
                .getPackageInfo(
                    context.packageName,
                    0
                )

        return if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.P
        ) {

            packageInfo.longVersionCode

        } else {

            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    }


    // ============================================================
    // PERMISO PARA INSTALAR APK
    // ============================================================

    fun canInstallPackages(): Boolean {

        return if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            context.packageManager
                .canRequestPackageInstalls()

        } else {

            true
        }
    }


    fun openInstallPermissionSettings() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val intent =
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse(
                        "package:${context.packageName}"
                    )
                ).apply {

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            context.startActivity(
                intent
            )
        }
    }


    // ============================================================
    // DESCARGAR APK
    // ============================================================

    suspend fun downloadUpdate(
        updateInfo: UpdateInfo,
        onProgress: (Int) -> Unit
    ): File? =
        withContext(Dispatchers.IO) {

            var connection: HttpURLConnection? = null

            try {

                val updateDirectory =
                    File(
                        context.cacheDir,
                        "updates"
                    )

                if (
                    !updateDirectory.exists()
                ) {
                    updateDirectory.mkdirs()
                }


                val apkFile =
                    File(
                        updateDirectory,
                        "ClinchPlayer.apk"
                    )


                if (
                    apkFile.exists()
                ) {
                    apkFile.delete()
                }


                connection =
                    URL(
                        updateInfo.apkUrl
                    )
                        .openConnection() as HttpURLConnection


                connection.instanceFollowRedirects =
                    true

                connection.connectTimeout =
                    15_000

                connection.readTimeout =
                    60_000

                connection.useCaches =
                    false

                connection.setRequestProperty(
                    "User-Agent",
                    "ClinchPlayer-Updater"
                )

                connection.connect()


                if (
                    connection.responseCode !in 200..299
                ) {
                    return@withContext null
                }


                val totalSize =
                    connection.contentLengthLong


                connection.inputStream
                    .use { input ->

                        FileOutputStream(
                            apkFile
                        ).use { output ->

                            val buffer =
                                ByteArray(
                                    8192
                                )

                            var downloaded =
                                0L

                            var lastProgress =
                                -1


                            while (true) {

                                val bytesRead =
                                    input.read(
                                        buffer
                                    )

                                if (
                                    bytesRead == -1
                                ) {
                                    break
                                }

                                output.write(
                                    buffer,
                                    0,
                                    bytesRead
                                )

                                downloaded +=
                                    bytesRead


                                if (
                                    totalSize > 0
                                ) {

                                    val progress =
                                        (
                                                downloaded *
                                                        100L /
                                                        totalSize
                                                )
                                            .toInt()
                                            .coerceIn(
                                                0,
                                                100
                                            )


                                    if (
                                        progress !=
                                        lastProgress
                                    ) {

                                        lastProgress =
                                            progress

                                        withContext(
                                            Dispatchers.Main
                                        ) {

                                            onProgress(
                                                progress
                                            )
                                        }
                                    }
                                }
                            }

                            output.flush()
                        }
                    }


                if (
                    apkFile.exists() &&
                    apkFile.length() > 0
                ) {

                    apkFile

                } else {

                    null
                }

            } catch (e: Exception) {

                e.printStackTrace()
                null

            } finally {

                connection?.disconnect()
            }
        }


    // ============================================================
    // ABRIR INSTALADOR
    // ============================================================

    fun installUpdate(
        apkFile: File
    ) {

        val apkUri =
            FileProvider
                .getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )


        val intent =
            Intent(
                Intent.ACTION_VIEW
            ).apply {

                setDataAndType(
                    apkUri,
                    "application/vnd.android.package-archive"
                )

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }


        context.startActivity(
            intent
        )
    }
}