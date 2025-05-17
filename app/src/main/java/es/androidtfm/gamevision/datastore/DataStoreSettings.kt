package es.androidtfm.gamevision.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 16/01/2025
 * Descripción: 
 */

// Primer DataStore para configuraciones generales
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "themeSettings")

//Metodos para el cambio de tema
class ThemeDataStore(context: Context) {
    private val themeDataStore = context.settingsDataStore
    private val theme = booleanPreferencesKey("theme") // Uso de booleanPreferencesKey

    // Flow que expone el estado del tema
    val isDarkTheme: Flow<Boolean> = themeDataStore.data
        .map { preferences ->
            preferences[theme] ?: false // Por defecto, es falso (modo claro)
        }

    // Función para cambiar el estado del tema
    suspend fun swapTheme(isDark: Boolean) {
        themeDataStore.edit { preferences ->
            preferences[theme] = isDark
        }
    }
}