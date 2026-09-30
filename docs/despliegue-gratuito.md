# Despliegue gratuito: Netlify + Render + Neon + UptimeRobot

Frontend en Netlify, backend en Render (Docker), base de datos en Neon (Postgres) y UptimeRobot para que Render no se duerma.

## 1. Base de datos: Aiven (Postgres gratuito)
(Alternativas: Neon o Supabase; solo cambian las variables `DB_*`.)
1. Crea cuenta en https://aiven.io y un servicio **PostgreSQL** con el plan **Free**.
2. En la pagina del servicio copia: Host, Port, Database name (`defaultdb`), User (`avnadmin`) y Password. Ojo: el puerto NO es 5432.
3. Flyway crea las tablas solo al arrancar el backend.

## 2. Render (backend)
1. https://render.com -> New -> Web Service -> conecta el repo `jmdelprado/Agenda`.
2. Rama: `produccion`. Runtime: **Docker**. Root Directory: `backend`. Instance type: **Free**.
3. Health Check Path: `/actuator/health/liveness`.
4. Variables de entorno:

| Variable | Valor |
|---|---|
| `DB_HOST` | host de la BD |
| `DB_PORT` | puerto de la BD (en Aiven no es 5432) |
| `DB_NAME` | nombre de la BD |
| `DB_USER` | usuario de la BD |
| `DB_PASSWORD` | contraseña de la BD |
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
Esto mantiene despierto el servicio de Render. El endpoint no toca la base de datos, asi la BD no se mantiene activa sin necesidad.

## Limitaciones
- Render free tiene 512 MB de RAM; la JVM esta limitada a `-Xmx300m`.
- Los planes gratuitos cambian: revisa sus condiciones.
