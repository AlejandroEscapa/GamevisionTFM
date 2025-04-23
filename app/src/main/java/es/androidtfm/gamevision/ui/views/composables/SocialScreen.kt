package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 26/02/2025
 * Descripción: 
 */

/**
 * Pantalla principal de la red social que muestra el timeline de mensajes.
 *
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param paddingValues PaddingValues para ajustar el layout.
 * @param navController Controlador de navegación.
 * @param userViewModel ViewModel para datos de usuario.
 * @param ddbbViewModel ViewModel para operaciones con la base de datos.
 */
@Composable
fun SocialScreen(
    isDarkTheme: Boolean,
    paddingValues: PaddingValues,
    navController: NavHostController,
    userViewModel: UserViewModel = viewModel(),
    ddbbViewModel: DDBBViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val formFields by userViewModel.formFields.collectAsState()
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val email = formFields["email"] ?: firebaseUser?.email

    var friendsList by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    var messageList by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    var isLoading by remember { mutableStateOf(true) }
    var comment by remember { mutableStateOf("") }
    val commentMaxLength = 280
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Efecto para obtener mensajes y amigos cuando cambie el email o se refresque
    LaunchedEffect(email, refreshTrigger) {
        email?.let { userEmail ->
            // Obtener lista de amigos del usuario
            friendsList = ddbbViewModel.getFriendsList(userEmail)

            // Combina mensajes de cada amigo y del propio usuario
            val allMessages = mutableListOf<Map<String, Any>>().apply {
                friendsList.forEach { friend ->
                    val friendEmail = friend["email"].toString()
                    addAll(ddbbViewModel.getFriendMessages(friendEmail)
                        .map { it + ("friendEmail" to friendEmail) })
                }
                addAll(ddbbViewModel.getFriendMessages(userEmail)
                    .map { it + ("friendEmail" to userEmail) })
            }
            val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
            messageList = allMessages.sortedByDescending {
                LocalDateTime.parse(it["hora"].toString(), formatter)
            }
            isLoading = false
        }
    }

    Surface(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SocialHeader(
                onRefresh = { refreshTrigger++ },
                navController = navController
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                items(messageList) { message ->
                    SocialCard(
                        userEmail = email.toString(),
                        friendEmail = message["friendEmail"].toString(),
                        message = message["texto"].toString(),
                        hora = message["hora"].toString(),
                        messageID = message["messageID"].toString(),
                        ddbbViewModel = ddbbViewModel,
                        isDarkMode = isDarkTheme,
                        onMessageDeleted = { refreshTrigger++ }
                    )
                }
            }
            CommentBar(
                comment = comment,
                onCommentChange = { newText ->
                    if (newText.length <= commentMaxLength) comment = newText
                },
                onSendClick = {
                    email?.let { userEmail ->
                        val formattedDateTime = LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
                        )
                        coroutineScope.launch {
                            ddbbViewModel.publishMessage(userEmail, comment, formattedDateTime)
                            comment = ""
                            refreshTrigger++
                        }
                    }
                },
                commentMaxLength = commentMaxLength
            )
        }
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SocialHeader(
    onRefresh: () -> Unit,
    navController: NavController
) {
    // Encabezado de la pantalla que muestra el título y botones para navegación y refresco
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
        // Título principal del timeline
        Text(
            text = "Timeline",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start
        )
        // Grupo de botones: navegación a la lista de amigos y refresco de datos
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.navigate("friendlist") }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Amigos",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refrescar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SocialCard(
    userEmail: String,
    friendEmail: String,
    message: String,
    hora: String,
    messageID: String,
    ddbbViewModel: DDBBViewModel,
    isDarkMode: Boolean,
    onMessageDeleted: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    // Fondo según el tema: en modo oscuro se usa un color acorde; en light, blanco.
    val backgroundColor = if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color.White

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.background(backgroundColor)
        ) {
            // Encabezado: avatar, nombre y hora
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = friendEmail.first().toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                // Nombre y hora
                Column {
                    Text(
                        text = friendEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = hora,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            // Contenido del mensaje
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 15.dp)
            )
            // Divisor para separar el contenido de las acciones
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            // Fila de acciones (botones)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                InteractionButton(
                    icon = Icons.Default.FavoriteBorder,
                    text = "Me gusta",
                    onClick = { /* Acción para 'Me gusta' */ },
                    isDarkMode = isDarkMode
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (userEmail == friendEmail) {
                    InteractionButton(
                        icon = Icons.Default.Delete,
                        text = "Borrar",
                        onClick = {
                            coroutineScope.launch {
                                ddbbViewModel.deleteMessage(friendEmail, messageID)
                                onMessageDeleted()
                            }
                        },
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}



@Composable
private fun InteractionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    // Selección de color del botón basado en el tema actual
    val buttonColor = if (isDarkMode)
        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    else
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    // Botón de texto estilizado para acciones de interacción
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun CommentBar(
    comment: String,
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit,
    commentMaxLength: Int
) {
    // Fuente de interacción para detectar pulsaciones y aplicar animación en el botón de enviar
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val sendButtonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Campo de entrada de texto para escribir el comentario
        TextField(
            value = comment,
            onValueChange = onCommentChange,
            placeholder = { Text("Haz un comentario...") },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .shadow(1.dp, RoundedCornerShape(24.dp)),
            trailingIcon = {
                // Botón de envío, animado según la interacción del usuario
                IconButton(
                    onClick = onSendClick,
                    enabled = comment.isNotBlank(),
                    modifier = Modifier.scale(sendButtonScale)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar comentario",
                        tint = if (comment.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
            interactionSource = interactionSource
        )
        // Indicador de la cantidad de caracteres escritos vs. el máximo permitido
        Text(
            text = "${comment.length} / $commentMaxLength",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp, end = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SocialScreenPreview() {
    SocialScreen(
        isDarkTheme = true,
        paddingValues = PaddingValues(),
        navController = rememberNavController(),
        userViewModel = viewModel(),
        ddbbViewModel = viewModel()
    )
}