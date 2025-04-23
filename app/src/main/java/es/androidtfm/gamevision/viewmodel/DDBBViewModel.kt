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

/**
 * ViewModel para la base de datos
 *
 * Contiene todos los metodos con los que se interactua con la base de datos.
 */

class DDBBViewModel : ViewModel() {

    companion object {
        private const val TAG = "DDBBViewModel"
        private const val USERS_COLLECTION = "users"
        private const val FRIENDS_COLLECTION = "friends"
        private const val HISTORY_COLLECTION = "history"
        private const val GAMES_COLLECTION = "games"
    }

    // Instancia de Firestore
    private val db = FirebaseFirestore.getInstance()

    // Flujos de estado para los datos del usuario y el indicador de carga
    private val _userData = MutableStateFlow<HashMap<String, String>?>(null)
    val userData: StateFlow<HashMap<String, String>?> = _userData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Obtiene los datos del usuario desde Firestore.
     */
    suspend fun fetchUserData(email: String) {
        _isLoading.value = true
        _userData.value = getUser(email)
        _isLoading.value = false
    }

    /**
     * Registra un nuevo usuario en Firestore.
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
     * Actualiza los datos del usuario en Firestore.
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

    /**
     * Recupera la URI de la imagen de perfil.
     */
    suspend fun recoverProfilePicture(email: String): Uri? {
        return try {
            val document = db.collection(USERS_COLLECTION)
                .document(email)
                .get()
                .await()
            document.getString("imageUri")?.let { Uri.parse(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error recuperando imagen de perfil: ${e.message}")
            null
        }
    }

    /**
     * Actualiza la imagen de perfil del usuario en Firestore.
     */
    suspend fun updateUserProfilePicture(email: String, imageUri: Uri) {
        try {
            db.collection(USERS_COLLECTION)
                .document(email)
                .update("imageUri", imageUri.toString())
                .await()
            Log.d(TAG, "Imagen de perfil actualizada para: $email")
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando imagen de perfil: ${e.message}")
        }
    }

    /**
     * Verifica las credenciales de inicio de sesión.
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
     * Añade un juego a una colección específica del usuario.
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
     * Añade un juego al historial del usuario.
     */
    suspend fun addGameToHistory(email: String, gameId: String) {
        addGameToCollection(email, gameId, HISTORY_COLLECTION)
    }

    /**
     * Elimina un juego de la colección "playedlist" del usuario.
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
     * Obtiene una lista de juegos de una colección específica del usuario.
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
     * Añade un amigo a la lista de amigos del usuario.
     */
    suspend fun addFriend(email: String, friendEmail: String) {
        try {
            val friendRef = db.collection(USERS_COLLECTION)
                .document(email)
                .collection(FRIENDS_COLLECTION)
                .document(friendEmail)
            val friendSnapshot = friendRef.get().await()
            if (friendSnapshot.exists()) {
                Log.w(TAG, "El amigo ya existe en la lista de amigos.")
                return
            }
            friendRef.set(emptyMap<String, Any>()).await()
            Log.d(TAG, "Amigo añadido correctamente.")
        } catch (e: Exception) {
            Log.e(TAG, "Error añadiendo amigo: ${e.message}")
        }
    }

    /**
     * Elimina un amigo de la lista de amigos del usuario.
     */
    fun removeFriend(email: String, friendEmail: String) {
        try {
            db.collection(USERS_COLLECTION)
                .document(email)
                .collection(FRIENDS_COLLECTION)
                .document(friendEmail)
                .delete()
            Log.d(TAG, "Amigo eliminado: $friendEmail")
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando amigo: ${e.message}")
        }
    }

    /**
     * Obtiene la lista de amigos con correo y nombre de usuario.
     */
    suspend fun getFriendsList(email: String): List<Map<String, Any>> {
        return try {
            val querySnapshot = db.collection(USERS_COLLECTION)
                .document(email)
                .collection(FRIENDS_COLLECTION)
                .get()
                .await()
            val friendsList = mutableListOf<Map<String, Any>>()
            querySnapshot.documents.forEach { document ->
                val friendEmail = document.id
                val friendData = getUser(friendEmail)
                val friendUsername = friendData?.get("username") ?: "No username"
                friendsList.add(mapOf("email" to friendEmail, "username" to friendUsername))
            }
            Log.d(TAG, "Friends: $friendsList")
            friendsList
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo amigos: ${e.message}")
            emptyList()
        }
    }

    /**
     * Publica un mensaje para el usuario.
     */
    suspend fun publishMessage(email: String, message: String, hora: String) {
        try {
            db.collection(USERS_COLLECTION)
                .document(email)
                .collection("messages")
                .add(
                    mapOf(
                        "texto" to message,
                        "hora" to hora
                    )
                )
                .await()
            Log.d(TAG, "Mensaje publicado correctamente para el usuario: $email")
        } catch (e: Exception) {
            Log.e(TAG, "Error al publicar el mensaje: ${e.message}")
        }
    }

    /**
     * Elimina un mensaje del usuario.
     */
    suspend fun deleteMessage(email: String, messageId: String) {
        try {
            db.collection(USERS_COLLECTION)
                .document(email)
                .collection("messages")
                .document(messageId)
                .delete()
                .await()
            Log.d(TAG, "Mensaje eliminado: $messageId")
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar el mensaje: ${e.message}")
        }
    }

    /**
     * Obtiene los mensajes de la colección "messages" del usuario.
     */
    suspend fun getFriendMessages(email: String): List<Map<String, Any>> {
        return try {
            val querySnapshot = db.collection(USERS_COLLECTION)
                .document(email)
                .collection("messages")
                .get()
                .await()
            if (querySnapshot.isEmpty) return emptyList()
            querySnapshot.documents.mapNotNull { document ->
                document.data?.toMutableMap()?.apply {
                    this["messageID"] = document.id
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo mensajes: ${e.message}")
            emptyList()
        }
    }

    /**
     * Obtiene los datos del usuario desde Firestore y los convierte en un HashMap.
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

    /**
     * Añade un nuevo usuario a Firestore.
     */
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

    /**
     * Verifica si el correo electrónico ya existe en Firestore.
     */
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
     * Cierra la sesión del usuario.
     */
    suspend fun logout() {
        _userData.value = null
        _isLoading.value = false
        FirebaseAuth.getInstance().signOut()
        Log.d(TAG, "Sesión cerrada y datos del usuario eliminados")
    }
}