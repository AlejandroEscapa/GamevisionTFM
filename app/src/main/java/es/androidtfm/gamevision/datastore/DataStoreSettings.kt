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

//// Segundo DataStore para datos de perfil
//val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "profileData")
//
//// Clase para el manejo de datos de perfil
//class ProfileDataStore(context: Context) {
//    private val profileDataStore = context.profileDataStore
//
//    private val usernameKey = stringPreferencesKey("username")
//    private val emailKey = stringPreferencesKey("email")
//    private val descriptionKey = stringPreferencesKey("description")
//
//    // Flow que expone el nombre de usuario
//    val usernameFlow: Flow<String?> = profileDataStore.data
//        .map { preferences ->
//            preferences[usernameKey]
//        }
//
//    // Flow que expone el nombre de usuario
//    val emailFlow: Flow<String?> = profileDataStore.data
//        .map { preferences ->
//            preferences[emailKey]
//        }
//
//    // Flow que expone el nombre de usuario
//    val descriptionFlow: Flow<String?> = profileDataStore.data
//        .map { preferences ->
//            preferences[descriptionKey]
//        }
//
//    // Funciones para guardar datos
//    suspend fun saveUsername(username: String) {
//        profileDataStore.edit { preferences ->
//            preferences[usernameKey] = username
//        }
//    }
//
//    suspend fun saveEmail(email: String) {
//        profileDataStore.edit { preferences ->
//            preferences[emailKey] = email
//        }
//    }
//
//    suspend fun saveDescription(description: String) {
//        profileDataStore.edit { preferences ->
//            preferences[descriptionKey] = description
//        }
//    }
//}

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