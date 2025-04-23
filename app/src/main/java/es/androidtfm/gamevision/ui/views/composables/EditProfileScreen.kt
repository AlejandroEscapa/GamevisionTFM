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

/**
 * Pantalla de edición de perfil.
 *
 * Esta función muestra la interfaz para editar el perfil del usuario,
 * incluyendo los campos de texto y un botón para guardar los cambios.
 *
 * @param isDarkTheme: Indica si el tema es oscuro.
 * @param paddingValues: Valores de relleno para el diseño.
 * @param navController: Controlador de navegación.
 * @param userViewModel: ViewModel para el usuario.
 * @param ddbbViewModel: ViewModel para la base de datos.
 */

@Composable
fun EditProfileScreen(
    isDarkTheme: Boolean,
    paddingValues: PaddingValues,
    navController: NavController?,
    userViewModel: UserViewModel,
    ddbbViewModel: DDBBViewModel
) {
    // Se observa el estado de los campos del formulario en el ViewModel del usuario.
    val loginFields by userViewModel.formFields.collectAsState()
    // Se crea un scope para lanzar corrutinas.
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(150.dp))
        // Se utiliza un componente personalizado que agrupa los campos del perfil.
        ProfileCard(loginFields, userViewModel) { updatedFields ->
            // Al hacer clic en guardar, se lanza una corrutina para actualizar la información del usuario.
            coroutineScope.launch {
                // Actualizar la información en la base de datos
                ddbbViewModel.updateUser(loginFields["email"].orEmpty(), updatedFields)
                // Antes de navegar hacia atrás, se marca el flag de actualización en la pantalla anterior.
                navController?.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("profileUpdate", true)
                // Se navega de vuelta a la pantalla de perfil.
                navController?.popBackStack()
            }
        }
    }
}

/**
 * Tarjeta de perfil.
 *
 * Este componente encapsula la interfaz que contiene los campos de edición del perfil
 * y el botón para guardar los cambios.
 */
@Composable
fun ProfileCard(
    loginFields: Map<String, String>,
    userViewModel: UserViewModel,
    onSaveClick: (HashMap<String, String?>) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Se itera sobre los campos del perfil para generar cada campo de texto.
            listOf(
                "nameSurname" to "Nombre completo",
                "username" to "Nombre de usuario",
                "description" to "Descripción",
                "country" to "País"
            ).forEach { (fieldKey, _) ->
                ProfileField(
                    fieldKey = fieldKey,
                    value = loginFields[fieldKey].orEmpty(),
                    userViewModel = userViewModel
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            // Botón para guardar los cambios realizados en el perfil.
            SaveButton {
                onSaveClick(
                    hashMapOf(
                        "nameSurname" to loginFields["nameSurname"],
                        "username" to loginFields["username"],
                        "description" to loginFields["description"],
                        "country" to loginFields["country"]
                    )
                )
            }
        }
    }
}

/**
 * Campo de texto para editar el perfil.
 *
 * Muestra un campo de texto personalizado con una etiqueta y un ícono
 * dependiendo del campo que se esté editando.
 */
@Composable
fun ProfileField(
    fieldKey: String,
    value: String,
    userViewModel: UserViewModel
) {
    // Mapa que relaciona cada campo con su etiqueta y su ícono correspondiente.
    val fieldData = mapOf(
        "nameSurname" to ("Introduce tu nombre completo" to Icons.Outlined.AccountCircle),
        "username" to ("Introduce tu nombre de usuario" to Icons.Outlined.Person),
        "description" to ("Añade una descripción" to Icons.Outlined.Info),
        "country" to ("Indica tu país" to Icons.Outlined.LocationOn)
    )

    // Se obtienen la etiqueta y el ícono según el campo; se usa un valor por defecto si no se encuentra.
    val (labelText, icon) = fieldData[fieldKey] ?: ("" to Icons.Outlined.Edit)

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
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }
    )
}

/**
 * Botón de guardado.
 *
 * Componente de botón que muestra la acción para guardar los cambios del perfil.
 */
@Composable
fun SaveButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
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

/**
 * Vista previa de la pantalla de edición de perfil.
 */
@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    // Nota: En esta vista previa se pasan instancias dummy de los ViewModels.
    EditProfileScreen(
        isDarkTheme = false,
        paddingValues = PaddingValues(),
        navController = null,
        userViewModel = UserViewModel(),
        ddbbViewModel = DDBBViewModel()
    )
}