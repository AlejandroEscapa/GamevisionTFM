package es.androidtfm.gamevision.ui.views.composables

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.androidtfm.gamevision.R
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 20/01/2025
 * Descripción: 
 */

/**
 * Pantalla de noticias, recuperandolas directamente de la API de NewsAPI.
 *
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param navController Controlador de navegación.
 * @param userViewModel ViewModel para manejar las noticias.
 */

@Composable
fun PassScreen(
    isDarkTheme: Boolean,
    navController: NavController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Estados del formulario y mensajes
    val message by userViewModel.message.collectAsState()
    val formFields by userViewModel.formFields.collectAsState()

    // Efecto para mostrar un Toast si hay un mensaje
    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            userViewModel.clearMessage()
        }
    }

    // Diseño principal de la pantalla de recuperación de contraseña
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Encabezado de la pantalla
            LoginHeader(
                isDarkTheme = isDarkTheme,
                title = "Recuperar Contraseña",
                subtitle = "Ingresa tu email para restablecerla"
            )

            // Tarjeta que contiene el formulario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    // Campo de correo electrónico
                    OutlinedTextField(
                        value = formFields["email"] ?: "",
                        onValueChange = { userViewModel.onFormFieldChange("email", it) },
                        label = {
                            Text(text = "Introduce tu correo: ")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = "Icono de correo"
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botón para enviar instrucciones
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val success = userViewModel.forgotPassword()
                                if (success) navController.navigate("login")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Enviar instrucciones",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    // Enlace para volver al inicio de sesión
                    TextButton(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Volver al log-in",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginHeader(
    isDarkTheme: Boolean, // Indica si el tema oscuro está activado
    title: String, // Título del encabezado
    subtitle: String // Subtítulo del encabezado
) {
    // Encabezado con el logo y el mensaje de bienvenida
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), // Mismo padding vertical que en LoginScreen
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .padding(8.dp),
            painter = painterResource(
                id = if (isDarkTheme) R.drawable.gamevisionnight
                else R.drawable.gamevision2
            ),
            contentDescription = "Logo",
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp)) // Igual que en LoginScreen
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(8.dp)) // Mismo espacio que en LoginScreen
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PassScreenPreview() {

    // Previsualización de la pantalla de recuperación de contraseña
    PassScreen(
        isDarkTheme = false,
        navController = NavController(LocalContext.current),
        userViewModel = UserViewModel()
    )
}