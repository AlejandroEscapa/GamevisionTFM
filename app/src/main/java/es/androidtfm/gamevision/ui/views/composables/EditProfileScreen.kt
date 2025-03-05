package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 12/02/2025
 * Descripción: 
 */

@Composable
fun EditProfileScreen(
    isDarkTheme: Boolean,
    paddingValues: PaddingValues,
    navController: NavController?,
    userViewModel: UserViewModel,
    ddbbViewModel: DDBBViewModel
) {
    val loginFields by userViewModel.formFields.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(150.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                listOf(
                    "nameSurname" to "Nombre completo",
                    "username" to "Nombre de usuario",
                    "description" to "Descripción",
                    "country" to "País"
                ).forEach { (fieldKey, label) ->
                    ProfileField(
                        fieldKey = fieldKey,
                        value = loginFields[fieldKey].orEmpty(),
                        userViewModel = userViewModel
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        val email = loginFields["email"].orEmpty()
                        val updatedFields = hashMapOf(
                            "nameSurname" to loginFields["nameSurname"],
                            "username" to loginFields["username"],
                            "description" to loginFields["description"],
                            "country" to loginFields["country"]
                        )

                        coroutineScope.launch {
                            ddbbViewModel.updateUser(email, updatedFields)
                            navController?.navigate("profile")
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
                    Text("Guardar cambios", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun ProfileField(
    fieldKey: String,
    value: String,
    userViewModel: UserViewModel
) {
    val labelText = when (fieldKey) {
        "nameSurname" -> "Introduce tu nombre completo"
        "username" -> "Introduce tu nombre de usuario"
        "description" -> "Añade una descripción"
        "country" -> "Indica tu país"
        else -> ""
    }

    OutlinedTextField(
        value = value,
        onValueChange = { userViewModel.onFormFieldChange(fieldKey, it) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        label = {
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = when (fieldKey) {
                    "nameSurname" -> Icons.Outlined.AccountCircle
                    "username" -> Icons.Outlined.Person
                    "description" -> Icons.Outlined.Info
                    "country" -> Icons.Outlined.LocationOn
                    else -> Icons.Outlined.Edit
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    EditProfileScreen(
        isDarkTheme = false,
        paddingValues = PaddingValues(),
        navController = null,
        userViewModel = UserViewModel(),
        ddbbViewModel = DDBBViewModel()
    )
}