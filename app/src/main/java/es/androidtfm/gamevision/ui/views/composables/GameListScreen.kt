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
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val email = formFields["email"]

    var gameIds by remember { mutableStateOf<List<String>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var sortOption by rememberSaveable { mutableStateOf("Alfabético") }
    var selectedList by rememberSaveable { mutableStateOf("playedlist") }

    val gamesMap = searchViewModel.gamesMap

    LaunchedEffect(email, selectedList) {
        email?.let { emailData ->
            ddbbViewModel.fetchUserData(emailData)
            val userData = ddbbViewModel.userData.value

            // Extraer información del perfil y actualizar el ViewModel
            userViewModel.onFormFieldChange("nameSurname", userData?.get("nameSurname") ?: "")
            userViewModel.onFormFieldChange("username", userData?.get("username") ?: "")
            userViewModel.onFormFieldChange("description", userData?.get("description") ?: "")
            userViewModel.onFormFieldChange("country", userData?.get("country") ?: "")
            userViewModel.onFormFieldChange("email", emailData)

            // Actualizar datos desde Firebase Auth
            FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
                userViewModel.onFormFieldChange(
                    "username",
                    firebaseUser.displayName?.replace("\\s".toRegex(), "") ?: ""
                )
                userViewModel.onFormFieldChange("nameSurname", firebaseUser.displayName ?: "")
                userViewModel.onFormFieldChange("email", firebaseUser.email ?: "")
            }

            val gamesData = ddbbViewModel.getUserGames(emailData, selectedList)
            val newGameIds = gamesData.mapNotNull { it["gameId"] as? String }

            newGameIds.map { gameId ->
                async { searchViewModel.fetchAndStoreGameDetails(gameId.toInt()) }
            }.awaitAll()

            gameIds = newGameIds
            isLoading = false
        }
    }


    val sortedGameIds by remember {
        derivedStateOf {
            when {
                gameIds == null -> null
                gameIds!!.isEmpty() -> emptyList()
                else -> {
                    when (sortOption) {
                        "Alfabético" -> gameIds!!.sortedBy { gamesMap[it.toInt()]?.name ?: "" }
                        "Rating (Asc.)" -> gameIds!!.sortedBy {
                            gamesMap[it.toInt()]?.rating ?: 0.0
                        }

                        "Rating (Desc.)" -> gameIds!!.sortedByDescending {
                            gamesMap[it.toInt()]?.rating ?: 0.0
                        }

                        "Recomendado (Asc.)" -> gameIds!!.sortedBy {
                            gamesMap[it.toInt()]?.ratingsCount ?: 0
                        }

                        "Recomendado (Desc.)" -> gameIds!!.sortedByDescending {
                            gamesMap[it.toInt()]?.ratingsCount ?: 0
                        }

                        else -> gameIds!!
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp, 10.dp, 16.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                HeaderTitle(selectedList)
                Spacer(modifier = Modifier.width(8.dp))
                SelectListButton(
                    ddbbViewModel,
                    onListSelected = { selectedList = it; isLoading = true })
                Spacer(modifier = Modifier.weight(1f))
                SortMenuButton(sortOption) { sortOption = it }
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
                    sortedGameIds == null -> Unit
                    sortedGameIds!!.isEmpty() -> GameListEmptyState()
                    else -> GameList(
                        sortedGameIds!!,
                        gamesMap,
                        navController,
                        searchViewModel,
                        context,
                        ddbbViewModel,
                        userViewModel
                    ) { deletedGameId -> gameIds = gameIds!!.filter { it != deletedGameId } }
                }
            }
        }
    )
}

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
            Spacer(Modifier.height(16.dp))
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
            .padding(bottom = 80.dp) // Añade un padding adicional en la parte inferior
    ) {
        items(gameIds) { gameId ->
            val game = gamesMap[gameId.toInt()]
            GameCard(
                navController,
                game,
                gameId,
                searchViewModel,
                context,
                ddbbViewModel,
                userViewModel,
                onGameDeleted
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
    Text(text = title, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Start)
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

    // Indicador de carga si no hay datos del juego
    if (game == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 16.dp)
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
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .height(250.dp) // Mayor altura para distribución
            .clickable { navController.navigate("gameDetails/${game.id}") },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen de fondo que ocupa la parte superior
            AsyncImage(
                model = game.backgroundImage,
                contentDescription = "Imagen del juego",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // Imagen ocupa la mitad superior
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )

            // Contenedor blanco para el texto en la parte inferior
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(16.dp) // Espaciado interno
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Título del juego
                        Text(
                            text = game.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis // Recorta el texto si es muy largo
                        )

                        // Año de lanzamiento del juego
                        Text(
                            text = "Lanzamiento: ${game.released.substring(0, 4)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    // Botón de eliminación
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                email?.let {
                                    ddbbViewModel.removeGameFromUser(it, gameId)
                                    onGameDeleted(gameId)
                                }
                            }
                        },
                        modifier = Modifier.size(32.dp)
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
                    onClick = { expanded = false; onListSelected(param) })
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
                Divider()
            }
            sortOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { expanded = false; onSortSelected(option) })
            }
        }
    }
}