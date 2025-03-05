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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import es.androidtfm.gamevision.R
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.GoogleViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 17/01/2025
 * Descripción: 
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

    val formFields by userViewModel.formFields.collectAsState()
    val message by userViewModel.message.collectAsState()
    val signInState by googleViewModel.signInState.observeAsState()

    HandleSignInState(signInState, navController, context)

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
            LoginHeader(isDarkTheme)

            // Eliminar el Spacer o ajustar el padding aquí
            Spacer(modifier = Modifier.height(0.dp)) // Ajustado a 0.dp

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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), // Ajustado a 0.dp
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

        Spacer(modifier = Modifier.height(16.dp)) // Mantener este espacio si es necesario
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(8.dp)) // Mantener este espacio si es necesario
        Text(
            text = "Inicia sesión para continuar",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun LoginForm(
    formFields: Map<String, String>,
    message: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
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
            OutlinedTextField(
                value = formFields["email"] ?: "",
                onValueChange = onEmailChange,
                label = {
                    Text(text = "Introduce tu correo: ")
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

            OutlinedTextField(
                value = formFields["password"] ?: "",
                onValueChange = onPasswordChange,
                label = {
                    Text(text = "Introduce tu contraseña: ")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            )

            // Forgot Password
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

            // Login Button
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

            // Divider
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

            // Google Button
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

            // Register Link
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
    signInState: GoogleViewModel.SignInState?,
    navController: NavController,
    context: Context
) {
    if (signInState is GoogleViewModel.SignInState.Success) {
        LaunchedEffect(key1 = signInState) {
            navController.navigateToNews()
        }
    }
}

private fun validateLoginForm(formFields: Map<String, String>): Boolean {
    return formFields["email"].isNullOrEmpty().not() && formFields["password"].isNullOrEmpty().not()
}

private fun NavController.navigateToNews() {
    navigate("news") {
        popUpTo("login") { inclusive = true }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
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