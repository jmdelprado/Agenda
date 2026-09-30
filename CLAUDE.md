guarda el # Agenda — Flujo de trabajo con ramas

Repositorio: https://github.com/jmdelprado/Agenda

## Ramas

- `develop`: rama de integración. Base de todo el desarrollo.
- `produccion`: rama de lo que se despliega en Netlify. Solo se toca cuando el usuario lo indica.
- `main`: rama inicial del repo. No se usa para el flujo de trabajo salvo que el usuario lo pida.

## Reglas

1. **Desarrollos y mejoras**: se crean desde `develop` en ramas `feature/<nombre-corto>` y se integran de vuelta en `develop`.
2. **Correcciones de subidas erróneas**: se crean desde `develop` en ramas `fix/<nombre-corto>` y se integran en `develop`, nunca directamente en `produccion`.
3. **`produccion`**: solo recibe volcados (merge) de `develop` cuando el usuario lo pida de forma explícita. Nunca hacer commits directos ni push a `produccion` por iniciativa propia.
4. Lo que hay en `produccion` es lo que se despliega en Netlify, así que cualquier cambio ahí tiene impacto real.

## Cómo actuar

- Antes de empezar un desarrollo, partir de `develop` actualizado (`git checkout develop && git pull`).
- Nombres de ramas en minúsculas y con guiones: `feature/recordatorios-email`, `fix/error-fecha-tarea`.
- No hacer push, merge ni force-push sin que el usuario lo haya pedido. Ante la duda sobre a qué rama va un cambio, preguntar.
- Nunca reescribir historial (`--force`, `reset --hard`) en `develop` ni `produccion`.
- Commits: mensajes claros y descriptivos.

## Planificación (specs)

Las funcionalidades se planifican en `specs/` (metodología spec-kit). Antes de desarrollar, leer la spec y las tareas correspondientes.

- `specs/001-kanban-agenda-workspaces/`: tablero Kanban, agenda con recordatorios y espacios de trabajo (backend Spring Boot hexagonal + frontend Angular).
- `specs/002-importar-eventos-x/`: **pendiente de desarrollar**. Leer cuentas de X (5-6), detectar eventos con fecha y crearlos como tareas con `dueAt` en el calendario de la agenda. Fase 1 asistida desde Chrome (sin cambios de backend); fase 2 opcional con automatización. Rama: `feature/importar-eventos-x`.

Reglas para esta feature: no inventar fechas (los casos ambiguos se preguntan al usuario), pedir confirmación antes de crear tareas y respetar las condiciones de uso de X.
