package es.androidtfm.gamevision

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 20/01/2025
 * Descripción: 
 */

data class User (
    val username: String,
    val email: String,
    val password: String
)

fun toString(user: User): String {
    return "User(username='${user.username}', email='${user.email}', password='${user.password}')"
}