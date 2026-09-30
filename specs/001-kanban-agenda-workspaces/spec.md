# Feature Specification: Tablero Kanban con Agenda y Espacios de Trabajo

**Feature Branch**: `001-kanban-agenda-workspaces`

**Created**: 2026-09-22

**Status**: Draft

**Input**: User description: "Quiero crear una herramienta de productividad que mezcle un tablero Kanban estilo Trello con una agenda para gestionar fechas límite y recordatorios. Debe incluir un sistema de pestañas o espacios de trabajo para poder separar visualmente las tareas de un cliente u otro. El backend debe exponer una API REST construida con Spring Boot, y el frontend debe ser una SPA desarrollada en Angular."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Gestionar tareas en un tablero Kanban (Priority: P1)

Como usuario, quiero crear, organizar y mover tarjetas de tareas a través de columnas de un tablero Kanban (por ejemplo, "Por hacer", "En progreso", "Hecho"), para poder visualizar de un vistazo el estado de mi trabajo.

**Why this priority**: Es el núcleo de la herramienta: sin un tablero Kanban funcional no existe producto. Debe poder usarse de forma independiente como MVP.

**Independent Test**: Se puede probar por completo creando un espacio de trabajo por defecto, añadiendo tarjetas, moviéndolas entre columnas y editando/eliminándolas, sin necesitar la agenda ni múltiples espacios de trabajo.

**Acceptance Scenarios**:

1. **Given** un tablero vacío, **When** el usuario crea una nueva tarjeta con un título, **Then** la tarjeta aparece en la primera columna del tablero.
2. **Given** una tarjeta existente en la columna "Por hacer", **When** el usuario la arrastra (o mueve) a la columna "En progreso", **Then** la tarjeta pasa a mostrarse en la nueva columna y su estado queda actualizado.
3. **Given** una tarjeta existente, **When** el usuario edita su título, descripción o la elimina, **Then** los cambios se reflejan inmediatamente en el tablero.
4. **Given** un tablero, **When** el usuario crea, renombra o elimina una columna, **Then** el tablero refleja la nueva estructura de columnas.

---

### User Story 2 - Gestionar fechas límite y recordatorios (Priority: P2)

Como usuario, quiero asignar una fecha límite a una tarea y recibir recordatorios antes de que venza, para no perder plazos importantes con mis clientes.

**Why this priority**: Es la característica diferencial que combina el tablero con una agenda; aporta valor directo pero depende de que existan tareas (User Story 1).

**Independent Test**: Se puede probar de forma independiente asignando una fecha límite a una tarea existente, verificando que aparece en una vista de agenda/calendario y que se genera un recordatorio antes del vencimiento.

**Acceptance Scenarios**:

1. **Given** una tarea sin fecha límite, **When** el usuario le asigna una fecha y hora límite, **Then** la tarea aparece reflejada en la vista de agenda en la fecha correspondiente.
2. **Given** una tarea con fecha límite próxima, **When** se alcanza el momento configurado de aviso, **Then** el usuario recibe un recordatorio.
3. **Given** una tarea vencida sin completar, **When** el usuario consulta el tablero o la agenda, **Then** la tarea se distingue visualmente como vencida.
4. **Given** una tarea con fecha límite, **When** el usuario modifica o elimina la fecha, **Then** la agenda y los recordatorios asociados se actualizan o cancelan en consecuencia.

---

### User Story 3 - Separar el trabajo por espacios de trabajo (Priority: P3)

Como usuario que gestiona varios clientes o proyectos, quiero crear distintos espacios de trabajo (pestañas) y cambiar entre ellos, para mantener visualmente separadas las tareas y la agenda de cada cliente.

**Why this priority**: Añade organización a gran escala sobre las funcionalidades base (tablero y agenda); es muy valiosa para el caso de uso multi-cliente pero el producto ya es útil con un único espacio de trabajo implícito.

**Independent Test**: Se puede probar de forma independiente creando dos espacios de trabajo distintos, añadiendo tareas separadas en cada uno, y verificando que el tablero y la agenda mostrados cambian al alternar entre pestañas.

**Acceptance Scenarios**:

1. **Given** el usuario en la herramienta, **When** crea un nuevo espacio de trabajo con un nombre, **Then** se genera un tablero Kanban y una agenda vacíos y aislados para ese espacio.
2. **Given** varios espacios de trabajo existentes, **When** el usuario cambia de pestaña, **Then** el tablero y la agenda mostrados corresponden únicamente a las tareas de ese espacio.
3. **Given** un espacio de trabajo existente, **When** el usuario lo renombra o elimina, **Then** el cambio se refleja en la lista de pestañas y (en el caso de eliminación) sus tareas dejan de mostrarse.
4. **Given** varios espacios de trabajo, **When** el usuario abre la vista de agenda global, **Then** puede ver un resumen combinado de las fechas límite próximas de todos los espacios de trabajo, indicando a qué espacio pertenece cada tarea.

---

### Edge Cases

- ¿Qué ocurre si el usuario intenta eliminar un espacio de trabajo que contiene tareas con fechas límite futuras y recordatorios pendientes?
- ¿Qué ocurre si el usuario elimina la última columna del tablero mientras contiene tarjetas?
- ¿Cómo se muestra una tarea cuya fecha límite ya pasó pero la tarjeta sigue en la columna "Hecho" (completada a tiempo) frente a una que sigue en "Por hacer" (vencida sin completar)?
- ¿Qué ocurre si el usuario no tiene conexión en el momento en que debería dispararse un recordatorio?
- ¿Qué sucede si dos tareas en espacios de trabajo distintos tienen la misma fecha límite?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE permitir crear, editar, mover y eliminar tarjetas de tareas dentro de columnas de un tablero Kanban.
- **FR-002**: El sistema DEBE permitir crear, renombrar, reordenar y eliminar columnas dentro de un tablero.
- **FR-003**: El sistema DEBE permitir asignar, modificar y eliminar una fecha (y hora) límite a cualquier tarea.
- **FR-004**: El sistema DEBE mostrar una vista de agenda/calendario con las tareas organizadas según su fecha límite.
- **FR-005**: El sistema DEBE generar recordatorios asociados a las fechas límite de las tareas y notificar al usuario antes del vencimiento.
- **FR-006**: El sistema DEBE permitir al usuario configurar con cuánta antelación desea recibir un recordatorio para una tarea.
- **FR-007**: El sistema DEBE distinguir visualmente las tareas vencidas (fecha límite pasada sin completar) de las que están al día.
- **FR-008**: El sistema DEBE permitir crear, renombrar y eliminar espacios de trabajo (pestañas).
- **FR-009**: El sistema DEBE aislar los datos (tareas, columnas, fechas límite, recordatorios) de cada espacio de trabajo, de modo que no se mezclen visualmente entre pestañas.
- **FR-010**: El sistema DEBE permitir al usuario cambiar de un espacio de trabajo a otro y reflejar inmediatamente el tablero y la agenda correspondientes.
- **FR-011**: El sistema DEBE conservar el estado (columna, fecha límite, contenido) de cada tarea al recargar la aplicación o volver a un espacio de trabajo.
- **FR-012**: El sistema DEBE impedir la pérdida accidental de datos al eliminar un espacio de trabajo, columna o tarea, solicitando confirmación previa.
- **FR-013**: El sistema DEBE ser de uso individual: cada cuenta de usuario tiene acceso únicamente a sus propios espacios de trabajo, tableros y agenda; no se requiere colaboración multi-usuario, asignación de tareas a terceros ni roles/permisos en esta versión.
- **FR-014**: El sistema DEBE entregar los recordatorios tanto mediante notificaciones dentro de la aplicación como por correo electrónico.
- **FR-015**: El sistema DEBE ofrecer una vista de agenda global que combine y muestre las fechas límite próximas de todos los espacios de trabajo del usuario, identificando a qué espacio pertenece cada tarea.

### Key Entities

- **Espacio de trabajo (Workspace)**: Representa el contexto de un cliente o proyecto; agrupa de forma aislada un tablero Kanban y una agenda propios. Atributos clave: nombre, fecha de creación.
- **Tablero (Board)**: Conjunto ordenado de columnas asociado a un espacio de trabajo.
- **Columna (Column)**: Etapa del flujo de trabajo (p. ej. "Por hacer", "En progreso", "Hecho") que contiene tarjetas; tiene un nombre y una posición/orden.
- **Tarjeta/Tarea (Card/Task)**: Unidad de trabajo con título, descripción, columna actual, fecha límite opcional y estado (vencida o al día). Pertenece a un único espacio de trabajo.
- **Recordatorio (Reminder)**: Aviso vinculado a la fecha límite de una tarea, con una antelación configurable y un estado (pendiente/enviado).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un usuario nuevo puede crear un espacio de trabajo, un tablero con columnas y su primera tarea en menos de 3 minutos sin ayuda externa.
- **SC-002**: El 95% de las acciones de mover una tarjeta entre columnas se reflejan visualmente en menos de 1 segundo.
- **SC-003**: El 100% de las tareas con fecha límite configurada generan su recordatorio en el momento de antelación indicado, sin recordatorios perdidos.
- **SC-004**: Un usuario que gestiona 5 espacios de trabajo distintos puede alternar entre ellos y confirmar visualmente que las tareas mostradas corresponden solo al espacio activo, sin errores de mezcla de datos, en el 100% de los casos.
- **SC-005**: El 90% de los usuarios identifican correctamente, a simple vista, qué tareas están vencidas frente a las que están al día.
- **SC-006**: El sistema conserva el 100% de los datos (tareas, columnas, fechas límite) tras recargar la aplicación o cerrar y volver a abrir sesión.
- **SC-007**: Un usuario con varios espacios de trabajo puede identificar en la vista de agenda global, en menos de 10 segundos, cuáles son sus 3 próximas fechas límite y a qué cliente/espacio pertenece cada una.

## Assumptions

- El backend se implementará como una API REST con Spring Boot y el frontend como una SPA en Angular, tal y como especifica el usuario; estas son restricciones técnicas dadas, no decisiones de esta especificación.
- Cada espacio de trabajo tiene un conjunto de columnas por defecto ("Por hacer", "En progreso", "Hecho") que el usuario puede personalizar.
- Los recordatorios se calculan a partir de una antelación configurable por tarea, con un valor por defecto razonable (p. ej. 24 horas antes) si el usuario no especifica uno.
- No se asume integración con calendarios externos (Google Calendar, Outlook) en esta primera versión; la agenda es una vista propia de la herramienta.
- El acceso a la herramienta requiere que el usuario haya iniciado sesión (autenticación estándar), aunque el detalle del mecanismo de autenticación no es objeto de esta especificación.
