# Guía de Componentes UI para UmeEgunero

Este documento describe los componentes UI reutilizables creados para facilitar el desarrollo de interfaces coherentes en la aplicación UmeEgunero.

## Componentes de Estado

### ContentStateHandler

Un componente que maneja los diferentes estados de contenido (carga, error, vacío) y muestra el componente adecuado.

```kotlin
ContentStateHandler(
    isLoading = viewModel.isLoading,
    isEmpty = viewModel.items.isEmpty(),
    error = viewModel.error,
    emptyMessage = "No hay elementos disponibles",
    onRetry = { viewModel.loadData() }
) {
    // Tu contenido aquí
    LazyColumn {
        items(viewModel.items) { item ->
            Text(item.name)
        }
    }
}
```

### ResultContent

Un componente específico para manejar los estados de `Result<T>` de la aplicación.

```kotlin
ResultContent(
    result = viewModel.dataResult,
    emptyMessage = "No hay datos disponibles",
    errorTitle = "Error de carga",
    onRetry = { viewModel.loadData() }
) { data ->
    // Tu contenido con los datos aquí
    Text("Datos cargados: ${data.size} elementos")
}
```

Para listas específicamente, puedes usar:

```kotlin
ResultContentList(
    result = viewModel.itemsResult,
    emptyMessage = "No hay elementos disponibles",
    onRetry = { viewModel.loadItems() }
) { items ->
    LazyColumn {
        items(items) { item ->
            Text(item.name)
        }
    }
}
```

## Componentes Individuales

### LoadingContent

Muestra un indicador de carga centrado en la pantalla.

```kotlin
LoadingContent()
```

### EmptyContent

Muestra un mensaje cuando no hay contenido.

```kotlin
EmptyContent(
    title = "Sin resultados",
    message = "No hay elementos que coincidan con tu búsqueda",
    actionText = "Limpiar filtros",
    onAction = { viewModel.clearFilters() }
)
```

### ErrorContent

Muestra un mensaje de error con opción de reintentar.

```kotlin
ErrorContent(
    title = "Error de conexión",
    message = "No se pudo conectar con el servidor",
    onRetry = { viewModel.retry() }
)
```

## Componentes de Feedback

### ConfirmationDialog

Diálogo para confirmar acciones importantes.

```kotlin
var showDialog by remember { mutableStateOf(false) }

if (showDialog) {
    ConfirmationDialog(
        title = "Eliminar elemento",
        message = "¿Estás seguro de que deseas eliminar este elemento? Esta acción no se puede deshacer.",
        confirmButtonText = "Eliminar",
        dismissButtonText = "Cancelar",
        isDestructive = true,
        onConfirm = {
            viewModel.deleteItem()
            showDialog = false
        },
        onDismiss = { showDialog = false }
    )
}
```

### StatusSnackbar

Snackbar mejorado con soporte para diferentes tipos de mensaje.

```kotlin
val (snackbarHostState, scope) = rememberSnackbarController()

// Para mostrar un snackbar
showSnackbar(
    hostState = snackbarHostState,
    message = "Elemento guardado correctamente",
    type = SnackbarType.SUCCESS,
    scope = scope
)

// Agregar el host del snackbar en tu Scaffold
Scaffold(
    snackbarHost = { StatusSnackbarHost(hostState = snackbarHostState) }
) {
    // Contenido del Scaffold
}
```

## Componentes de Entrada

### SearchBar

Barra de búsqueda personalizada con manejo automático de estado.

```kotlin
// Versión completa con control de estado externo
var searchQuery by remember { mutableStateOf("") }

SearchBar(
    query = searchQuery,
    onQueryChange = { searchQuery = it },
    onSearch = { viewModel.searchItems(it) },
    placeholder = "Buscar alumnos...",
    autoFocus = true
)

// Versión simplificada con estado interno
SimpleSearchBar(
    onSearch = { viewModel.searchItems(it) },
    placeholder = "Buscar alumnos...",
    initialQuery = "",
    debounceTime = 300
)
```

## Componentes de Visualización

### UserAvatar

Muestra un avatar de usuario, ya sea con imagen o iniciales.

```kotlin
UserAvatar(
    imageUrl = user.avatarUrl,
    userName = user.name,
    size = 48.dp,
    borderWidth = 2.dp
)
```

## Uso en Composables Existentes

Estos componentes pueden utilizarse en cualquier parte de la aplicación donde sea necesario manejar estados, mostrar información y recibir entrada del usuario. Todos siguen las guías de Material Design 3 y utilizan la paleta de colores de la aplicación.

## Cómo Combinar Componentes

Los componentes están diseñados para trabajar juntos. Por ejemplo:

```kotlin
Scaffold(
    topBar = {
        DefaultTopAppBar(
            title = "Búsqueda de Alumnos",
            onNavigateBack = { navController.popBackStack() }
        )
    },
    snackbarHost = {
        StatusSnackbarHost(hostState = snackbarHostState)
    }
) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        SimpleSearchBar(
            onSearch = { viewModel.searchAlumnos(it) },
            placeholder = "Buscar por nombre o clase..."
        )
        
        ResultContentList(
            result = viewModel.alumnosResult,
            emptyMessage = "No se encontraron alumnos",
            errorTitle = "Error al buscar alumnos",
            onRetry = { viewModel.searchAlumnos(viewModel.currentQuery) }
        ) { alumnos ->
            LazyColumn {
                items(alumnos) { alumno ->
                    ListItem(
                        headlineContent = { Text(alumno.nombreCompleto) },
                        supportingContent = { Text(alumno.clase) },
                        leadingContent = {
                            UserAvatar(
                                userName = alumno.nombreCompleto,
                                imageUrl = alumno.avatarUrl
                            )
                        }
                    )
                }
            }
        }
    }
}
``` 