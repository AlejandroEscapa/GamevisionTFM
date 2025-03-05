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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import es.androidtfm.gamevision.R
import kotlinx.coroutines.delay

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 17/01/2025
 * Descripción: 
 */

@Composable
fun HomeScreen(
    isDarkTheme: Boolean?, // Puede ser null mientras se carga
    onThemeChange: (Boolean) -> Unit,
    navController: NavHostController,
    isGuest: Boolean,
    onGuestStatusChange: (Boolean) -> Unit
) {
    // Estado para controlar si el tema ya ha sido cargado
    var isThemeLoaded by remember { mutableStateOf(false) }

    // Efecto para actualizar el estado cuando el tema esté listo
    LaunchedEffect(Unit) {
        delay(1000)
        isThemeLoaded = true
    }

    // Muestra la pantalla de carga hasta que el tema esté listo
    if (!isThemeLoaded) {
        LoadingScreen()
    } else {
        // Asegúrate de que isDarkTheme no sea null
        val theme = isDarkTheme!!

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { onThemeChange(!theme) }, // Usa el valor no nulo
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Image(
                    painter = painterResource(
                        id = if (theme) R.drawable.daynightthemewhite
                        else R.drawable.daynightthemeblack
                    ),
                    contentDescription = "Cambiar Tema"
                )
            }

            Column {
                Spacer(modifier = Modifier.height(120.dp))

                Image(
                    painter = painterResource(
                        id = if (theme) R.drawable.gamevisionnight
                        else R.drawable.gamevision2
                    ),
                    contentDescription = "Logo"
                )

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

    HomeScreen(
        isDarkTheme = false, // Simula un tema cargado
        onThemeChange = { /* No necesitas implementación en la vista previa */ },
        navController = navController,
        isGuest = false,
        onGuestStatusChange = { /* No necesitas implementación en la vista previa */ }
    )
}
