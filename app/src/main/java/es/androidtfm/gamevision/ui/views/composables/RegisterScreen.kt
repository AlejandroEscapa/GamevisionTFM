package es.androidtfm.gamevision.ui.views.composables

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import es.androidtfm.gamevision.R
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 20/01/2025
 * Descripción: 
 */

/**
 * Pantalla de registro de usuarios.
 *
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param navController Controlador de navegación.
 * @param userViewModel ViewModel para manejar los datos del usuario.
 * @param ddbbViewModel ViewModel para manejar la base de datos.
 */

@Composable
fun RegisterScreen(
    isDarkTheme: Boolean, // Indica si el tema oscuro está activado
    navController: NavController, // Controlador de navegación
    userViewModel: UserViewModel = UserViewModel(), // ViewModel para manejar los datos del usuario
    ddbbViewModel: DDBBViewModel = DDBBViewModel() // ViewModel para manejar la base de datos
) {
    val context = LocalContext.current
    val registrationMessage by userViewModel.message.collectAsState() // Mensaje de registro
    val formFields by userViewModel.formFields.collectAsState() // Campos del formulario
    val coroutineScope = rememberCoroutineScope() // CoroutineScope para operaciones asíncronas

    // Efecto para mostrar un Toast si hay un mensaje de registro
    LaunchedEffect(registrationMessage) {
        if (registrationMessage.isNotBlank()) {
            Toast.makeText(context, registrationMessage, Toast.LENGTH_LONG).show()
            userViewModel.clearMessage()
        }
    }

    // Diseño principal de la pantalla de registro
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
            // Encabezado de la pantalla de registro
            LoginHeader(
                isDarkTheme = isDarkTheme,
                title = "Registro",
                subtitle = "Regístrate para comenzar"
            )

            // Espaciador ajustado
            Spacer(modifier = Modifier.height(0.dp))

            // Formulario de registro
            RegistrationForm(
                formFields = formFields,
                onFormFieldChange = { field, value ->
                    userViewModel.onFormFieldChange(field, value)
                },
                onRegisterClick = {
                    coroutineScope.launch {
                        val success = ddbbViewModel.registerUser(formFields)
                        if (success) navController.navigate("login")
                    }
                },
                onLoginClick = { navController.navigate("login") }
            )
        }
    }
}

/**
 * Formulario de registro de usuarios.
 *
 * @param formFields Campos del formulario.
 * @param onFormFieldChange Función para manejar cambios en los campos del formulario.
 * @param onRegisterClick Función para manejar el evento de registro.
 * @param onLoginClick Función para manejar el evento de inicio de sesión.
 */

@Composable
private fun RegistrationForm(
    formFields: Map<String, String>,
    onFormFieldChange: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val passwordVisibility = remember {
        mutableStateMapOf(
            "password" to false,
            "confirmPassword" to false
        )
    }

    // Tarjeta que contiene el formulario de registro
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp), // Reducido de 16dp
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp) // Reducido de 24dp
                .fillMaxWidth()
        ) {
            // Lista de campos del formulario
            listOf(
                "nameSurname" to "Nombre completo",
                "username" to "Nombre de usuario",
                "email" to "Email",
                "password" to "Contraseña",
                "confirmPassword" to "Confirmar contraseña"
            ).forEach { (field, label) ->
                OutlinedTextField(
                    value = formFields[field].orEmpty(),
                    onValueChange = { onFormFieldChange(field, it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    label = {
                        Text(
                            text = when (field) {
                                "nameSurname" -> "Introduce tu nombre completo"
                                "username" -> "Introduce tu nombre de usuario"
                                "email" -> "Introduce tu email"
                                "password" -> "Introduce tu contraseña"
                                "confirmPassword" -> "Confirma tu contraseña"
                                else -> ""
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    },
                    visualTransformation = if (
                        (field == "password" || field == "confirmPassword")
                        && passwordVisibility[field] != true
                    )
                        PasswordVisualTransformation()
                    else
                        VisualTransformation.None,
                    leadingIcon = {
                        Icon(
                            imageVector = when (field) {
                                "nameSurname" -> Icons.Outlined.AccountCircle
                                "username" -> Icons.Outlined.Person
                                "email" -> Icons.Outlined.Email
                                else -> Icons.Outlined.Lock
                            },
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (field == "password" || field == "confirmPassword") {
                            val visible = passwordVisibility[field] ?: false
                            IconButton(
                                modifier = Modifier.padding(end = 10.dp),
                                onClick = {
                                    passwordVisibility[field] = !visible
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Visibility,
                                    contentDescription = "Mostrar contraseña",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Botón de registro
            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Registrarse", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(15.dp))

            // Enlace para iniciar sesión
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onLoginClick,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        "¿Ya tienes cuenta? Inicia Sesión",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * Cabecera de la pantalla de registro.
 *
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param title Título de la pantalla.
 * @param subtitle Subtítulo de la pantalla.
 */

@Composable
private fun LoginHeader(
    isDarkTheme: Boolean,
    title: String,
    subtitle: String
) {
    // Encabezado con el logo y el mensaje de bienvenida
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp), // Reducido de 32.dp
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp)) // Igual que en LoginScreen
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(10.dp)) // Mismo espacio que en LoginScreen
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(10.dp)) // Mismo espacio que en LoginScreen
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {

    // Previsualización de la pantalla de registro
    RegisterScreen(
        isDarkTheme = false,
        navController = NavController(LocalContext.current)
    )
}