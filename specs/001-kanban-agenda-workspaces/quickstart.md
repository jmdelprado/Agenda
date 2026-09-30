# Quickstart: Tablero Kanban con Agenda y Espacios de Trabajo

Guía para levantar el entorno y validar manualmente los 3 flujos principales de la feature (US1-US3 en `spec.md`) de extremo a extremo. No sustituye a los tests automatizados definidos en `contracts/api-contracts.md`; es la comprobación de que el sistema funciona como un todo.

## Prerrequisitos

- JDK 21
- Node.js 20+ y Angular CLI 18
- Docker (para PostgreSQL local y, opcionalmente, para el propio backend)
- Un servidor SMTP de pruebas (p. ej. Mailhog/Mailpit en local) para verificar el envío de recordatorios por email sin usar un proveedor real

## 1. Levantar dependencias

```bash
docker run -d --name kanban-agenda-db -e POSTGRES_DB=kanban_agenda -e POSTGRES_USER=app -e POSTGRES_PASSWORD=app -p 5432:5432 postgres:16
docker run -d --name kanban-agenda-smtp -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

## 2. Arrancar el backend

```bash
cd backend
./mvnw spring-boot:run
```

Verifica que Flyway aplicó las migraciones y que `http://localhost:8080/swagger-ui.html` muestra el contrato OpenAPI (ver `contracts/api-contracts.md`).

## 3. Arrancar el frontend

```bash
cd frontend
npm install
npm start
```

Abre `http://localhost:4200`.

## 4. Escenarios de validación (mapeados a Acceptance Scenarios de spec.md)

### Escenario A — Tablero Kanban (User Story 1)

1. Regístrate/inicia sesión.
2. Al entrar por primera vez se crea un espacio de trabajo con tablero y columnas por defecto ("Por hacer", "En progreso", "Hecho").
3. Crea una tarjeta con título → **esperado**: aparece en "Por hacer".
4. Arrastra la tarjeta a "En progreso" → **esperado**: cambia de columna al soltarla, sin recargar la página, en menos de 1s (SC-002).
5. Edita el título de la tarjeta y elimínala → **esperado**: los cambios se reflejan de inmediato.

### Escenario B — Agenda y recordatorios (User Story 2)

1. Crea una tarjeta y asígnale una fecha límite cercana (p. ej. dentro de 2 minutos) con antelación de recordatorio de 1 minuto.
2. Abre la vista de agenda del espacio de trabajo → **esperado**: la tarea aparece en la fecha asignada.
3. Espera al momento del recordatorio → **esperado**: aparece una notificación in-app (panel de notificaciones, vía polling) y llega un correo a Mailhog (`http://localhost:8025`).
4. Deja pasar la fecha límite sin completar la tarea → **esperado**: se marca visualmente como vencida en el tablero y en la agenda (FR-007).
5. Cambia o elimina la fecha límite de una tarea con recordatorio pendiente → **esperado**: el recordatorio anterior se cancela y no se envía.

### Escenario C — Espacios de trabajo / pestañas (User Story 3)

1. Crea un segundo espacio de trabajo (p. ej. "Cliente B").
2. Añade tarjetas distintas en cada espacio de trabajo.
3. Alterna entre pestañas → **esperado**: el tablero y la agenda mostrados corresponden solo al espacio activo, sin mezclar tarjetas (SC-004).
4. Abre la vista de agenda global (`/agenda/global`) → **esperado**: se listan las fechas límite próximas de ambos espacios de trabajo, cada una identificando a qué cliente pertenece (FR-015, SC-007).
5. Elimina uno de los espacios de trabajo con tareas y recordatorios pendientes → **esperado**: se pide confirmación (FR-012) y, tras confirmar, sus tareas dejan de mostrarse y los recordatorios pendientes se cancelan.

## 5. Ejecutar los tests automatizados

```bash
cd backend && ./mvnw test
cd frontend && npm test && npx cypress run
```

## Referencias

- Modelo de datos: [data-model.md](./data-model.md)
- Contrato de la API: [contracts/api-contracts.md](./contracts/api-contracts.md)
- Decisiones técnicas y alternativas evaluadas: [research.md](./research.md)
