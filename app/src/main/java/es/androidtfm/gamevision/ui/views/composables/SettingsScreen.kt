package es.androidtfm.gamevision.ui.views.composables

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 11/02/2025
 * Descripción: 
 */

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    paddingValues: PaddingValues,
    navController: NavController?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row {
            Text(text = "Settings")
        }

        Row {
            Text(text = "Settings")
            Text(text = "Settings")


            // DARK MODE

            // NOTIFICACIONES

            //
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        isDarkTheme = false,
        paddingValues = PaddingValues(),
        navController = null
    )
}