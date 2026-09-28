package com.example.clinchplayer.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(
    name = "iptv_session"
)


class SessionManager(
    private val context: Context
) {

    companion object {

        private val SERVER_KEY =
            stringPreferencesKey(
                "server"
            )

        private val USERNAME_KEY =
            stringPreferencesKey(
                "username"
            )

        private val PASSWORD_KEY =
            stringPreferencesKey(
                "password"
            )

        private val LOGGED_IN_KEY =
            booleanPreferencesKey(
                "logged_in"
            )

        private val EXPIRATION_DATE_KEY =
            stringPreferencesKey(
                "expiration_date"
            )
    }


    val sessionFlow: Flow<IPTVSession> =
        context
            .dataStore
            .data
            .map { preferences ->

                IPTVSession(
                    server =
                        preferences[SERVER_KEY]
                            ?: "",

                    username =
                        preferences[USERNAME_KEY]
                            ?: "",

                    password =
                        preferences[PASSWORD_KEY]
                            ?: "",

                    isLoggedIn =
                        preferences[LOGGED_IN_KEY]
                            ?: false,

                    expirationDate =
                        preferences[EXPIRATION_DATE_KEY]
                )
            }


    suspend fun saveSession(
        session: IPTVSession
    ) {

        context
            .dataStore
            .edit { preferences ->

                preferences[SERVER_KEY] =
                    session.server

                preferences[USERNAME_KEY] =
                    session.username

                preferences[PASSWORD_KEY] =
                    session.password

                preferences[LOGGED_IN_KEY] =
                    session.isLoggedIn


                val expirationDate =
                    session.expirationDate


                if (
                    expirationDate.isNullOrBlank()
                ) {

                    preferences.remove(
                        EXPIRATION_DATE_KEY
                    )

                } else {

                    preferences[EXPIRATION_DATE_KEY] =
                        expirationDate
                }
            }
    }


    suspend fun clearSession() {

        context
            .dataStore
            .edit {
                it.clear()
            }
    }
}
