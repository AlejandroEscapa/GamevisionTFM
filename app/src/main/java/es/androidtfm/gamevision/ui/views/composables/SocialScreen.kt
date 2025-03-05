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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
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
    val email = formFields["email"]

    var friendsList by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    var messageList by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    var isLoading by remember { mutableStateOf(true) }
    var comment by remember { mutableStateOf("") }
    val commentMaxLength = 280
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Carga de datos
    LaunchedEffect(email, refreshTrigger) {
        email?.let { emailData ->
            ddbbViewModel.fetchUserData(emailData)
            friendsList = ddbbViewModel.getFriendsList(emailData)

            val allMessages = mutableListOf<Map<String, Any>>()
            friendsList.forEach { friend ->
                val friendEmail = friend["email"].toString()
                allMessages += ddbbViewModel.getFriendMessages(friendEmail).map {
                    it + ("friendEmail" to friendEmail)
                }
            }
            allMessages += ddbbViewModel.getFriendMessages(emailData).map {
                it + ("friendEmail" to emailData)
            }

            messageList = allMessages.sortedByDescending {
                LocalDateTime.parse(
                    it["hora"].toString(),
                    DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
                )
            }
            isLoading = false
        }
    }

    // Diseño principal de SocialScreen
    Surface(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SocialHeader(onRefresh = { refreshTrigger++ }, navController = navController)

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
                        ddbbViewModel,
                        isDarkMode = isDarkTheme, // Se ajusta el fondo según el tema
                        onMessageDeleted = { refreshTrigger++ }
                    )
                }
            }

            CommentBar(
                comment = comment,
                onCommentChange = { newText ->
                    if (newText.length <= commentMaxLength) {
                        comment = newText
                    }
                },
                onSendClick = {
                    email?.let {
                        val formattedDateTime = LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
                        )
                        coroutineScope.launch {
                            ddbbViewModel.publishMessage(it, comment, formattedDateTime)
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
        // Texto en la parte izquierda
        Text(
            text = "Timeline",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start
        )

        // Botones agrupados a la derecha
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.navigate("friendlist") }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Friends",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
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

    val cardBackgroundColor = if (isDarkMode) {
        MaterialTheme.colorScheme.surfaceVariant // Fondo claro para destacar
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isDarkMode) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline) // Borde tenue
        } else {
            null
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera con avatar y hora
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = friendEmail.first().toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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


            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InteractionButton(
                    icon = Icons.Default.FavoriteBorder,
                    text = "Me gusta",
                    onClick = { /* Acción */ }
                )
                if (userEmail == friendEmail) {
                    InteractionButton(
                        icon = Icons.Default.Delete,
                        text = "Borrar",
                        onClick = {
                            coroutineScope.launch {
                                ddbbViewModel.deleteMessage(friendEmail, messageID)
                                onMessageDeleted()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InteractionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CommentBar(
    comment: String,
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit,
    commentMaxLength: Int
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val sendButtonEnabled = comment.isNotBlank()
    val sendButtonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium), label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
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
                IconButton(
                    onClick = onSendClick,
                    enabled = sendButtonEnabled,
                    modifier = Modifier.scale(sendButtonScale)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar comentario",
                        tint = if (sendButtonEnabled) MaterialTheme.colorScheme.primary
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

        // Contador de caracteres
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