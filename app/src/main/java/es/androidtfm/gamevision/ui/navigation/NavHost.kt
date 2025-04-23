package es.androidtfm.gamevision.ui.navigation

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import es.androidtfm.gamevision.datastore.ThemeDataStore
import es.androidtfm.gamevision.ui.views.composables.EditProfileScreen
import es.androidtfm.gamevision.ui.views.composables.FriendsList
import es.androidtfm.gamevision.ui.views.composables.GameDetails
import es.androidtfm.gamevision.ui.views.composables.GameListScreen
import es.androidtfm.gamevision.ui.views.composables.HomeScreen
import es.androidtfm.gamevision.ui.views.composables.LoginScreen
import es.androidtfm.gamevision.ui.views.composables.NewsScreen
import es.androidtfm.gamevision.ui.views.composables.PassScreen
import es.androidtfm.gamevision.ui.views.composables.ProfileScreen
import es.androidtfm.gamevision.ui.views.composables.RegisterScreen
import es.androidtfm.gamevision.ui.views.composables.SearchScreen
import es.androidtfm.gamevision.ui.views.composables.SocialScreen
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.GoogleViewModel
import es.androidtfm.gamevision.viewmodel.NewsViewModel
import es.androidtfm.gamevision.viewmodel.SearchViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 15/01/2025
 * Descripción: 
 */

@Composable
fun NavHost(
    navController: NavHostController,
    themeDataStore: ThemeDataStore,
    onThemeChange: (Boolean) -> Unit,
    userViewModel: UserViewModel,
    googleViewModel: GoogleViewModel,
    newsViewModel: NewsViewModel,
    googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>?,
    onGoogleSignInClick: () -> Unit,
    ddbbViewModel: DDBBViewModel,
    isGuest: Boolean,
    searchViewModel: SearchViewModel
) {
    val isDarkTheme by themeDataStore.isDarkTheme.collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // Pantalla principal de la aplicación
        composable("main") {
            HomeScreen(
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange,
                navController = navController,
                isGuest = isGuest,
                onGuestStatusChange = { guestStatus ->
                    userViewModel.setGuestStatus(guestStatus)
                }
            )
        }

        // Pantalla de Login
        composable("login") {
            LoginScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                navconThemeChange = onThemeChange,
                userViewModel = userViewModel,
                googleViewModel = googleViewModel,
                googleSignInLauncher = googleSignInLauncher,
                onGoogleSignInClick = onGoogleSignInClick,
                ddbbViewModel = ddbbViewModel
            )
        }

        // Pantalla de noticias: Es la pantalla principal donde se carga la información del usuario (fetch centralizado)
        composable("news") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel // Se utiliza el ViewModel compartido
            ) { paddingValues ->
                NewsScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    newsViewModel = newsViewModel,
                    userViewModel = userViewModel,
                    ddbbViewModel = ddbbViewModel,
                    paddingValues = paddingValues
                )
            }
        }

        // Pantalla del perfil
        composable("profile") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                ProfileScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    paddingValues = paddingValues,
                    navController = navController,
                    userViewModel = userViewModel,
                    ddbbViewModel = ddbbViewModel,
                    googleViewModel = googleViewModel
                )
            }
        }

        // Pantalla de lista de juegos
        composable("gamelist") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                GameListScreen(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    paddingValues = paddingValues,
                    ddbbViewModel = ddbbViewModel,
                    searchViewModel = searchViewModel,
                    userViewModel = userViewModel
                )
            }
        }

        // Pantalla de búsqueda de juegos
        composable("gameSearch") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                SearchScreen(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    viewModel = searchViewModel,
                    paddingValues = paddingValues
                )
            }
        }

        // Pantalla de registro
        composable("register") {
            RegisterScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                userViewModel = userViewModel,
                ddbbViewModel = ddbbViewModel
            )
        }

        // Pantalla de recuperación de contraseña
        composable("passrecover") {
            PassScreen(
                isDarkTheme = isDarkTheme,
                navController = navController,
                userViewModel = UserViewModel() // Si es posible, utiliza el mismo userViewModel
            )
        }

        // Pantalla de edición del perfil
        composable("editProfile") {
            EditProfileScreen(
                isDarkTheme = isDarkTheme,
                paddingValues = PaddingValues(),
                navController = navController,
                userViewModel = userViewModel,
                ddbbViewModel = ddbbViewModel
            )
        }

        // Pantalla de la red social (timeline)
        composable("social") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                SocialScreen(
                    isDarkTheme = isDarkTheme,
                    paddingValues = paddingValues,
                    navController = navController,
                    userViewModel = userViewModel,
                    ddbbViewModel = ddbbViewModel
                )
            }
        }

        // Pantalla de detalles del juego
        composable("gameDetails/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toIntOrNull()
            val parentEntry = remember(backStackEntry) {
                // Verifica si la ruta "gameSearch" está en la pila de navegación
                if (navController.currentBackStackEntry?.destination?.route == "gameSearch") {
                    navController.getBackStackEntry("gameSearch")
                } else {
                    backStackEntry
                }
            }
            val sharedViewModel: SearchViewModel = viewModel(parentEntry)

            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                gameId?.let {
                    GameDetails(
                        navController = navController,
                        isDarkTheme = isDarkTheme,
                        viewModel = sharedViewModel,
                        paddingValues = paddingValues,
                        gameId = it,
                        ddbbViewModel = ddbbViewModel,
                        userViewModel = userViewModel
                    )
                }
            }
        }

        // Pantalla de amigos
        composable("friendlist") {
            AppScaffold(
                navController = navController,
                userViewModel = userViewModel
            ) { paddingValues ->
                FriendsList(
                    isDarkTheme = isDarkTheme,
                    paddingValues = paddingValues,
                    navController = navController,
                    userViewModel = userViewModel,
                    ddbbViewModel = ddbbViewModel
                )
            }
        }
    }
}