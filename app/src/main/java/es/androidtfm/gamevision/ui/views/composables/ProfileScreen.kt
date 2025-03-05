package es.androidtfm.gamevision.ui.views.composables

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import es.androidtfm.gamevision.R
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.GoogleViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.io.File

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 18/01/2025
 * Descripción:
 */

data class ProfileInfo(
    var nameSurname: String,
    var username: String,
    var description: String,
    var country: String,
    var email: String
)

@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    paddingValues: PaddingValues,
    navController: NavController?,
    userViewModel: UserViewModel,
    ddbbViewModel: DDBBViewModel,
    googleViewModel: GoogleViewModel,
    onThemeChange: (Boolean) -> Unit
) {
    val profileInfo by userViewModel.profileInfo.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(currentUser) {
        currentUser?.email?.let { userViewModel.fetchUserData(it) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        )
                ) {
                    IconButton(
                        onClick = { onThemeChange(!isDarkTheme) },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isDarkTheme) R.drawable.daynightthemewhite
                                else R.drawable.daynightthemeblack),
                            contentDescription = "Theme",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Profile Content
                if (isLoading) {
                    ProfileLoadingIndicator()
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Profile Image
                        Card(
                            shape = CircleShape,
                            elevation = CardDefaults.cardElevation(8.dp),
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = (-60).dp)
                        ) {
                            ProfileImage(profileInfo.email, ddbbViewModel)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp, 80.dp, 20.dp, 20.dp)
                        ) {
                            // Profile Info
                            ProfileHeaderSection(
                                name = profileInfo.nameSurname,
                                username = profileInfo.username,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Details Card
                            ProfileDetailsCard(
                                description = profileInfo.description,
                                country = profileInfo.country,
                                email = profileInfo.email
                            )

                            // Settings Section
                            ProfileActionsSection(
                                context = LocalContext.current,
                                navController = navController,
                                userViewModel = userViewModel,
                                ddbbViewModel = ddbbViewModel,
                                googleViewModel = googleViewModel,
                                modifier = Modifier.padding(top = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileImage(email: String, ddbbViewModel: DDBBViewModel) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Recuperar la imagen al cargar
    LaunchedEffect(email) {
        if (email.isNotEmpty()) {
            ddbbViewModel.recoverProfilePicture(
                email = email,
                onSuccess = { uri ->
                    Log.d("ProfileImage", "Recovered imageUri: $uri")
                    imageUri = uri
                },
                onError = { exception ->
                    Log.e("ProfileImage", "Error: ${exception.message}")
                }
            )
        }
    }

    // Guardar imagen localmente
    fun saveImageLocally(uri: Uri): String {
        val file = File(context.filesDir, "profile_image_${System.currentTimeMillis()}.jpg")
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Log.d("ImageStorage", "Image saved locally at: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("ImageStorage", "Error saving image locally: ${e.message}")
        }
        return file.absolutePath
    }

    // Lanzador para seleccionar una nueva imagen
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("ProfileImage", "User selected new imageUri: $it")
            val localPath = saveImageLocally(it)
            val localUri = Uri.fromFile(File(localPath))
            imageUri = localUri
            coroutineScope.launch {
                ddbbViewModel.updateUserProfilePicture(email, localUri)
            }
        }
    }

    // Mostrar la imagen o el ícono de edición
    Box(modifier = Modifier.fillMaxSize()) {
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { pickImageLauncher.launch("image/*") }
            )
        } ?: Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Edit Icon",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp)
                .clickable { pickImageLauncher.launch("image/*") }
        )
    }
}

@Composable
private fun ProfileHeaderSection(
    name: String,
    username: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "@$username",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileDetailsCard(description: String, country: String, email: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Biografía",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = description.ifEmpty { "No description provided" },
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(16.dp))

            ProfileDetailItem(
                icon = Icons.Default.LocationOn,
                title = "Location",
                value = country.ifEmpty { "Not specified" }
            )

            ProfileDetailItem(
                icon = Icons.Default.Email,
                title = "Contact",
                value = email
            )
        }
    }
}

@Composable
private fun ProfileDetailItem(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ProfileActionsSection(
    context: Context,
    navController: NavController?,
    userViewModel: UserViewModel,
    ddbbViewModel: DDBBViewModel,
    googleViewModel: GoogleViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {

        FilledTonalButton(
            onClick = { navController?.navigate("friendlist") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Seguidos")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Edit Profile Button
        FilledTonalButton(
            onClick = { navController?.navigate("editProfile") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Editar perfil")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Logout Button
        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    ddbbViewModel.logout()
                    FirebaseAuth.getInstance().signOut()
                    userViewModel.clearFormFields()
                    userViewModel.clearUserData()
                    googleViewModel.logout(
                        context = context,
                        onSuccess = {},
                        onError = {}
                    )
                    navController?.navigate("main") {
                        popUpTo(id = navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun ProfileLoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(top = 50.dp)
                .size(48.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        isDarkTheme = false,
        onThemeChange = {},
        paddingValues = PaddingValues(),
        navController = null,
        userViewModel = UserViewModel(),
        ddbbViewModel = DDBBViewModel(),
        googleViewModel = GoogleViewModel()
    )
}