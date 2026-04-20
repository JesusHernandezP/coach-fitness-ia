# Changelog

All notable changes to Fitness AI Coach are documented here.
Format: [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Versioning: [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-04-20

### Added

#### Backend (Spring Boot 3.3 + Java 21)
- JWT authentication (`POST /auth/register`, `POST /auth/login`)
- Metabolic profile with Mifflin-St Jeor macro calculator
- Weight log endpoints with automatic profile + target recalculation
- Activity log (upsert per user/date)
- Dashboard aggregation endpoints (weight progress, weekly summary, today snapshot)
- Groq LLM chat integration (`llama-3.3-70b-versatile`) with per-conversation history and profile context
- Rate limiting: 20 messages/hour/user
- Swagger UI (`/swagger-ui.html`) via springdoc-openapi
- Actuator health endpoint (`/actuator/health`)
- Docker multi-stage build (Maven → eclipse-temurin:21-jre-alpine)

#### Web (Angular 18)
- Dark gold theme (`#0F0F0F` / `#1A1A1A` / `#D4B200`)
- Auth pages (Login / Register) with JWT interceptor and route guard
- Profile page: metabolic form + live macro targets card
- Dashboard: weight line chart, steps bar chart, activity summary, inline activity log
- Chat: conversation list, message bubbles, optimistic UI, auto-scroll

#### Android (Kotlin + Jetpack Compose)
- Material3 dark theme with gold accent
- Auth screens (Login / Register) with `AuthViewModel` StateFlow
- Profile screen with form + onboarding flow
- Dashboard screen with Canvas line/bar charts and quick-action FABs
- Chat screen with `LazyColumn` + `imePadding()`
- Log Activity and Log Weight screens
- Hilt DI, Retrofit + Moshi, DataStore JWT persistence
- Per-buildType `API_BASE_URL` (`10.0.2.2:8080` debug / env var release)

#### Infrastructure
- Docker Compose (postgres + backend + web) for local development
- GitHub Actions CI: backend (Maven + JaCoCo + GHCR push on main), web (lint + test + build), Android (ktlint + detekt + APK)
- Android release workflow triggered by `v*` tags — builds signed AAB and creates GitHub Release

[Unreleased]: https://github.com/JesusHernandezP/coach-fitness-ia/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/JesusHernandezP/coach-fitness-ia/releases/tag/v1.0.0
