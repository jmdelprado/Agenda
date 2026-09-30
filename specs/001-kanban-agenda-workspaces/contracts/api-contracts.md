# API Contracts: Tablero Kanban con Agenda y Espacios de Trabajo

API REST expuesta por el backend Spring Boot. Formato: `MÉTODO /ruta` — descripción, request/response resumidos. El contrato definitivo y navegable se genera vía `springdoc-openapi` (Swagger UI) a partir de estos endpoints (ver research.md §7); este documento fija su alcance mínimo.

Convenciones: todas las rutas bajo `/api/v1`. Autenticación por cabecera `Authorization: Bearer <JWT>` salvo `/auth/*`. Respuestas de error: `{ "error": string, "details"?: object }` con el código HTTP correspondiente (400 validación, 401 no autenticado, 403 recurso de otro usuario, 404 no encontrado, 409 conflicto).

**Nota de arquitectura**: cada endpoint se implementa como un adaptador de entrada (`{modulo}/infrastructure/in/web`, ver `plan.md`) que solo traduce la petición HTTP a la invocación del caso de uso correspondiente (puerto de entrada en `{modulo}/application/port/in`) y mapea el resultado de dominio a DTO de respuesta; no contiene lógica de negocio.

## Auth

| Endpoint | Descripción | Request | Response |
|---|---|---|---|
| `POST /auth/register` | Crear cuenta | `{ email, password }` | `201 { userId }` |
| `POST /auth/login` | Iniciar sesión | `{ email, password }` | `200 { accessToken, refreshToken }` |
| `POST /auth/refresh` | Renovar access token | `{ refreshToken }` | `200 { accessToken }` |

## Workspaces

| Endpoint | Descripción | Request | Response |
|---|---|---|---|
| `GET /workspaces` | Listar espacios de trabajo del usuario (pestañas) | — | `200 [{ id, name, createdAt }]` |
| `POST /workspaces` | Crear espacio de trabajo (crea Board con columnas por defecto "Por hacer"/"En progreso"/"Hecho") | `{ name }` | `201 { id, name, createdAt, board: { id, columns: [{ id, name, position }] } }` |
| `PATCH /workspaces/{id}` | Renombrar espacio de trabajo | `{ name }` | `200 { id, name }` |
| `DELETE /workspaces/{id}` | Eliminar (soft delete) espacio de trabajo; cancela recordatorios asociados | — | `204` |

## Boards & Columns

| Endpoint | Descripción | Request | Response |
|---|---|---|---|
| `GET /workspaces/{workspaceId}/board` | Obtener tablero completo (columnas + tarjetas, con la fecha límite/antelación/estado de vencida de cada tarea) del espacio activo | — | `200 { boardId, columns: [{ id, name, position, tasks: [{ id, title, description, completed, dueAt, reminderLeadMinutes, overdue }] }] }` |
| `POST /workspaces/{workspaceId}/board/columns` | Crear columna (se añade al final del tablero; la posición la asigna el servidor) | `{ name }` | `201 { id, name, position }` |
| `PATCH /boards/columns/{columnId}` | Renombrar columna (no reordena; ver Nota) | `{ name }` | `200 OK` (sin cuerpo) |
| `DELETE /boards/columns/{columnId}` | Eliminar columna (rechaza si es la única del tablero; requiere reasignar/eliminar tarjetas) | — | `204` / `409` si es la última columna |

**Nota**: el reordenamiento de columnas (`position`) no está implementado en esta versión — quedó fuera del alcance real de User Story 1 (crear/renombrar/eliminar sí, reordenar no se pidió en ningún acceptance scenario de `spec.md`). Si se necesita en el futuro, añadir un `PATCH .../reorder` dedicado siguiendo el mismo patrón que `PATCH /tasks/{taskId}/due-date`.

## Tasks

| Endpoint | Descripción | Request | Response |
|---|---|---|---|
| `POST /boards/columns/{columnId}/tasks` | Crear tarjeta (sin fecha límite; se asigna después vía el endpoint dedicado) | `{ title, description? }` | `201 { id, columnId, title, description, completed }` |
| `PATCH /tasks/{taskId}` | Editar tarjeta (título, descripción) | `{ title, description? }` | `200 OK` (sin cuerpo) |
| `PATCH /tasks/{taskId}/due-date` | Asignar/modificar/eliminar (`dueAt: null`) la fecha límite y la antelación del recordatorio (US2-AS1/AS4) — endpoint dedicado, separado de la edición de título/descripción porque invoca el caso de uso `SetTaskDueDateUseCase` del módulo `reminder`, no `UpdateTaskUseCase` del módulo `task` | `{ dueAt: string \| null, reminderLeadMinutes?: number }` | `200 { taskId, dueAt, reminderLeadMinutes, overdue }` |
| `PATCH /tasks/{taskId}/move` | Mover tarjeta a otra columna (a la cola; sin reordenar dentro de la columna). Si la columna destino es la de mayor posición del tablero (columna terminal, p.ej. "Hecho"), la tarea se marca completada; en cualquier otro caso se reabre | `{ columnId }` | `200 OK` (sin cuerpo) |
| `DELETE /tasks/{taskId}` | Eliminar tarjeta (cancela recordatorio asociado) | — | `204` |

## Agenda

| Endpoint | Descripción | Request | Response |
|---|---|---|---|
| `GET /workspaces/{workspaceId}/agenda` | Tareas con fecha límite del espacio activo, ordenadas por `dueAt` | — | `200 [{ taskId, title, dueAt, overdue }]` |
| `GET /agenda/global` | Vista consolidada de fechas límite próximas de todos los espacios no archivados del usuario (FR-015) | — | `200 [{ taskId, title, dueAt, overdue, workspaceId, workspaceName }]` |

**Nota**: los filtros opcionales por rango de fechas (`from`/`to`) descritos en una versión anterior de este documento no se implementaron — no los exigía ningún acceptance scenario de `spec.md` y ambos endpoints ya devuelven solo tareas con `dueAt` no nulo, ordenadas. Añadir si un caso de uso futuro los necesita.

## Reminders & Notifications

| Endpoint | Descripción | Request | Response |
|---|---|---|---|
| `GET /notifications` | Notificaciones in-app del usuario (para *short polling* del frontend, ver research.md §4) | query opcional `unreadOnly` | `200 [{ id, message, reminderId, readAt, createdAt }]` |
| `PATCH /notifications/{id}/read` | Marcar notificación como leída | — | `200 { id, readAt }` |

## Contract tests a cubrir (backend/src/test/java/.../contract)

- Registro/login devuelven `401` ante credenciales inválidas y `201`/`200` en el camino feliz.
- Un usuario no puede leer/modificar workspaces, tareas o notificaciones de otro usuario (`403`/`404`).
- Crear un workspace crea automáticamente un board con las columnas por defecto.
- Eliminar la última columna de un board devuelve `409`.
- Editar/eliminar `dueAt` de una tarea cancela el `Reminder` `PENDING` asociado.
- `GET /agenda/global` incluye tareas de más de un workspace con su `workspaceName` correcto y excluye las de otros usuarios.
