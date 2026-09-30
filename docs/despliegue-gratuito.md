# Despliegue gratuito: Netlify + Render + Neon + UptimeRobot

Frontend en Netlify, backend en Render (Docker), base de datos en Neon (Postgres) y UptimeRobot para que Render no se duerma.

## 1. Neon (base de datos)
1. Crea cuenta en https://neon.tech y un proyecto (región cercana a la de Render, p. ej. Frankfurt).
2. En el dashboard, copia los datos de conexión: host (`ep-xxxx.eu-central-1.aws.neon.tech`), base de datos, usuario y contraseña.
3. Flyway crea las tablas solo al arrancar el backend.

## 2. Render (backend)
1. https://render.com -> New -> Web Service -> conecta el repo `jmdelprado/Agenda`.
2. Rama: `produccion`. Runtime: **Docker**. Root Directory: `backend`. Instance type: **Free**.
3. Health Check Path: `/actuator/health/liveness`.
4. Variables de entorno:

| Variable | Valor |
|---|---|
| `DB_HOST` | host de Neon |
| `DB_PORT` | `5432` |
| `DB_NAME` | nombre de la BD en Neon |
| `DB_USER` | usuario de Neon |
| `DB_PASSWORD` | contraseña de Neon |
| `DB_PARAMS` | `?sslmode=require` |
| `JWT_SECRET` | cadena aleatoria larga (>= 32 bytes) |
| `FRONTEND_ORIGIN` | URL de Netlify, sin barra final (p. ej. `https://mi-agenda.netlify.app`) |

Opcionales: `SMTP_*` (recordatorios por email), `GOOGLE_*`, `GEMINI_API_KEY`.
Sin SMTP configurado los emails de recordatorio fallaran, pero la app funciona.

5. Cuando termine el deploy, anota el dominio (`https://xxxx.onrender.com`).

## 3. Netlify
En `netlify.toml` cambia `TU-BACKEND.onrender.com` por el dominio de Render y sube el cambio a `produccion`.
El navegador llama a `/api/v1/...` en Netlify y este lo reenvia a Render (sin problemas de CORS).

## 4. UptimeRobot
1. https://uptimerobot.com -> Add New Monitor -> tipo HTTP(s).
2. URL: `https://xxxx.onrender.com/actuator/health/liveness`, intervalo 5 minutos.
Esto mantiene despierto el servicio de Render. El endpoint no toca la base de datos, asi Neon puede suspenderse cuando no hay uso.

## Limitaciones
- Render free tiene 512 MB de RAM; la JVM esta limitada a `-Xmx300m`.
- Neon tarda ~1 s en despertar tras un rato sin uso.
- Los planes gratuitos cambian: revisa sus condiciones.
