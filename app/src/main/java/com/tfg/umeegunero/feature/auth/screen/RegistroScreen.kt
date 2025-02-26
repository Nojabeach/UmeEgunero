package com.tfg.umeegunero.feature.auth.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Contacto
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.RegistroUsuarioForm
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.feature.auth.viewmodel.RegistroUiState
import com.tfg.umeegunero.feature.auth.viewmodel.RegistroViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch

// Extensión para verificar si el tema es claro
fun ColorScheme.isLight(): Boolean {
    // En Material 3, podemos usar esta aproximación para detectar si estamos en tema claro
    val backgroundColor = this.background
    // Calculamos un valor aproximado de luminosidad (0.0 - 1.0)
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return luminance > 0.5
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onRegistroCompletado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Detector de éxito en el registro
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onRegistroCompletado()
        }
    }

    // Mostrar errores en Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(message = it)
                viewModel.clearError()
            }
        }
    }

    // Determinar si estamos en modo claro u oscuro
    val isLight = MaterialTheme.colorScheme.isLight()

    // Crear un gradiente elegante para el fondo, estilo iOS
    val gradientColors = if (!isLight) {
        // Gradiente para modo oscuro
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        )
    } else {
        // Gradiente para modo claro, estilo iOS
        listOf(
            Color(0xFFF0F4FF), // Azul muy claro
            Color(0xFFF8F9FF), // Casi blanco con tinte azul
            Color(0xFFF0FAFF)  // Azul muy claro con tinte cyan
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro Familiar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicador de progreso
                LinearProgressIndicator(
                    progress = { uiState.currentStep.toFloat() / uiState.totalSteps.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Título del paso actual
                Text(
                    text = when (uiState.currentStep) {
                        1 -> "Datos Personales"
                        2 -> "Dirección"
                        3 -> "Alumnos y Centro"
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Contenido del paso actual
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = uiState.currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300)) togetherWith
                                        slideOutHorizontally(
                                            targetOffsetX = { -it },
                                            animationSpec = tween(300)
                                        ) + fadeOut(animationSpec = tween(300))
                            } else {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300)) togetherWith
                                        slideOutHorizontally(
                                            targetOffsetX = { it },
                                            animationSpec = tween(300)
                                        ) + fadeOut(animationSpec = tween(300))
                            }
                        },
                        label = "Step Animation"
                    ) { step ->
                        when (step) {
                            1 -> DatosPersonalesStep(
                                dni = uiState.form.dni,
                                email = uiState.form.email,
                                password = uiState.form.password,
                                confirmPassword = uiState.form.confirmPassword,
                                nombre = uiState.form.nombre,
                                apellidos = uiState.form.apellidos,
                                telefono = uiState.form.telefono,
                                subtipo = uiState.form.subtipo,
                                onDniChange = { viewModel.updateFormField("dni", it) },
                                onEmailChange = { viewModel.updateFormField("email", it) },
                                onPasswordChange = { viewModel.updateFormField("password", it) },
                                onConfirmPasswordChange = { viewModel.updateFormField("confirmPassword", it) },
                                onNombreChange = { viewModel.updateFormField("nombre", it) },
                                onApellidosChange = { viewModel.updateFormField("apellidos", it) },
                                onTelefonoChange = { viewModel.updateFormField("telefono", it) },
                                onSubtipoChange = { viewModel.updateSubtipoFamiliar(it) },
                                errors = uiState.formErrors
                            )
                            2 -> DireccionStep(
                                calle = uiState.form.direccion.calle,
                                numero = uiState.form.direccion.numero,
                                piso = uiState.form.direccion.piso,
                                codigoPostal = uiState.form.direccion.codigoPostal,
                                ciudad = uiState.form.direccion.ciudad,
                                provincia = uiState.form.direccion.provincia,
                                onCalleChange = { viewModel.updateFormField("calle", it) },
                                onNumeroChange = { viewModel.updateFormField("numero", it) },
                                onPisoChange = { viewModel.updateFormField("piso", it) },
                                onCodigoPostalChange = { viewModel.updateFormField("codigoPostal", it) },
                                onCiudadChange = { viewModel.updateFormField("ciudad", it) },
                                onProvinciaChange = { viewModel.updateFormField("provincia", it) },
                                errors = uiState.formErrors
                            )
                            3 -> AlumnosCentroStep(
                                alumnos = uiState.form.alumnosDni,
                                centroId = uiState.form.centroId,
                                centros = uiState.centros,
                                isLoadingCentros = uiState.isLoadingCentros,
                                onAddAlumno = { viewModel.addAlumnoDni(it) },
                                onRemoveAlumno = { viewModel.removeAlumnoDni(it) },
                                onCentroSelect = { viewModel.updateFormField("centroId", it) },
                                errors = uiState.formErrors
                            )
                            else -> Box {}
                        }
                    }
                }

                // Botones de navegación
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (uiState.currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Anterior")
                        }

                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    }

                    if (uiState.currentStep < uiState.totalSteps) {
                        Button(
                            onClick = { viewModel.nextStep() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Siguiente")
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.submitRegistration() },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .padding(end = 8.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Text("Registrarse")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DatosPersonalesStep(
    dni: String,
    email: String,
    password: String,
    confirmPassword: String,
    nombre: String,
    apellidos: String,
    telefono: String,
    subtipo: SubtipoFamiliar,
    onDniChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onSubtipoChange: (SubtipoFamiliar) -> Unit,
    errors: Map<String, String>
) {
    val scrollState = rememberScrollState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // DNI
                OutlinedTextField(
                    value = dni,
                    onValueChange = onDniChange,
                    label = { Text("DNI") },
                    leadingIcon = {
                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("dni"),
                    supportingText = {
                        if (errors.containsKey("dni")) {
                            Text(errors["dni"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("email"),
                    supportingText = {
                        if (errors.containsKey("email")) {
                            Text(errors["email"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Close else Icons.Default.Check,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("password"),
                    supportingText = {
                        if (errors.containsKey("password")) {
                            Text(errors["password"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Confirmar Contraseña
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text("Confirmar Contraseña") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Close else Icons.Default.Check,
                                contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("confirmPassword"),
                    supportingText = {
                        if (errors.containsKey("confirmPassword")) {
                            Text(errors["confirmPassword"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Datos personales
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("nombre"),
                    supportingText = {
                        if (errors.containsKey("nombre")) {
                            Text(errors["nombre"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Apellidos
                OutlinedTextField(
                    value = apellidos,
                    onValueChange = onApellidosChange,
                    label = { Text("Apellidos") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("apellidos"),
                    supportingText = {
                        if (errors.containsKey("apellidos")) {
                            Text(errors["apellidos"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Teléfono
                OutlinedTextField(
                    value = telefono,
                    onValueChange = onTelefonoChange,
                    label = { Text("Teléfono") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    isError = errors.containsKey("telefono"),
                    supportingText = {
                        if (errors.containsKey("telefono")) {
                            Text(errors["telefono"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Tipo de familiar
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "¿Qué relación tienes con el alumno?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TipoFamiliarOptions(
                    selectedType = subtipo,
                    onTypeSelected = onSubtipoChange
                )
            }
        }
    }
}

@Composable
fun TipoFamiliarOptions(
    selectedType: SubtipoFamiliar,
    onTypeSelected: (SubtipoFamiliar) -> Unit
) {
    Column {
        SubtipoFamiliar.values().forEach { tipo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedType == tipo,
                    onClick = { onTypeSelected(tipo) }
                )

                Text(
                    text = when(tipo) {
                        SubtipoFamiliar.PADRE -> "Padre"
                        SubtipoFamiliar.MADRE -> "Madre"
                        SubtipoFamiliar.TUTOR -> "Tutor/a legal"
                        SubtipoFamiliar.OTRO -> "Otro familiar"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
// Componente auxiliar para las previews
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreenPreviewContent(
    uiState: RegistroUiState,
    onNavigateBack: () -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onSubmit: () -> Unit,
    onDniChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onSubtipoChange: (SubtipoFamiliar) -> Unit,
    onCalleChange: (String) -> Unit,
    onNumeroChange: (String) -> Unit,
    onPisoChange: (String) -> Unit,
    onCodigoPostalChange: (String) -> Unit,
    onCiudadChange: (String) -> Unit,
    onProvinciaChange: (String) -> Unit,
    onAddAlumno: (String) -> Unit,
    onRemoveAlumno: (String) -> Unit,
    onCentroSelect: (String) -> Unit
) {
    // Determinar si estamos en modo claro u oscuro
    val isLight = MaterialTheme.colorScheme.isLight()

    // Crear un gradiente elegante para el fondo, estilo iOS
    val gradientColors = if (!isLight) {
        // Gradiente para modo oscuro
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        )
    } else {
        // Gradiente para modo claro, estilo iOS
        listOf(
            Color(0xFFF0F4FF), // Azul muy claro
            Color(0xFFF8F9FF), // Casi blanco con tinte azul
            Color(0xFFF0FAFF)  // Azul muy claro con tinte cyan
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro Familiar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicador de progreso
                LinearProgressIndicator(
                    progress = { uiState.currentStep.toFloat() / uiState.totalSteps.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Título del paso actual
                Text(
                    text = when (uiState.currentStep) {
                        1 -> "Datos Personales"
                        2 -> "Dirección"
                        3 -> "Alumnos y Centro"
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Contenido del paso actual
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (uiState.currentStep) {
                        1 -> DatosPersonalesStep(
                            dni = uiState.form.dni,
                            email = uiState.form.email,
                            password = uiState.form.password,
                            confirmPassword = uiState.form.confirmPassword,
                            nombre = uiState.form.nombre,
                            apellidos = uiState.form.apellidos,
                            telefono = uiState.form.telefono,
                            subtipo = uiState.form.subtipo,
                            onDniChange = onDniChange,
                            onEmailChange = onEmailChange,
                            onPasswordChange = onPasswordChange,
                            onConfirmPasswordChange = onConfirmPasswordChange,
                            onNombreChange = onNombreChange,
                            onApellidosChange = onApellidosChange,
                            onTelefonoChange = onTelefonoChange,
                            onSubtipoChange = onSubtipoChange,
                            errors = uiState.formErrors
                        )
                        2 -> DireccionStep(
                            calle = uiState.form.direccion.calle,
                            numero = uiState.form.direccion.numero,
                            piso = uiState.form.direccion.piso,
                            codigoPostal = uiState.form.direccion.codigoPostal,
                            ciudad = uiState.form.direccion.ciudad,
                            provincia = uiState.form.direccion.provincia,
                            onCalleChange = onCalleChange,
                            onNumeroChange = onNumeroChange,
                            onPisoChange = onPisoChange,
                            onCodigoPostalChange = onCodigoPostalChange,
                            onCiudadChange = onCiudadChange,
                            onProvinciaChange = onProvinciaChange,
                            errors = uiState.formErrors
                        )
                        3 -> AlumnosCentroStep(
                            alumnos = uiState.form.alumnosDni,
                            centroId = uiState.form.centroId,
                            centros = uiState.centros,
                            isLoadingCentros = uiState.isLoadingCentros,
                            onAddAlumno = onAddAlumno,
                            onRemoveAlumno = onRemoveAlumno,
                            onCentroSelect = onCentroSelect,
                            errors = uiState.formErrors
                        )
                        else -> Box {}
                    }
                }

                // Botones de navegación
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (uiState.currentStep > 1) {
                        OutlinedButton(
                            onClick = onPreviousStep,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Anterior")
                        }

                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    }

                    if (uiState.currentStep < uiState.totalSteps) {
                        Button(
                            onClick = onNextStep,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Siguiente")
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onSubmit,
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .padding(end = 8.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Text("Registrarse")
                        }
                    }
                }
            }
        }
    }
}

// Vista previa para la pantalla completa de registro
@Preview(showBackground = true)
@Composable
fun RegistroScreenPreview() {
    UmeEguneroTheme {
        Surface {
            // Creamos un estado de UI simulado para la vista previa
            val mockUiState = RegistroUiState(
                currentStep = 1,
                totalSteps = 3,
                form = RegistroUsuarioForm(
                    dni = "12345678A",
                    email = "ejemplo@correo.com",
                    password = "contraseña",
                    confirmPassword = "contraseña",
                    nombre = "Juan",
                    apellidos = "Pérez García",
                    telefono = "600123456",
                    subtipo = SubtipoFamiliar.PADRE
                ),
                centros = listOf(
                    Centro(
                        id = "centro1",
                        nombre = "Colegio Ejemplo 1",
                        direccion = Direccion(
                            calle = "Calle Principal",
                            numero = "1",
                            codigoPostal = "28001",
                            ciudad = "Madrid",
                            provincia = "Madrid"
                        ),
                        contacto = Contacto(
                            telefono = "912345678",
                            email = "colegio@ejemplo.com"
                        )
                    ),
                    Centro(
                        id = "centro2",
                        nombre = "Colegio Ejemplo 2",
                        direccion = Direccion(
                            calle = "Avenida Principal",
                            numero = "10",
                            codigoPostal = "28002",
                            ciudad = "Madrid",
                            provincia = "Madrid"
                        ),
                        contacto = Contacto(
                            telefono = "912345679",
                            email = "colegio2@ejemplo.com"
                        )
                    )
                )
            )

            // Vista simulada de la pantalla de registro
            RegistroScreenPreviewContent(
                uiState = mockUiState,
                onNavigateBack = {},
                onNextStep = {},
                onPreviousStep = {},
                onSubmit = {},
                onDniChange = {},
                onEmailChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onNombreChange = {},
                onApellidosChange = {},
                onTelefonoChange = {},
                onSubtipoChange = {},
                onCalleChange = {},
                onNumeroChange = {},
                onPisoChange = {},
                onCodigoPostalChange = {},
                onCiudadChange = {},
                onProvinciaChange = {},
                onAddAlumno = {},
                onRemoveAlumno = {},
                onCentroSelect = {}
            )
        }
    }
}

// Vista previa en modo oscuro
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RegistroScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        Surface {
            // Creamos un estado de UI simulado para la vista previa
            val mockUiState = RegistroUiState(
                currentStep = 1,
                totalSteps = 3,
                form = RegistroUsuarioForm(
                    dni = "12345678A",
                    email = "ejemplo@correo.com",
                    password = "contraseña",
                    confirmPassword = "contraseña",
                    nombre = "Juan",
                    apellidos = "Pérez García",
                    telefono = "600123456",
                    subtipo = SubtipoFamiliar.PADRE
                ),
                centros = listOf(
                    Centro(
                        id = "centro1",
                        nombre = "Colegio Ejemplo 1"
                    ),
                    Centro(
                        id = "centro2",
                        nombre = "Colegio Ejemplo 2"
                    )
                )
            )

            // Vista simulada de la pantalla de registro
            RegistroScreenPreviewContent(
                uiState = mockUiState,
                onNavigateBack = {},
                onNextStep = {},
                onPreviousStep = {},
                onSubmit = {},
                onDniChange = {},
                onEmailChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onNombreChange = {},
                onApellidosChange = {},
                onTelefonoChange = {},
                onSubtipoChange = {},
                onCalleChange = {},
                onNumeroChange = {},
                onPisoChange = {},
                onCodigoPostalChange = {},
                onCiudadChange = {},
                onProvinciaChange = {},
                onAddAlumno = {},
                onRemoveAlumno = {},
                onCentroSelect = {}
            )
        }
    }
}