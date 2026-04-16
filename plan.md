# Plan: Fitness AI Coach — App Multiplataforma

## Contexto

Aplicacion fitness multiplataforma (web + Android) con backend unico. El usuario llena un perfil metabolico, la app calcula sus objetivos nutricionales (calorias y macros), registra su progreso diario (peso, pasos, calorias quemadas), muestra un dashboard con graficos de evolucion y ofrece un chat con IA con rol de nutricionista/entrenador.

El proyecto se alojara en **GitHub**, con **CI/CD** automatizado, **Swagger/OpenAPI** para documentar la API, y sera **desplegado** en cloud (web + DB) y en **Google Play** (APK/AAB firmado).

### Stack decidido

| Capa | Tecnologia |
|------|-----------|
| Backend | Java 21 + Spring Boot 3.3 + Spring Security (JWT) + Spring Data JPA |
| DB | PostgreSQL 16 |
| LLM | Groq API (Llama 3.3 70B, plan gratuito) |
| Web | Angular 18 + TypeScript + ng2-charts (Chart.js) |
| Android | Kotlin + Jetpack Compose + Retrofit + Hilt + DataStore |
| Formula nutricional | Mifflin-St Jeor + factor actividad |
| Tracking pasos/calorias | Entrada manual |
| Contenedores | Docker + Docker Compose |
| Documentacion API | springdoc-openapi (Swagger UI) |
| Repositorio | GitHub (mono-repo) |
| CI/CD | GitHub Actions |
| Deploy backend + DB | Render / Railway / Fly.io (plan free) |
| Deploy web | Vercel / Netlify / Cloudflare Pages |
| Distribucion Android | Google Play Console (Internal Testing → Production) |

---

## Arquitectura general

```
┌──────────────────┐        ┌──────────────────┐
│  Angular 18      │        │  Android         │
│  (Vercel/Netlify)│        │  (Google Play)   │
└────────┬─────────┘        └────────┬─────────┘
         │ HTTPS/JSON + JWT          │
         └─────────────┬─────────────┘
                       ▼
              ┌──────────────────┐        ┌───────────────┐
              │ Spring Boot API  │───────▶│ Groq API      │
              │ (Render/Railway) │        │ (chat LLM)    │
              │ + Swagger UI     │        └───────────────┘
              └────────┬─────────┘
                       ▼
              ┌──────────────────┐
              │ PostgreSQL 16    │
              │ (managed cloud)  │
              └──────────────────┘
```

### Modelo de datos (entidades principales)

- `User` (id, email, passwordHash, createdAt)
- `MetabolicProfile` (userId, age, sex, heightCm, currentWeightKg, workActivity, weeklyExerciseDays, exerciseType, exerciseMinutes, dailySteps, dietType, goal, activityLevel, updatedAt)
- `NutritionTarget` (userId, calories, proteinG, carbsG, fatG, calculatedAt)
- `WeightLog` (id, userId, weightKg, loggedAt)
- `ActivityLog` (id, userId, date, steps, caloriesBurned, notes) — UNIQUE(userId, date)
- `ChatConversation` (id, userId, title, createdAt)
- `ChatMessage` (id, conversationId, role, content, createdAt)

---

## Estructura del repositorio

```
coach-fitness-ia/                    # repo Git
├── backend/                         # Spring Boot
│   ├── src/main/java/...
│   ├── src/test/java/...
│   ├── Dockerfile
│   └── pom.xml
├── web/                             # Angular
│   ├── src/
│   ├── Dockerfile
│   └── package.json
├── android/                         # Android Studio project
│   ├── app/
│   └── build.gradle.kts
├── .github/
│   └── workflows/
│       ├── backend-ci.yml
│       ├── web-ci.yml
│       └── android-ci.yml
├── docker-compose.yml               # dev local
├── docker-compose.prod.yml          # referencia prod
├── .env.example
├── .gitignore
├── CONTRIBUTING.md
├── LICENSE
└── README.md
```

---

## Buenas practicas transversales

Aplican a los 3 modulos durante toda la implementacion:

- **Commits**: Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`).
- **Ramas**: `main` (produccion, protegida), `develop` (integracion), `feature/<ticket>` por ticket.
- **Pull Requests**: PR obligatorio a `develop`/`main`, revision + CI en verde antes de merge.
- **Naming**: clases `PascalCase`, metodos/variables `camelCase`, constantes `UPPER_SNAKE`, archivos Angular `kebab-case.component.ts`, paquetes Java en minusculas.
- **Codigo autodocumentado**: nombres claros, funciones pequeñas (< 40 lineas), sin comentarios obvios. Javadoc/TSDoc solo en APIs publicas no triviales.
- **Tests**: minimo 70% cobertura en capas de servicio backend; componentes Angular con smoke tests; ViewModels Android con tests unitarios.
- **Lint / Format**:
  - Backend: Spotless + Google Java Format.
  - Web: ESLint + Prettier.
  - Android: ktlint + detekt.
- **Secrets**: jamas en repo. `.env` ignorado. GitHub Secrets para CI. Cloud env vars en dashboard del proveedor.
- **Logs estructurados**: backend con SLF4J + Logback JSON en prod.
- **Errores**: respuestas homogeneas `{timestamp, status, error, message, path}`.

---

## Tickets de implementacion

Orden recomendado: T-01 → T-07 (backend operativo) antes de T-08+. T-13+ (Android) puede correr en paralelo con T-08–T-12 (web). T-21+ (CI/CD + deploy) al final, pero CI backend (T-21) se puede montar tras T-02 para ganar feedback temprano.

### Fase 0 — Bootstrap y repositorio

#### T-00 — Inicializar repositorio GitHub
- Crear repo `coach-fitness-ia` en GitHub (publico o privado).
- Subir estructura base + `README.md` + `.gitignore` (Java, Node, Android, IDE).
- Configurar proteccion de rama `main`: PR obligatorio, status checks, no force-push.
- `CONTRIBUTING.md` con convencion de commits y flujo de ramas.
- `LICENSE` (MIT sugerido).

#### T-01 — Monorepo + Docker Compose base
- Crear `backend/`, `web/`, `android/`.
- `docker-compose.yml` con servicio `postgres` (volumen persistente, puerto 5432).
- `.env.example` con `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `GROQ_API_KEY`.
- **Verificar:** `docker compose up postgres` y conectar con cliente SQL.

---

### Fase 1 — Backend Spring Boot

#### T-02 — Proyecto Spring Boot + autenticacion JWT
- Generar con Spring Initializr: `web`, `security`, `data-jpa`, `validation`, `postgresql`, `lombok`, `actuator`.
- Entidad `User` + `UserRepository`.
- `AuthController`: `POST /api/v1/auth/register`, `POST /api/v1/auth/login` → devuelve JWT.
- `JwtService` (firma HS256, exp 24h) + `JwtAuthFilter` en `SecurityFilterChain`.
- `BCryptPasswordEncoder` para hash.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) → respuestas homogeneas.
- CORS configurable via `app.cors.allowed-origins`.
- **Verificar:** registrar + login via Postman/curl, JWT valido en endpoint protegido.

#### T-03 — Perfil metabolico + calculo nutricional
- Entidad `MetabolicProfile` (1-1 con `User`).
- `NutritionCalculatorService`:
  - BMR Mifflin-St Jeor: `H: 10·kg + 6.25·cm − 5·age + 5` / `M: ... − 161`.
  - TDEE = BMR · factor actividad (sedentario 1.2, ligero 1.375, moderado 1.55, activo 1.725, muy activo 1.9).
  - Ajuste por objetivo: perder `−500 kcal`, mantener `0`, ganar `+300 kcal`.
  - Macros segun `dietType`:
    - Estandar 30P / 40C / 30G · Keto 30P / 10C / 60G · Vegetariano 25P / 50C / 25G · Ayuno intermitente 35P / 35C / 30G.
  - Proteina minimo `1.8 g/kg`.
- `ProfileController`:
  - `GET /api/v1/profile/me`
  - `PUT /api/v1/profile/me` → persiste + recalcula `NutritionTarget`.
  - `GET /api/v1/profile/targets`.
- **Verificar:** test unitario (hombre 30a, 170cm, 80kg, moderado → ~2360 kcal).

#### T-04 — Registro de peso
- Entidad `WeightLog`.
- `WeightController`:
  - `POST /api/v1/weights` `{weightKg, loggedAt?}`
  - `GET /api/v1/weights?from=&to=`
  - `DELETE /api/v1/weights/{id}`.
- Al crear, actualizar `currentWeightKg` en perfil y recalcular targets.

#### T-05 — Registro de actividad diaria
- Entidad `ActivityLog` con `UNIQUE(userId, date)`.
- `ActivityController`:
  - `POST /api/v1/activities` (upsert).
  - `GET /api/v1/activities?from=&to=`.
  - `GET /api/v1/activities/today`.

#### T-06 — Endpoints de agregacion dashboard
- `DashboardController`:
  - `GET /api/v1/dashboard/weight-progress?days=90`
  - `GET /api/v1/dashboard/weekly-summary`
  - `GET /api/v1/dashboard/today`.

#### T-07 — Integracion chat Groq
- `GroqClient` via `RestClient` → `https://api.groq.com/openai/v1/chat/completions`, modelo `llama-3.3-70b-versatile`.
- System prompt con rol nutricionista/entrenador + datos del perfil del usuario inyectados.
- Entidades `ChatConversation`, `ChatMessage`.
- `ChatController`:
  - `GET /api/v1/chat/conversations`
  - `POST /api/v1/chat/conversations`
  - `GET /api/v1/chat/conversations/{id}/messages`
  - `POST /api/v1/chat/conversations/{id}/messages`.
- Rate limiting 20 msg/hora/usuario.
- **Verificar:** mensaje real → respuesta coherente con rol.

#### T-07.1 — Swagger / OpenAPI con springdoc
- Dependencia `springdoc-openapi-starter-webmvc-ui` (v2.x compatible con Spring Boot 3.3).
- `OpenApiConfig`: titulo "Fitness AI Coach API", version, descripcion, contacto, licencia.
- Esquema de seguridad `bearerAuth` (JWT) declarado a nivel global.
- Anotaciones `@Tag`, `@Operation`, `@ApiResponse` en cada controlador.
- DTOs anotados con `@Schema(description=..., example=...)`.
- UI expuesta en `/swagger-ui.html` y JSON en `/v3/api-docs`.
- En produccion, Swagger UI solo accesible si `app.docs.enabled=true` (configurable por entorno).
- **Verificar:** abrir `/swagger-ui.html`, probar login, copiar JWT, autorizar y ejecutar endpoint protegido.

#### T-07.2 — Healthcheck y observabilidad
- `/actuator/health` publico (liveness/readiness).
- `/actuator/info` con version del build.
- Logs JSON en prod (`logback-spring.xml` con perfil `prod`).

---

### Fase 2 — Frontend Web (Angular)

#### T-08 — Bootstrap Angular + layout oscuro
- `ng new web --routing --style=scss --standalone`.
- Instalar `ng2-charts chart.js`.
- Layout con sidebar fijo (Panel / Chat / Perfil) + header "FITNESS AI COACH".
- Paleta: fondo `#0F0F0F`, tarjetas `#1A1A1A`, acento amarillo `#D4B200`.
- `environments/` con `environment.ts` (dev: `http://localhost:8080/api/v1`) y `environment.prod.ts` (URL del API desplegado).

#### T-09 — Auth module + guard + interceptor
- Paginas `/login`, `/register` (diseño igual a `image copy 3.png`).
- `AuthService` (login, register, logout, `currentUser$`, JWT en `localStorage`).
- `authGuard` redirige a `/login` si no hay token.
- `authInterceptor` añade `Authorization: Bearer <jwt>`.
- Rutas protegidas: `/dashboard`, `/chat`, `/profile`.

#### T-10 — Pagina Perfil (replica `image.png`)
- Form reactivo: datos personales + objetivo fitness.
- Tarjeta derecha: macros calculados (4 cajas).
- `Guardar perfil` (amarillo), `Cerrar sesion` (rojo outline).

#### T-11 — Pagina Panel (replica `image copy 2.png`)
- "Progreso de peso" — line chart de `/dashboard/weight-progress`.
- "Resumen semanal" — bar chart pasos ultimos 7 dias.
- "Resumen de actividad" — 4 mini-tarjetas.
- Form inline "registrar actividad de hoy".

#### T-12 — Pagina Chat (replica `image copy.png`)
- Burbujas mensajes, input con enviar redondo amarillo, estado `enviando...`, scroll automatico.

---

### Fase 3 — Cliente Android

#### T-13 — Bootstrap Android
- Kotlin + Compose, minSdk 26, targetSdk 34.
- Dependencias: `hilt-android`, `retrofit`, `converter-moshi`, `okhttp-logging-interceptor`, `datastore-preferences`, `navigation-compose`, `vico` para charts.
- Tema Material3 dark, color primario `#D4B200`.
- `BuildConfig.API_BASE_URL` configurable por buildType (debug → `10.0.2.2:8080`, release → URL prod).

#### T-14 — Capa red + persistencia token
- `ApiService` Retrofit con todos los endpoints.
- `AuthInterceptor` (OkHttp) → header JWT.
- `TokenStore` con `DataStore<Preferences>`.
- `NetworkModule` Hilt.

#### T-15 — Pantallas auth (Login / Register)

#### T-16 — Pantalla Perfil

#### T-17 — Pantalla Dashboard

#### T-18 — Pantalla Chat

#### T-19 — Registro actividad + peso
- `LogActivityScreen`, `LogWeightScreen`, accesibles desde FAB del Dashboard.

---

### Fase 4 — Empaquetado y CI/CD

#### T-20 — Dockerfiles + docker-compose completo
- `backend/Dockerfile` multi-stage (`maven:3.9-eclipse-temurin-21` → `eclipse-temurin:21-jre-alpine`).
- `web/Dockerfile` multi-stage (`node:20-alpine` build → `nginx:alpine` sirve `dist/`).
- `docker-compose.yml` dev: postgres + backend + web.
- `.dockerignore` en cada modulo.
- `nginx.conf` para Angular (fallback `index.html` para routing SPA).

#### T-21 — CI Backend (GitHub Actions)
- `.github/workflows/backend-ci.yml`:
  - Trigger: PR y push a `main`/`develop`, filtro `paths: backend/**`.
  - Jobs: `setup-java` (21, temurin) → cache maven → `mvn verify` (compila, lint, tests, JaCoCo).
  - Subir reporte de cobertura como artefacto.
  - Servicio postgres en workflow para tests de integracion.
  - Build de imagen Docker y push a GitHub Container Registry (ghcr.io) solo en `main`.

#### T-22 — CI Web (GitHub Actions)
- `.github/workflows/web-ci.yml`:
  - Trigger con filtro `paths: web/**`.
  - `setup-node` 20 → cache npm → `npm ci` → `npm run lint` → `npm run test -- --watch=false --browsers=ChromeHeadless` → `npm run build -- --configuration production`.
  - Artefacto `dist/`.

#### T-23 — CI Android (GitHub Actions)
- `.github/workflows/android-ci.yml`:
  - Trigger filtro `paths: android/**`.
  - `setup-java` 21 → cache gradle → `./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug`.
  - APK debug como artefacto.

#### T-24 — Deploy backend + DB (Render o Railway)
- Crear servicio **Postgres managed** → copiar connection string.
- Crear **Web Service** con runtime Docker, conecta al repo GitHub rama `main`.
- Env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `GROQ_API_KEY`, `SPRING_PROFILES_ACTIVE=prod`, `APP_DOCS_ENABLED=true`, `APP_CORS_ALLOWED_ORIGINS=<url-web>`.
- Auto-deploy on push a `main`.
- Healthcheck `/actuator/health`.
- **Verificar:** Swagger accesible en `https://api.../swagger-ui.html`.

#### T-25 — Deploy Web (Vercel o Netlify)
- Import del repo → root directory `web/` → build command `npm run build` → output `dist/web/browser`.
- Env var `API_URL` → inyectada en build de Angular.
- Auto-deploy en cada push a `main`, preview deploy por PR.
- **Verificar:** la web en produccion consume la API de T-24.

#### T-26 — Release Android + Google Play
- Preparacion:
  - Crear cuenta Google Play Console (pago unico 25 USD).
  - Generar keystore release (`keytool -genkey -v -keystore release.jks ...`) — guardar en lugar seguro (**no commit**).
  - `build.gradle.kts` con `signingConfigs.release` leyendo `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` desde env.
  - `applicationId` final (p.ej. `com.fitnessaicoach.app`), `versionCode` + `versionName`.
- Workflow release:
  - `.github/workflows/android-release.yml` disparado por tag `v*`.
  - Restaurar keystore desde secret base64 → `./gradlew bundleRelease` → subir AAB como artefacto.
- Subida manual inicial al Play Console:
  - Crear app → ficha (descripcion, capturas, icono, politica de privacidad).
  - Track **Internal Testing** primero → añadir emails testers.
  - Promocion a **Closed → Open → Production** cuando este validado.
- Opcional: `gradle-play-publisher` para subida automatica desde CI.

#### T-27 — Tag de release + changelog
- Adoptar **SemVer**: `vMAJOR.MINOR.PATCH`.
- `CHANGELOG.md` actualizado en cada release (formato Keep a Changelog).
- GitHub Release creado automaticamente por workflow al pushear tag, adjuntando AAB y notas.

---

## Archivos criticos

| Archivo | Rol |
|---------|-----|
| `backend/src/main/java/.../NutritionCalculatorService.java` | Nucleo calculo macros |
| `backend/src/main/java/.../security/JwtService.java` | Firma y validacion JWT |
| `backend/src/main/java/.../chat/GroqClient.java` | Integracion LLM |
| `backend/src/main/java/.../config/OpenApiConfig.java` | Swagger |
| `backend/src/main/resources/application.yml` | Config DB, JWT, Groq, docs |
| `backend/src/main/resources/application-prod.yml` | Overrides prod |
| `web/src/app/core/auth/auth.interceptor.ts` | Inyeccion JWT |
| `web/src/environments/environment.prod.ts` | URL API prod |
| `android/app/src/main/java/.../data/network/ApiService.kt` | Contrato backend |
| `android/app/build.gradle.kts` | Signing config release |
| `docker-compose.yml` | Orquestacion dev |
| `.github/workflows/*.yml` | Pipelines CI/CD |
| `README.md` | Onboarding + deploy |

---

## Verificacion end-to-end

### Local
1. `docker compose up` → Postgres + backend + web.
2. `http://localhost:4200` → registrar, login, rellenar perfil, pesos, actividad, chat.
3. `http://localhost:8080/swagger-ui.html` → API documentada.
4. Emulador Android con `BASE_URL=10.0.2.2:8080` repite el flujo.

### Cloud
5. Push a `main` → CI verde → deploy automatico backend (Render/Railway) y web (Vercel/Netlify).
6. `https://api.../actuator/health` → `UP`.
7. `https://api.../swagger-ui.html` → documentacion publica.
8. Web de produccion consume la API real.
9. AAB firmado subido al track Internal Testing de Google Play → app instalable en dispositivo real.

### Tests automatizados
- `./mvnw verify` — backend (unitarios + integracion + JaCoCo).
- `npm run test && npm run lint` — web.
- `./gradlew testDebugUnitTest ktlintCheck detekt` — Android.
- Todos deben pasar en GitHub Actions antes de merge a `main`.
