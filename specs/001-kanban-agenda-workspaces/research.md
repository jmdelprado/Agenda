# Research: Tablero Kanban con Agenda y Espacios de Trabajo

Todos los puntos del *Technical Context* quedan resueltos aquí (no quedan `NEEDS CLARIFICATION`).

## 1. Base de datos

- **Decision**: PostgreSQL 16, acceso vía Spring Data JPA + Hibernate.
- **Rationale**: El dominio es fuertemente relacional (usuario → espacios de trabajo → tableros → columnas → tarjetas → recordatorios, todo con integridad referencial y aislamiento por espacio de trabajo requerido por FR-009). PostgreSQL ofrece transacciones ACID, tipos de fecha/hora robustos (necesarios para fechas límite y recordatorios) y es el estándar de facto en el ecosistema Spring Boot.
- **Alternatives considered**: MongoDB (rechazado: el aislamiento estricto y las relaciones padre-hijo se modelan mejor con FK e integridad referencial que con documentos); MySQL (alternativa viable, pero PostgreSQL tiene mejor soporte de tipos temporales y extensibilidad futura).

## 2. Autenticación

- **Decision**: Spring Security con autenticación basada en JWT (access token + refresh token), ya que el backend es una API REST sin estado consumida por una SPA separada.
- **Rationale**: Evita el manejo de sesiones con cookies entre dominios/orígenes distintos (backend y frontend desplegados por separado), es el patrón estándar para APIs REST + SPA, y es compatible con FR-013 (cuenta individual, sin roles/permisos complejos).
- **Alternatives considered**: Sesión de servidor con cookies (rechazado: añade complejidad de CORS/cookies cross-origin sin beneficio, dado que no hay necesidad de SSR); OAuth2/login social (fuera de alcance en esta versión, puede añadirse después sin romper el contrato JWT).

## 3. Disparo fiable de recordatorios (SC-003: 0% recordatorios perdidos)

- **Decision**: Los recordatorios se persisten en base de datos con estado (`PENDING`/`SENT`/`FAILED`) y momento de disparo calculado. Un job `@Scheduled` (fixed-delay corto, p. ej. cada 60s) consulta los recordatorios `PENDING` cuyo momento ya se alcanzó, los procesa de forma idempotente y actualiza su estado.
- **Rationale**: Al estar persistido en base de datos (no en memoria/temporizador del cliente), el recordatorio sobrevive a reinicios del backend y a que el usuario no tenga la app abierta, cumpliendo la garantía de "0 recordatorios perdidos" y el requisito de entrega por email (FR-014) incluso sin sesión activa.
- **Alternatives considered**: Temporizadores en el cliente Angular (rechazado: no dispara si la pestaña está cerrada, no cumple el canal de email); cola de mensajería dedicada tipo RabbitMQ/Kafka (rechazado por ahora: complejidad operativa innecesaria para la escala inicial de una sola instancia; revisar si el volumen de recordatorios crece significativamente).

## 4. Entrega de notificaciones in-app

- **Decision**: *Short polling*: el frontend consulta periódicamente (p. ej. cada 30-60s) un endpoint `/notifications` para refrescar el panel de notificaciones.
- **Rationale**: Simplicidad de implementación sin infraestructura adicional; suficiente para el volumen esperado (uso individual, no tiempo real crítico).
- **Alternatives considered**: WebSocket/Server-Sent Events (más inmediato pero añade complejidad de infraestructura y gestión de conexiones; se deja como mejora futura si se requiere notificación instantánea).

## 5. Drag-and-drop del tablero Kanban

- **Decision**: `@angular/cdk` (Drag and Drop) para mover tarjetas entre columnas.
- **Rationale**: Librería oficial del ecosistema Angular, ya integrada si se usa Angular Material, accesible, sin dependencias de terceros pesadas.
- **Alternatives considered**: Librerías Kanban de terceros (rechazadas: mayor acoplamiento y menor control sobre el modelo de datos propio de columnas/tarjetas).

## 6. Librería de componentes UI

- **Decision**: Angular Material.
- **Rationale**: Se integra de forma nativa con Angular CDK (ya elegido para drag-and-drop), componentes accesibles por defecto, ampliamente soportado.
- **Alternatives considered**: PrimeNG (más widgets prediseñados pero mayor peso y menor integración directa con CDK).

## 7. Documentación de la API

- **Decision**: `springdoc-openapi` para generar la especificación OpenAPI 3 y Swagger UI directamente desde los controllers Spring.
- **Rationale**: Mantiene el contrato sincronizado con el código, facilita el desarrollo paralelo del frontend contra un contrato verificable.

## 8. Migraciones de base de datos

- **Decision**: Flyway, con scripts versionados en `backend/src/main/resources/db/migration`.
- **Rationale**: Estándar en el ecosistema Spring Boot, versionado explícito y reproducible del esquema.

## 9. Estrategia de testing

- **Decision**: Backend: JUnit 5 + Spring Boot Test (tests de slice `@WebMvcTest`/`@DataJpaTest`) + Testcontainers con PostgreSQL real para tests de integración. Frontend: Jasmine/Karma (por defecto del Angular CLI) para unitarios de componentes/servicios, Cypress para los flujos E2E que cubren los escenarios de aceptación de las 3 historias de usuario (mover tarjetas, fecha límite + recordatorio, cambio de espacio de trabajo).
- **Rationale**: Cobertura en las tres capas (contrato, integración con BD real, E2E de flujos de usuario) sin sobre-invertir en infraestructura de test para una app de este tamaño.

## 10. Arquitectura del backend

- **Decision**: Arquitectura Hexagonal (Puertos y Adaptadores) por módulo de dominio: `domain/` (entidades puras) → `application/` (casos de uso + puertos de entrada/salida) → `infrastructure/` (adaptadores de entrada REST, adaptadores de salida JPA/email/scheduler), con la regla de dependencia apuntando siempre hacia el dominio.
- **Rationale**: Requisito explícito del usuario. Además, aísla las reglas de negocio (p.ej. cálculo de `overdue`, invariante "no eliminar la última columna", cancelación de recordatorios al cambiar `dueAt`) de Spring/JPA, permitiendo testear los casos de uso (`application/service`) de forma unitaria con los puertos de salida mockeados, sin contexto Spring ni base de datos — acelera el feedback de los tests unitarios frente a tests de integración con Testcontainers.
- **Alternatives considered**: Arquitectura en capas tradicional (controller → service → repository, todo acoplado a JPA) — rechazada por mandato explícito del usuario y porque mezclaría entidades JPA con lógica de dominio, dificultando el testeo aislado; Clean Architecture con más capas (use case + interactors + presenters) — rechazada por sobre-ingeniería para el tamaño actual del proyecto, los puertos de entrada/salida ya cubren la separación necesaria.

## 11. Despliegue / plataforma objetivo

- **Decision**: Backend empaquetado como imagen Docker (Linux); frontend compilado como artefactos estáticos servidos vía Nginx o un CDN.
- **Rationale**: Despliegue independiente de frontend y backend, consistente con la separación de repos/carpetas `backend/` y `frontend/`, y portable a cualquier proveedor cloud o on-premise.
