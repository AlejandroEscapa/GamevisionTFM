package es.androidtfm.gamevision.ui.views.composables

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import es.androidtfm.gamevision.retrofit.Game
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.SearchViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 18/01/2025
 * Descripción: 
 */

/**
 * Pantalla con las listas de juegos.
 *
 * @param navController Controlador de navegación.
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param onThemeChange Función para cambiar el tema.
 * @param paddingValues PaddingValues para ajustar el layout.
 * @param ddbbViewModel ViewModel para operaciones con la base de datos.
 * @param searchViewModel ViewModel para obtener detalles del juego.
 * @param userViewModel ViewModel para datos de usuario.
 */
@Composable
fun GameListScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    paddingValues: PaddingValues,
    ddbbViewModel: DDBBViewModel,
    searchViewModel: SearchViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val formFields by userViewModel.formFields.collectAsState()
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val email = formFields["email"] ?: firebaseUser?.email

    var gameIds by remember { mutableStateOf<List<String>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var sortOption by rememberSaveable { mutableStateOf("Alfabético") }
    var selectedList by rememberSaveable { mutableStateOf("playedlist") }
    val gamesMap = searchViewModel.gamesMap

    // Efecto que se ejecuta cuando cambia el email o la lista seleccionada
    LaunchedEffect(email, selectedList) {
        email?.let { emailData ->
            // Se refresca la información del usuario
            ddbbViewModel.fetchUserData(emailData)
            val userData = ddbbViewModel.userData.value

            // Actualiza la información en el UserViewModel
            userViewModel.onFormFieldChange("nameSurname", userData?.get("nameSurname") ?: "")
            userViewModel.onFormFieldChange("username", userData?.get("username") ?: "")
            userViewModel.onFormFieldChange("description", userData?.get("description") ?: "")
            userViewModel.onFormFieldChange("country", userData?.get("country") ?: "")
            userViewModel.onFormFieldChange("email", emailData)

            // Obtiene los juegos del usuario en función de la lista seleccionada
            val gamesData = ddbbViewModel.getUserGames(emailData, selectedList)
            val newGameIds = gamesData.mapNotNull { it["gameId"] as? String }
            // Si la lista es "history", se limita a 20 elementos para rendimiento
            val limitedGameIds = if (selectedList == "history") newGameIds.take(20) else newGameIds

            // Se obtiene de forma asíncrona los detalles de cada juego
            limitedGameIds.map { gameId ->
                async { searchViewModel.fetchAndStoreGameDetails(gameId.toInt()) }
            }.awaitAll()

            gameIds = limitedGameIds
            isLoading = false
        }
    }

    // Se genera una lista de IDs ordenada según la opción de ordenamiento
    val sortedGameIds by remember {
        derivedStateOf {
            gameIds?.let { ids ->
                if (ids.isEmpty()) emptyList() else when (sortOption) {
                    "Alfabético" -> ids.sortedBy { gamesMap[it.toInt()]?.name ?: "" }
                    "Rating (Asc.)" -> ids.sortedBy { gamesMap[it.toInt()]?.rating ?: 0.0 }
                    "Rating (Desc.)" -> ids.sortedByDescending { gamesMap[it.toInt()]?.rating ?: 0.0 }
                    "Recomendado (Asc.)" -> ids.sortedBy { gamesMap[it.toInt()]?.ratingsCount ?: 0 }
                    "Recomendado (Desc.)" -> ids.sortedByDescending { gamesMap[it.toInt()]?.ratingsCount ?: 0 }
                    else -> ids
                }
            } ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                HeaderTitle(selectedList)
                Spacer(modifier = Modifier.width(8.dp))
                SelectListButton(
                    ddbbViewModel = ddbbViewModel,
                    onListSelected = { selected ->
                        selectedList = selected
                        isLoading = true
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                SortMenuButton(currentSortOption = sortOption) { sortOption = it }
            }
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    isLoading -> LoadingIndicator()
                    sortedGameIds.isEmpty() -> GameListEmptyState()
                    else -> GameList(
                        gameIds = sortedGameIds,
                        gamesMap = gamesMap,
                        navController = navController,
                        searchViewModel = searchViewModel,
                        context = context,
                        ddbbViewModel = ddbbViewModel,
                        userViewModel = userViewModel,
                        onGameDeleted = { deletedGameId ->
                            gameIds = gameIds?.filter { it != deletedGameId }
                        }
                    )
                }
            }
        }
    )
}

/**
 * Indicador de carga para la pantalla de juegos.
 */
@Composable
fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Estado vacío de la lista de juegos.
 */
@Composable
fun GameListEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Sin juegos",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tienes juegos añadidos aún",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GameList(
    gameIds: List<String>,
    gamesMap: Map<Int, Game>,
    navController: NavController,
    searchViewModel: SearchViewModel,
    context: Context,
    ddbbViewModel: DDBBViewModel,
    userViewModel: UserViewModel,
    onGameDeleted: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp) // Additional bottom padding
    ) {
        items(gameIds) { gameId ->
            val game = gamesMap[gameId.toInt()]
            GameCard(
                navController = navController,
                game = game,
                gameId = gameId,
                searchViewModel = searchViewModel,
                context = context,
                ddbbViewModel = ddbbViewModel,
                userViewModel = userViewModel,
                onGameDeleted = onGameDeleted
            )
        }
    }
}

@Composable
fun HeaderTitle(selectedList: String) {
    val title = when (selectedList) {
        "wishlist" -> "Lista de deseos"
        "playedlist" -> "Juegos jugados"
        "history" -> "Historial de juegos"
        else -> "Juegos jugados"
    }
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Start
    )
}

@Composable
fun GameCard(
    navController: NavController,
    game: Game?,
    gameId: String,
    searchViewModel: SearchViewModel,
    context: Context,
    ddbbViewModel: DDBBViewModel,
    userViewModel: UserViewModel,
    onGameDeleted: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val formFields by userViewModel.formFields.collectAsState()
    val email = formFields["email"]

    // Show loading state if game data isn’t available yet
    if (game == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(200.dp)
            .clickable { navController.navigate("gameDetails/${game.id}") },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image
            AsyncImage(
                model = game.backgroundImage,
                contentDescription = "Imagen del juego",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )
            // Bottom panel with game details
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(12.dp)
            ) {
                // Game name
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Row with year, genres and delete button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Año: ")
                                }
                                append(game.released.substring(0, 4))
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (game.genres.isNotEmpty()) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Géneros: ")
                                    }
                                    append(game.genres.joinToString(", ") { it.name })
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Delete icon button with functionality
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                email?.let {
                                    ddbbViewModel.removeGameFromUser(it, gameId)
                                    onGameDeleted(gameId)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .padding(bottom = 10.dp, end = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar juego",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(27.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectListButton(
    ddbbViewModel: DDBBViewModel,
    modifier: Modifier = Modifier,
    onListSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val listas = listOf(
        "Lista de deseos" to "wishlist",
        "Juegos jugados" to "playedlist",
        "Historial de juegos" to "history"
    )

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Seleccionar lista",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listas.forEach { (displayName, param) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        expanded = false
                        onListSelected(param)
                    }
                )
            }
        }
    }
}

@Composable
fun SortMenuButton(currentSortOption: String, onSortSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val sortOptions = listOf(
        "Alfabético",
        "Rating (Asc.)",
        "Rating (Desc.)",
        "Recomendado (Asc.)",
        "Recomendado (Desc.)"
    )

    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Ordenar",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Ordenar por:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider()
            }
            sortOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSortSelected(option)
                    }
                )
            }
        }
    }
}