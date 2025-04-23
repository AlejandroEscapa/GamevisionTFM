package es.androidtfm.gamevision.viewmodel

import androidx.lifecycle.ViewModel
import es.androidtfm.gamevision.retrofit.Article
import es.androidtfm.gamevision.retrofit.RetrofitInstance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 17/01/2025
 * Descripción: 
 */

/**
 * ViewModel para la búsqueda de noticias
 *
 * Contiene todos los metodos con los que se interactua con la API de noticias.
 */

class NewsViewModel : ViewModel() {

    // Clave de la API para acceder al servicio de noticias
    private val apiKey = "860f15b681614860b332dc2f3cac8f02"

    /**
     * Obtiene noticias filtradas según una consulta.
     * @param query: Término de búsqueda para filtrar las noticias.
     * @return Lista de artículos filtrados y ordenados.
     */
    suspend fun fetchFilteredNews(query: String): List<Article> {
        // Llamada a la API para obtener las noticias
        val response = RetrofitInstance.newsApi.getEverything(query, apiKey)

        // Filtra las noticias que no contienen "[Removed]" en el título o no tienen imagen
        return response.articles.filter {
            !it.title.contains("[Removed]", ignoreCase = true)
                    && !it.urlToImage.isNullOrEmpty()
        }.sortedByDescending { it.publishedAt } // Ordena por fecha de publicación en orden descendente
            .take(25) // Limita el resultado a 25 artículos
    }

    /**
     * Formatea la fecha de publicación de un artículo.
     * @param dateString: Cadena de fecha en formato ISO 8601 (ej. "2023-10-05T14:30:00Z").
     * @return Cadena de fecha formateada (ej. "14:30 05-10-2023").
     */
    fun formatPublishedAt(dateString: String): String {
        // Define el formato de entrada (ISO 8601)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        // Define el formato de salida (HH:mm dd-MM-yyyy)
        val outputFormat = SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault())

        return try {
            // Convierte la cadena de fecha a un objeto Date
            val date: Date = inputFormat.parse(dateString) ?: return ""
            // Formatea la fecha al nuevo formato y devuelve la cadena
            outputFormat.format(date)
        } catch (e: Exception) {
            // Maneja la excepción si la cadena no es válida
            ""
        }
    }
}