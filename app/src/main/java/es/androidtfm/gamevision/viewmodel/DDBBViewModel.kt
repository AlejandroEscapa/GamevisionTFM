package es.androidtfm.gamevision.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 16/02/2025
 * Descripción: 
 */

class DDBBViewModel : ViewModel() {
    // Constantes
    companion object {
        private const val TAG = "DDBBViewModel"
        private const val USERS_COLLECTION = "users"
        private const val FRIENDS_COLLECTION = "friends"
        private const val HISTORY_COLLECTION = "history"
        private const val GAMES_COLLECTION = "games"
    }

    private val db = FirebaseFirestore.getInstance()

    // Flujos de estado
    private val _userData = MutableStateFlow<HashMap<String, String>?>(null)
    val userData: StateFlow<HashMap<String, String>?> = _userData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Obtiene los datos del usuario desde Firestore
     * @param email Email del usuario a buscar
     */
    suspend fun fetchUserData(email: String) {
        _isLoading.value = true
        _userData.value = getUser(email)
        _isLoading.value = false
    }

    /**
     * Registra un nuevo usuario en Firestore
     * @param formFields Map con los datos del formulario de registro
     * @return Boolean indicando si el registro fue exitoso
     */
    suspend fun registerUser(formFields: Map<String, String>): Boolean {
        val email = formFields["email"].orEmpty()
        if (email.isNotEmpty() && !checkEmailExists(email)) {
            val user = hashMapOf<String, String?>(
                "nameSurname" to formFields["nameSurname"],
                "username" to formFields["username"],
                "password" to formFields["password"],
                "description" to null,
                "country" to null
            )
            addUser(email, user)
            return true
        }
        return false
    }

    /**
     * Actualiza los datos de un usuario en Firestore
     * @param email Email del usuario a actualizar
     * @param updatedFields Campos a actualizar
     */
    suspend fun updateUser(email: String, updatedFields: HashMap<String, String?>) {
        try {
            db.collection(USERS_COLLECTION)
                .document(email)
                .update(updatedFields.filterValues { it != null })
                .await()
            Log.d(TAG, "Usuario actualizado: $email")
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando usuario: ${e.message}")
        }
    }

    suspend fun recoverProfilePicture(
        email: String, onSuccess: (Uri?) -> Unit, onError: (Exception) -> Unit
    ) {
        db.collection("users").document(email)
            .get()
            .addOnSuccessListener { document ->
                val uriString = document.getString("imageUri")
                val uri = uriString?.let { Uri.parse(it) }
                onSuccess(uri) // Devuelve la URI como Uri (o null si no existe)
            }
            .addOnFailureListener { exception ->
                onError(exception) // Manejo del error
            }
    }

    suspend fun updateUserProfilePicture(email: String, imageUri: Uri) {
        try {
            db.collection(USERS_COLLECTION)
                .document(email)
                .update("imageUri", imageUri.toString())
                .await()

        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando imagen de perfil: ${e.message}")
        }
    }

    /**
     * Verifica las credenciales de login
     * @param email Email del usuario
     * @param password Contraseña a verificar
     * @return Boolean indicando si las credenciales son válidas
     */
    suspend fun loginCheck(email: String, password: String): Boolean {
        return try {
            val document = db.collection(USERS_COLLECTION)
                .document(email)
                .get()
                .await()

            document.exists() && document.getString("password") == password
        } catch (e: Exception) {
            Log.e(TAG, "Error en login: ${e.message}")
            false
        }
    }

    /**
     * Manejo de juegos del usuario
     */

    /**
     * Añade un juego a una colección específica del usuario
     * @param email Email del usuario
     * @param gameId ID del juego a añadir
     * @param targetCollection Colección destino (games/history)
     */
    suspend fun addGameToCollection(email: String, gameId: String, targetCollection: String) {
        if (email.isEmpty()) {
            Log.w(TAG, "Intento de añadir juego sin usuario autenticado")
            return
        }

        try {
            val gameData = hashMapOf("gameId" to gameId)
            db.collection(USERS_COLLECTION)
                .document(email)
                .collection(targetCollection)
                .document(gameId)
                .set(gameData)
                .await()

            Log.d(TAG, "Juego añadido a $targetCollection: $gameId")
        } catch (e: Exception) {
            Log.e(TAG, "Error añadiendo juego: ${e.message}")
        }
    }

    /**
     * Añade un juego a una colección específica del usuario
     * @param email Email del usuario
     * @param gameId ID del juego a añadir
     */
    suspend fun addGameToHistory(email: String, gameId: String) {
        if (email.isEmpty()) {
            Log.w(TAG, "Intento de añadir juego sin usuario autenticado")
            return
        }

        try {
            val gameData = hashMapOf("gameId" to gameId)
            db.collection(USERS_COLLECTION)
                .document(email)
                .collection(HISTORY_COLLECTION)
                .document(gameId)
                .set(gameData)
                .await()

            Log.d(TAG, "Juego añadido a $HISTORY_COLLECTION: $gameId")
        } catch (e: Exception) {
            Log.e(TAG, "Error añadiendo juego: ${e.message}")
        }
    }

    /**
     * Elimina un juego de la colección de juegos del usuario
     * @param email Email del usuario
     * @param gameId ID del juego a eliminar
     */
    suspend fun removeGameFromUser(email: String, gameId: String) {
        try {
            db.collection(USERS_COLLECTION)
                .document(email)
                .collection("playedlist")
                .document(gameId)
                .delete()
                .await()

            Log.d(TAG, "Juego eliminado: $email, $gameId")
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando juego: ${e.message}")
        }
    }

    /**
     * Obtiene los juegos de una colección específica del usuario
     * @param email Email del usuario
     * @param collectionName Nombre de la colección a consultar
     * @return Lista de juegos
     */
    suspend fun getUserGames(email: String, collectionName: String): List<Map<String, Any>> {
        return try {
            val querySnapshot = db.collection(USERS_COLLECTION)
                .document(email)
                .collection(collectionName)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo juegos: ${e.message}")
            emptyList()
        }
    }

    /**
     * Gestión de amigos
     */

    suspend fun addFriend(email: String, friendEmail: String) {
        try {
            // Referencia al documento del amigo en la subcolección de amigos
            val friendRef = db.collection(USERS_COLLECTION)
                .document(email)
                .collection(FRIENDS_COLLECTION)
                .document(friendEmail)

            // Verificar si el amigo ya existe en la lista
            val friendSnapshot = friendRef.get().await()
            if (friendSnapshot.exists()) {
                Log.w(TAG, "El amigo ya existe en la lista de amigos.")
                return
            }

            // Crear el documento del amigo sin campos adicionales
            friendRef.set(mapOf<String, Any>()).await()
            Log.d(TAG, "Amigo añadido correctamente.")

        } catch (e: Exception) {
            Log.e(TAG, "Error añadiendo amigo: ${e.message}")
        }
    }

    fun removeFriend(email: String, friendEmail: String) {
        try {
            db.collection(USERS_COLLECTION)
                .document(email)
                .collection(FRIENDS_COLLECTION)
                .document(friendEmail)
                .delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando amigo: ${e.message}")
        }
    }

    /**
     * Obtiene la lista de amigos de un usuario
     * @param email Email del usuario
     * @return Lista de amigos con sus datos
     */
    suspend fun getFriendsList(email: String): List<Map<String, Any>> {
        return try {
            val querySnapshot = db.collection(USERS_COLLECTION)
                .document(email)
                .collection(FRIENDS_COLLECTION)
                .get()
                .await()

            Log.d(TAG, "Lista de amigos obtenida correctamente.")
            Log.d(TAG, querySnapshot.documents.toString())

            val friendsList = mutableListOf<Map<String, Any>>()

            // Recuperar el email de cada amigo y usar getUser para obtener su username
            querySnapshot.documents.forEach { document ->
                val friendEmail = document.id
                val friendData = getUser(friendEmail)

                if (friendData != null) {
                    val friendUsername = friendData["username"] ?: "No username"
                    friendsList.add(mapOf("email" to friendEmail, "username" to friendUsername))
                } else {
                    friendsList.add(mapOf("email" to friendEmail, "username" to "No username"))
                }
            }

            Log.d(TAG, "Friends: $friendsList") // Imprime la lista de amigos con email y username
            friendsList
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo amigos: ${e.message}")
            emptyList()
        }
    }

    suspend fun publishMessage(email: String, message: String, hora: String) {
        try {
            // Añadir un nuevo documento a la colección "messages" con un ID autogenerado
            db.collection(USERS_COLLECTION)
                .document(email)
                .collection("messages")
                .add(
                    mapOf(
                        "texto" to message, // Campo "texto" con el mensaje
                        "hora" to hora      // Campo "hora" con la fecha/hora formateada
                    )
                )
                .await() // Esperar a que se complete la operación

            Log.d(TAG, "Mensaje publicado correctamente para el usuario: $email")
        } catch (e: Exception) {
            Log.e(TAG, "Error al publicar el mensaje: ${e.message}")
        }
    }

    suspend fun deleteMessage(email: String, messageId: String) {
        try {
            db.collection(USERS_COLLECTION)
                .document(email)
                .collection("messages")
                .document(messageId)
                .delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar el mensaje: ${e.message}")
        }
    }

    suspend fun getFriendMessages(email: String): List<Map<String, Any>> {
        val querySnapshot = db.collection(USERS_COLLECTION)
            .document(email)
            .collection("messages")
            .get()
            .await()

        if (querySnapshot.isEmpty) return emptyList()

        return querySnapshot.documents.mapNotNull { document ->
            document.data?.toMutableMap()?.apply {
                // Agregamos el identificador del mensaje al mapa
                this["messageID"] = document.id
            }
        }
    }

    /**
     * Funciones de utilidad
     */
    private suspend fun getUser(email: String): HashMap<String, String>? {
        return try {
            val document = db.collection(USERS_COLLECTION)
                .document(email)
                .get()
                .await()

            document.data?.mapValues { it.value.toString() } as? HashMap<String, String>
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo usuario: ${e.message}")
            null
        }
    }

    private suspend fun addUser(email: String, user: HashMap<String, String?>) {
        try {
            db.collection(USERS_COLLECTION)
                .document(email)
                .set(user)
                .await()

            Log.d(TAG, "Usuario registrado: $email")
        } catch (e: Exception) {
            Log.e(TAG, "Error registrando usuario: ${e.message}")
        }
    }

    suspend fun checkEmailExists(email: String): Boolean {
        return try {
            val document = db.collection(USERS_COLLECTION)
                .document(email)
                .get()
                .await()

            document.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error verificando email: ${e.message}")
            false
        }
    }

    /**
     * Limpia los datos del usuario y cierra sesión
     */
    suspend fun logout() {
        _userData.value = null
        _isLoading.value = false
        FirebaseAuth.getInstance().signOut()
        Log.d(TAG, "Sesión cerrada y datos del usuario eliminados")
    }
}