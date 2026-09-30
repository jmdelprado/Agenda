# Feature Specification: Importar eventos desde cuentas de X al calendario de la Agenda

**Feature Branch**: `feature/importar-eventos-x` (desde `develop`)

**Created**: 2026-09-30

**Status**: Draft (planificación)

**Input**: "A partir de 5-6 cuentas de X (Twitter), acceder a su contenido, sintetizar la información y organizarla en el propio calendario de la agenda."

## Contexto

El calendario de la agenda muestra hoy **tareas con fecha límite** (`GET /api/v1/workspaces/{id}/agenda`, `AgendaController`) y, opcionalmente, eventos de Google Calendar (solo lectura). No existe una entidad "evento" propia. Por tanto, cada evento detectado se materializa como una **tarea con `dueAt`** dentro de un tablero del workspace, de modo que aparece en el calendario y hereda los recordatorios existentes.

Endpoints existentes reutilizables:
- `POST /api/v1/boards/columns/{columnId}/tasks` — crear tarea
- `PATCH /api/v1/tasks/{taskId}/due-date` — asignar fecha límite

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Importación puntual asistida (Priority: P1)

Como usuario, quiero indicar 5-6 cuentas de X y que Claude lea sus publicaciones recientes, detecte eventos con fecha y los cree en mi calendario, para no tener que revisar cada cuenta a mano.

**Independent Test**: Con una cuenta de prueba, se revisan las publicaciones, se presenta la lista de eventos detectados, y tras confirmarla aparecen como tareas con fecha en la agenda del workspace elegido.

**Acceptance Scenarios**:

1. **Given** una lista de cuentas y un periodo a revisar, **When** se lanza la lectura, **Then** se obtiene una lista de eventos candidatos con título, fecha/hora, cuenta de origen y enlace al tweet.
2. **Given** eventos candidatos con fecha ambigua o incompleta, **When** se presenta la lista, **Then** quedan marcados para revisión y no se crean sin confirmación.
3. **Given** eventos confirmados, **When** se importan, **Then** se crean como tareas con fecha límite en la columna destino y aparecen en el calendario.
4. **Given** un evento ya importado antes, **When** se repite la importación, **Then** no se duplica.

### User Story 2 - Importación recurrente automatizada (Priority: P2)

Como usuario, quiero que la importación se ejecute periódicamente sin intervención, para tener el calendario siempre actualizado.

**Independent Test**: Un proceso programado consulta las cuentas configuradas y crea solo los eventos nuevos.

**Acceptance Scenarios**:

1. **Given** cuentas configuradas y una periodicidad, **When** se cumple el intervalo, **Then** se crean únicamente los eventos nuevos.
2. **Given** un fallo de acceso a X, **When** falla la ejecución, **Then** se registra el error sin afectar a los datos existentes.

## Requirements *(mandatory)*

- **FR-001**: El sistema DEBE permitir configurar la lista de cuentas de X a vigilar (5-6 inicialmente).
- **FR-002**: El sistema DEBE extraer de cada publicación: título del evento, fecha/hora, cuenta de origen y URL del tweet.
- **FR-003**: NO DEBE inventar fechas que la publicación no indique; los casos ambiguos se marcan para revisión.
- **FR-004**: Cada evento DEBE crearse como tarea con `dueAt` en el workspace y columna destino elegidos, con el enlace al tweet y la cuenta de origen en la descripción.
- **FR-005**: DEBE evitar duplicados (clave: URL/ID del tweet + fecha del evento).
- **FR-006**: DEBE respetar las condiciones de uso de X y aplicar límites de frecuencia moderados.

## Assumptions

- **Fase 1 (US1)**: la lectura se hace desde Chrome con la sesión de X del usuario (extensión Claude in Chrome), sin API de pago y sin cambios en el backend; solo se usa la API REST existente.
- **Fase 2 (US2)**: requeriría la API oficial de X (de pago) o un mecanismo equivalente, y un job programado en el backend (hexagonal, junto al módulo `reminder`).
- Hace falta backend en marcha, autenticación (token de usuario) y un workspace/columna destino, p. ej. una columna "Eventos X".
- Información por definir con el usuario: @ de las cuentas, tipo de eventos a buscar, periodo histórico, workspace/columna destino.

## Open Questions

1. ¿Cuáles son las cuentas y qué tipo de eventos interesan?
2. ¿Workspace y columna destino?
3. ¿Se pasa a la Fase 2 (automatización) tras validar la Fase 1?
