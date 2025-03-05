package es.androidtfm.gamevision.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import es.androidtfm.gamevision.ui.views.composables.ProfileInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 22/01/2025
 * Descripción:
 */

class UserViewModel : ViewModel() {

    // Estado del formulario
    private val _formFields = MutableStateFlow(
        mutableMapOf(
            "username" to "",
            "password" to "",
            "confirmPassword" to "",
            "country" to "",
            "email" to "",
            "nameSurname" to "",
            "description" to ""
        )
    )
    val formFields: StateFlow<Map<String, String>> = _formFields.asStateFlow()

    // Estado del perfil
    private val _profileInfo = MutableStateFlow(ProfileInfo("", "", "", "", ""))
    val profileInfo: StateFlow<ProfileInfo> = _profileInfo.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado para mensajes
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    // Estado para modo invitado
    private val _isGuest = MutableStateFlow(false)
    val isGuest: StateFlow<Boolean> = _isGuest.asStateFlow()

    init {
        // Combinar datos de DDBBViewModel y formFields
        viewModelScope.launch {
            combine(
                _formFields,
                _profileInfo
            ) { formFields, profileInfo ->
                extractProfileInfo(formFields, profileInfo)
            }.collect { combinedInfo ->
                _profileInfo.value = combinedInfo
            }
        }

        // Actualizar datos desde Firebase Auth
        viewModelScope.launch {
            FirebaseAuth.getInstance().currentUser?.let { user ->
                updateProfileInfoFromFirebase(user)
            }
        }
    }

    // Obtener datos del usuario
    fun fetchUserData(email: String) {
        _isLoading.value = true
        viewModelScope.launch {
            // Aquí llamarías a DDBBViewModel.fetchUserData(email) si es necesario
            _isLoading.value = false
        }
    }

    // Extraer información del perfil
    private fun extractProfileInfo(
        formFields: Map<String, String>,
        profileInfo: ProfileInfo
    ): ProfileInfo {
        return ProfileInfo(
            nameSurname = formFields["nameSurname"] ?: profileInfo.nameSurname,
            username = formFields["username"] ?: profileInfo.username,
            description = formFields["description"] ?: profileInfo.description,
            country = formFields["country"] ?: profileInfo.country,
            email = formFields["email"] ?: profileInfo.email
        )
    }

    // Actualizar datos desde Firebase
    private fun updateProfileInfoFromFirebase(firebaseUser: FirebaseUser) {
        val googleName = firebaseUser.displayName ?: "Nombre"
        val googleEmail = firebaseUser.email ?: "Email"

        updateFormField("username", googleName.replace("\\s".toRegex(), ""))
        updateFormField("nameSurname", googleName)
        updateFormField("email", googleEmail)
    }

    // Métodos existentes (sin cambios)
    fun updateFormField(key: String, value: String) {
        _formFields.value = _formFields.value.toMutableMap().apply {
            this[key] = value
        }
    }

    fun onFormFieldChange(field: String, value: String) = updateFormField(field, value)

    fun loginUser(): Boolean {
        return if (validateFields(listOf("email", "password"))) true
        else {
            setMessage("Por favor, completa todos los campos")
            false
        }
    }

    fun registerUser(formFields: Map<String, String>): Boolean {
        val fields = _formFields.value
        if (!validateFields(
                listOf(
                    "username",
                    "nameSurname",
                    "email",
                    "password",
                    "confirmPassword"
                )
            )
        ) {
            setMessage("Por favor, completa todos los campos")
            return false
        }

        if (fields["password"] != fields["confirmPassword"]) {
            setMessage("Las contraseñas no coinciden")
            return false
        }
        return true
    }

    suspend fun forgotPassword(): Boolean {
        val email = _formFields.value["email"].orEmpty()
        if (email.isEmpty()) {
            setMessage("Por favor, ingresa tu correo electrónico")
            return false
        }

        // Aquí llamarías a DDBBViewModel.checkEmailExists(email) si es necesario
        return true
    }

    fun clearFormFields() {
        _formFields.value = mutableMapOf()
    }

    private fun validateFields(fields: List<String>): Boolean {
        return fields.all { _formFields.value[it].orEmpty().isNotEmpty() }
    }

    fun clearMessage() = setMessage("")

    fun setMessage(msg: String) {
        _message.value = msg
    }

    fun clearUserData() {
        _formFields.value = mutableMapOf()
        _message.value = ""
    }

    fun setGuestStatus(isGuest: Boolean) {
        _isGuest.value = isGuest
    }
}
