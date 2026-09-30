# Tasks: Importar eventos desde cuentas de X

**Rama**: `feature/importar-eventos-x` (desde `develop`, ver `CLAUDE.md`)
**Spec**: `specs/002-importar-eventos-x/spec.md`

## Phase 1: Preparación

- [ ] T001 Definir con el usuario: cuentas de X, tipo de eventos, periodo, workspace y columna destino
- [ ] T002 Arrancar el entorno (`docker compose up`, backend, frontend) y obtener un usuario/token de prueba
- [ ] T003 Crear en el workspace destino una columna "Eventos X"

## Phase 2: User Story 1 - Importación puntual asistida (P1) 🎯 MVP

- [ ] T004 [US1] Leer publicaciones recientes de cada cuenta desde Chrome con la sesión de X del usuario
- [ ] T005 [US1] Extraer eventos candidatos (título, fecha/hora, cuenta, URL del tweet) y marcar los ambiguos
- [ ] T006 [US1] Presentar la lista al usuario y obtener confirmación antes de crear nada
- [ ] T007 [US1] Crear tareas vía `POST /boards/columns/{columnId}/tasks` y asignar fecha con `PATCH /tasks/{id}/due-date`
- [ ] T008 [US1] Evitar duplicados comprobando tareas ya existentes (URL del tweet en la descripción)
- [ ] T009 [US1] Verificar que los eventos aparecen en el calendario de la agenda

**Checkpoint**: US1 validada de forma independiente.

## Phase 3: User Story 2 - Automatización (P2, opcional)

- [ ] T010 [US2] Decidir fuente de datos: API oficial de X u otra alternativa
- [ ] T011 [US2] Diseñar módulo backend hexagonal (`domain` → `application` → `infrastructure`) para importación programada
- [ ] T012 [US2] Persistir configuración de cuentas y registro de eventos importados (migración Flyway)
- [ ] T013 [US2] Job programado con registro de errores y sin duplicados
- [ ] T014 [US2] Pruebas y validación

## Notas

- Todo se desarrolla en `feature/importar-eventos-x` y se integra en `develop`; `produccion` solo por indicación del usuario.
- No inventar fechas: lo dudoso se pregunta al usuario.
