package es.androidtfm.gamevision.ui.views.composables

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import es.androidtfm.gamevision.retrofit.Article
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.NewsViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 15/01/2025
 * Descripción: Pantalla de noticias, que recupera las noticias mediante NewsAPI y además
 * realiza el fetch inicial de datos de usuario para centralizarlos en el UserViewModel.
 */

@Composable
fun NewsScreen(
    isDarkTheme: Boolean, // Indica si el tema oscuro está activado
    onThemeChange: (Boolean) -> Unit, // Función para cambiar el tema
    newsViewModel: NewsViewModel, // ViewModel para manejar las noticias
    userViewModel: UserViewModel, // ViewModel compartido para datos del usuario
    ddbbViewModel: DDBBViewModel, // Opcional, para otras operaciones con la base de datos
    paddingValues: PaddingValues // Valores de padding para la pantalla
) {
    // Estado para la lista de artículos
    val newsState = remember { mutableStateOf<List<Article>>(emptyList()) }
    val context = LocalContext.current

    // Al iniciar la pantalla se obtienen las noticias y se realiza el fetch del usuario
    LaunchedEffect(Unit) {
        // Fetch de noticias filtradas
        val result = newsViewModel.fetchFilteredNews("games")
        newsState.value = result

        // Si hay usuario logueado, se realiza el fetch centralizado de los datos del usuario
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.email?.let { email ->
            // Actualizamos el UserViewModel pasando también la instancia de ddbbViewModel
            userViewModel.fetchUserData(email, ddbbViewModel)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Cabecera de la pantalla de noticias
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp, 10.dp, 16.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Noticias",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Start
                )
            }
        }
        // Se muestran los artículos o un indicador de carga
        if (newsState.value.isNotEmpty()) {
            items(newsState.value) { article ->
                ArticleCard(
                    article = article,
                    onArticleClick = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    isDarkMode = isDarkTheme
                )
            }
        } else {
            item {
                NewsLoadingIndicator()
            }
        }
    }
}

/*
 * Composable para representar cada artículo en una tarjeta.
 */
@Composable
fun ArticleCard(
    article: Article, // Artículo a mostrar
    onArticleClick: (String) -> Unit, // Función para manejar el clic en el artículo
    isDarkMode: Boolean, // Indica si el modo oscuro está activado
    viewModel: NewsViewModel = NewsViewModel() // ViewModel para formatear la fecha
) {
    // Selección de colores en función del modo
    val cardBackgroundColor = if (isDarkMode) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    // Tarjeta que muestra el artículo
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        border = if (isDarkMode) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onArticleClick(article.url) }
        ) {
            // Imagen del artículo
            AsyncImage(
                model = article.urlToImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )
            // Contenido textual del artículo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = article.source.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = viewModel.formatPublishedAt(article.publishedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/*
 * Composable que muestra un indicador de carga mientras se obtienen las noticias.
 */
@Composable
fun NewsLoadingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Cargando noticias...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/*
 * Vista previa de la pantalla de noticias.
 */
@Preview(showBackground = true)
@Composable
fun NewsScreenPreview() {
    NewsScreen(
        isDarkTheme = false,
        onThemeChange = {},
        newsViewModel = NewsViewModel(),
        userViewModel = UserViewModel(),
        ddbbViewModel = DDBBViewModel(),
        paddingValues = PaddingValues()
    )
}