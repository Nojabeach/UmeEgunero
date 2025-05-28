# Sistema de Comunicación Unificado

## Resumen

El sistema de comunicación unificado de UmeEgunero centraliza todas las formas de comunicación en una sola interfaz y arquitectura, simplificando la interacción del usuario, reduciendo la duplicación de código y ofreciendo una experiencia coherente independientemente del perfil del usuario.

## Diagrama de Arquitectura

```mermaid
flowchart TB
    subgraph Frontend["Capa de Presentación"]
        UI[Interfaz de Usuario Unificada]
        UI --> |Muestra| Inbox[Bandeja de Entrada]
        UI --> |Muestra| Detail[Detalle Mensaje]
        UI --> |Muestra| Compose[Redactar Mensaje]
    end
    
    subgraph ViewModel["Capa de Lógica de Negocio"]
        VM_Inbox[UnifiedInboxViewModel]
        VM_Detail[MessageDetailViewModel]
        VM_Compose[NewMessageViewModel]
    end
    
    subgraph Repository["Capa de Repositorio"]
        UMR[UnifiedMessageRepository]
    end
    
    subgraph Database["Capa de Datos"]
        Firestore[(Firestore)]
        subgraph Collections["Colecciones"]
            Messages[("unified_messages")]
            Conversations[("conversations")]
        end
    end
    
    UI --> ViewModel
    ViewModel --> Repository
    Repository --> Database
    
    Firestore --- Collections
    
    style Frontend fill:#f9f9f9,stroke:#333,stroke-width:2px
    style ViewModel fill:#e1f5fe,stroke:#0288d1,stroke-width:2px
    style Repository fill:#e8f5e9,stroke:#4caf50,stroke-width:2px
    style Database fill:#fff3e0,stroke:#ff9800,stroke-width:2px
    style Collections fill:#fff8e1,stroke:#ffc107,stroke-width:2px
```

## Estructura del Modelo

El sistema se basa en el modelo `UnifiedMessage` que unifica los conceptos de:
- Mensajes de chat
- Notificaciones
- Comunicados/anuncios
- Incidencias
- Registros de asistencia
- Mensajes del sistema

### Diagrama de Flujo de Mensajes

```mermaid
sequenceDiagram
    autonumber
    participant Usuario as Usuario
    participant UI as Interfaz de Usuario
    participant ViewModel as ViewModel
    participant Repo as UnifiedMessageRepository
    participant Firestore as Firestore
    
    Usuario->>UI: Interactúa con la app
    
    alt Enviar Mensaje
        Usuario->>UI: Crea mensaje
        UI->>ViewModel: sendMessage()
        ViewModel->>Repo: sendMessage(unifiedMessage)
        Repo->>Firestore: Guarda en colección unified_messages
        Firestore-->>Repo: Confirmación
        Repo-->>ViewModel: Resultado
        ViewModel-->>UI: Actualiza estado (success/error)
        UI-->>Usuario: Feedback visual
    
    else Ver Bandeja de Entrada
        Usuario->>UI: Abre bandeja
        UI->>ViewModel: loadMessages()
        ViewModel->>Repo: getCurrentUserInbox()
        Repo->>Firestore: Consulta mensajes por usuario
        Firestore-->>Repo: Lista de mensajes
        Repo-->>ViewModel: Resultado
        ViewModel-->>UI: Actualiza estado con mensajes
        UI-->>Usuario: Muestra bandeja unificada
    
    else Ver Detalle de Mensaje
        Usuario->>UI: Selecciona mensaje
        UI->>ViewModel: loadMessage(id)
        ViewModel->>Repo: getMessageById(id)
        Repo->>Firestore: Consulta mensaje específico
        Firestore-->>Repo: Datos del mensaje
        Repo-->>ViewModel: Resultado
        ViewModel-->>UI: Actualiza estado con detalle
        UI-->>Usuario: Muestra detalle de mensaje
        ViewModel->>Repo: markAsRead(messageId)
        Repo->>Firestore: Actualiza estado a READ
    end
```

### Diagrama de Tipos de Mensaje

```mermaid
classDiagram
    class MessageType {
        <<enumeration>>
        CHAT
        NOTIFICATION
        ANNOUNCEMENT
        INCIDENT
        ATTENDANCE
        DAILY_RECORD
        SYSTEM
    }
    
    class MessagePriority {
        <<enumeration>>
        LOW
        NORMAL
        HIGH
        URGENT
    }
    
    class MessageStatus {
        <<enumeration>>
        UNREAD
        READ
        PENDING
        DELIVERED
        FAILED
    }
    
    class UnifiedMessage {
        String id
        String title
        String content
        MessageType type
        MessagePriority priority
        String senderId
        String senderName
        String receiverId
        List~String~ receiversIds
        Timestamp timestamp
        MessageStatus status
        Timestamp readTimestamp
        Map~String,String~ metadata
        String relatedEntityId
        String relatedEntityType
        List~Map~ attachments
        List~MessageAction~ actions
        String conversationId
        String replyToId
    }
    
    class MessageAction {
        String id
        String label
        String actionType
        Map~String,String~ data
        Boolean requiresConfirmation
        String confirmationMessage
    }
    
    UnifiedMessage "1" *-- "1" MessageType
    UnifiedMessage "1" *-- "1" MessagePriority
    UnifiedMessage "1" *-- "1" MessageStatus
    UnifiedMessage "1" *-- "*" MessageAction
```

## Estructura de Datos

El sistema utiliza una colección principal en Firestore:

### Colección `unified_messages`

Almacena todos los mensajes independientemente del tipo. Cada documento contiene los campos del modelo `UnifiedMessage`.

```mermaid
erDiagram
    UNIFIED_MESSAGES {
        string id PK
        string title
        string content
        string type
        string priority
        string senderId FK
        string senderName
        string receiverId FK
        list receiversIds
        timestamp timestamp
        string status
        timestamp readTimestamp
        map metadata
        string relatedEntityId FK
        string relatedEntityType
        list attachments
        list actions
        string conversationId FK
        string replyToId FK
    }
    
    CONVERSATIONS {
        string id PK
        list participantIds
        string title
        timestamp lastMessageTimestamp
        timestamp createdAt
        timestamp updatedAt
        string entityId FK
        string entityType
    }
    
    USERS {
        string id PK
        string nombre
        string email
        string tipoUsuario
    }
    
    UNIFIED_MESSAGES }|--|| USERS : "remitente"
    UNIFIED_MESSAGES }|--o| USERS : "destinatario"
    UNIFIED_MESSAGES }o--o{ CONVERSATIONS : "pertenece"
    CONVERSATIONS }o--o{ USERS : "participantes"
```

## Flujo de Navegación

```mermaid
stateDiagram-v2
    [*] --> UnifiedInbox
    
    state "Bandeja de Entrada" as UnifiedInbox
    state "Detalle de Mensaje" as MessageDetail
    state "Nuevo Mensaje" as NewMessage
    state "Responder Mensaje" as ReplyMessage
    
    UnifiedInbox --> MessageDetail: Ver mensaje
    UnifiedInbox --> NewMessage: Crear mensaje
    MessageDetail --> ReplyMessage: Responder
    
    MessageDetail --> UnifiedInbox: Volver
    NewMessage --> UnifiedInbox: Enviar/Cancelar
    ReplyMessage --> UnifiedInbox: Enviar/Cancelar
    
    state MessageDetail {
        [*] --> ShowMessage
        ShowMessage --> Action
        
        state Action {
            [*] --> MarkAsRead
            [*] --> DeleteMessage
        }
    }
    
    state NewMessage {
        [*] --> SelectType
        SelectType --> SelectRecipient
        SelectRecipient --> ComposeContent
        ComposeContent --> SetPriority
        SetPriority --> Send
    }
```

## Interacción entre Perfiles

### Diagrama de Comunicación

```mermaid
flowchart LR
    subgraph AdmSistema["Administrador Sistema"]
        AS_Send[Enviar]
        AS_Read[Leer]
    end
    
    subgraph AdmCentro["Administrador Centro"]
        AC_Send[Enviar]
        AC_Read[Leer]
    end
    
    subgraph Profesor["Profesor"]
        P_Send[Enviar]
        P_Read[Leer]
    end
    
    subgraph Familiar["Familiar"]
        F_Send[Enviar]
        F_Read[Leer]
    end
    
    subgraph Mensaje["Tipos de Mensaje"]
        Chat[Chat]
        Notif[Notificación]
        Anuncio[Comunicado]
        Incidencia[Incidencia]
        Asistencia[Asistencia]
        Registro[Registro Diario]
    end
    
    AdmSistema ---> Mensaje
    Mensaje ----> AdmSistema
    
    AdmCentro ---> Mensaje
    Mensaje ----> AdmCentro
    
    Profesor ---> Mensaje
    Mensaje ----> Profesor
    
    Familiar ---> Chat
    Familiar ---> Incidencia
    Mensaje ----> Familiar
    
    style AdmSistema fill:#e1bee7,stroke:#8e24aa
    style AdmCentro fill:#bbdefb,stroke:#1976d2
    style Profesor fill:#c8e6c9,stroke:#388e3c
    style Familiar fill:#ffecb3,stroke:#ffa000
    style Mensaje fill:#f5f5f5,stroke:#616161
```

## Integración con Solicitudes de Vinculación

El sistema de comunicación unificado se integra con el sistema de solicitudes de vinculación para proporcionar notificaciones automáticas en diferentes puntos del proceso de vinculación entre familiares y alumnos.

### Diagrama de Flujo de Solicitudes

```mermaid
sequenceDiagram
    participant Familiar
    participant Sistema
    participant AdminCentro
    participant Notificación
    
    Familiar->>Sistema: Crea solicitud de vinculación
    Sistema->>AdminCentro: Genera mensaje tipo NOTIFICATION
    
    alt Solicitud Aprobada
        AdminCentro->>Sistema: Aprueba solicitud
        Sistema->>Familiar: Genera mensaje tipo NOTIFICATION (aprobada)
        Sistema->>Notificación: Envía push notification
    else Solicitud Rechazada
        AdminCentro->>Sistema: Rechaza solicitud
        Sistema->>Familiar: Genera mensaje tipo NOTIFICATION (rechazada)
        Sistema->>Notificación: Envía push notification
    end
```

### Estructura de Mensaje de Solicitud

Los mensajes de solicitud se generan automáticamente y se almacenan en el sistema unificado con las siguientes características:

```mermaid
classDiagram
    class SolicitudVinculacionMessage {
        type = MessageType.NOTIFICATION
        priority = MessagePriority.HIGH
        title = "Solicitud de vinculación [estado]"
        content = "Información detallada de la solicitud"
        metadata {
            solicitudId: String
            alumnoDni: String
            estado: EstadoSolicitud
            tipoNotificacion: "SOLICITUD_VINCULACION"
        }
        relatedEntityId = "ID de la solicitud"
        relatedEntityType = "SOLICITUD_VINCULACION"
    }
```

## Integración con Sistema de Notificaciones

El sistema de comunicación unificado está estrechamente integrado con el sistema de notificaciones push de la aplicación mediante Firebase Cloud Messaging (FCM).

### Flujo de Notificaciones

```mermaid
flowchart TB
    subgraph Origen["Origen de Notificaciones"]
        Message["Nuevo Mensaje"]
        Solicitud["Cambio Estado Solicitud"]
        Sistema["Evento del Sistema"]
        Registro["Nuevo Registro Diario"]
    end
    
    subgraph Procesamiento["Procesamiento"]
        Unificado["Sistema de Comunicación Unificado"]
        FCM["Firebase Cloud Messaging"]
    end
    
    subgraph Destino["Entrega"]
        Push["Notificación Push"]
        InApp["Notificación In-App"]
    end
    
    Message --> Unificado
    Solicitud --> Unificado
    Sistema --> Unificado
    Registro --> Unificado
    
    Unificado --> |Genera mensajes|FCM
    Unificado --> |Actualiza contador|InApp
    
    FCM --> Push
    
    style Origen fill:#f5f5f5,stroke:#333,stroke-width:1px
    style Procesamiento fill:#e3f2fd,stroke:#1976d2,stroke-width:1px
    style Destino fill:#e8f5e9,stroke:#388e3c,stroke-width:1px
```

### Estructura de Notificación Push

```mermaid
classDiagram
    class FCMNotification {
        notification {
            title: String
            body: String
        }
        data {
            type: "MENSAJE" | "SOLICITUD" | "SISTEMA" | "REGISTRO"
            entityId: String
            action: "VIEW" | "APPROVE" | "REJECT"
            deeplink: String
        }
        android {
            priority: "high"
            notification {
                channel_id: String
                icon: String
                color: String
                click_action: String
            }
        }
    }
```

## Ventajas del Sistema Unificado

1. **Experiencia de usuario mejorada**:
   - Interfaz única para todos los tipos de comunicación
   - Menor curva de aprendizaje
   - Acceso centralizado a toda la información

2. **Arquitectura simplificada**:
   - Menos código redundante
   - Mantenimiento simplificado
   - Coherencia en toda la aplicación

3. **Flexibilidad**:
   - Facilidad para añadir nuevos tipos de comunicación
   - Adaptable a diferentes perfiles de usuario
   - Extensible para futuras necesidades

4. **Mejor rendimiento**:
   - Consultas optimizadas a la base de datos
   - Menor duplicación de datos
   - Caché compartida

5. **Integración completa**:
   - Interoperabilidad con sistema de solicitudes
   - Conexión directa con el sistema de notificaciones push
   - Soporte para eventos del sistema y registros diarios

## Conclusión

El sistema de comunicación unificado representa una mejora significativa en la arquitectura y experiencia de usuario de UmeEgunero, simplificando la forma en que todos los actores del ecosistema educativo interactúan entre sí, manteniendo al mismo tiempo la separación lógica necesaria entre los diferentes tipos de comunicación. La integración con los sistemas de solicitudes y notificaciones garantiza que los usuarios reciban información importante a través de múltiples canales, mejorando la comunicación general y la experiencia de usuario en la aplicación.

## Detalles de Implementación

### Marcado de Mensajes como Leídos

#### Marcado Automático (Nuevo)

Para mejorar la experiencia de usuario, los mensajes ahora se marcan automáticamente como leídos cuando un usuario abre la pantalla de detalle:

```kotlin
@Composable
fun MessageDetailScreen(
    messageId: String,
    onBack: () -> Unit,
    onNavigateToConversation: (String) -> Unit = {},
    viewModel: MessageDetailViewModel = hiltViewModel()
) {
    // Cargar el mensaje
    LaunchedEffect(messageId) {
        viewModel.loadMessage(messageId)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Marcar automáticamente como leído cuando se abre el mensaje
    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            if (!message.isRead) {
                viewModel.markAsRead()
            }
        }
    }
    
    // Resto del componente...
}
```

Excepciones al marcado automático:
- Comunicados que requieren confirmación explícita (flag `requireConfirmation`)
- Mensajes que ya están marcados como leídos

El marcado automático se implementa en el ViewModel:

```kotlin
fun markAsRead() {
    val currentMessage = _uiState.value.message ?: return
    
    viewModelScope.launch {
        try {
            val result = messageRepository.markAsRead(currentMessage.id)
            
            when (result) {
                is Result.Success -> {
                    // Actualizar el estado local
                    _uiState.update { state ->
                        state.copy(
                            message = state.message?.copy(
                                status = MessageStatus.READ
                            )
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = "Error al marcar como leído: ${result.message}") }
                }
                else -> {}
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar mensaje como leído: ${currentMessage.id}")
        }
    }
}
```

Esta funcionalidad:
1. Mejora la experiencia de usuario al eliminar pasos manuales
2. Mantiene sincronizado el estado de lectura en Firebase
3. Respeta los requisitos especiales para comunicados oficiales
4. Optimiza el flujo de trabajo con los mensajes 