package es.androidtfm.gamevision.viewmodel

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await


/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 27/01/2025
 * Descripción: 
 */

/**
 * ViewModel para la gestión de la API de Google
 *
 * Contiene todos los metodos con los que se interactua con esta API.
 */

class GoogleViewModel : ViewModel() {

    // Cliente para el inicio de sesión con Google (One Tap)
    private var oneTapClient: SignInClient? = null

    // Solicitud de inicio de sesión con Google
    private var signInRequest: BeginSignInRequest? = null

    // Instancia de FirebaseAuth para la autenticación con Firebase
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // ID del cliente web para la autenticación con Google
    private var webClientId: String = "" // Se almacena el webClientId

    // Estado del inicio de sesión con Google (LiveData)
    private val _signInState = MutableLiveData<SignInState>()
    val signInState: LiveData<SignInState> get() = _signInState

    // Estados posibles del inicio de sesión
    sealed class SignInState {
        object Idle : SignInState()         // Estado inicial o inactivo
        object Loading : SignInState()      // Proceso de carga
        data class Success(val userUid: String) : SignInState()  // Inicio de sesión exitoso
        data class Error(val message: String) : SignInState()      // Error en el inicio de sesión
    }

    /**
     * Inicializa el inicio de sesión con Google.
     *
     * @param client Cliente de inicio de sesión (One Tap).
     * @param webClientId ID del cliente web para la autenticación con Google.
     */
    fun initializeGoogleSignIn(client: SignInClient, webClientId: String) {
        this.webClientId = webClientId // Guardamos el webClientId
        oneTapClient = client
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)                    // Habilita el uso de tokens de ID de Google
                    .setServerClientId(webClientId)          // Configura el ID del cliente web
                    .setFilterByAuthorizedAccounts(false)    // Permite cuentas no autorizadas
                    .build()
            )
            .setAutoSelectEnabled(true) // Habilita la selección automática de la cuenta
            .build()
    }

    /**
     * Inicia el proceso de inicio de sesión con Google.
     *
     * @param onSuccess Callback que se ejecuta si se obtiene el IntentSender correctamente.
     * @param onError Callback que se ejecuta si ocurre un error.
     */
    fun signIn(onSuccess: (IntentSender) -> Unit, onError: (String) -> Unit) {
        // Establecemos el estado a Loading para reflejar que se inicia el proceso
        _signInState.value = SignInState.Loading

        val client = oneTapClient ?: run {
            onError("oneTapClient no inicializado")
            _signInState.value = SignInState.Error("oneTapClient no inicializado")
            return
        }
        val request = signInRequest ?: run {
            onError("signInRequest no configurado")
            _signInState.value = SignInState.Error("signInRequest no configurado")
            return
        }
        client.beginSignIn(request)
            .addOnSuccessListener { result ->
                Log.d("GoogleViewModel", "IntentSender obtenido con éxito")
                onSuccess(result.pendingIntent.intentSender) // Devuelve el IntentSender para iniciar la actividad
            }
            .addOnFailureListener { e ->
                Log.e("GoogleViewModel", "Error en beginSignIn: ${e.message}")
                onError("Error obteniendo el IntentSender: ${e.message}")
                _signInState.value = SignInState.Error("Error obteniendo el IntentSender: ${e.message}")
            }
    }

    /**
     * Maneja el resultado del inicio de sesión con Google.
     *
     * @param data Intent con los datos del inicio de sesión.
     */
    fun handleSignInResult(data: Intent?) {
        if (data == null) {
            _signInState.value = SignInState.Error("Intent nulo")
            return
        }
        try {
            val client = oneTapClient ?: throw IllegalStateException("oneTapClient no inicializado")
            val credential = client.getSignInCredentialFromIntent(data) // Obtiene la credencial del Intent
            val idToken = credential.googleIdToken // Obtiene el token de ID de Google

            if (idToken != null) {
                firebaseAuthWithGoogle(idToken) // Autentica con Firebase usando el token
            } else {
                _signInState.value = SignInState.Error("Token de ID no disponible")
            }
        } catch (e: ApiException) {
            _signInState.value = SignInState.Error("Error en el inicio de sesión: ${e.statusCode}")
        } catch (e: Exception) {
            _signInState.value = SignInState.Error("Error inesperado: ${e.message}")
        }
    }

    /**
     * Autentica con Firebase usando el token de ID de Google.
     *
     * @param idToken Token de ID de Google.
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d("GoogleViewModel", "Autenticando con Firebase...")
        val credential = GoogleAuthProvider.getCredential(idToken, null) // Crea la credencial de Firebase
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        Log.d("GoogleViewModel", "Autenticación exitosa: UID=${it.uid}")
                        _signInState.value = SignInState.Success(it.uid) // Notifica el éxito
                    } ?: run {
                        Log.e("GoogleViewModel", "Error: Usuario de Firebase es nulo")
                        _signInState.value = SignInState.Error("Usuario de Firebase es nulo")
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Error en autenticación con Firebase"
                    Log.e("GoogleViewModel", errorMessage)
                    _signInState.value = SignInState.Error(errorMessage)
                }
            }
    }

    /**
     * Cierra la sesión del usuario en Firebase y Google.
     *
     * @param context Contexto de la aplicación.
     * @param onSuccess Callback que se ejecuta si el cierre de sesión es exitoso.
     * @param onError Callback que se ejecuta si ocurre un error.
     */
    fun logout(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            // Cerrar sesión en Firebase
            firebaseAuth.signOut()

            // Configurar opciones de Google Sign-In usando el mismo webClientId
            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
            googleSignInClient.signOut()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("GoogleViewModel", "Sesión cerrada correctamente")
                        _signInState.postValue(SignInState.Idle) // Restablece el estado a Idle
                        onSuccess()
                    } else {
                        val errorMsg = task.exception?.message ?: "Error desconocido"
                        Log.e("GoogleViewModel", errorMsg)
                        _signInState.postValue(SignInState.Error(errorMsg))
                        onError(errorMsg)
                    }
                }
        } catch (e: Exception) {
            Log.e("GoogleViewModel", "Error general en logout: ${e.message}")
            _signInState.postValue(SignInState.Error(e.message ?: "Error general en logout"))
            onError(e.message ?: "Error general en logout")
        }
    }
}