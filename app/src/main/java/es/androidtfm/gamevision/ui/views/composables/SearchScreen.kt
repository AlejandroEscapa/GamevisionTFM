package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import es.androidtfm.gamevision.retrofit.Game
import es.androidtfm.gamevision.viewmodel.SearchViewModel

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 19/01/2025
 * Descripción: 
 */

@Composable
fun SearchScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    viewModel: SearchViewModel,
    paddingValues: PaddingValues
) {
    var searchQuery by remember { mutableStateOf("") }
    var hasSearched by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var sortCriteria by remember { mutableStateOf("rating") }
    var sortAscending by remember { mutableStateOf(true) }
    val games by viewModel.games.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) { }

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

                if (!hasSearched && games.isEmpty()) {
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = "¡Busca tu primer juego!",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 30.dp)
                    )
                }

                error?.let {
                    Text(
                        text = "Error: $it",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    if (hasSearched && games.isEmpty() && !isLoading) {
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
                        items(games.sortedWith(compareBy<Game> {
                            when (sortCriteria) {
                                "rating" -> if (sortAscending) it.rating else -it.rating
                                "name" -> it.name
                                "release" -> if (sortAscending) it.released else it.released.reversed()
                                else -> it.name
                            }
                        })) { game ->
                            GameCard(game = game, navController = navController)
                        }
                    }
                }
            }

            if (hasSearched && games.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Ordenar")
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(text = { Text("Nombre") }, onClick = {
                    sortCriteria = "name"
                    expanded = false
                })
                DropdownMenuItem(text = { Text("Rating (Asce.)") }, onClick = {
                    sortCriteria = "rating"
                    sortAscending = true
                    expanded = false
                })
                DropdownMenuItem(text = { Text("Rating (Desc.)") }, onClick = {
                    sortCriteria = "rating"
                    sortAscending = false
                    expanded = false
                })
                DropdownMenuItem(text = { Text("Lanzamiento (Asce.)") }, onClick = {
                    sortCriteria = "release"
                    sortAscending = true
                    expanded = false
                })
                DropdownMenuItem(text = { Text("Lanzamiento (Desc.)") }, onClick = {
                    sortCriteria = "release"
                    sortAscending = false
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun GameCard(
    game: Game,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                navController.navigate("gameDetails/${game.id}")
            },
        shape = MaterialTheme.shapes.medium
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = game.backgroundImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .alpha(0.6f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Text(
                    text = "Rating: ${"%.1f".format(game.rating)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )

                Text(
                    text = "Fecha de lanzamiento: ${game.released.substring(0, 4)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val searchButtonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium), label = ""
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
                .shadow(4.dp, RoundedCornerShape(24.dp)), // Sombra aplicada correctamente
            trailingIcon = {
                IconButton(
                    onClick = onSearch,
                    modifier = Modifier.scale(searchButtonScale)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
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
    val fakeViewModel = SearchViewModel()
    SearchScreen(
        navController = NavController(LocalContext.current),
        isDarkTheme = false,
        viewModel = fakeViewModel,
        paddingValues = PaddingValues()
    )
}