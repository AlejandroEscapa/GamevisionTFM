package es.androidtfm.gamevision.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.androidtfm.gamevision.retrofit.Game
import es.androidtfm.gamevision.retrofit.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 19/01/2025
 * Descripción: 
 */

/**
 * ViewModel para la búsqueda de juegos
 *
 * Contiene todos los métodos con los que se interactúa con la API de juegos.
 */

class SearchViewModel : ViewModel() {

    // Estados para la búsqueda de juegos
    private val _games = MutableStateFlow<List<Game>>(emptyList())
    val games: StateFlow<List<Game>> = _games

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Estado para almacenar múltiples juegos por ID
    private val _gamesMap = mutableStateMapOf<Int, Game>()
    val gamesMap: SnapshotStateMap<Int, Game> = _gamesMap

    // Estados para los detalles del juego
    private val _gameDetails = MutableStateFlow<Game?>(null)
    val gameDetails: StateFlow<Game?> = _gameDetails

    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails

    private val _errorDetails = MutableStateFlow<String?>(null)
    val errorDetails: StateFlow<String?> = _errorDetails

    /**
     * Función para buscar juegos.
     * @param query: Término de búsqueda para encontrar juegos.
     */
    fun fetchGames(query: String) {
        viewModelScope.launch {
            _isLoading.value = true // Indica que la carga ha comenzado
            _error.value = null    // Limpia cualquier error previo

            // Limpia la consulta eliminando espacios innecesarios y comillas
            val cleanedQuery = query.trim().replace("\"", "")
            val requestBody = "search \"$cleanedQuery\"; fields *;"

            try {
                // Llamada a la API para buscar juegos
                val response = RetrofitInstance.gamesApi.searchGames(cleanedQuery)
                _games.value = response.results // Actualiza la lista de juegos
            } catch (e: HttpException) {
                // Maneja errores HTTP
                val errorResponse = e.response()?.errorBody()?.string()
                _error.value = "Error al cargar juegos: $errorResponse"
            } catch (e: Exception) {
                // Maneja errores generales
                _error.value = "Error al cargar juegos: ${e.message}"
            } finally {
                _isLoading.value = false // Carga finalizada
            }
        }
    }

    /**
     * Función para obtener detalles del juego por ID.
     * @param gameId: ID del juego del cual se desean obtener los detalles.
     */
    fun fetchGameDetails(gameId: Int) {
        viewModelScope.launch {
            _isLoadingDetails.value = true // Indica que la carga ha comenzado
            _errorDetails.value = null     // Limpia cualquier error previo

            try {
                // Llamada a la API para obtener los detalles del juego
                val response = RetrofitInstance.gamesApi.getGameDetails(gameId)
                _gameDetails.value = response // Actualiza los detalles del juego
            } catch (e: HttpException) {
                // Maneja errores HTTP
                val errorResponse = e.response()?.errorBody()?.string()
                _errorDetails.value = "Error al cargar detalles: $errorResponse"
            } catch (e: Exception) {
                // Maneja errores generales
                _errorDetails.value = "Error al cargar detalles: ${e.message}"
            } finally {
                _isLoadingDetails.value = false // Carga finalizada
            }
        }
    }

    /**
     * Función para obtener detalles de un juego y almacenarlos en el mapa.
     * @param gameId: ID del juego del cual se desean obtener los detalles.
     */
    suspend fun fetchAndStoreGameDetails(gameId: Int) {
        viewModelScope.launch {
            _isLoadingDetails.value = true // Indica que la carga ha comenzado
            try {
                // Llamada a la API para obtener los detalles del juego
                val response = RetrofitInstance.gamesApi.getGameDetails(gameId)
                _gamesMap[gameId] = response // Almacena los detalles en el mapa
            } catch (e: Exception) {
                // Maneja errores generales (puedes agregar más detalles si es necesario)
            } finally {
                _isLoadingDetails.value = false // Carga finalizada
            }
        }
    }

    /**
     * Función para eliminar un juego del mapa.
     * @param gameId: ID del juego que se desea eliminar.
     */
    fun removeGame(gameId: Int) {
        gamesMap.remove(gameId) // Elimina el juego del mapa
    }
}