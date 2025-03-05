package es.androidtfm.gamevision.ui.views.composables

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import es.androidtfm.gamevision.retrofit.Game
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.SearchViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 19/02/2025
 * Descripción: 
 */

@Composable
fun GameDetails(
    navController: NavController,
    isDarkTheme: Boolean,
    paddingValues: PaddingValues,
    gameId: Int,
    ddbbViewModel: DDBBViewModel,
    userViewModel: UserViewModel,
    viewModel: SearchViewModel = viewModel(
        viewModelStoreOwner = navController.getBackStackEntry("searchScreen")
    )
) {
    val formFields by userViewModel.formFields.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(gameId) {
        viewModel.fetchGameDetails(gameId)
        formFields["email"]?.let { email ->
            ddbbViewModel.fetchUserData(email)
            ddbbViewModel.addGameToHistory(email, gameId.toString())
        }
    }

    val game by viewModel.gameDetails.collectAsState()
    val isLoading by viewModel.isLoadingDetails.collectAsState()
    val error by viewModel.errorDetails.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            isLoading -> FullScreenLoader()
            error != null -> ErrorMessage(error)
            game != null -> GameContent(
                navController,
                game,
                ddbbViewModel,
                formFields["email"]
            )

            else -> EmptyState()
        }
    }
}

@Composable
private fun GameContent(
    navController: NavController,
    game: Game?,
    ddbbViewModel: DDBBViewModel,
    email: String?
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        GameHeader(game)

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            MetaDataSection(game)
            Spacer(Modifier.height(24.dp))
            ActionButtons(navController, ddbbViewModel, game, email)
        }
    }
}

@Composable
private fun GameHeader(game: Game?) {
    Box(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = game?.backgroundImage,
            contentDescription = "Imagen del juego",
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentScale = ContentScale.Crop,
            placeholder = rememberVectorPainter(Icons.Default.Star),
            error = rememberVectorPainter(Icons.Default.Clear)
        )

        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = game?.name ?: "Nombre no disponible",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MetaDataSection(game: Game?) {
    val items = mutableListOf<MetaItem>().apply {
        game?.suggestionsCount?.let {
            add(
                MetaItem(
                    icon = Icons.Default.Star,
                    title = "Veces recomendado",
                    value = it.toString()
                )
            )
        }
        game?.metacritic?.let {
            add(
                MetaItem(
                    icon = Icons.Default.Star,
                    title = "Metacritic Score",
                    value = it.toString()
                )
            )
        }
        game?.rating?.let {
            add(
                MetaItem(
                    icon = Icons.Default.Star,
                    title = "RAWG Rating",
                    value = it.toString()
                )
            )
        }
        game?.released?.let {
            add(
                MetaItem(
                    icon = Icons.Default.DateRange,
                    title = "Lanzamiento",
                    value = it
                )
            )
        }
        game?.genres?.joinToString { it.name }?.let {
            add(
                MetaItem(
                    icon = Icons.Default.Info,
                    title = "Género",
                    value = it
                )
            )
        }
    }

    items.forEach { item ->
        MetaItemRow(item)
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun MetaItemRow(item: MetaItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = item.value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ActionButtons(
    navController: NavController,
    ddbbViewModel: DDBBViewModel,
    game: Game?,
    email: String?
) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilledTonalButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            Spacer(Modifier.width(8.dp))
            Text("Volver")
        }

        AddButtonFunctionality(ddbbViewModel, game, email, Modifier.weight(1f))

        // Botón para compartir
        Button(
            onClick = {
                game?.let { gameDetails ->
                    val shareText =
                        "¡Mira este juego! ${gameDetails.name} - ${gameDetails.backgroundImage}"
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    navController.context.startActivity(
                        Intent.createChooser(
                            sendIntent,
                            "Compartir juego"
                        )
                    )
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Share, contentDescription = "Compartir")
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
fun AddButtonFunctionality(
    ddbbViewModel: DDBBViewModel,
    game: Game?,
    email: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listas = listOf("Juegos jugados", "Lista de deseos")

    Box(modifier = modifier) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir")
            Spacer(Modifier.width(8.dp))
            Text("Añadir")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listas.forEach { lista ->
                DropdownMenuItem(
                    text = { Text(lista) },
                    onClick = {
                        expanded = false
                        coroutineScope.launch {
                            game?.id?.let { gameId ->
                                email?.let {
                                    val category = when (lista) {
                                        "Lista de deseos" -> "wishlist"
                                        "Juegos jugados" -> "playedlist"
                                        else -> "unknown"
                                    }
                                    ddbbViewModel.addGameToCollection(it, gameId.toString(), category)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FullScreenLoader() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ErrorMessage(error: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = error ?: "Error desconocido",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Sin datos",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No se encontraron detalles",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ToastMessage(message: String, onDismiss: () -> Unit) {
    val toastVisible = remember { mutableStateOf(true) }
    if (toastVisible.value) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        LaunchedEffect(Unit) {
            delay(2000)
            toastVisible.value = false
            onDismiss()
        }
    }
}

data class MetaItem(
    val icon: ImageVector,
    val title: String,
    val value: String
)

