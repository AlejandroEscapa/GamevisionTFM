package es.androidtfm.gamevision

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import es.androidtfm.gamevision.ui.navigation.NavHost
import es.androidtfm.gamevision.ui.theme.DarkColorPalette
import es.androidtfm.gamevision.ui.theme.LightColorPalette
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.GoogleViewModel
import es.androidtfm.gamevision.viewmodel.NewsViewModel
import es.androidtfm.gamevision.viewmodel.SearchViewModel
import es.androidtfm.gamevision.viewmodel.ThemeViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val googleViewModel: GoogleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Google Sign-In en el ViewModel
        val oneTapClient = Identity.getSignInClient(this)
        val webClientId = getString(R.string.default_web_client_id)
        googleViewModel.initializeGoogleSignIn(oneTapClient, webClientId)

        // Inicializar el ActivityResultLauncher para Google Sign-In
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                googleViewModel.handleSignInResult(data)
            } else {
                Log.e("MainActivity", "Google Sign-In cancelado o fallido")
                Toast.makeText(this, "Google Sign-In cancelado o fallido", Toast.LENGTH_SHORT).show()
            }
        }

        // Observar el estado del inicio de sesión de Google
        googleViewModel.signInState.observe(this) { state ->
            when (state) {
                is GoogleViewModel.SignInState.Success -> {
                    Log.d("MainActivity", "Usuario autenticado con UID: ${state.userUid}")
                }
                is GoogleViewModel.SignInState.Error -> {
                    Log.e("MainActivity", state.message)
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> { /* Estado Idle o Loading: No hacer nada */ }
            }
        }

        setContent {
            // Se instancian los ViewModels compartidos a nivel de actividad
            val themeViewModel: ThemeViewModel = viewModel()
            val userViewModel: UserViewModel = viewModel()
            val newsViewModel: NewsViewModel = viewModel()
            val ddbbViewModel: DDBBViewModel = viewModel()
            // Para SearchViewModel se instancia aquí; en producción podrías compartirlo o instanciarlo desde un contenedor de navegación.
            val searchViewModel = SearchViewModel()

            MainScreen(
                window = window,
                themeViewModel = themeViewModel,
                userViewModel = userViewModel,
                googleViewModel = googleViewModel,
                newsViewModel = newsViewModel,
                googleSignInLauncher = googleSignInLauncher,
                onGoogleSignInClick = {
                    lifecycleScope.launch { initiateGoogleSignIn() }
                },
                ddbbViewModel = ddbbViewModel,
                searchViewModel = searchViewModel // Si NewsScreen u otras pantallas lo requieren
            )
        }
    }

    // Función para iniciar el flujo de Google Sign-In
    private suspend fun initiateGoogleSignIn() {
        googleViewModel.signIn(
            onSuccess = { intentSender ->
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                    googleSignInLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    Log.e("GoogleSignIn", "Error al lanzar el intent: ${e.message}")
                }
            },
            onError = { errorMessage ->
                Log.e("GoogleSignIn", "Error en signIn(): $errorMessage")
            }
        )
    }
}

@Composable
fun MainScreen(
    window: Window,
    themeViewModel: ThemeViewModel,
    userViewModel: UserViewModel,
    googleViewModel: GoogleViewModel,
    newsViewModel: NewsViewModel,
    googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>,
    onGoogleSignInClick: () -> Unit,
    ddbbViewModel: DDBBViewModel,
    searchViewModel: SearchViewModel
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val colors = if (isDarkTheme) DarkColorPalette else LightColorPalette
    val navController = rememberNavController()
    val themeDataStore = themeViewModel.themeDataStore
    // Observar estado de invitado
    val isGuest by userViewModel.isGuest.collectAsState()

    var isNavHostInitialized by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = colors) {
        // Configuración del sistema UI (barras de estado, navegación, etc.)
        SystemUiController(window, isDarkTheme)

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
            color = MaterialTheme.colorScheme.background
        ) {
            // Se invoca el NavGraph compartido, pasando los ViewModels ya instanciados
            NavHost(
                navController = navController,
                themeDataStore = themeDataStore,
                onThemeChange = { themeViewModel.toggleTheme() },
                userViewModel = userViewModel,
                googleViewModel = googleViewModel,
                newsViewModel = newsViewModel,
                googleSignInLauncher = googleSignInLauncher,
                onGoogleSignInClick = onGoogleSignInClick,
                ddbbViewModel = ddbbViewModel,
                isGuest = isGuest,
                searchViewModel = searchViewModel
            )

            // Se controla la inicialización del NavHost
            LaunchedEffect(navController) {
                isNavHostInitialized = true
            }
        }
    }

    // Navegación automática: Si hay usuario autenticado y no es invitado, se navega a "news"
    LaunchedEffect(isNavHostInitialized) {
        if (isNavHostInitialized) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && !isGuest) {
                navController.navigate("news") {
                    popUpTo("main") { inclusive = true }
                }
            }
        }
    }
}

/*
 * Configuración de UI para el sistema (barras de estado y navegación)
 */
@Composable
fun SystemUiController(window: Window, isDarkTheme: Boolean) {
    val isLightTheme = !isSystemInDarkTheme()
    val systemBarColor = if (isLightTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface

    SideEffect {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        window.statusBarColor = systemBarColor.toArgb()
        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme
    }
}