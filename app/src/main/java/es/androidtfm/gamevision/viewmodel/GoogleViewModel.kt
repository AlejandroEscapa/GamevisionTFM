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

class GoogleViewModel : ViewModel() {

    private var oneTapClient: SignInClient? = null
    private var signInRequest: BeginSignInRequest? = null
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var webClientId: String = "" // Almacenaremos el webClientId

    private val _signInState = MutableLiveData<SignInState>()
    val signInState: LiveData<SignInState> get() = _signInState

    sealed class SignInState {
        object Idle : SignInState()
        object Loading : SignInState()
        data class Success(val userUid: String) : SignInState()
        data class Error(val message: String) : SignInState()
    }

    fun initializeGoogleSignIn(client: SignInClient, webClientId: String) {
        this.webClientId = webClientId // Guardamos el webClientId
        oneTapClient = client
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(webClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    fun signIn(onSuccess: (IntentSender) -> Unit, onError: (String) -> Unit) {
        val client = oneTapClient ?: run {
            onError("oneTapClient no inicializado")
            return
        }
        val request = signInRequest ?: run {
            onError("signInRequest no configurado")
            return
        }
        client.beginSignIn(request)
            .addOnSuccessListener { result ->
                Log.d("GoogleViewModel", "IntentSender obtenido con éxito")
                onSuccess(result.pendingIntent.intentSender)
            }
            .addOnFailureListener { e ->
                Log.e("GoogleViewModel", "Error en beginSignIn: ${e.message}")
                onError("Error obteniendo el IntentSender: ${e.message}")
            }
    }

    fun handleSignInResult(data: Intent?) {
        if (data == null) {
            _signInState.value = SignInState.Error("Intent nulo")
            return
        }
        try {
            val client = oneTapClient ?: throw IllegalStateException("oneTapClient no inicializado")
            val credential = client.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken

            if (idToken != null) {
                firebaseAuthWithGoogle(idToken)
            } else {
                _signInState.value = SignInState.Error("Token de ID no disponible")
            }
        } catch (e: ApiException) {
            _signInState.value = SignInState.Error("Error en el inicio de sesión: ${e.statusCode}")
        } catch (e: Exception) {
            _signInState.value = SignInState.Error("Error inesperado: ${e.message}")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d("GoogleViewModel", "Autenticando con Firebase...")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        Log.d("GoogleViewModel", "Autenticación exitosa: UID=${it.uid}")
                        _signInState.value = SignInState.Success(it.uid)
                    } ?: run {
                        Log.e("GoogleViewModel", "Error: Usuario de Firebase es nulo")
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Error en autenticación con Firebase"
                    Log.e("GoogleViewModel", errorMessage)
                }
            }
    }

    fun logout(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            // 1. Cerrar sesión en Firebase
            firebaseAuth.signOut()

            // 2. Configurar opciones de Google Sign-In con el mismo webClientId
            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            // 3. Cerrar sesión en Google
            val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
            googleSignInClient.signOut()
                .addOnCompleteListener { task ->
                    Handler(Looper.getMainLooper()).post {
                        if (task.isSuccessful) {
                            Log.d("GoogleViewModel", "Sesión cerrada correctamente")
                            _signInState.postValue(SignInState.Idle)
                            onSuccess()
                        } else {
                            val errorMsg = task.exception?.message ?: "Error desconocido"
                            Log.e("GoogleViewModel", errorMsg)
                            onError(errorMsg)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("GoogleViewModel", "Error general en logout: ${e.message}")
            Handler(Looper.getMainLooper()).post {
                onError(e.message ?: "Error general en logout")
            }
        }
    }
}