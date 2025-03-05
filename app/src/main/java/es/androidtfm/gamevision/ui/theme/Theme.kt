package es.androidtfm.gamevision.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography // Asegúrate de importar este Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 15/01/2025
 * Descripción: 
 */

// Definir los colores para el tema claro
val LightColorPalette = lightColorScheme(
    primary = Color(0xFF404040), // gray_dark
    primaryContainer = Color(0xFF8C8C8C), // gray_medium
    secondary = Color(0xFFBFBFBF), // gray_silver
    background = Color(0xFFF2F2F2), // gray_light
    surface = Color(0xFFF2F2F2), // gray_light
    onPrimary = Color(0xFFFFFFFF), // white
    onSecondary = Color(0xFF000000), // black
    onBackground = Color(0xFF000000), // black
    onSurface = Color(0xFF000000) // black
)

// Definir los colores para el tema oscuro
val DarkColorPalette = darkColorScheme(
    primary = Color(0xFFBFBFBF), // gray_silver
    primaryContainer = Color(0xFF8C8C8C), // gray_medium
    secondary = Color(0xFF404040), // gray_dark
    background = Color(0xFF0D0D0D), // gray_charcoal
    surface = Color(0xFF0D0D0D), // gray_charcoal
    onPrimary = Color(0xFF000000), // black
    onSecondary = Color(0xFFFFFFFF), // white
    onBackground = Color(0xFFFFFFFF), // white
    onSurface = Color(0xFFFFFFFF) // white
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Elige el esquema de colores basado en el modo oscuro o claro
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    // Define tu propio Typography si es necesario, o usa el predeterminado
    val typography = Typography()

    // Aplica el tema con los colores seleccionados
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        shapes = Shapes(
            extraLarge = RoundedCornerShape(28.dp) // Añadir forma personalizada
        )
    ) {

    }
}