package es.androidtfm.gamevision.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.androidtfm.gamevision.datastore.ThemeDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 16/01/2025
 * Descripción: 
 */

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    val themeDataStore = ThemeDataStore(application)

    // Exponemos el estado del tema como un `StateFlow`
    val isDarkTheme: StateFlow<Boolean> = themeDataStore.isDarkTheme.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        false // Valor inicial (por defecto modo claro)
    )

    // Función para alternar el tema
    fun toggleTheme() {
        viewModelScope.launch {
            val currentTheme = isDarkTheme.value
            themeDataStore.swapTheme(!currentTheme)
        }
    }
}