package es.androidtfm.gamevision.ui.views.composables

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 03/03/2025
 * Descripción: 
 */

@Composable
fun FriendsList(
    isDarkTheme: Boolean,
    paddingValues: PaddingValues,
    navController: NavController,
    userViewModel: UserViewModel,
    ddbbViewModel: DDBBViewModel
) {
    val formFields by userViewModel.formFields.collectAsState()
    val email = formFields["email"]
    var friendsList by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    var searchField by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) } // Estado de carga
    val coroutineScope = rememberCoroutineScope()

    // Recuperar la lista de amigos cuando cambia el email
    LaunchedEffect(email) {
        friendsList = if (!email.isNullOrEmpty()) { // Manejo de email nulo o vacío
            ddbbViewModel.getFriendsList(email).also {
                isLoading = false // Datos cargados, cambiamos el estado
            }
        } else {
            emptyList() // Limpia la lista si el email es nulo
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp) // <- Padding horizontal agregado aquí
                .padding(bottom = paddingValues.calculateBottomPadding() + 80.dp)
        ) {
            if (isLoading) {
                //... loading indicator
            } else {
                LazyColumn(
                    contentPadding = paddingValues,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp, 0.dp, 0.dp, 10.dp) // <- Padding modificado
                ) {
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
                                text = "Seguidos",
                                style = MaterialTheme.typography.headlineLarge,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                    items(friendsList) { friend ->
                        // Muestra cada amigo como un elemento de la lista
                        if (email != null) {
                            FriendItem(friend = friend, email, ddbbViewModel) { updatedFriendsList ->
                                friendsList = updatedFriendsList // Actualiza la lista de amigos
                            }
                        }
                    }
                }
            }
        }

        // Search field at the bottom of the screen
        TextField(
            value = searchField,
            onValueChange = { searchField = it },
            placeholder = { Text("Introduce el email de tu amigo...") },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = paddingValues.calculateBottomPadding() + 15.dp) // Added padding to separate it from the screen edges and list
                .shadow(4.dp, RoundedCornerShape(24.dp)),
            trailingIcon = {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (ddbbViewModel.checkEmailExists(searchField)) {
                                ddbbViewModel.addFriend(email.toString(), searchField)
                                friendsList = ddbbViewModel.getFriendsList(email.toString()) // Actualiza la lista de amigos
                            }
                            else {
                                Log.d("FriendsList", "El usuario no existe")
                            }
                        }
                    },
                    modifier = Modifier
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
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
        )
    }
}

@Composable
fun FriendItem(
    friend: Map<String, Any>,
    email: String,
    ddbbViewModel: DDBBViewModel,
    onFriendRemoved: (List<Map<String, Any>>) -> Unit // Callback para actualizar la lista
) {
    val coroutineScope = rememberCoroutineScope()
    val username = friend["username"] as? String ?: "No username"
    val friendEmail = friend["email"] as? String ?: "No email"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Reducción del padding
        shape = RoundedCornerShape(12.dp), // Reducción del tamaño del borde redondeado
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Reducción de la elevación
    ) {
        Row( // Cambio de Column a Row para un diseño más compacto
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp) // Reducción del padding interno
        ) {
            Column(
                modifier = Modifier.weight(1f) // Permite que los textos ocupen el espacio restante
            ) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1, // Limita a una línea para un diseño más compacto
                    overflow = TextOverflow.Ellipsis // Agrega elipsis si el texto es muy largo
                )
                Text(
                    text = friendEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1, // Limita a una línea para un diseño más compacto
                    overflow = TextOverflow.Ellipsis // Agrega elipsis si el texto es muy largo
                )
            }
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        ddbbViewModel.removeFriend(email, friendEmail)
                        val updatedFriendsList = ddbbViewModel.getFriendsList(email)
                        onFriendRemoved(updatedFriendsList)
                    }
                },
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FriendsListPreview() {
    val context = LocalContext.current
    FriendsList(
        isDarkTheme = false,
        paddingValues = PaddingValues(),
        navController = remember { NavController(context) },
        userViewModel = UserViewModel(), // Instancia ficticia para el preview
        ddbbViewModel = DDBBViewModel() // Instancia ficticia para el preview
    )
}