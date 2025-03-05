package es.androidtfm.gamevision.retrofit

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 19/01/2025
 * Descripción: 
 */

val key = "f1d385d0a0cc44bf9e3d3de37ee3bb29"

interface GameApiService {
    @GET("games")
    suspend fun searchGames(
        @Query("search") search: String,
        @Query("key") key: String = "f1d385d0a0cc44bf9e3d3de37ee3bb29"
    ): ApiResponse

    @GET("games/{id}") // La ruta incluye el parámetro {id}
    suspend fun getGameDetails(
        @Path("id") gameId: Int, // El ID del juego se pasa como parámetro
        @Query("key") key: String = "f1d385d0a0cc44bf9e3d3de37ee3bb29" // La clave de API
    ): Game // Asegúrate de que 'Game' sea el modelo correcto para los detalles del juego
}