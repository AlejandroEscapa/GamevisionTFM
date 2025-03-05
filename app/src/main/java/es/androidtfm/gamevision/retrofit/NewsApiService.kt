package es.androidtfm.gamevision.retrofit

import retrofit2.http.GET
import retrofit2.http.Query

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 17/01/2025
 * Descripción: 
 */

interface NewsApiService {
    @GET("v2/everything")
    suspend fun getEverything(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String
    ): New
}