# Agenda — Flujo de trabajo con ramas

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
