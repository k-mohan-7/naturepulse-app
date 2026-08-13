package com.simats.naturepulse.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.simats.naturepulse.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "naturepulse_prefs")

class PreferencesManager(private val context: Context) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val userAdapter = moshi.adapter(User::class.java)

    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val USER_KEY = stringPreferencesKey("user_json")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val userFlow: Flow<User?> = context.dataStore.data.map { prefs ->
        prefs[USER_KEY]?.let { json ->
            runCatching { userAdapter.fromJson(json) }.getOrNull()
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun saveUser(user: User) {
        context.dataStore.edit { it[USER_KEY] = userAdapter.toJson(user) }
    }

    suspend fun clear() {
        context.dataStore.edit {
            it.remove(TOKEN_KEY)
            it.remove(USER_KEY)
        }
    }

    /** Synchronous read for OkHttp interceptor (called on background thread) */
    suspend fun getTokenNow(): String? = context.dataStore.data.first()[TOKEN_KEY]
}
