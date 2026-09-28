package com.example.clinchplayer.ui.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinchplayer.data.IPTVSession
import com.example.clinchplayer.data.SessionManager
import com.example.clinchplayer.network.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


class LoginViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    // ================================================================
    // LOGIN DATA
    // ================================================================

    var server by mutableStateOf("")
    var username by mutableStateOf("")
    var password by mutableStateOf("")


    // ================================================================
    // STATE
    // ================================================================

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var loginSuccess by mutableStateOf(false)
        private set


    // ================================================================
    // LOGIN
    // ================================================================

    fun login() {

        val currentServer =
            server.trim()

        val currentUsername =
            username.trim()

        val currentPassword =
            password.trim()


        // ============================================================
        // VALIDATION
        // ============================================================

        if (
            currentServer.isBlank() ||
            currentUsername.isBlank() ||
            currentPassword.isBlank()
        ) {

            errorMessage =
                "Por favor, completa todos los campos"

            return
        }


        // ============================================================
        // NORMALIZE SERVER
        // ============================================================

        val baseUrl = normalizeServer(
            currentServer
        )


        Log.d(
            "LoginViewModel",
            "Connecting to server: $baseUrl"
        )


        // ============================================================
        // LOGIN REQUEST
        // ============================================================

        viewModelScope.launch {

            isLoading = true
            errorMessage = null
            loginSuccess = false


            try {

                val api =
                    RetrofitClient.createApi(
                        baseUrl
                    )


                val response =
                    api.login(
                        username = currentUsername,
                        password = currentPassword
                    )


                Log.d(
                    "LoginViewModel",
                    "Login HTTP: ${response.code()}"
                )


                // ====================================================
                // HTTP ERROR
                // ====================================================

                if (!response.isSuccessful) {

                    errorMessage =
                        "Servidor no responde correctamente (HTTP ${response.code()})"

                    return@launch
                }


                val body =
                    response.body()


                // ====================================================
                // EMPTY RESPONSE
                // ====================================================

                if (body == null) {

                    errorMessage =
                        "El servidor devolvió una respuesta vacía"

                    return@launch
                }


                // ====================================================
                // AUTHENTICATION
                // ====================================================

                val authenticated =
                    body.userInfo?.auth == 1


                val active =
                    body.userInfo
                        ?.status
                        ?.equals(
                            "Active",
                            ignoreCase = true
                        ) == true


                if (
                    authenticated &&
                    active
                ) {

                    // ================================================
                    // SAVE EXACT SERVER USED FOR SUCCESSFUL LOGIN
                    // ================================================

                    val expirationDate =
                        body.userInfo
                            ?.expDate
                            ?.trim()
                            ?.takeIf { value ->
                                value.isNotBlank() &&
                                        value != "0" &&
                                        !value.equals(
                                            "null",
                                            ignoreCase = true
                                        )
                            }


                    val session =
                        IPTVSession(
                            server = baseUrl,
                            username = currentUsername,
                            password = currentPassword,
                            isLoggedIn = true,
                            expirationDate = expirationDate
                        )


                    sessionManager.saveSession(
                        session
                    )


                    Log.d(
                        "LoginViewModel",
                        "Session saved successfully"
                    )


                    Log.d(
                        "LoginViewModel",
                        "Expiration timestamp: ${expirationDate ?: "not available"}"
                    )


                    loginSuccess = true

                } else {

                    errorMessage =
                        "Credenciales incorrectas o cuenta inactiva"
                }


            } catch (e: CancellationException) {

                throw e


            } catch (e: SocketTimeoutException) {

                Log.e(
                    "LoginViewModel",
                    "Server timeout",
                    e
                )


                errorMessage =
                    "El servidor tardó demasiado en responder"


            } catch (e: UnknownHostException) {

                Log.e(
                    "LoginViewModel",
                    "Unknown host",
                    e
                )


                errorMessage =
                    "No se pudo encontrar el servidor"


            } catch (e: ConnectException) {

                Log.e(
                    "LoginViewModel",
                    "Connection failed",
                    e
                )


                errorMessage =
                    "No se pudo conectar al servidor"


            } catch (e: Exception) {

                Log.e(
                    "LoginViewModel",
                    "Login error: ${e.javaClass.simpleName}: ${e.message}",
                    e
                )


                errorMessage =
                    "Error de conexión: ${e.javaClass.simpleName}"


            } finally {

                isLoading = false
            }
        }
    }


    // ================================================================
    // NORMALIZE SERVER URL
    // ================================================================

    private fun normalizeServer(
        value: String
    ): String {

        var result =
            value.trim()


        // ============================================================
        // ADD HTTP IF USER DID NOT ENTER A PROTOCOL
        // ============================================================

        if (
            !result.startsWith(
                "http://",
                ignoreCase = true
            ) &&
            !result.startsWith(
                "https://",
                ignoreCase = true
            )
        ) {

            result =
                "http://$result"
        }


        // ============================================================
        // REMOVE PLAYER_API.PHP IF USER PASTED FULL API URL
        // ============================================================

        result =
            result.substringBefore(
                "/player_api.php",
                missingDelimiterValue = result
            )


        // ============================================================
        // REMOVE GET.PHP IF AN M3U URL WAS PASTED
        // ============================================================

        result =
            result.substringBefore(
                "/get.php",
                missingDelimiterValue = result
            )


        // ============================================================
        // RETROFIT REQUIRES TRAILING /
        // ============================================================

        result =
            result.trimEnd('/') + "/"


        return result
    }
}