# Fitness AI Coach

Aplicacion fitness multiplataforma con IA. Perfil metabolico → calculo de macros → seguimiento de progreso → chat nutricionista/entrenador.

## Plataformas

| Cliente | Tecnologia |
|---------|-----------|
| Web | Angular 18 |
| Android | Kotlin + Jetpack Compose |
| Backend | Java 21 + Spring Boot 3.3 |
| Base de datos | PostgreSQL 16 |
| Chat IA | Groq API (Llama 3.3 70B) |

## Arranque local

### Requisitos

- Docker + Docker Compose
- Java 21 (para desarrollo backend sin Docker)
- Node 20 (para desarrollo web sin Docker)
- Android Studio Ladybug+ (para desarrollo Android)

### Con Docker Compose (recomendado)

```bash
cp .env.example .env
# Editar .env con tus valores
docker compose up
```

| Servicio | URL |
|----------|-----|
| Web Angular | http://localhost:4200 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 |

### Sin Docker (desarrollo)

```bash
# Backend
cd backend && ./mvnw spring-boot:run

# Web
cd web && npm ci && npm start

# Android: abrir android/ en Android Studio
# Emulador apunta a 10.0.2.2:8080 (buildType debug)
```

## Variables de entorno

Copiar `.env.example` y completar:

```
DB_URL=jdbc:postgresql://localhost:5432/fitnesscoach
DB_USER=postgres
DB_PASSWORD=changeme
JWT_SECRET=<min-32-chars-random>
GROQ_API_KEY=<tu-api-key-de-console.groq.com>
APP_CORS_ALLOWED_ORIGINS=http://localhost:4200
APP_DOCS_ENABLED=true
SPRING_PROFILES_ACTIVE=dev
```

API key gratuita de Groq: https://console.groq.com

## Estructura del repositorio

```
backend/    # Spring Boot REST API
web/        # Angular SPA
android/    # Android Compose app
.github/    # GitHub Actions CI/CD
docker-compose.yml
.env.example
plan.md     # Diseño tecnico completo y tickets
CLAUDE.md   # Guia para Claude Code
```

## Contribuir

Ver [CONTRIBUTING.md](CONTRIBUTING.md).

## Licencia

MIT — ver [LICENSE](LICENSE).
