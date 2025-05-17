package es.androidtfm.gamevision.ui.views.composables

import android.content.Context
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.google.firebase.auth.FirebaseAuth
import es.androidtfm.gamevision.R
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.GoogleViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 17/01/2025
 * Descripción: 
 */

/**
 * Pantalla de login de la aplicación.
 *
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param navController Controlador de navegación.
 * @param navconThemeChange Función para cambiar el tema.
 * @param userViewModel ViewModel para datos de usuario.
 * @param googleViewModel ViewModel para operaciones con Google.
 * @param googleSignInLauncher Launcher para iniciar sesión con Google.
 * @param onGoogleSignInClick Función para iniciar sesión con Google.
 * @param ddbbViewModel ViewModel para operaciones con la base de datos.
 */

@Composable
fun LoginScreen(
    isDarkTheme: Boolean,
    navController: NavController,
    navconThemeChange: (Boolean) -> Unit,
    userViewModel: UserViewModel,
    googleViewModel: GoogleViewModel,
    googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>?,
    onGoogleSignInClick: () -> Unit,
    ddbbViewModel: DDBBViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Estados del formulario y mensajes
    val formFields by userViewModel.formFields.collectAsState()
    val message by userViewModel.message.collectAsState()
    val signInState by googleViewModel.signInState.observeAsState()

    // Maneja el estado del inicio de sesión con Google
    HandleSignInState(signInState, navController, context)

    // Diseño principal de la pantalla de inicio de sesión
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
            // Encabezado de la pantalla de inicio de sesión
            LoginHeader(isDarkTheme)

            // Formulario de inicio de sesión
            LoginForm(
                formFields = formFields,
                message = message,
                onEmailChange = { userViewModel.onFormFieldChange("email", it) },
                onPasswordChange = { userViewModel.onFormFieldChange("password", it) },
                onLoginClick = {
                    if (validateLoginForm(formFields)) {
                        coroutineScope.launch {
                            val exist = ddbbViewModel.loginCheck(
                                formFields["email"].toString(),
                                formFields["password"].toString()
                            )
                            if (!exist) {
                                userViewModel.setMessage("Usuario o contraseña incorrectos")
                            } else {
                                ddbbViewModel.fetchUserData(formFields["email"].toString())
                                userViewModel.setGuestStatus(false)
                                navController.navigateToNews()
                            }
                        }
                    }
                },
                onForgotPasswordClick = { navController.navigate("passrecover") },
                onGoogleSignInClick = {
                    onGoogleSignInClick()
                    googleViewModel.signIn(
                        onSuccess = { intentSender ->
                            googleSignInLauncher?.launch(
                                IntentSenderRequest.Builder(intentSender).build()
                            )
                            userViewModel.setGuestStatus(false)

                            coroutineScope.launch {
                                ddbbViewModel.fetchUserData(formFields["email"].toString())
                            }
                        },
                        onError = { errorMessage ->
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }
    }
}

@Composable
private fun LoginHeader(isDarkTheme: Boolean) {
    // Encabezado con el logo y el mensaje de bienvenida
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 90.dp), // Ajustado a 0.dp
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp)) // Mantener este espacio si es necesario
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Inicia sesión para continuar",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

/**
 * Formulario de inicio de sesión.
 *
 * @param formFields Campos del formulario.
 * @param message Mensaje de error o información.
 * @param onEmailChange Función para cambiar el correo electrónico.
 * @param onPasswordChange Función para cambiar la contraseña.
 * @param onLoginClick Función para iniciar sesión.
 * @param onForgotPasswordClick Función para recuperar la contraseña.
 * @param onGoogleSignInClick Función para iniciar sesión con Google.
 * @param onRegisterClick Función para navegar a la pantalla de registro.
 */

@Composable
private fun LoginForm(
    formFields: Map<String, String>,
    message: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    // Tarjeta que contiene el formulario de inicio de sesión
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
            // Mensaje de error o información
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                )
            }
            // Campo de correo electrónico
            OutlinedTextField(
                value = formFields["email"] ?: "",
                onValueChange = onEmailChange,
                label = {
                    Text(
                        text = "Introduce el email: ",
                        fontSize = 14.sp)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Campo de contraseña
            OutlinedTextField(
                value = formFields["password"] ?: "",
                onValueChange = onPasswordChange,
                label = {
                    Text(
                        text = "Introduce la contraseña:",
                        fontSize = 14.sp // Cambia este valor según el tamaño que quieras
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (!passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                },
                trailingIcon = {
                    IconButton(
                        modifier = Modifier.padding(end = 10.dp),
                        onClick = { passwordVisible = !passwordVisible })
                    {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = "Mostrar contraseña",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }
            )

            // Enlace para recuperar contraseña
            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Text(
                    "Recuperar contraseña",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón de inicio de sesión
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Iniciar sesión", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divisor
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = "o continuar con",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de inicio de sesión con Google
            OutlinedButton(
                onClick = onGoogleSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.android_light_rd_na),
                        contentDescription = "Google",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Continuar con Google",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // Enlace para registrarse
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        "¿No tienes cuenta? Regístrate",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun HandleSignInState(
    signInState: GoogleViewModel.SignInState?, // Estado del inicio de sesión con Google
    navController: NavController, // Controlador de navegación
    context: Context // Contexto de la aplicación
) {
    // Navega a la pantalla de noticias si el inicio de sesión es exitoso
    if (signInState is GoogleViewModel.SignInState.Success) {
        LaunchedEffect(key1 = signInState) {
            navController.navigateToNews()
        }
    }
}

// Valida si los campos del formulario están completos
private fun validateLoginForm(formFields: Map<String, String>): Boolean {
    return formFields["email"].isNullOrEmpty().not() && formFields["password"].isNullOrEmpty().not()
}

// Navega a la pantalla de noticias
private fun NavController.navigateToNews() {
    navigate("news") {
        popUpTo("login") { inclusive = true }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {

    // Previsualización de la pantalla de inicio de sesión
    LoginScreen(
        isDarkTheme = false,
        navController = NavController(LocalContext.current),
        navconThemeChange = {},
        userViewModel = UserViewModel(),
        googleViewModel = GoogleViewModel(),
        googleSignInLauncher = null,
        onGoogleSignInClick = {},
        ddbbViewModel = DDBBViewModel()
    )
}