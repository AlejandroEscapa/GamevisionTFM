package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import es.androidtfm.gamevision.R
import kotlinx.coroutines.delay

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 17/01/2025
 * Descripción: 
 */

/**
 * Pantalla de inicio de la aplicación.
 *
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param onThemeChange Función para cambiar el tema.
 * @param navController Controlador de navegación.
 * @param isGuest Indica si el usuario es invitado.
 * @param onGuestStatusChange Función para cambiar el estado de invitado.
 */

@Composable
fun HomeScreen(
    isDarkTheme: Boolean?,
    onThemeChange: (Boolean) -> Unit,
    navController: NavHostController,
    isGuest: Boolean,
    onGuestStatusChange: (Boolean) -> Unit
) {
    // Estado para controlar si el tema ya ha sido cargado
    var isThemeLoaded by remember { mutableStateOf(false) }

    // Efecto para actualizar el estado cuando el tema esté listo
    LaunchedEffect(Unit) {
        delay(1000) // Simula un retraso de carga
        isThemeLoaded = true
    }

    // Muestra la pantalla de carga hasta que el tema esté listo
    if (!isThemeLoaded) {
        LoadingScreen()
    } else {
        // Asegúrate de que isDarkTheme no sea null
        val theme = isDarkTheme!!

        // Diseño principal de la pantalla de inicio
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Botón para cambiar el tema
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.TopCenter).padding(vertical = 15.dp)
            ) {
                IconButton(
                    onClick = { onThemeChange(!theme) }
                ) {
                    Image(
                        painter = painterResource(
                            id = if (theme) R.drawable.daynightthemewhite
                            else R.drawable.daynightthemeblack
                        ),
                        contentDescription = "Cambiar Tema"
                    )
                }
                Text(
                    text = if (theme) "Modo día" else "Modo noche",
                    fontSize = 12.sp
                )
            }

            // Contenido de la pantalla de inicio
            Column {
                Spacer(modifier = Modifier.height(120.dp))

                // Logo de la aplicación
                Image(
                    painter = painterResource(
                        id = if (theme) R.drawable.gamevisionnight
                        else R.drawable.gamevision2
                    ),
                    contentDescription = "Logo"
                )

                // Botón para iniciar sesión
                Button(
                    onClick = {
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = "Iniciar sesión")
                }

                // Botón para continuar como invitado
                Button(
                    onClick = {
                        onGuestStatusChange(true)
                        navController.navigate("news")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = "Continuar como invitado")
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {

    // Pantalla de carga con un indicador circular
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Previsualización de la pantalla de inicio
    HomeScreen(
        isDarkTheme = false, // Simula un tema cargado
        onThemeChange = { /* No necesitas implementación en la vista previa */ },
        navController = navController,
        isGuest = false,
        onGuestStatusChange = { /* No necesitas implementación en la vista previa */ }
    )
}