# Roadmap - AI Coach y diario fitness

## Vision

Convertir Fitness AI Coach en un diario fitness inteligente:

```text
Usuario + reloj + chat natural -> backend -> Supabase -> coach IA + dashboard
```

La IA actua como nutricionista y entrenador personal, usando objetivos, historico, comidas, actividad y memoria para recomendar acciones.

## Orden de implementacion recomendado

1. `T-28` Personalizacion de usuario.
2. `T-29` Nutrition Journal.
3. `T-30` AI Coach Tools.
4. `T-33` Dashboard KPIs y evolucion.
5. `T-32` Health Connect Sync.
6. `T-31` AI Memory + RAG.
7. `T-34` AI Weekly Review.

## Estrategia de ramas

Cada ticket debe implementarse en una rama independiente:

```text
feature/t-28-user-personalization
feature/t-29-nutrition-journal
feature/t-30-ai-coach-tools
feature/t-33-dashboard-kpis
feature/t-32-health-connect-sync
feature/t-31-ai-memory-rag
feature/t-34-ai-weekly-review
```

Cada rama debe abrir PR hacia `main`, pasar CI y desplegar en Render/Vercel si aplica.

## Reglas generales

- No mezclar tickets funcionales en un mismo PR.
- Toda tabla nueva requiere migracion Flyway.
- Todo endpoint nuevo requiere Swagger/OpenAPI.
- Toda respuesta debe estar filtrada por usuario autenticado.
- El chat no debe guardar acciones ambiguas sin confirmacion.
- Health Connect es puente Android; Angular consume el backend.
- Fotos de comida quedan fuera hasta nuevo ticket.

## Dependencias entre tickets

```text
T-28 -> independiente
T-29 -> base para T-30, T-33 y T-34
T-30 -> depende de T-29
T-33 -> depende de T-29; mejora si T-32 existe
T-32 -> puede ir despues de T-29 o T-33
T-31 -> depende de T-30 para aportar valor real
T-34 -> depende de T-29; mejora con T-31 y T-32
```

## Verificacion minima por PR

Backend:

```text
mvn verify
```

Web:

```text
npm run build -- --configuration production
```

Android:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Si un ticket toca solo una capa, ejecutar al menos esa capa y documentar lo no ejecutado.

