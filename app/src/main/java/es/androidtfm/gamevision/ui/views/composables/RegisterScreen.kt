package es.androidtfm.gamevision.ui.views.composables

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.content.MediaType.Companion.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@Composable
fun RegisterScreen(
    isDarkTheme: Boolean,
    navController: NavController,
    userViewModel: UserViewModel = UserViewModel(),
    ddbbViewModel: DDBBViewModel = DDBBViewModel()
) {
    val context = LocalContext.current
    val registrationMessage by userViewModel.message.collectAsState()
    val formFields by userViewModel.formFields.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(registrationMessage) {
        if (registrationMessage.isNotBlank()) {
            Toast.makeText(context, registrationMessage, Toast.LENGTH_LONG).show()
            userViewModel.clearMessage()
        }
    }

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
            LoginHeader(
                isDarkTheme = isDarkTheme,
                title = "Crear Cuenta",
                subtitle = "Regístrate para comenzar"
            )

            // Ajusta el padding vertical aquí
            Spacer(modifier = Modifier.height(0.dp))

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

@Composable
private fun RegistrationForm(
    formFields: Map<String, String>,
    onFormFieldChange: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
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
                    visualTransformation = if (field.contains("password"))
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
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

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

@Composable
private fun LoginHeader(
    isDarkTheme: Boolean,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), // Reducido de 32.dp
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .padding(6.dp),
            painter = painterResource(
                id = if (isDarkTheme) R.drawable.gamevisionnight
                else R.drawable.gamevision2
            ),
            contentDescription = "Logo",
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        isDarkTheme = false,
        navController = NavController(LocalContext.current)
    )
}