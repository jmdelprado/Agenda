# Implementation Plan: Tablero Kanban con Agenda y Espacios de Trabajo

**Branch**: `001-kanban-agenda-workspaces` | **Date**: 2026-09-22 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-kanban-agenda-workspaces/spec.md`

## Summary

Herramienta de productividad de uso individual que combina un tablero Kanban (columnas y tarjetas arrastrables) con una agenda de fechas límite y recordatorios (in-app + email), organizada en espacios de trabajo aislados (uno por cliente/proyecto) más una vista de agenda global que consolida los vencimientos de todos los espacios. Enfoque técnico: backend API REST con Spring Boot (Java) sobre PostgreSQL, estructurado con Arquitectura Hexagonal (Puertos y Adaptadores) para mantener el dominio aislado de frameworks, con un job programado (adaptador de infraestructura) que garantiza el disparo fiable de recordatorios; frontend SPA en Angular consumiendo esa API, con arrastrar-y-soltar nativo de Angular CDK para el tablero.

## Technical Context

**Language/Version**: Java 21 (LTS) para el backend; TypeScript 5.x / Angular 18 para el frontend

**Primary Dependencies**: Backend — Spring Boot 3.3 (Web, Data JPA, Validation, Security, Mail, Scheduling), springdoc-openapi, Flyway. Frontend — Angular 18, Angular CDK (drag-drop), Angular Material, RxJS

**Architecture**: Backend en Arquitectura Hexagonal (Puertos y Adaptadores): dominio (entidades puras, sin anotaciones de framework) → aplicación (casos de uso + puertos de entrada/salida) → infraestructura (adaptadores de entrada REST y adaptadores de salida JPA/email/scheduler), con la regla de dependencia apuntando siempre hacia el dominio. Frontend Angular organizado por *feature modules* consumiendo la API vía servicios HTTP (sin acoplarse a la estructura interna del backend).

**Storage**: PostgreSQL 16 (datos relacionales: usuarios, espacios de trabajo, tableros, columnas, tarjetas, recordatorios, notificaciones)

**Testing**: Backend — JUnit 5 + Spring Boot Test (tests de slice) + Testcontainers (PostgreSQL) para integración. Frontend — Jasmine/Karma (unitarios, por defecto de Angular CLI) + Cypress (flujos E2E críticos: drag-and-drop, recordatorios, cambio de espacio de trabajo)

**Target Platform**: Backend contenerizado (Docker) desplegable en Linux; frontend como build estático servido vía Nginx/CDN; navegadores evergreen (últimas 2 versiones de Chrome, Firefox, Edge, Safari)

**Project Type**: Web application (frontend Angular + backend Spring Boot separados, comunicándose vía API REST)

**Performance Goals**: Movimiento de tarjetas reflejado en UI en <1s para el 95% de las acciones (SC-002); API p95 <300ms para operaciones CRUD típicas bajo carga moderada

**Constraints**: 0% de recordatorios perdidos (SC-003) → el disparo de recordatorios debe sobrevivir a reinicios del backend (persistido en base de datos, no en memoria); aislamiento estricto de datos entre espacios de trabajo (FR-009); envío de email de recordatorios depende de un servicio SMTP configurado

**Scale/Scope**: Uso individual por cuenta (FR-013); una cuenta puede gestionar decenas de espacios de trabajo, cada uno con un tablero de tamaño moderado (decenas de tarjetas); diseño inicial dimensionado para cientos-miles de cuentas, no multi-tenant compartido

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

`.specify/memory/constitution.md` contiene únicamente el template sin rellenar (sin principios ratificados). No existen gates de constitución que evaluar para esta feature. Recomendación: ejecutar `/speckit-constitution` si el equipo quiere fijar principios vinculantes antes de `/speckit-implement`; esto no bloquea la planificación actual.

**Estado**: PASS (sin gates definidos) — sin cambios tras Phase 1.

## Project Structure

### Documentation (this feature)

```text
specs/001-kanban-agenda-workspaces/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   └── api-contracts.md
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/agenda/kanban/
│   ├── {modulo}/                        # workspace, board, task, reminder, notification, auth
│   │   ├── domain/                      # CAPA DE DOMINIO — entidades puras (sin @Entity/@Service ni deps de Spring)
│   │   │   ├── model/                   # p.ej. Workspace, Board, Column, Task, Reminder (POJOs + invariantes)
│   │   │   └── exception/                # excepciones de dominio (p.ej. LastColumnCannotBeDeletedException)
│   │   ├── application/                  # CAPA DE APLICACIÓN — casos de uso y puertos
│   │   │   ├── port/in/                  # puertos de entrada: interfaces de caso de uso (p.ej. CreateTaskUseCase)
│   │   │   ├── port/out/                 # puertos de salida: interfaces que el dominio necesita (p.ej. TaskRepositoryPort, ReminderNotifierPort)
│   │   │   └── service/                  # implementación de los casos de uso (orquestan dominio + puertos de salida)
│   │   └── infrastructure/               # CAPA DE INFRAESTRUCTURA — adaptadores
│   │       ├── in/web/                   # adaptadores de entrada: @RestController, DTOs request/response, mappers DTO↔dominio
│   │       └── out/persistence/          # adaptadores de salida: @Entity JPA, Spring Data repositories, mappers dominio↔JPA que implementan los port/out
│   ├── reminder/infrastructure/out/scheduling/  # adaptador de salida: job @Scheduled que dispara ReminderDispatchUseCase
│   ├── notification/infrastructure/out/mail/    # adaptador de salida: EmailSenderPort implementado con Spring Mail
│   ├── shared/                           # tipos compartidos entre módulos (p.ej. Result/DomainException base) — sin lógica de infraestructura
│   └── config/                           # configuración Spring transversal (seguridad, CORS, OpenAPI, mail, scheduling) — es infraestructura, no dominio
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/      # scripts Flyway (V1__init.sql, ...)
└── src/test/java/com/agenda/kanban/
    ├── contract/           # tests de contrato de los adaptadores REST (MockMvc/WebTestClient) por endpoint
    ├── integration/        # tests de integración de adaptadores de salida (JPA con Testcontainers) y de casos de uso completos
    └── unit/                # tests unitarios del dominio y de los casos de uso (application/service), con los puertos de salida mockeados

frontend/
├── src/app/
│   ├── core/               # interceptores HTTP (JWT), guards de auth, servicios base
│   ├── shared/             # componentes/pipes reutilizables (tarjeta, badge vencida, etc.)
│   ├── features/
│   │   ├── auth/           # login/registro
│   │   ├── workspaces/     # listado y gestión de pestañas/espacios de trabajo
│   │   ├── board/           # tablero Kanban (columnas, drag-and-drop de tarjetas)
│   │   ├── agenda/          # agenda por espacio + vista de agenda global
│   │   └── notifications/  # panel de notificaciones in-app
│   └── app.routes.ts
├── src/environments/
└── e2e/                     # specs Cypress
```

**Structure Decision**: Aplicación web con separación clara backend/frontend (Opción 2), tal como exige el usuario. `backend/` expone la API REST con Spring Boot organizada primero por módulo de dominio (workspace, board, task, reminder, notification, auth) y, dentro de cada módulo, por capa hexagonal (`domain/` → `application/` → `infrastructure/`), de modo que:
- La capa de **dominio** (entidades puras: `Workspace`, `Board`, `Column`, `Task`, `Reminder`, `Notification`, `User` de `data-model.md`) no depende de Spring ni de JPA — son POJOs con sus invariantes y reglas de negocio.
- La capa de **aplicación** define los casos de uso (puertos de entrada, p.ej. `CreateTaskUseCase`, `MoveTaskUseCase`, `DispatchDueRemindersUseCase`) y los puertos de salida que esos casos de uso necesitan (p.ej. `TaskRepositoryPort`, `ReminderRepositoryPort`, `NotificationSenderPort`), sin conocer su implementación concreta.
- La capa de **infraestructura** contiene los adaptadores: de entrada (`infrastructure/in/web` — los `@RestController` de `contracts/api-contracts.md`, que solo traducen HTTP↔caso de uso) y de salida (`infrastructure/out/persistence` — entidades `@Entity`/repositorios Spring Data que implementan los puertos de salida, `infrastructure/out/mail` para el envío de email, `infrastructure/out/scheduling` para el job de recordatorios).
- La regla de dependencia es unidireccional hacia el dominio: `infrastructure` → `application` → `domain`; el dominio y la aplicación no importan clases de `infrastructure` (JPA, Spring Web, etc.), lo que permite testear los casos de uso con los puertos de salida mockeados (`src/test/.../unit`) sin levantar contexto Spring ni base de datos.

`frontend/` es la SPA Angular organizada por *feature modules*, con `core/` y `shared/` para lo transversal, y consume el backend únicamente a través de los endpoints REST expuestos por los adaptadores de entrada — no conoce ni depende de la organización hexagonal interna del backend. Ambos proyectos se despliegan y versionan de forma independiente dentro del mismo repositorio.

## Complexity Tracking

*Sin violaciones de constitución que justificar (no hay gates definidos).*
