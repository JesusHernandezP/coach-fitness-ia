# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Estado actual

Repo greenfield. Solo existen `plan.md` (fuente de verdad del diseño) e `imagenes-referenciales/` (mockups UI). No hay codigo aun. Cualquier implementacion debe seguir los tickets numerados T-00..T-27 de `plan.md`.

## Proyecto

Fitness AI Coach: app multiplataforma (web Angular + Android Compose) con backend Spring Boot. Usuario llena perfil metabolico → calcula macros (Mifflin-St Jeor) → registra peso/pasos/calorias → dashboard con graficos → chat IA rol nutricionista/entrenador (Groq).

## Stack

| Capa | Tec |
|------|-----|
| Backend | Java 21 + Spring Boot 3.3 + Spring Security (JWT) + JPA + PostgreSQL 16 |
| Docs API | springdoc-openapi → `/swagger-ui.html` |
| LLM | Groq API (`llama-3.3-70b-versatile`) |
| Web | Angular 18 + ng2-charts (Chart.js) |
| Android | Kotlin + Jetpack Compose + Retrofit + Hilt + DataStore + Vico charts |
| Infra | Docker Compose local; Render/Railway (API+DB), Vercel/Netlify (web), Google Play (AAB) |
| CI/CD | GitHub Actions (backend, web, android, release por tag SemVer) |

## Arquitectura

```
Angular web ─┐
             ├─► Spring Boot REST /api/v1/** ─► PostgreSQL
Android app ─┘        │
                      └─► Groq Chat Completions
```

Flujo clave: `PUT /profile/me` y `POST /weights` recalculan `NutritionTarget` (cache de macros). `ActivityLog` tiene `UNIQUE(userId, date)` y sus endpoints hacen upsert. Chat guarda historial por `ChatConversation` y envia al LLM system prompt con contexto del perfil + rate limit 20 msg/h/usuario.

## Estructura objetivo (ver `plan.md`)

```
backend/   # Spring Boot — Dockerfile multi-stage
web/       # Angular — Dockerfile nginx
android/   # Compose project — signing config desde env vars
.github/workflows/  # backend-ci.yml, web-ci.yml, android-ci.yml, android-release.yml
docker-compose.yml
.env.example
```

## Comandos (una vez exista el codigo)

Arranque local completo:
```
docker compose up
```
- Web: http://localhost:4200
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Postgres: localhost:5432

Backend (`backend/`):
```
./mvnw spring-boot:run          # arranca con perfil dev
./mvnw verify                   # compila + lint (Spotless) + tests + JaCoCo
./mvnw test -Dtest=NutritionCalculatorServiceTest#calculatesTdeeCorrectly
./mvnw spring-boot:build-image  # imagen Docker
```

Web (`web/`):
```
npm ci
npm start                       # ng serve
npm run lint                    # ESLint + Prettier
npm test -- --watch=false --browsers=ChromeHeadless
npm run build -- --configuration production
```

Android (`android/`):
```
./gradlew assembleDebug
./gradlew installDebug
./gradlew testDebugUnitTest
./gradlew ktlintCheck detekt
./gradlew bundleRelease         # AAB firmado (requiere env keystore)
./gradlew test --tests "com.fitnessaicoach.ProfileViewModelTest.savesProfile"
```

Emulador Android apunta al host via `10.0.2.2:8080` (configurado en `BuildConfig.API_BASE_URL` segun buildType).

## Endpoints REST (base `/api/v1`)

```
POST   /auth/register              {email, password}
POST   /auth/login                 → JWT
GET    /profile/me
PUT    /profile/me                 → persiste + recalcula targets
GET    /profile/targets            macros cacheados
POST   /weights                    {weightKg, loggedAt?}  → actualiza perfil + recalcula
GET    /weights?from=&to=
DELETE /weights/{id}
POST   /activities                 upsert por (user, date)
GET    /activities?from=&to=
GET    /activities/today
GET    /dashboard/weight-progress?days=90
GET    /dashboard/weekly-summary
GET    /dashboard/today
GET    /chat/conversations
POST   /chat/conversations
GET    /chat/conversations/{id}/messages
POST   /chat/conversations/{id}/messages
```

Todos salvo `/auth/**` y `/actuator/health` requieren `Authorization: Bearer <jwt>`.

## Logica nutricional

Mifflin-St Jeor. Hombre: `BMR = 10·kg + 6.25·cm − 5·age + 5`. Mujer: `... − 161`. TDEE = BMR · factor (sedentario 1.2 / ligero 1.375 / moderado 1.55 / activo 1.725 / muy activo 1.9). Objetivo: perder −500, mantener 0, ganar +300. Split macros por dieta (estandar 30/40/30, keto 30/10/60, vegetariano 25/50/25, ayuno 35/35/30). Proteina minimo 1.8 g/kg. Caso de referencia para test: hombre 30a, 170cm, 80kg, moderado, perder peso, estandar → `{cal≈2360, prot≈160, carbs≈286, fat≈64}`.

## Convenciones

- Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`).
- Ramas: `main` protegida (prod) · `develop` (integracion) · `feature/<ticket>`.
- PR obligatorio, CI en verde antes de merge.
- Naming: Java `PascalCase`/`camelCase`/`UPPER_SNAKE`, Angular archivos `kebab-case.component.ts`, paquetes Java minusculas.
- Codigo autodocumentado: funciones < 40 lineas, nombres claros, comentarios solo donde el "por que" no sea obvio.
- Javadoc/TSDoc en APIs publicas no triviales.
- Cobertura minima: 70% capa servicio backend.
- Lint: Spotless (Java) · ESLint+Prettier (TS) · ktlint+detekt (Kotlin).
- Errores homogeneos `{timestamp, status, error, message, path}`.
- Logs JSON en perfil `prod` (Logback).
- SemVer + `CHANGELOG.md` (Keep a Changelog).
- `@Operation`, `@Tag`, `@Schema` anotados en controllers/DTOs — Swagger es contrato.

## Secrets y configuracion

Nunca commit de `.env`, keystores, service accounts. Variables criticas: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `GROQ_API_KEY`, `APP_CORS_ALLOWED_ORIGINS`, `APP_DOCS_ENABLED`, `SPRING_PROFILES_ACTIVE`. Android release usa `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` desde env. GitHub Actions lee de Secrets; cloud (Render/Vercel) de dashboard.

## Referencias

- `plan.md` — diseño completo, tickets T-00..T-27, verificacion end-to-end.
- `imagenes-referenciales/` — mockups UI (tema oscuro + acento amarillo `#D4B200`, tarjetas `#1A1A1A`, fondo `#0F0F0F`).
