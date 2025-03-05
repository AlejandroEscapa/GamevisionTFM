package es.androidtfm.gamevision.retrofit

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 17/01/2025
 * Descripción: 
 */

data class New(
    val status: String,
    val totalResults: Long,
    val articles: List<Article> // Lista de artículos
)

data class Article(
    val source: Source,
    val author: String?,
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String
)

data class Source(
    val id: String?,
    val name: String
)