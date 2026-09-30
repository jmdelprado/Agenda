# Data Model: Tablero Kanban con Agenda y Espacios de Trabajo

Basado en las entidades clave de `spec.md` y los requisitos funcionales FR-001 a FR-015.

**Nota de arquitectura (Hexagonal, ver `plan.md` y `research.md` §10)**: las entidades descritas a continuación son el **modelo de dominio** (`{modulo}/domain/model/`): POJOs sin anotaciones `@Entity`/JPA que encapsulan sus invariantes (p.ej. "no eliminar la última columna", cálculo de `overdue`). La capa de infraestructura (`{modulo}/infrastructure/out/persistence/`) define, por cada entidad de dominio, una entidad JPA equivalente (mismas columnas/tipos que la tabla siguiente) más un *mapper* dominio↔JPA; el dominio y los casos de uso de `application/` nunca importan directamente las clases `@Entity`.

## User

Cuenta individual (FR-013: sin colaboración multi-usuario; cada cuenta ve solo sus propios datos).

| Campo | Tipo | Reglas |
|---|---|---|
| id | UUID | PK |
| email | string | único, obligatorio, formato email válido |
| passwordHash | string | obligatorio, nunca expuesto en API |
| createdAt | timestamp | auto |

**Relaciones**: 1 User → N Workspace.

## Workspace (Espacio de trabajo / pestaña)

Representa un cliente o proyecto; agrupa un tablero y una agenda aislados (FR-008, FR-009).

| Campo | Tipo | Reglas |
|---|---|---|
| id | UUID | PK |
| userId | UUID | FK → User, obligatorio |
| name | string | obligatorio, 1-100 caracteres, único por usuario |
| createdAt | timestamp | auto |
| archivedAt | timestamp | nullable; workspace "eliminado" se archiva (soft delete) para permitir confirmación/recuperación (FR-012) |

**Relaciones**: 1 Workspace → 1 Board (creado automáticamente al crear el workspace, FR-008/US3-AS1). 1 Workspace → N Task (a través del Board).

**Reglas de negocio**:
- Al crear un Workspace se crea automáticamente un Board con las columnas por defecto ("Por hacer", "En progreso", "Hecho") — ver Assumptions en spec.md.
- Eliminar un Workspace requiere confirmación (FR-012) y, si contiene tareas con recordatorios pendientes, dichos recordatorios se cancelan.

## Board (Tablero)

| Campo | Tipo | Reglas |
|---|---|---|
| id | UUID | PK |
| workspaceId | UUID | FK → Workspace, único (relación 1:1) |

**Relaciones**: 1 Board → N Column.

## Column (Columna)

Etapa del flujo de trabajo (FR-002).

| Campo | Tipo | Reglas |
|---|---|---|
| id | UUID | PK |
| boardId | UUID | FK → Board, obligatorio |
| name | string | obligatorio, 1-50 caracteres |
| position | integer | orden dentro del tablero, único por boardId |

**Relaciones**: 1 Column → N Task.

**Reglas de negocio**: no se permite eliminar una columna que sea la única del tablero (edge case de spec.md); si se elimina una columna con tarjetas, deben reasignarse o eliminarse explícitamente (confirmación requerida, FR-012).

## Task (Tarjeta / Tarea)

| Campo | Tipo | Reglas |
|---|---|---|
| id | UUID | PK |
| columnId | UUID | FK → Column, obligatorio |
| title | string | obligatorio, 1-200 caracteres |
| description | text | opcional |
| dueAt | timestamp | opcional (nullable); fecha y hora límite (FR-003) |
| reminderLeadMinutes | integer | opcional; antelación configurable del recordatorio (FR-006); si `dueAt` está definido y este campo es nulo, se usa el valor por defecto (24h = 1440 min) |
| completedAt | timestamp | nullable; se marca al mover la tarjeta a una columna terminal (p. ej. "Hecho") |
| createdAt / updatedAt | timestamp | auto |

**Estado derivado (no persistido)**: `overdue` = `dueAt` existe, `dueAt < now()` y `completedAt` es nulo (FR-007).

**Relaciones**: N Task → 1 Column (y transitivamente 1 Workspace). 1 Task → 0..1 Reminder activo.

**Reglas de negocio**:
- Cambiar `dueAt` o eliminarlo recalcula/cancela el Reminder asociado (US2-AS4).
- Mover una tarjeta actualiza `columnId`; si la columna destino es terminal, se registra `completedAt`.

## Reminder (Recordatorio)

| Campo | Tipo | Reglas |
|---|---|---|
| id | UUID | PK |
| taskId | UUID | FK → Task, obligatorio |
| triggerAt | timestamp | calculado = `task.dueAt - reminderLeadMinutes`; obligatorio |
| status | enum | `PENDING`, `SENT`, `CANCELLED`, `FAILED` |
| channels | set(enum) | `IN_APP`, `EMAIL` (FR-014: ambos por defecto) |
| sentAt | timestamp | nullable |

**Ciclo de vida (state transitions)**:

```
PENDING --(triggerAt alcanzado y enviado con éxito)--> SENT
PENDING --(dueAt/reminder modificado o tarea/columna/workspace eliminado)--> CANCELLED
PENDING --(fallo de envío tras reintentos)--> FAILED (se reintenta en el siguiente ciclo del job hasta un máximo, luego permanece FAILED y se notifica in-app igualmente si ese canal tuvo éxito)
```

**Relaciones**: N Reminder → 1 Task (normalmente 0..1 Reminder activo por Task; se permite histórico si el usuario reprograma).

## Notification (Notificación in-app)

Registro de lo que se muestra en el panel in-app cuando un Reminder se dispara (FR-014).

| Campo | Tipo | Reglas |
|---|---|---|
| id | UUID | PK |
| userId | UUID | FK → User |
| reminderId | UUID | FK → Reminder |
| message | string | generado (p. ej. "«{task.title}» vence en X") |
| readAt | timestamp | nullable; marca de lectura por el usuario |
| createdAt | timestamp | auto |

## Vista de agenda global

No es una entidad nueva: es una consulta que agrega `Task` con `dueAt` no nulo de todos los `Workspace` de un `User` (vía `Column → Board → Workspace`), devolviendo también el nombre del workspace de origen (FR-015, SC-007).

## Resumen de relaciones

```
User (1) ── (N) Workspace (1) ── (1) Board (1) ── (N) Column (1) ── (N) Task (1) ── (0..N hist.) Reminder
User (1) ── (N) Notification (N) ── (1) Reminder
```
