package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import es.androidtfm.gamevision.retrofit.Game
import es.androidtfm.gamevision.viewmodel.SearchViewModel

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 19/01/2025
 * Descripción: 
 */

/**
 * Pantalla principal de búsqueda de juegos.
 *
 * @param navController Controlador de navegación.
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param viewModel ViewModel para manejar la lógica de búsqueda.
 * @param paddingValues Valores de padding para la pantalla.
 */

@Composable
fun SearchScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    viewModel: SearchViewModel,
    paddingValues: PaddingValues
) {
    // Estados locales para controlar la búsqueda, criterios de ordenación y visibilidad del menú.
    var searchQuery by remember { mutableStateOf("") }
    var hasSearched by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var sortCriteria by rememberSaveable { mutableStateOf("rating") }
    var sortAscending by rememberSaveable { mutableStateOf(false) } // Orden descendente por defecto

    // Se recogen los estados de la lista de juegos, indicador de carga y errores desde el ViewModel.
    val games by viewModel.games.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Cálculo de la lista ordenada según el criterio y orden especificado.
    val sortedGames = when (sortCriteria) {
        "rating" -> if (sortAscending) games.sortedBy { it.rating } else games.sortedByDescending { it.rating }
        "name" -> if (sortAscending) games.sortedBy { it.name } else games.sortedByDescending { it.name }
        "release" -> if (sortAscending) games.sortedBy { it.released } else games.sortedByDescending { it.released }
        else -> games
    }

    // Contenedor principal de la pantalla
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                // Barra de búsqueda para introducir el término de búsqueda
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            hasSearched = true
                            viewModel.fetchGames(searchQuery)
                        }
                    }
                )

                // Mensaje inicial cuando aún no se ha realizado ninguna búsqueda
                if (!hasSearched && games.isEmpty()) {
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = "¡Busca tu primer juego!",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // Indicador de carga mientras se obtienen los datos
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 30.dp)
                    )
                }

                // Muestra un mensaje de error en caso de producirse alguno
                error?.let {
                    Text(
                        text = "Error: $it",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // Lista de juegos obtenidos de la búsqueda
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    if (hasSearched && games.isEmpty() && !isLoading) {
                        // Mensaje cuando no se encuentran resultados
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "No se encontraron resultados")
                            }
                        }
                    } else {
                        // Se muestran los juegos ordenados según los criterios seleccionados
                        items(sortedGames) { game ->
                            GameCard(game = game, navController = navController)
                        }
                    }
                }
            }

            // Botón flotante para abrir el menú de ordenación, visible si hay juegos en la lista.
            if (games.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { sortMenuExpanded = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(30.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Abrir menú de ordenación",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Menú desplegable para seleccionar el criterio y orden de clasificación
            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Nombre ${if (sortCriteria == "name") "✓" else ""}") },
                    onClick = {
                        sortCriteria = "name"
                        sortAscending = true
                        sortMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rating (Asc.) ${if (sortCriteria == "rating" && sortAscending) "✓" else ""}") },
                    onClick = {
                        sortCriteria = "rating"
                        sortAscending = true
                        sortMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rating (Desc.) ${if (sortCriteria == "rating" && !sortAscending) "✓" else ""}") },
                    onClick = {
                        sortCriteria = "rating"
                        sortAscending = false
                        sortMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Lanzamiento (Asc.) ${if (sortCriteria == "release" && sortAscending) "✓" else ""}") },
                    onClick = {
                        sortCriteria = "release"
                        sortAscending = true
                        sortMenuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Lanzamiento (Desc.) ${if (sortCriteria == "release" && !sortAscending) "✓" else ""}") },
                    onClick = {
                        sortCriteria = "release"
                        sortAscending = false
                        sortMenuExpanded = false
                    }
                )
            }
        }
    }
}

/**
 * Componente que muestra la tarjeta de un juego.
 *
 * @param game Juego a mostrar.
 * @param navController Controlador de navegación.
 */
@Composable
fun GameCard(
    game: Game,
    navController: NavController
) {
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
            // Imagen de fondo del juego
            AsyncImage(
                model = game.backgroundImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )

            // Panel inferior semitransparente con detalles del juego
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
                // Nombre del juego
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

                // Fila con el año de lanzamiento, géneros y rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Año de lanzamiento
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
                        // Géneros (si están disponibles)
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
                    // Chip que muestra el rating del juego
                    Chip(
                        text = "⭐ ${"%.1f".format(game.rating)}",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Componente tipo chip para mostrar información compacta, como el rating.
 */
@Composable
private fun Chip(
    text: String,
    containerColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

/**
 * Barra de búsqueda personalizada.
 *
 * @param query Término de búsqueda actual.
 * @param onQueryChange Función para manejar cambios en el término de búsqueda.
 * @param onSearch Función para manejar la acción de buscar.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    // Se utiliza para detectar la interacción y animar el ícono de búsqueda.
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val searchButtonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Buscar juegos...") },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .shadow(4.dp, RoundedCornerShape(24.dp)),
            trailingIcon = {
                IconButton(
                    onClick = onSearch,
                    modifier = Modifier.scale(searchButtonScale)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Iniciar búsqueda",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true,
            interactionSource = interactionSource
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {

    // Se crea un ViewModel de ejemplo para la vista previa.
    val fakeViewModel = SearchViewModel()
    SearchScreen(
        navController = NavController(LocalContext.current),
        isDarkTheme = false,
        viewModel = fakeViewModel,
        paddingValues = PaddingValues()
    )
}