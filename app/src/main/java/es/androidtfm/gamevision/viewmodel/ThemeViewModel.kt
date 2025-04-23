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

/**
 * ViewModel para gestionar el tema de la aplicación.
 *
 * Se encarga de manejar el estado del tema (claro/oscuro) y proporcionar métodos para alternarlo.
 */

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    // Instancia de `ThemeDataStore` para acceder a las preferencias de tema
    val themeDataStore = ThemeDataStore(application)

    // Exponemos el estado del tema como un `StateFlow`
    val isDarkTheme: StateFlow<Boolean> = themeDataStore.isDarkTheme.stateIn(
        viewModelScope, // CoroutineScope del ViewModel
        SharingStarted.Lazily, // Inicia la recolección de datos cuando hay al menos un observador
        false // Valor inicial (por defecto modo claro)
    )

    /**
     * Función para alternar el tema entre claro y oscuro.
     */
    fun toggleTheme() {
        viewModelScope.launch {
            // Obtiene el estado actual del tema
            val currentTheme = isDarkTheme.value
            // Cambia el tema al estado opuesto
            themeDataStore.swapTheme(!currentTheme)
        }
    }
}