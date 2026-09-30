---

description: "Task list template for feature implementation"
---

# Tasks: Tablero Kanban con Agenda y Espacios de Trabajo

**Input**: Design documents from `/specs/001-kanban-agenda-workspaces/`

**Prerequisites**: plan.md (requerido), spec.md (requerido para las historias de usuario), research.md, data-model.md, contracts/api-contracts.md, quickstart.md

**Tests**: No solicitados explícitamente en la especificación; no se generan tareas de test dedicadas. La verificación funcional se hace mediante los escenarios de `quickstart.md` (tarea T075).

**Organization**: Las tareas están agrupadas por historia de usuario para permitir implementación y validación independientes de cada una. El backend sigue Arquitectura Hexagonal por módulo (`domain/` → `application/` → `infrastructure/`), tal y como define `plan.md`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias pendientes)
- **[Story]**: Historia de usuario a la que pertenece la tarea (US1, US2, US3)
- Se incluyen rutas de archivo exactas en cada descripción

## Path Conventions

Aplicación web (ver `plan.md` → Project Structure): `backend/src/main/java/com/agenda/kanban/` (Java/Spring Boot, hexagonal) y `frontend/src/app/` (Angular).

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Inicialización de los proyectos backend y frontend.

- [X] T001 Crear el esqueleto Maven del backend (Spring Boot 3.3, Java 21; dependencias Web, Data JPA, Validation, Security, Mail) en `backend/pom.xml` y `backend/src/main/java/com/agenda/kanban/Application.java`
- [X] T002 [P] Crear el workspace Angular 18 del frontend (componentes standalone, routing habilitado) en `frontend/` (`frontend/angular.json`, `frontend/src/app/app.config.ts`)
- [X] T003 [P] Crear `docker-compose.yml` en la raíz del repositorio con servicios PostgreSQL 16 y Mailhog (según `quickstart.md`)
- [X] T004 Configurar `backend/src/main/resources/application.yml` (datasource PostgreSQL, JPA, Flyway, secreto/expiración JWT, `spring.mail` apuntando a Mailhog) (depende de T001, T003)
- [X] T005 [P] Crear la estructura de paquetes vacía `domain/`, `application/{port/in,port/out,service}/`, `infrastructure/{in/web,out/persistence}/` para los módulos `auth`, `workspace`, `board`, `task`, `reminder`, `notification` bajo `backend/src/main/java/com/agenda/kanban/` (depende de T001)
- [X] T006 [P] Crear la estructura de carpetas `core/`, `shared/`, `features/{auth,workspaces,board,agenda,notifications}/` bajo `frontend/src/app/` (depende de T002)
- [X] T007 [P] Configurar ESLint + Prettier en `frontend/.eslintrc.json` y `frontend/.prettierrc` (depende de T002)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Esquema de base de datos, autenticación JWT y configuración transversal que bloquean el trabajo de cualquier historia de usuario.

**⚠️ CRITICAL**: Ninguna historia de usuario puede comenzar hasta completar esta fase.

- [X] T008 Crear la migración Flyway `backend/src/main/resources/db/migration/V1__init.sql` con las tablas: `users` (`email` único y obligatorio, formato validado en la capa de aplicación; `password_hash` obligatorio), `workspaces` (`name` obligatorio, 1-100 caracteres, único por `user_id`; `archived_at` nullable), `boards` (`workspace_id` FK obligatorio y único, relación 1:1), `columns` (`name` obligatorio, 1-50 caracteres; `position` entero único por `board_id`), `tasks` (`title` obligatorio, 1-200 caracteres; `description` opcional; `due_at` opcional; `reminder_lead_minutes` opcional; `completed_at` nullable), `reminders` (`task_id` FK obligatorio; `trigger_at` obligatorio; `status` en `PENDING`/`SENT`/`CANCELLED`/`FAILED`; `channels` con `IN_APP`/`EMAIL`; `sent_at` nullable), `notifications` (`user_id` FK, `reminder_id` FK, `message` obligatorio, `read_at` nullable, `created_at` automático) — según `data-model.md`
- [X] T009 [P] Crear la clase base de dominio `DomainException` en `backend/src/main/java/com/agenda/kanban/shared/DomainException.java`
- [X] T010 [P] Crear la entidad de dominio `User` (id, `email` único/obligatorio con formato válido, `passwordHash` obligatorio y nunca expuesto, `createdAt`) en `backend/src/main/java/com/agenda/kanban/auth/domain/model/User.java`
- [X] T011 [P] Definir los puertos de aplicación de `auth`: `RegisterUserUseCase`, `AuthenticateUserUseCase`, `RefreshTokenUseCase` (`port/in`); `UserRepositoryPort`, `PasswordHasherPort`, `TokenGeneratorPort` (`port/out`) en `backend/src/main/java/com/agenda/kanban/auth/application/port/`
- [X] T012 Implementar `RegisterUserService` y `AuthenticateUserService` en `backend/src/main/java/com/agenda/kanban/auth/application/service/` (depende de T010, T011)
- [X] T013 [P] Implementar el adaptador JPA de `User` (`UserJpaEntity`, repositorio Spring Data, `UserRepositoryAdapter` que implementa `UserRepositoryPort`, mapper dominio↔JPA) en `backend/src/main/java/com/agenda/kanban/auth/infrastructure/out/persistence/` (depende de T011)
- [X] T014 [P] Implementar `PasswordHasherAdapter` (BCrypt) y `JwtTokenAdapter` (implementa `TokenGeneratorPort`) en `backend/src/main/java/com/agenda/kanban/auth/infrastructure/out/security/` (depende de T011)
- [X] T015 Implementar `AuthController` (`POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`) + DTOs en `backend/src/main/java/com/agenda/kanban/auth/infrastructure/in/web/AuthController.java` (depende de T012, T014)
- [X] T016 Configurar la cadena de filtros de Spring Security (JWT stateless, CORS para el origen del frontend) en `backend/src/main/java/com/agenda/kanban/config/SecurityConfig.java` (depende de T014)
- [X] T017 [P] Configurar `springdoc-openapi`/Swagger UI en `backend/src/main/java/com/agenda/kanban/config/OpenApiConfig.java`
- [X] T018 [P] Configurar `GlobalExceptionHandler` que traduce `DomainException` y errores de validación al formato `{ "error": string, "details"?: object }` con los códigos 400/401/403/404/409 en `backend/src/main/java/com/agenda/kanban/config/GlobalExceptionHandler.java` (depende de T009)
- [X] T019 [P] Configurar `JavaMailSender` en `backend/src/main/java/com/agenda/kanban/config/MailConfig.java`
- [X] T020 [P] Configurar `@EnableScheduling` en `backend/src/main/java/com/agenda/kanban/config/SchedulingConfig.java`
- [X] T021 Implementar el interceptor HTTP JWT y `AuthService` (registro/login/refresh, almacenamiento del token) en `frontend/src/app/core/interceptors/jwt.interceptor.ts` y `frontend/src/app/core/auth/auth.service.ts` (depende de T015)
- [X] T022 [P] Implementar el guard de autenticación en `frontend/src/app/core/guards/auth.guard.ts`
- [X] T023 [P] Implementar los componentes de login/registro y sus rutas en `frontend/src/app/features/auth/` (depende de T021)
- [X] T024 [P] Configurar `frontend/src/environments/environment.ts` y `environment.prod.ts` con `apiBaseUrl`
- [X] T025 Configurar el enrutado raíz de Angular (rutas protegidas por `auth.guard`) en `frontend/src/app/app.routes.ts` (depende de T022, T023)

**Checkpoint**: Autenticación y esqueleto de proyecto listos — puede comenzar el trabajo de las historias de usuario.

---

## Phase 3: User Story 1 - Gestionar tareas en un tablero Kanban (Priority: P1) 🎯 MVP

**Goal**: Crear, mover, editar y eliminar tarjetas a través de columnas de un tablero Kanban dentro de un espacio de trabajo.

**Independent Test**: Crear un espacio de trabajo por defecto, añadir tarjetas, moverlas entre columnas y editarlas/eliminarlas, sin necesitar la agenda ni múltiples espacios de trabajo.

- [X] T026 [P] [US1] Crear la entidad de dominio `Workspace` (id, `userId`, `name` obligatorio 1-100 caracteres único por usuario, `createdAt`, `archivedAt` nullable) en `backend/src/main/java/com/agenda/kanban/workspace/domain/model/Workspace.java`
- [X] T027 [P] [US1] Crear la entidad de dominio `Board` (id, `workspaceId` FK único, relación 1:1) en `backend/src/main/java/com/agenda/kanban/board/domain/model/Board.java`
- [X] T028 [P] [US1] Crear la entidad de dominio `Column` (id, `boardId`, `name` obligatorio 1-50 caracteres, `position` entero único por `boardId`) con el invariante "no se puede eliminar si es la única columna del tablero" y la excepción `LastColumnCannotBeDeletedException` en `backend/src/main/java/com/agenda/kanban/board/domain/model/Column.java` y `backend/src/main/java/com/agenda/kanban/board/domain/exception/LastColumnCannotBeDeletedException.java`
- [X] T029 [P] [US1] Crear la entidad de dominio `Task` (id, `columnId`, `title` obligatorio 1-200 caracteres, `description` opcional, `completedAt` nullable) en `backend/src/main/java/com/agenda/kanban/task/domain/model/Task.java`
- [X] T030 [US1] Definir los puertos de aplicación de `workspace`/`board`: `CreateWorkspaceUseCase` (crea `Workspace` + `Board` + 3 columnas por defecto "Por hacer"/"En progreso"/"Hecho"), `CreateColumnUseCase`, `RenameColumnUseCase`, `DeleteColumnUseCase` (`port/in`); `WorkspaceRepositoryPort`, `BoardRepositoryPort`, `ColumnRepositoryPort` (`port/out`) en `backend/src/main/java/com/agenda/kanban/workspace/application/port/` y `backend/src/main/java/com/agenda/kanban/board/application/port/` (depende de T026-T028)
- [X] T031 [US1] Definir los puertos de aplicación de `task`: `CreateTaskUseCase`, `UpdateTaskUseCase`, `MoveTaskUseCase`, `DeleteTaskUseCase` (`port/in`); `TaskRepositoryPort` (`port/out`) en `backend/src/main/java/com/agenda/kanban/task/application/port/` (depende de T029)
- [X] T032 [US1] Implementar `CreateWorkspaceService`, `CreateColumnService`, `RenameColumnService`, `DeleteColumnService` (valida el invariante de última columna) en `backend/src/main/java/com/agenda/kanban/workspace/application/service/` y `backend/src/main/java/com/agenda/kanban/board/application/service/` (depende de T030)
- [X] T033 [US1] Implementar `CreateTaskService`, `UpdateTaskService`, `MoveTaskService` (marca `completedAt` al mover a la columna terminal "Hecho"), `DeleteTaskService` en `backend/src/main/java/com/agenda/kanban/task/application/service/` (depende de T031)
- [X] T034 [P] [US1] Implementar el adaptador JPA de `Workspace` (`WorkspaceJpaEntity`, repositorio Spring Data, `WorkspaceRepositoryAdapter`, mapper) en `backend/src/main/java/com/agenda/kanban/workspace/infrastructure/out/persistence/` (depende de T030)
- [X] T035 [P] [US1] Implementar los adaptadores JPA de `Board`/`Column` (`BoardJpaEntity`, `ColumnJpaEntity`, repositorios, adapters, mappers) en `backend/src/main/java/com/agenda/kanban/board/infrastructure/out/persistence/` (depende de T030)
- [X] T036 [P] [US1] Implementar el adaptador JPA de `Task` (`TaskJpaEntity`, repositorio, `TaskRepositoryAdapter`, mapper) en `backend/src/main/java/com/agenda/kanban/task/infrastructure/out/persistence/` (depende de T031)
- [X] T037 [US1] Implementar `WorkspaceController` (`POST /workspaces`, creación con tablero por defecto) + DTOs en `backend/src/main/java/com/agenda/kanban/workspace/infrastructure/in/web/WorkspaceController.java` (depende de T032, T034)
- [X] T038 [US1] Implementar `BoardController` (`GET /workspaces/{workspaceId}/board`, `POST/PATCH/DELETE` de columnas) + DTOs en `backend/src/main/java/com/agenda/kanban/board/infrastructure/in/web/BoardController.java` (depende de T032, T035)
- [X] T039 [US1] Implementar `TaskController` (crear, editar, mover, eliminar tarjeta) + DTOs en `backend/src/main/java/com/agenda/kanban/task/infrastructure/in/web/TaskController.java` (depende de T033, T036)
- [X] T040 [P] [US1] Implementar `WorkspaceService` del frontend (crear/obtener el espacio de trabajo activo) en `frontend/src/app/features/workspaces/workspace.service.ts` (depende de T037)
- [X] T041 [P] [US1] Implementar `BoardService` del frontend (obtener tablero, gestionar columnas) en `frontend/src/app/features/board/board.service.ts` (depende de T038)
- [X] T042 [P] [US1] Implementar `TaskService` del frontend (crear/editar/mover/eliminar tarjeta) en `frontend/src/app/features/board/task.service.ts` (depende de T039)
- [X] T043 [US1] Implementar `BoardComponent` con columnas y arrastrar-y-soltar de tarjetas (`cdkDropList`/`cdkDrag` de Angular CDK), reflejando el movimiento en menos de 1s (SC-002) en `frontend/src/app/features/board/board.component.ts` y `.html` (depende de T041)
- [X] T044 [US1] Implementar `TaskCardComponent` (crear, editar título/descripción, eliminar con confirmación) en `frontend/src/app/features/board/task-card/task-card.component.ts` (depende de T042, T043)
- [X] T045 [US1] Implementar la gestión de columnas (crear/renombrar/eliminar, bloqueando la eliminación de la última columna) en `frontend/src/app/features/board/column-manager/column-manager.component.ts` (depende de T041, T043)
- [X] T046 [US1] Conectar la ruta `/board` del espacio de trabajo activo, creando automáticamente el primer espacio de trabajo si el usuario no tiene ninguno, en `frontend/src/app/app.routes.ts` y `frontend/src/app/features/board/board.component.ts` (depende de T040, T043)

**Checkpoint**: User Story 1 funcional y comprobable de forma independiente.

---

## Phase 4: User Story 2 - Gestionar fechas límite y recordatorios (Priority: P2)

**Goal**: Asignar fecha límite a una tarea, verla en la agenda del espacio de trabajo y recibir recordatorios (in-app + email) antes del vencimiento.

**Independent Test**: Asignar una fecha límite a una tarea existente, verificar que aparece en la vista de agenda y que se genera un recordatorio antes del vencimiento.

- [X] T047 [P] [US2] Extender la entidad de dominio `Task` añadiendo `dueAt` opcional (nullable) y `reminderLeadMinutes` opcional (si `dueAt` está definido y este campo es nulo, se usa el valor por defecto 24h = 1440 min) y el método derivado `isOverdue()` = `dueAt` existe, `dueAt < now()` y `completedAt` es nulo, en `backend/src/main/java/com/agenda/kanban/task/domain/model/Task.java` (depende de T029)
- [X] T048 [P] [US2] Crear la entidad de dominio `Reminder` (id, `taskId`, `triggerAt` = `dueAt - reminderLeadMinutes` obligatorio, `status` en `PENDING`/`SENT`/`CANCELLED`/`FAILED`, `channels` con `IN_APP`/`EMAIL`, `sentAt` nullable) con las transiciones `PENDING → SENT/CANCELLED/FAILED` documentadas en `data-model.md` en `backend/src/main/java/com/agenda/kanban/reminder/domain/model/Reminder.java`
- [X] T049 [P] [US2] Crear la entidad de dominio `Notification` (id, `userId`, `reminderId`, `message` generado, `readAt` nullable, `createdAt` automático) en `backend/src/main/java/com/agenda/kanban/notification/domain/model/Notification.java`
- [X] T050 [US2] Definir los puertos de aplicación de `reminder`/`notification`: `SetTaskDueDateUseCase`, `CancelReminderUseCase`, `DispatchDueRemindersUseCase` (`port/in`); `ReminderRepositoryPort`, `NotificationRepositoryPort`, `EmailSenderPort` (`port/out`) en `backend/src/main/java/com/agenda/kanban/reminder/application/port/` y `backend/src/main/java/com/agenda/kanban/notification/application/port/` (depende de T047-T049)
- [X] T051 [US2] Implementar `SetTaskDueDateService` (recalcula el `Reminder.triggerAt` o cancela el recordatorio previo si `dueAt` cambia o se elimina, según el escenario US2-AS4) y `CancelReminderService` en `backend/src/main/java/com/agenda/kanban/reminder/application/service/` (depende de T050)
- [X] T052 [US2] Implementar `DispatchDueRemindersService` (procesa de forma idempotente los recordatorios `PENDING` cuyo `triggerAt` ya se alcanzó, crea la `Notification` in-app, envía el email vía `EmailSenderPort` y marca `SENT`/`FAILED`) en `backend/src/main/java/com/agenda/kanban/reminder/application/service/DispatchDueRemindersService.java` (depende de T050)
- [X] T053 [P] [US2] Implementar el adaptador JPA de `Reminder` en `backend/src/main/java/com/agenda/kanban/reminder/infrastructure/out/persistence/` (depende de T050)
- [X] T054 [P] [US2] Implementar el adaptador JPA de `Notification` en `backend/src/main/java/com/agenda/kanban/notification/infrastructure/out/persistence/` (depende de T050)
- [X] T055 [P] [US2] Implementar `EmailSenderAdapter` (implementa `EmailSenderPort` usando el `JavaMailSender` configurado en T019) en `backend/src/main/java/com/agenda/kanban/notification/infrastructure/out/mail/EmailSenderAdapter.java` (depende de T050)
- [X] T056 [US2] Implementar `ReminderSchedulerAdapter` (`@Scheduled` con fixed-delay ~60s que invoca `DispatchDueRemindersUseCase`) en `backend/src/main/java/com/agenda/kanban/reminder/infrastructure/out/scheduling/ReminderSchedulerAdapter.java` (depende de T052)
- [X] T057 [US2] Extender `TaskController` con `PATCH` de `dueAt`/`reminderLeadMinutes` (invoca `SetTaskDueDateUseCase`) en `backend/src/main/java/com/agenda/kanban/task/infrastructure/in/web/TaskController.java` (depende de T039, T051)
- [X] T058 [US2] Implementar `AgendaController` con `GET /workspaces/{workspaceId}/agenda` en `backend/src/main/java/com/agenda/kanban/task/infrastructure/in/web/AgendaController.java` (depende de T036)
- [X] T059 [US2] Implementar `NotificationController` (`GET /notifications`, `PATCH /notifications/{id}/read`) en `backend/src/main/java/com/agenda/kanban/notification/infrastructure/in/web/NotificationController.java` (depende de T054)
- [X] T060 [P] [US2] Implementar `AgendaService` y `AgendaComponent` del frontend (lista por espacio de trabajo ordenada por `dueAt`, distingue tareas vencidas) en `frontend/src/app/features/agenda/agenda.service.ts` y `agenda.component.ts` (depende de T058)
- [X] T061 [P] [US2] Implementar `NotificationService` del frontend (*short polling* cada 30-60s) y `NotificationsPanelComponent` en `frontend/src/app/features/notifications/` (depende de T059)
- [X] T062 [US2] Extender `TaskCardComponent` con el formulario de fecha límite/antelación de recordatorio y el badge visual de "vencida" en `frontend/src/app/features/board/task-card/task-card.component.ts` (depende de T044, T057)

**Checkpoint**: User Story 1 y 2 funcionan, cada una de forma independiente.

---

## Phase 5: User Story 3 - Separar el trabajo por espacios de trabajo (Priority: P3)

**Goal**: Crear, renombrar, eliminar y cambiar entre espacios de trabajo (pestañas) manteniendo el tablero y la agenda aislados por espacio, más una vista de agenda global.

**Independent Test**: Crear dos espacios de trabajo distintos, añadir tareas separadas en cada uno y verificar que el tablero/agenda mostrados cambian al alternar entre pestañas.

- [X] T063 [US3] Extender `WorkspaceController` con `GET /workspaces` (listar), `PATCH /workspaces/{id}` (renombrar) y `DELETE /workspaces/{id}` (soft delete con confirmación) en `backend/src/main/java/com/agenda/kanban/workspace/infrastructure/in/web/WorkspaceController.java` (depende de T037, T051)
- [X] T064 [US3] Implementar `DeleteWorkspaceService` (archiva el workspace y cancela, vía `CancelReminderUseCase`, los recordatorios `PENDING` de sus tareas) y `RenameWorkspaceService` en `backend/src/main/java/com/agenda/kanban/workspace/application/service/` (depende de T032, T051)
- [X] T065 [US3] Implementar `GetGlobalAgendaUseCase`/servicio que agrega `Task.dueAt` de todos los `Workspace` no archivados del usuario, incluyendo `workspaceName` (FR-015) en `backend/src/main/java/com/agenda/kanban/task/application/service/GetGlobalAgendaService.java` (depende de T036)
- [X] T066 [US3] Extender `AgendaController` con `GET /agenda/global` en `backend/src/main/java/com/agenda/kanban/task/infrastructure/in/web/AgendaController.java` (depende de T065)
- [X] T067 [P] [US3] Implementar la gestión de pestañas del frontend (listar/crear/renombrar/eliminar con confirmación, FR-012) en `frontend/src/app/features/workspaces/workspace-tabs.component.ts` (depende de T040, T063)
- [X] T068 [US3] Implementar el cambio de espacio de trabajo activo, recargando el tablero y la agenda del espacio seleccionado sin mezclar datos entre pestañas (SC-004) en `frontend/src/app/features/workspaces/workspace.service.ts` y `frontend/src/app/core/state/active-workspace.store.ts` (depende de T067)
- [X] T069 [P] [US3] Implementar la vista de agenda global del frontend, identificando el espacio de trabajo de cada tarea (SC-007) en `frontend/src/app/features/agenda/global-agenda.component.ts` (depende de T060, T066)

**Checkpoint**: Las 3 historias de usuario funcionan de forma independiente y en conjunto.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Mejoras que afectan a varias historias de usuario.

- [X] T070 [P] Revisar el contrato OpenAPI generado (Swagger UI) frente a `specs/001-kanban-agenda-workspaces/contracts/api-contracts.md` y actualizar el documento si difiere
- [X] T071 [P] Añadir índices de rendimiento (`tasks.due_at`, `reminders.trigger_at`) vía `backend/src/main/resources/db/migration/V2__indexes.sql`
- [X] T072 [P] Revisión de accesibilidad y diseño responsivo de `BoardComponent` y `WorkspaceTabsComponent` en `frontend/src/app/features/board/` y `frontend/src/app/features/workspaces/`
- [X] T073 Hardening de seguridad: limitar la tasa de intentos en `POST /auth/login` y gestionar expiración/rotación del refresh token en `backend/src/main/java/com/agenda/kanban/auth/infrastructure/in/web/AuthController.java` y `backend/src/main/java/com/agenda/kanban/config/SecurityConfig.java`
- [X] T074 [P] Añadir logging estructurado en los puntos clave (creación/movimiento de tarea, disparo de recordatorio) en `backend/src/main/java/com/agenda/kanban/task/infrastructure/in/web/TaskController.java` y `backend/src/main/java/com/agenda/kanban/reminder/infrastructure/out/scheduling/ReminderSchedulerAdapter.java`
- [X] T075 Ejecutar manualmente los escenarios A, B y C de `quickstart.md` de extremo a extremo y corregir cualquier discrepancia encontrada — **EJECUTADA end-to-end** (Docker Desktop se pudo arrancar en un intento posterior: `docker compose up -d` con PostgreSQL 16 + Mailhog reales, backend levantado con `mvn spring-boot:run` contra esa base de datos). Validado mediante un script Python (`requests`) que ejercita la API real: registro/login, creación de workspace con columnas por defecto, crear/mover/editar/eliminar tarjetas, invariante de última columna (409), asignar fecha límite y esperar el disparo real del scheduler (~75s) → notificación in-app generada y **email real recibido en Mailhog** (asunto "Recordatorio: Entrega urgente", cuerpo y destinatario correctos), tarea marcada `overdue` tras vencer, segundo workspace con aislamiento de datos verificado (tablero de un cliente no expone tareas del otro), agenda global combinando ambos workspaces con `workspaceName`, renombrar/eliminar workspace (soft delete) con cancelación en cascada de sus tareas/recordatorios, y aislamiento entre usuarios distintos (403 al intentar leer el tablero de otro usuario). Sin discrepancias reales encontradas: los 4 fallos iniciales del script de validación eran falsos positivos del propio script (comparación incorrecta del JSON de Mailhog; expectativa equivocada sobre el invariante de columnas — la regla es "no eliminar la única columna del tablero", no "columna no vacía"; el borrado de una columna con tarjetas cascada intencionalmente a nivel de BD y está protegido por confirmación explícita en el frontend, `column-manager.component.ts`, cumpliendo FR-012).

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias — puede empezar de inmediato.
- **Foundational (Phase 2)**: Depende de Setup — BLOQUEA todas las historias de usuario.
- **User Stories (Phase 3+)**: Todas dependen de completar Foundational.
  - US1 (T026-T046) no depende de otras historias.
  - US2 (T047-T062) reutiliza `Task` (T029) y `TaskController` (T039) de US1, pero es comprobable de forma independiente en cuanto US1 existe.
  - US3 (T063-T069) reutiliza `Workspace`/`WorkspaceController` (T026, T037) y `DispatchDueRemindersUseCase`/`CancelReminderUseCase` (T050-T051) de US1/US2, pero su propia funcionalidad (listar/renombrar/eliminar/cambiar pestañas, agenda global) es comprobable de forma independiente.
- **Polish (Phase 6)**: Depende de que las historias deseadas estén completas.

### Within Each User Story

- Entidades de dominio antes que puertos de aplicación.
- Puertos de aplicación antes que sus implementaciones (`service/`).
- Servicios de aplicación y adaptadores de salida (`infrastructure/out/`) antes que los adaptadores de entrada (`infrastructure/in/web`) que los invocan.
- Backend (API) antes que los servicios/componentes del frontend que la consumen.

### Parallel Opportunities

- Todas las tareas [P] de Setup (T002, T003, T005-T007) pueden ejecutarse en paralelo.
- En Foundational, T009-T011, T013-T014, T017-T020, T022, T024 son [P] entre sí (archivos distintos).
- Dentro de US1: T026-T029 (entidades de dominio) en paralelo; T034-T036 (adaptadores JPA) en paralelo; T040-T042 (servicios HTTP del frontend) en paralelo.
- Dentro de US2: T047-T049 (entidades de dominio) en paralelo; T053-T055 (adaptadores) en paralelo; T060-T061 (frontend) en paralelo.
- Dentro de US3: T067 y T069 son [P] entre sí.

---

## Parallel Example: User Story 1

```bash
# Lanzar juntas las entidades de dominio de User Story 1:
Task: "Crear la entidad de dominio Workspace en backend/src/main/java/com/agenda/kanban/workspace/domain/model/Workspace.java"
Task: "Crear la entidad de dominio Board en backend/src/main/java/com/agenda/kanban/board/domain/model/Board.java"
Task: "Crear la entidad de dominio Column en backend/src/main/java/com/agenda/kanban/board/domain/model/Column.java"
Task: "Crear la entidad de dominio Task en backend/src/main/java/com/agenda/kanban/task/domain/model/Task.java"

# Lanzar juntos los adaptadores JPA de User Story 1:
Task: "Implementar el adaptador JPA de Workspace en backend/src/main/java/com/agenda/kanban/workspace/infrastructure/out/persistence/"
Task: "Implementar los adaptadores JPA de Board/Column en backend/src/main/java/com/agenda/kanban/board/infrastructure/out/persistence/"
Task: "Implementar el adaptador JPA de Task en backend/src/main/java/com/agenda/kanban/task/infrastructure/out/persistence/"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1: Setup
2. Completar Phase 2: Foundational (CRÍTICO — bloquea todas las historias)
3. Completar Phase 3: User Story 1
4. **DETENERSE y VALIDAR**: probar User Story 1 de forma independiente (escenario A de `quickstart.md`)
5. Desplegar/demostrar si está listo

### Incremental Delivery

1. Setup + Foundational → base lista
2. Añadir User Story 1 → validar de forma independiente → demo (¡MVP!)
3. Añadir User Story 2 → validar de forma independiente → demo
4. Añadir User Story 3 → validar de forma independiente → demo
5. Cada historia añade valor sin romper las anteriores

---

## Notes

- [P] = archivos distintos, sin dependencias pendientes
- La etiqueta [Story] mapea cada tarea a su historia de usuario para trazabilidad
- Cada historia de usuario es completable y comprobable de forma independiente
- Hacer commit tras cada tarea o grupo lógico de tareas
- Se puede detener en cualquier checkpoint para validar una historia de forma independiente
- Respetar siempre la regla de dependencia hexagonal: `infrastructure` → `application` → `domain`
