# T-28 - Personalizacion de usuario

## Objetivo

Agregar identidad visible al usuario para que la app y la IA puedan tratarlo de forma personal sin cambiar el modelo nutricional actual.

## Alcance

- Agregar `display_name` al usuario o perfil.
- Permitir editar el nombre desde Web y Android.
- Mostrar el nombre en dashboard, perfil y chat.
- Incluir el nombre en el contexto del coach IA.

## Fuera de alcance

- Avatares.
- Preferencias avanzadas.
- RAG o memoria semantica.

## Backend

### Datos

Crear migracion Flyway:

```sql
ALTER TABLE users ADD COLUMN display_name VARCHAR(120);
```

### API

Actualizar respuestas y requests existentes sin crear endpoints nuevos salvo que sea estrictamente necesario:

- `POST /api/v1/auth/register`: aceptar `displayName` opcional.
- `GET /api/v1/profile/me`: devolver `displayName`.
- `PUT /api/v1/profile/me`: permitir actualizar `displayName`.

### Reglas

- `displayName` opcional.
- Si llega vacio, guardar `NULL` o mantener valor anterior segun endpoint.
- Longitud maxima: 120 caracteres.
- No debe usarse como identificador de seguridad.

## Web

- Agregar campo `Nombre` en perfil.
- Mostrar saludo en dashboard: `Hola, {displayName}`.
- Si no hay nombre, usar texto neutro: `Hola`.

## Android

- Agregar campo de nombre en pantalla de perfil.
- Mostrar saludo en dashboard.

## Pruebas

Backend:

- Registro con `displayName`.
- Perfil devuelve `displayName`.
- Perfil actualiza `displayName`.
- Validacion de longitud.

Web:

- Build production.
- Smoke test de perfil si existe infraestructura.

Android:

- `testDebugUnitTest`.
- `assembleDebug`.

## Criterios de aceptacion

- El usuario puede guardar su nombre.
- El nombre se ve en Web y Android.
- El chat recibe el nombre en el contexto.
- CI backend, web y android pasan segun archivos afectados.

