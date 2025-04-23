package es.androidtfm.gamevision.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

/**
 * ViewModel para gestionar los datos del usuario y el formulario de registro/inicio de sesión.
 *
 * Se encarga de manejar el estado del formulario, la información del perfil, la carga y los mensajes.
 */

/**
 * Modelo de datos del perfil.
 */
data class ProfileInfo(
    var nameSurname: String,
    var username: String,
    var description: String,
    var country: String,
    var email: String
)

class UserViewModel : ViewModel() {

    // Estado del formulario con los campos inicializados
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

    // Estado del perfil del usuario
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
        // Combinar los datos del formulario con el perfil actual
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

        // Actualizar datos desde Firebase Auth (actualiza username, nameSurname y email)
        viewModelScope.launch {
            FirebaseAuth.getInstance().currentUser?.let { user ->
                updateProfileInfoFromFirebase(user)
            }
        }
    }

    /**
     * Obtiene los datos del usuario desde Firestore mediante DDBBViewModel y actualiza el formulario.
     * @param email: Correo electrónico del usuario.
     * @param ddbbViewModel Instancia del ViewModel que maneja la base de datos.
     */
    fun fetchUserData(email: String, ddbbViewModel: DDBBViewModel) {
        _isLoading.value = true
        viewModelScope.launch {
            // Se obtiene la información real del usuario desde Firestore
            ddbbViewModel.fetchUserData(email)
            ddbbViewModel.userData.value?.let { userData ->
                updateFormField("nameSurname", userData["nameSurname"].orEmpty())
                updateFormField("username", userData["username"].orEmpty())
                updateFormField("description", userData["description"].orEmpty())
                updateFormField("country", userData["country"].orEmpty())
                updateFormField("email", email)
            } ?: Log.e("UserViewModel", "No se pudieron obtener datos para el email: $email")
            _isLoading.value = false
        }
    }

    /**
     * Extrae la información del perfil combinando los campos del formulario y la información existente.
     */
    private fun extractProfileInfo(
        formFields: Map<String, String>,
        profileInfo: ProfileInfo
    ): ProfileInfo {
        Log.d("UserViewModel", "formFields: $formFields | profileInfo previo: $profileInfo")
        return ProfileInfo(
            nameSurname = if (formFields["nameSurname"].isNullOrBlank()) profileInfo.nameSurname else formFields["nameSurname"]!!,
            username = if (formFields["username"].isNullOrBlank()) profileInfo.username else formFields["username"]!!,
            description = if (formFields["description"].isNullOrBlank()) profileInfo.description else formFields["description"]!!,
            country = if (formFields["country"].isNullOrBlank()) profileInfo.country else formFields["country"]!!,
            email = if (formFields["email"].isNullOrBlank()) profileInfo.email else formFields["email"]!!
        )
    }

    /**
     * Actualiza la información del perfil desde Firebase Auth.
     */
    private fun updateProfileInfoFromFirebase(firebaseUser: FirebaseUser) {
        val googleName = firebaseUser.displayName ?: "Nombre"
        val googleEmail = firebaseUser.email ?: "Email"
        updateFormField("username", googleName.replace("\\s".toRegex(), ""))
        updateFormField("nameSurname", googleName)
        updateFormField("email", googleEmail)
    }

    /**
     * Actualiza un campo específico del formulario.
     */
    fun updateFormField(key: String, value: String) {
        _formFields.value = _formFields.value.toMutableMap().apply {
            this[key] = value
        }
    }

    /**
     * Maneja el cambio en los campos del formulario.
     */
    fun onFormFieldChange(field: String, value: String) = updateFormField(field, value)

    /**
     * Valida los campos necesarios para el inicio de sesión.
     */
    fun loginUser(): Boolean {
        return if (validateFields(listOf("email", "password"))) true
        else {
            setMessage("Por favor, completa todos los campos")
            false
        }
    }

    /**
     * Valida los campos necesarios para el registro de un nuevo usuario.
     */
    fun registerUser(formFields: Map<String, String>): Boolean {
        val fields = _formFields.value
        if (!validateFields(listOf("username", "nameSurname", "email", "password", "confirmPassword"))) {
            setMessage("Por favor, completa todos los campos")
            return false
        }
        if (fields["password"] != fields["confirmPassword"]) {
            setMessage("Las contraseñas no coinciden")
            return false
        }
        return true
    }

    /**
     * Maneja la solicitud de recuperación de contraseña.
     */
    suspend fun forgotPassword(): Boolean {
        val email = _formFields.value["email"].orEmpty()
        if (email.isEmpty()) {
            setMessage("Por favor, ingresa tu correo electrónico")
            return false
        }
        return true
    }

    /**
     * Limpia todos los campos del formulario.
     */
    fun clearFormFields() {
        _formFields.value = mutableMapOf(
            "username" to "",
            "password" to "",
            "confirmPassword" to "",
            "country" to "",
            "email" to "",
            "nameSurname" to "",
            "description" to ""
        )
    }

    /**
     * Valida si los campos especificados están completos.
     */
    private fun validateFields(fields: List<String>): Boolean {
        return fields.all { _formFields.value[it].orEmpty().isNotEmpty() }
    }

    /**
     * Limpia el mensaje actual.
     */
    fun clearMessage() = setMessage("")

    /**
     * Establece un mensaje en el estado.
     */
    fun setMessage(msg: String) {
        _message.value = msg
    }

    /**
     * Limpia los datos del usuario y los mensajes.
     */
    fun clearUserData() {
        clearFormFields()
        _message.value = ""
    }

    /**
     * Establece el estado de modo invitado.
     */
    fun setGuestStatus(isGuest: Boolean) {
        _isGuest.value = isGuest
    }
}